package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import tss.dao.ContractsDao;
import tss.dao.PersonDao;
import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.entity.Contract;
import tss.entity.ContractStatus;
import tss.entity.FederalState;
import tss.entity.Person;
import tss.entity.Role;
import tss.entity.TimesheetFrequency;
import tss.entity.TimesheetStatus;
import tss.logic.ContractLogic;
import tss.logic.TerminationBlockedException;
import tss.logic.TerminationBlockedException;
import tss.logic.TimesheetLogic;

@Stateless
public class ContractLogicImp implements ContractLogic {

    @EJB
    private ContractsDao contractsDao;

    @EJB
    private PersonDao personDao;

    @EJB
    TimesheetLogic timesheetLogic;

    @Override
    public ContractDTO createContract(String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency timesheetFrequency, double hoursPerWeek, double hoursDue, int workingDaysPerWeek, int vacationDaysPerYear, PersonDTO person, FederalState state) {
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

        contractsDao.createContract(contract);
        return toDTO(contract);
    }

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
        return toDTO(contract);
    }

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

    @Override
    public ContractDTO searchContract(Long contractId) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }
        return toDTO(contract);
    }

    @Override
    public List<ContractDTO> findAllContracts() {
        return contractsDao.findAllContracts()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<ContractDTO> findAllArchivedContractsForSupervisor(long id) {
        return contractsDao.findAllArchivedContractsForSupervisor(id)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<ContractDTO> findAllArchivedContracts() {
        return contractsDao.findAllArchivedContracts()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public void CheckForArchivedTimesheet(Contract contract) {
        // Only a TERMINATED contract can become ARCHIVED. allMatch on an empty list is true,
        // so without this check a PREPARED contract would be archived by mistake.
        if (contract.getStatus() == ContractStatus.TERMINATED
                && contract.getTimesheet().stream()
                        .allMatch(ts -> ts.getStatus() == TimesheetStatus.ARCHIVED)) {
            contract.setStatus(ContractStatus.ARCHIVED);
            contractsDao.UpdateContract(contract);
        }
    }

    public static LocalDate terminationDate() {
        return LocalDate.now();
    }

    // PREPARED -> STARTED and TERMINATED -> ARCHIVED. Termination goes through terminateContract.
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
                        .collect(Collectors.toList()));
        dto.setAssistantIds(
                c.getAssistants().stream()
                        .map(Person::getId)
                        .collect(Collectors.toList()));
        return dto;
    }

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

    @Override
    public boolean hasUnresolvedInProgressTimesheets(Long contractId) {
        // IN_PROGRESS with at least one entry
        return timesheetLogic.getTimesheetsForContract(contractId).stream()
                .anyMatch(t -> t.getStatus() == TimesheetStatus.IN_PROGRESS
                && t.getEntries() != null
                && !t.getEntries().isEmpty());
    }

    @Override
    public boolean hasTimesheetsPendingSupervisorSignature(Long contractId) {
        return timesheetLogic.getTimesheetsForContract(contractId).stream()
                .anyMatch(t -> t.getStatus() == TimesheetStatus.SIGNED_BY_EMPLOYEE);
    }

    @Override
    public boolean hasEmptyInProgressTimesheets(Long contractId) {
        return timesheetLogic.getTimesheetsForContract(contractId).stream()
                .anyMatch(t -> t.getStatus() == TimesheetStatus.IN_PROGRESS
                && (t.getEntries() == null || t.getEntries().isEmpty()));
    }

    @Override
    public long countContractsByEmployee(Long personId) {
        return contractsDao.countContractsByEmployee(personId);
    }
}
