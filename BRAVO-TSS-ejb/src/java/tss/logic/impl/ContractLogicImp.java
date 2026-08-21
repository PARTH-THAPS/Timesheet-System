package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import tss.dao.ContractsDao;
import tss.dao.PersonDao;
import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.entity.ContractStatus;
import tss.entity.TimesheetFrequency;
import tss.logic.ContractLogic;
import tss.entity.Contract;
import tss.entity.Person;
import tss.entity.Timesheet;
import tss.entity.TimesheetStatus;
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
    public ContractDTO createContract(String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency timesheetFrequency, double hoursPerWeek, double hoursDue, int workingDaysPerWeek, int vacationDaysPerYear, PersonDTO person, String state) {
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
        contract.setPerson(personEnt);
        if (state == null) {
            contract.setState("Deutschland");
        } else {
            contract.setState(state);
        }
        contractsDao.createContract(contract);
        return toDTO(contract);
    }

    @Override
    public ContractDTO updateContract(Contract updatedContract) {
        Contract contract = contractsDao.findContract(updatedContract.getId());
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + updatedContract.getId());
        }
        if (contract.getStatus() != ContractStatus.PREPARED) {
            throw new IllegalStateException("Contract can only be updated when status is PREPARED");
        }
        contract.setStartDate(updatedContract.getStartDate());
        contract.setEndDate(updatedContract.getEndDate());
        contract.setFrequency(updatedContract.getFrequency());
        contract.setHoursPerWeek(updatedContract.getHoursPerWeek());
        contract.setVacationHours(updatedContract.getVacationHours());
        contract.setHoursDue(updatedContract.getHoursDue());
        contract.setWorkingDaysPerWeek(updatedContract.getWorkingDaysPerWeek());
        contract.setVacationDaysPerYear(updatedContract.getVacationDaysPerYear());
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

    public void CheckForArchivedTimesheet(Contract contract) {
        boolean allArchivedTimesheet = contract.getTimesheet().stream().allMatch(ts -> ts.getStatus() == TimesheetStatus.ARCHIVED);
        if (allArchivedTimesheet) {
            contract.setStatus(ContractStatus.ARCHIVED);
        }
        contractsDao.UpdateContract(contract);
    }

    public static LocalDate terminationDate() {
        return LocalDate.now();
    }

    @Override
    public ContractDTO  updateContractStatus(Long contractId, ContractStatus newStatus) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }
        ContractStatus currentStatus = contract.getStatus();
        if (currentStatus == ContractStatus.PREPARED && newStatus == ContractStatus.STARTED) {
            contract.setStatus(newStatus);
            timesheetLogic.generateTimesheetsForContract(contract);
        } else if (currentStatus == ContractStatus.STARTED && newStatus == ContractStatus.TERMINATED) {
            List<Timesheet> timesheets = contract.getTimesheet();
            boolean hasInProgressTimesheet = timesheets.stream().anyMatch(t -> t.getStatus() == TimesheetStatus.IN_PROGRESS && t.getEntries() != null && !t.getEntries().isEmpty());
            if (hasInProgressTimesheet) {
                throw new IllegalStateException("Cannot terminate contract. There are timesheets in progress with some entries.");
            }
            LocalDate date = terminationDate();
            contract.setTerminationDate(date);
            contract.setStatus(newStatus);
        } else if (currentStatus == ContractStatus.TERMINATED && newStatus == ContractStatus.ARCHIVED) {
            contract.setStatus(newStatus);
        } else {
            throw new IllegalStateException("Invalid status transition: " + currentStatus + "From" + newStatus);
        }
        contractsDao.UpdateContract(contract);
        return toDTO(contract);
    }

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
        dto.setPersonUuid(c.getPerson() != null ? c.getPerson().getUuid() : null);
        return dto;
    }
    
    
}
