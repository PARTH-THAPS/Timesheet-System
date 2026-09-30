package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import tss.dao.ContractsDao;
import tss.dao.PersonDao;
import tss.dto.ContractDTO;
import tss.dto.ContractStatisticsDTO;
import tss.dto.PersonDTO;
import tss.dto.TimesheetDTO;
import tss.dto.TimesheetEntryDTO;
import tss.entity.Contract;
import tss.entity.ContractStatus;
import tss.entity.FederalState;
import tss.entity.Person;
import tss.entity.Role;
import tss.entity.TimesheetFrequency;
import tss.entity.TimesheetStatus;
import tss.logic.ContractLogic;
import tss.logic.TerminationBlockedException;
import tss.logic.TimesheetLogic;

/**
 * Implementation of the {@link ContractLogic} interface.
 * Handles creation, modification, termination, and state transitions of employment contracts.
 */
@Stateless
public class ContractLogicImp implements ContractLogic {

    @EJB
    private ContractsDao contractsDao;

    @EJB
    private PersonDao personDao;

    @EJB
    TimesheetLogic timesheetLogic;

    /**
     * Creates a new contract in PREPARED status.
     *
     * @param name The contract name.
     * @param startDate The start date of the contract.
     * @param endDate The end date of the contract.
     * @param timesheetFrequency Timesheet generation frequency.
     * @param hoursPerWeek Hours required per week.
     * @param hoursDue Total hours due over the contract duration.
     * @param workingDaysPerWeek Working days per week.
     * @param vacationDaysPerYear Allowed vacation days per year.
     * @param person The assigned employee.
     * @param state The relevant federal state.
     * @param archiveDuration Archival retention time.
     * @return The created ContractDTO.
     */
    @Override
    public ContractDTO createContract(String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency timesheetFrequency, double hoursPerWeek, double hoursDue, int workingDaysPerWeek, int vacationDaysPerYear, PersonDTO person, FederalState state, int archiveDuration) {
        validateContractDates(startDate, endDate);

        Person personEnt = personDao.findPersonById(person.getId());
        if (personEnt == null) {
            throw new IllegalArgumentException("No Person found with id: " + person.getId());
        }

        Contract contract = new Contract();
        contract.setStatus(ContractStatus.PREPARED);
        contract.setName(name);
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setFrequency(timesheetFrequency);
        contract.setTerminationDate(null);
        contract.setHoursPerWeek(hoursPerWeek);
        contract.setVacationHours(vacationHours(startDate, endDate, workingDaysPerWeek, vacationDaysPerYear, hoursPerWeek));
        contract.setHoursDue(hoursDue);
        contract.setWorkingDaysPerWeek(workingDaysPerWeek);
        contract.setVacationDaysPerYear(vacationDaysPerYear);
        contract.setEmployee(personEnt);
        if (state == null) {
            contract.setState(FederalState.RLP);
        } else {
            contract.setState(state);
        }
        contract.setArchiveDuration(archiveDuration);
        contractsDao.createContract(contract);
        personEnt.setRoles(Role.EMPLOYEE);
        personDao.updatePerson(personEnt);
        return toDTO(contract);
    }

    /**
     * Updates an existing PREPARED contract.
     *
     * @param updatedContract The updated ContractDTO values.
     * @return The modified ContractDTO.
     */
    @Override
    public ContractDTO updateContract(ContractDTO updatedContract) {
        Contract contract = contractsDao.findContract(updatedContract.getId());
        if (contract == null) {
            throw new IllegalArgumentException(
                    "No contract found with id: " + updatedContract.getId()
            );
        }

        if (contract.getStatus() != ContractStatus.PREPARED) {
            throw new IllegalStateException(
                    "Contract can only be updated when status is PREPARED"
            );
        }

        validateContractDates(
                updatedContract.getStartDate(),
                updatedContract.getEndDate()
        );

        Person person = personDao.findPersonById(
                updatedContract.getPersonId()
        );

        if (person == null) {
            throw new IllegalArgumentException(
                    "No person found with id: "
                    + updatedContract.getPersonId()
            );
        }

        Person previousEmployee = contract.getEmployee();

        contract.setName(updatedContract.getName());
        contract.setStartDate(updatedContract.getStartDate());
        contract.setEndDate(updatedContract.getEndDate());
        contract.setFrequency(updatedContract.getFrequency());
        contract.setHoursPerWeek(updatedContract.getHoursPerWeek());
        contract.setWorkingDaysPerWeek(
                updatedContract.getWorkingDaysPerWeek()
        );
        contract.setVacationDaysPerYear(
                updatedContract.getVacationDaysPerYear()
        );
        contract.setState(updatedContract.getState());
        contract.setArchiveDuration(updatedContract.getArchiveDuration());
        contract.setEmployee(person);

        contract.setVacationHours(
                vacationHours(
                        updatedContract.getStartDate(),
                        updatedContract.getEndDate(),
                        updatedContract.getWorkingDaysPerWeek(),
                        updatedContract.getVacationDaysPerYear(),
                        updatedContract.getHoursPerWeek()
                )
        );

        contractsDao.UpdateContract(contract);

        person.setRoles(Role.EMPLOYEE);
        personDao.updatePerson(person);

        if (previousEmployee != null && !previousEmployee.getId().equals(person.getId())) {
            boolean stillEmployeeElsewhere = previousEmployee.getEmployeeContract().stream()
                    .anyMatch(c -> !c.getId().equals(updatedContract.getId()));
            if (!stillEmployeeElsewhere) {
                previousEmployee.removeRole(Role.EMPLOYEE);
                personDao.updatePerson(previousEmployee);
            }
        }
        return toDTO(contract);
    }

    /**
     * Deletes a contract if it is still in the PREPARED status.
     *
     * @param contractId The ID of the contract to delete.
     */
    @Override
    public void deleteContract(Long contractId) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }
        if (contract.getStatus() != ContractStatus.PREPARED) {
            throw new IllegalStateException("Contract can only be deleted when status is PREPARED");
        }
        contractsDao.deleteContract(contract);
    }

    /**
     * Finds a contract by ID.
     *
     * @param contractId The ID of the contract.
     * @return The corresponding ContractDTO.
     */
    @Override
    public ContractDTO searchContract(Long contractId) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }
        return toDTO(contract);
    }

    /**
     * Calculates statistics (worked hours, due hours, vacation) for a given contract.
     *
     * @param contractId The ID of the contract.
     * @return A ContractStatisticsDTO.
     */
    @Override
    public ContractStatisticsDTO getContractStatistics(Long contractId) {

        Contract contract = contractsDao.findContract(contractId);

        if (contract == null) {
            throw new IllegalArgumentException(
                    "No contract found with id: " + contractId
            );
        }

        List<TimesheetDTO> timesheets
                = timesheetLogic.getTimesheetsForContract(contractId);

        double totalHoursWorked = timesheets.stream()
                .filter(timesheet -> timesheet.getEntries() != null)
                .flatMap(timesheet -> timesheet.getEntries().stream())
                .mapToDouble(TimesheetEntryDTO::getHours)
                .sum();

        double totalWorkingHours = contract.getHoursDue();

        double totalVacationHours = contract.getVacationHours();

        double usedVacationHours
                = timesheetLogic.getUsedVacationHours(contractId);

        double totalVacationHoursLeft = Math.max(
                totalVacationHours - usedVacationHours,
                0.0
        );

        double balance
                = totalWorkingHours - totalHoursWorked;

        double totalHoursDue
                = Math.max(balance, 0.0);

        ContractStatisticsDTO statistics
                = new ContractStatisticsDTO();

        statistics.setTotalWorkingHours(totalWorkingHours);
        statistics.setTotalHoursWorked(totalHoursWorked);
        statistics.setTotalVacationHours(totalVacationHours);
        statistics.setTotalVacationHoursLeft(totalVacationHoursLeft);
        statistics.setTotalHoursDue(totalHoursDue);
        statistics.setBalance(balance);

        return statistics;
    }

    /**
     * Retrieves all contracts in the system.
     *
     * @return A list of ContractDTOs.
     */
    @Override
    public List<ContractDTO> findAllContracts() {
        return contractsDao.findAllContracts()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Retrieves all archived contracts linked to a specific supervisor.
     *
     * @param id The supervisor's ID.
     * @return A list of archived ContractDTOs.
     */
    @Override
    public List<ContractDTO> findAllArchivedContractsForSupervisor(long id) {
        return contractsDao.findAllArchivedContractsForSupervisor(id)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Retrieves all archived contracts system-wide.
     *
     * @return A list of archived ContractDTOs.
     */
    @Override
    public List<ContractDTO> findAllArchivedContracts() {
        return contractsDao.findAllArchivedContracts()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Verifies if all timesheets within a contract are archived, and if so, archives the contract itself.
     *
     * @param contract The contract entity to check.
     */
    @Override
    public void CheckForArchivedTimesheet(Contract contract) {
        boolean allArchivedTimesheet = contract.getTimesheet().stream().allMatch(ts -> ts.getStatus() == TimesheetStatus.ARCHIVED);
        if (allArchivedTimesheet) {
            contract.setStatus(ContractStatus.ARCHIVED);
        }
        contractsDao.UpdateContract(contract);
    }

    
//    @Override
//    public void CheckForArchivedTimesheet(Contract contract) {
//        if (contract.getStatus() == ContractStatus.TERMINATED
//                && contract.getTimesheet().stream()
//                        .allMatch(ts -> ts.getStatus() == TimesheetStatus.ARCHIVED)) {
//            contract.setStatus(ContractStatus.ARCHIVED);
//            contractsDao.UpdateContract(contract);
//        }
//    }

    public static LocalDate terminationDate() {
        return LocalDate.now();
    }

    // PREPARED -> STARTED and TERMINATED -> ARCHIVED. Termination goes through terminateContract.
    /**
     * Updates the status of a contract (e.g., PREPARED to STARTED or TERMINATED to ARCHIVED).
     *
     * @param contractId The ID of the contract.
     * @param newStatus The new ContractStatus to apply.
     * @return The updated ContractDTO.
     */
    @Override
    public ContractDTO updateContractStatus(Long contractId, ContractStatus newStatus) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }
        ContractStatus currentStatus = contract.getStatus();

        if (currentStatus == ContractStatus.PREPARED && newStatus == ContractStatus.STARTED) {
            contract.setStatus(newStatus);
            timesheetLogic.generateTimesheetsForContract(contractId);

        } else if (currentStatus == ContractStatus.TERMINATED && newStatus == ContractStatus.ARCHIVED) {
            boolean allArchived = timesheetLogic.getTimesheetsForContract(contractId).stream()
                    .allMatch(t -> t.getStatus() == TimesheetStatus.ARCHIVED);
            if (!allArchived) {
                throw new IllegalStateException(
                        "Contract can only be archived when all timesheets are archived.");
            }
            contract.setStatus(newStatus);

        } else {
            // includes STARTED -> TERMINATED: use terminateContract instead
            throw new IllegalStateException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        contractsDao.UpdateContract(contract);
        return toDTO(contract);
    }

    // STARTED -> TERMINATED
    /**
     * Terminates a STARTED contract, setting a termination date and clearing empty timesheets.
     *
     * @param contractId The contract ID to terminate.
     * @param confirmed Whether termination is forced despite incomplete records.
     * @return The updated ContractDTO.
     */
    @Override
    public ContractDTO terminateContract(Long contractId, boolean confirmed) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }
        if (contract.getStatus() != ContractStatus.STARTED) {
            throw new IllegalStateException("Only a started contract can be terminated.");
        }

        // Signed by the employee only: waiting for the supervisor
        if (hasTimesheetsPendingSupervisorSignature(contractId)) {
            throw new TerminationBlockedException(
                    "Contract cannot be terminated: a timesheet is signed by the employee "
                    + "and still awaiting the supervisor's signature.");
        }

        // IN_PROGRESS with entries: error
        if (hasUnresolvedInProgressTimesheets(contractId) && !confirmed) {
            throw new TerminationBlockedException(
                    "Contract cannot be terminated: a timesheet is still in progress and contains entries. "
                    + "Please have it signed by the employee and the supervisor first.");
        }

        contract.setTerminationDate(terminationDate());
        contract.setStatus(ContractStatus.TERMINATED);
        timesheetLogic.deleteInProgressTimesheets(contractId);   // only empty ones are left here

        contractsDao.UpdateContract(contract);
        return toDTO(contract);
    }

//     boolean nothingLeftToArchive = timesheetLogic.getTimesheetsForContract(contractId).stream()
//                .allMatch(t -> t.getStatus() == TimesheetStatus.ARCHIVED);
//        if (nothingLeftToArchive) {
//            contract.setStatus(ContractStatus.ARCHIVED);
//        }
    public static double vacationHours(LocalDate startDate, LocalDate endDate, int workingDaysPerWeek, int vacationDaysPerYear, double hoursPerWeek) {
        long durationInMonths = ChronoUnit.MONTHS.between(startDate, endDate.plusDays(1));
        return vacationDaysPerYear * (double) durationInMonths / 12 * hoursPerWeek / workingDaysPerWeek;
    }

    private void validateContractDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date can not be null");
        }
        if (startDate.getDayOfMonth() != 1) {
            throw new IllegalArgumentException("Start date must be the first day of a month");
        }
        if (endDate.getDayOfMonth() != endDate.lengthOfMonth()) {
            throw new IllegalArgumentException("End date must be the last day of a month");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }

    private ContractDTO toDTO(Contract c) {
        ContractDTO dto = new ContractDTO();
        dto.setId(c.getId());
        dto.setUuid(c.getUuid());
        dto.setJpaVersion(c.getJpaVersion());
        dto.setName(c.getName());
        dto.setStartDate(c.getStartDate());
        dto.setEndDate(c.getEndDate());
        dto.setFrequency(c.getFrequency());
        dto.setHoursPerWeek(c.getHoursPerWeek());
        dto.setHoursDue(c.getHoursDue());
        dto.setVacationHours(c.getVacationHours());
        dto.setWorkingDaysPerWeek(c.getWorkingDaysPerWeek());
        dto.setVacationDaysPerYear(c.getVacationDaysPerYear());
        dto.setState(c.getState());
        dto.setStatus(c.getStatus());
        dto.setTerminationDate(c.getTerminationDate());
        dto.setArchiveDuration(c.getArchiveDuration());
        if (c.getEmployee() != null) {
            dto.setPersonId(c.getEmployee().getId());
            dto.setPersonUuid(c.getEmployee().getUuid());
        }
        if (c.getSupervisor() != null) {
            dto.setSupervisorId(c.getSupervisor().getId());
        }

        dto.setSecretaryIds(
                c.getSecretaries().stream()
                        .map(Person::getId)
                        .toList()
        );
        dto.setAssistantIds(
                c.getAssistants().stream()
                        .map(Person::getId)
                        .toList()
        );

        return dto;
    }

    /**
     * Adds secretaries to a contract.
     *
     * @param contractId The contract ID.
     * @param personIds The list of user IDs to assign as secretaries.
     */
    @Override
    public void addSecretary(Long contractId, List<Long> personIds) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }

        List<Person> people = personIds.stream()
                .map(id -> {
                    Person p = personDao.findPersonById(id);
                    if (p == null) {
                        throw new IllegalArgumentException("No person found with id: " + id);
                    }
                    return p;
                })
                .toList();

        contract.addSecretary(people);
        contractsDao.UpdateContract(contract);

        for (Person p : people) {
            p.setRoles(Role.SECRETARY);
            personDao.updatePerson(p);
        }
    }

    /**
     * Removes secretaries from a contract.
     *
     * @param contractId The contract ID.
     * @param personIds The list of user IDs to remove from the secretary role.
     */
    @Override
    public void removeSecretary(Long contractId, List<Long> personIds) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }

        Person[] people = personIds.stream()
                .map(id -> {
                    Person p = personDao.findPersonById(id);
                    if (p == null) {
                        throw new IllegalArgumentException("No person found with id: " + id);
                    }
                    return p;
                })
                .toArray(Person[]::new);

        contract.removeSecretary(people);
        contractsDao.UpdateContract(contract);

        for (Person p : people) {
            boolean stillSecretaryElsewhere = p.getSecretaryContract().stream()
                    .anyMatch(c -> !c.getId().equals(contractId));
            if (!stillSecretaryElsewhere) {
                p.removeRole(Role.SECRETARY);
                personDao.updatePerson(p);
            }
        }
    }

    /**
     * Adds assistants to a contract.
     *
     * @param contractId The contract ID.
     * @param personIds The list of user IDs to assign as assistants.
     */
    @Override
    public void addAssistant(Long contractId, List<Long> personIds) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }

        Person[] people = personIds.stream()
                .map(id -> {
                    Person p = personDao.findPersonById(id);
                    if (p == null) {
                        throw new IllegalArgumentException("No person found with id: " + id);
                    }
                    return p;
                })
                .toArray(Person[]::new);

        contract.addAssistant(people);
        contractsDao.UpdateContract(contract);

        for (Person p : people) {
            p.setRoles(Role.ASSISTANT);
            personDao.updatePerson(p);
        }
    }

    /**
     * Removes assistants from a contract.
     *
     * @param contractId The contract ID.
     * @param personIds The list of user IDs to remove from the assistant role.
     */
    @Override
    public void removeAssistant(Long contractId, List<Long> personIds) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }

        Person[] people = personIds.stream()
                .map(id -> {
                    Person p = personDao.findPersonById(id);
                    if (p == null) {
                        throw new IllegalArgumentException("No person found with id: " + id);
                    }
                    return p;
                })
                .toArray(Person[]::new);

        contract.removeAssistant(people);
        contractsDao.UpdateContract(contract);

        for (Person p : people) {
            boolean stillAssistantElsewhere = p.getAssistantContract().stream()
                    .anyMatch(c -> !c.getId().equals(contractId));
            if (!stillAssistantElsewhere) {
                p.removeRole(Role.ASSISTANT);
                personDao.updatePerson(p);
            }
        }
    }

    /**
     * Assigns a supervisor to a contract.
     *
     * @param contractId The contract ID.
     * @param personId The ID of the supervisor.
     */
    @Override
    public void addSupervisor(Long contractId, Long personId) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }

        Person person = personDao.findPersonById(personId);
        if (person == null) {
            throw new IllegalArgumentException("No person found with id: " + personId);
        }

        contract.setSupervisor(person);
        contractsDao.UpdateContract(contract);

        person.setRoles(Role.SUPERVISOR);
        personDao.updatePerson(person);
    }

    /**
     * Removes the assigned supervisor from a contract.
     *
     * @param contractId The contract ID.
     */
    @Override
    public void removeSupervisor(Long contractId) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }

        Person previousSupervisor = contract.getSupervisor();

        contract.removeSupervisor();
        contractsDao.UpdateContract(contract);

        if (previousSupervisor != null) {
            boolean stillSupervisorElsewhere = previousSupervisor.getSupervisorContract().stream()
                    .anyMatch(c -> !c.getId().equals(contractId));
            if (!stillSupervisorElsewhere) {
                previousSupervisor.removeRole(Role.SUPERVISOR);
                personDao.updatePerson(previousSupervisor);
            }
        }
    }

    /**
     * Checks if a contract has any IN_PROGRESS timesheets that contain entries.
     *
     * @param contractId The contract ID.
     * @return true if unresolved timesheets exist, false otherwise.
     */
    @Override
    public boolean hasUnresolvedInProgressTimesheets(Long contractId) {
        // IN_PROGRESS with at least one entry
        return timesheetLogic.getTimesheetsForContract(contractId).stream()
                .anyMatch(t -> t.getStatus() == TimesheetStatus.IN_PROGRESS
                && t.getEntries() != null
                && !t.getEntries().isEmpty());
    }

    /**
     * Checks if a contract has timesheets signed by the employee but waiting for supervisor approval.
     *
     * @param contractId The contract ID.
     * @return true if pending supervisor signatures exist, false otherwise.
     */
    @Override
    public boolean hasTimesheetsPendingSupervisorSignature(Long contractId) {
        return timesheetLogic.getTimesheetsForContract(contractId).stream()
                .anyMatch(t -> t.getStatus() == TimesheetStatus.SIGNED_BY_EMPLOYEE);
    }

    /**
     * Checks if a contract has IN_PROGRESS timesheets that have no entries.
     *
     * @param contractId The contract ID.
     * @return true if empty timesheets exist, false otherwise.
     */
    @Override
    public boolean hasEmptyInProgressTimesheets(Long contractId) {
        return timesheetLogic.getTimesheetsForContract(contractId).stream()
                .anyMatch(t -> t.getStatus() == TimesheetStatus.IN_PROGRESS
                && (t.getEntries() == null || t.getEntries().isEmpty()));
    }

    /**
     * Returns the total count of contracts associated with a specific employee.
     *
     * @param personId The employee ID.
     * @return The number of contracts.
     */
    @Override
    public long countContractsByEmployee(Long personId) {
        return contractsDao.countContractsByEmployee(personId);
    }
}
