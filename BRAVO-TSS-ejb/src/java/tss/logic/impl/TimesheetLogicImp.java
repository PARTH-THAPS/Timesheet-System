package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import tss.dto.TimesheetDTO;
import tss.entity.Contract;
import tss.entity.Timesheet;
import tss.entity.TimesheetEntry;
import tss.logic.TimesheetLogic;
import tss.dao.TimesheetDao;
import tss.entity.ContractStatus;
import tss.entity.TimesheetFrequency;
import tss.entity.TimesheetStatus;
import tss.dao.ContractsDao;
import tss.dao.TimeSheetEntriesDao;
import tss.entity.Holiday;
import tss.logic.HolidayLogic;

@Stateless
public class TimesheetLogicImp implements TimesheetLogic {

    @EJB
    private TimesheetDao timesheetDAO;

    @EJB
    private ContractsDao contractsDao;

    @EJB
    private TimeSheetEntriesDao timesheetEntriesDao;

    @EJB
    HolidayLogic holidayLogic;


    @Override
    public void generateTimesheetsForContract(Contract contract) {
        if (contract == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        if (contract.getStatus() != ContractStatus.STARTED) {
            throw new IllegalStateException("Contract must be STARTED to generate timesheets");
        }
        if (contract.getStartDate() == null || contract.getEndDate() == null) {
            throw new IllegalStateException("Contract start date and end date must be set");
        }

        LocalDate startDate = contract.getStartDate();
        LocalDate endDate = contract.getEndDate();

        while (!startDate.isAfter(endDate)) {
            LocalDate periodEnd = calculatePeriodEnd(startDate, contract.getFrequency(), endDate);

            Timesheet ts = new Timesheet();
            ts.setStatus(TimesheetStatus.IN_PROGRESS);
            ts.setStartDate(startDate);
            ts.setEndDate(periodEnd);
            ts.setContract(contract);
            //ts.setHoursDue(hoursDue(periodStart, periodEnd, contract.getHoursPerWeek(), contract.getWorkingDaysPerWeek()));
            ts.setSignedByEmployee(null);
            ts.setSignedBySupervisor(null);

            timesheetDAO.createTimesheet(ts);


            startDate = periodEnd.plusDays(1);
        }
    }

    private LocalDate calculatePeriodEnd(LocalDate startDate, TimesheetFrequency frequency, LocalDate endDate) {

        LocalDate periodEnd;

        if (frequency == TimesheetFrequency.WEEKLY) {
            periodEnd = startDate.plusWeeks(1).minusDays(1);
        } else {
            periodEnd = startDate.with(TemporalAdjusters.lastDayOfMonth());
        }

        if (periodEnd.isAfter(endDate)) {
            return endDate;
        } else {
            return periodEnd;
        }
    }

    @Override
    public Timesheet addEntry(Long timesheetId, TimesheetEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Entry cannot be null");
        }
        Timesheet timesheet = timesheetDAO.findById(timesheetId);
        if (timesheet == null) {
            throw new IllegalArgumentException("No timesheet found with id: " + timesheetId);
        }
        validateEntryModification(timesheet);

        entry.setTimesheet(timesheet);
        timesheet.getEntries().add(entry);

        timesheetDAO.updateTimesheet(timesheet);
        return timesheet;
    }

    //Add Timesheet Entries
    public void addTimesheet(Timesheet timeSheet) {
        TimesheetEntry timesheetEntries = new TimesheetEntry();
        List<Holiday> holidays = checkForHolidays(timeSheet, "RLP");
        timesheetEntries.setTimesheet(timeSheet);
        timesheetEntries.setType(null);
        timesheetEntries.setDescription(null);
        timesheetEntries.setEntryDate(null);
        timesheetEntries.setStartTime(null);
        timesheetEntries.setEndTime(null);
        timesheetEntriesDao.addTimeSheetEntries(timesheetEntries);
    }

    //holidays logic
    public List<Holiday> checkForHolidays(Timesheet timeSheet, String State) {
        LocalDate startDate = timeSheet.getStartDate();
        LocalDate endDate = timeSheet.getEndDate();
        return holidayLogic.findByStateAndRange(State, startDate, endDate);
    }

    @Override
    public Timesheet updateEntry(Long timesheetId, Long entryId, TimesheetEntry updatedEntry) {
        if (updatedEntry == null) {
            throw new IllegalArgumentException("Updated entry cannot be null");
        }
        Timesheet timesheet = timesheetDAO.findById(timesheetId);
        if (timesheet == null) {
            throw new IllegalArgumentException("No timesheet found with id: " + timesheetId);
        }
        validateEntryModification(timesheet);

        TimesheetEntry existing = findEntry(timesheet, entryId);

        existing.setEntryDate(updatedEntry.getEntryDate());
        existing.setStartTime(updatedEntry.getStartTime());
        existing.setEndTime(updatedEntry.getEndTime());
        existing.setDescription(updatedEntry.getDescription());
        existing.setType(updatedEntry.getType());

        timesheetDAO.updateTimesheet(timesheet);
        return timesheet;
    }

    @Override
    public void removeEntry(Long timesheetId, Long entryId) {
        Timesheet timesheet = timesheetDAO.findById(timesheetId);
        if (timesheet == null) {
            throw new IllegalArgumentException("No timesheet found with id: " + timesheetId);
        }
        validateEntryModification(timesheet);

        TimesheetEntry entry = findEntry(timesheet, entryId);
        timesheet.getEntries().remove(entry);

        timesheetDAO.updateTimesheet(timesheet);
    }

    @Override
    public void deleteInProgressTimesheets(Contract contract) {
        if (contract == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        List<Timesheet> timesheets = contract.getTimesheet();
        if (timesheets == null) {
            return;
        }
        List<Timesheet> toDelete = timesheets.stream()
                .filter(t -> t.getStatus() == TimesheetStatus.IN_PROGRESS)
                .toList();
        for (Timesheet ts : toDelete) {
            timesheetDAO.deleteTimesheet(ts);
        }
    }

    @Override
    public List<Timesheet> getTimesheetsForContract(Long contractId) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }
        return timesheetDAO.findByContract(contract);
    }

    @Override
    public Timesheet getTimesheetById(Long timesheetId) {
        Timesheet timesheet = timesheetDAO.findById(timesheetId);
        if (timesheet == null) {
            throw new IllegalArgumentException("No timesheet found with id: " + timesheetId);
        }
        return timesheet;
    }

    @Override
    public Timesheet signByEmployee(Long timesheetId) {
        Timesheet timesheet = timesheetDAO.findById(timesheetId);
        if (timesheet == null) {
            throw new IllegalArgumentException("No timesheet found with id: " + timesheetId);
        }
        if (timesheet.getStatus() != TimesheetStatus.IN_PROGRESS) {
            throw new IllegalStateException("Timesheet can only be signed by employee when IN_PROGRESS");
        }
        timesheet.setSignedByEmployee(LocalDate.now());
        timesheet.setStatus(TimesheetStatus.SIGNED_BY_EMPLOYEE);
        timesheetDAO.updateTimesheet(timesheet);
        return timesheet;
    }

    @Override
    public Timesheet revokeEmployeeSignature(Long timesheetId) {
        Timesheet timesheet = timesheetDAO.findById(timesheetId);
        if (timesheet == null) {
            throw new IllegalArgumentException("No timesheet found with id: " + timesheetId);
        }
        if (timesheet.getStatus() != TimesheetStatus.SIGNED_BY_EMPLOYEE) {
            throw new IllegalStateException("Signature can only be revoked when status is SIGNED_BY_EMPLOYEE");
        }
        timesheet.setSignedByEmployee(null);
        timesheet.setStatus(TimesheetStatus.IN_PROGRESS);
        timesheetDAO.updateTimesheet(timesheet);
        return timesheet;
    }

    @Override
    public Timesheet signBySupervisor(Long timesheetId) {
        Timesheet timesheet = timesheetDAO.findById(timesheetId);
        if (timesheet == null) {
            throw new IllegalArgumentException("No timesheet found with id: " + timesheetId);
        }
        if (timesheet.getStatus() != TimesheetStatus.SIGNED_BY_EMPLOYEE) {
            throw new IllegalStateException("Timesheet can only be signed by supervisor when SIGNED_BY_EMPLOYEE");
        }
        timesheet.setSignedBySupervisor(LocalDate.now());
        timesheet.setStatus(TimesheetStatus.SIGNED_BY_SUPERVISOR);
        timesheetDAO.updateTimesheet(timesheet);
        return timesheet;
    }

    @Override
    public Timesheet requestChanges(Long timesheetId) {
        Timesheet timesheet = timesheetDAO.findById(timesheetId);
        if (timesheet == null) {
            throw new IllegalArgumentException("No timesheet found with id: " + timesheetId);
        }
        if (timesheet.getStatus() != TimesheetStatus.SIGNED_BY_EMPLOYEE) {
            throw new IllegalStateException("Changes can only be requested when status is SIGNED_BY_EMPLOYEE");
        }
        timesheet.setSignedByEmployee(null);
        timesheet.setStatus(TimesheetStatus.IN_PROGRESS);
        timesheetDAO.updateTimesheet(timesheet);
        return timesheet;
    }

    private double calculateHoursDue(LocalDate startDate, LocalDate endDate, double hoursPerWeek, int workingDaysPerWeek, Contract contract) {
        if (contract == null) {
            throw new IllegalStateException("Timesheet must be linked to a contract");
        }

        if (workingDaysPerWeek <= 0) {
            throw new IllegalStateException("Contract workingDaysPerWeek must be greater than zero");
        }

        int workingDaysInPeriod = countWorkingDays(startDate, endDate);
        int publicHolidaysInPeriod = 0; // TODO: Compute Public Holiday (CN4d/CN4e)

        return (workingDaysInPeriod - publicHolidaysInPeriod) * hoursPerWeek / workingDaysPerWeek;
    }

    private int countWorkingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalStateException("startDate and endDate must be set before calculating hours due");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }

        int count = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (isWorkingDay(current)) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    private boolean isWorkingDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    private void validateEntryModification(Timesheet timesheet) {
        if (timesheet.getStatus() != TimesheetStatus.IN_PROGRESS) {
            throw new IllegalStateException("Entries can only be modified when Timesheet is IN_PROGRESS");
        }
        Contract contract = timesheet.getContract();
        if (contract == null || contract.getStatus() != ContractStatus.STARTED) {
            throw new IllegalStateException("Entries can only be modified when Contract is STARTED");
        }
    }

    private TimesheetEntry findEntry(Timesheet timesheet, Long entryId) {
        if (entryId == null) {
            throw new IllegalArgumentException("entryId cannot be null");
        }

        for (TimesheetEntry entry : timesheet.getEntries()) {
            if (entryId.equals(entry.getId())) {
                return entry;
            }
        }

        throw new IllegalArgumentException("No entry found with id: " + entryId);
    }

    @Override
    public List<TimesheetDTO> findByEmployeeUsername(String emailAddress) {
        List<Timesheet> entities = timesheetDAO.findByEmployeeUsername(emailAddress);

        List<TimesheetDTO> dtos = new ArrayList<>();

        for (Timesheet t : entities) {
            TimesheetDTO dto = new TimesheetDTO();
            dto.setStartDate(t.getStartDate());
            dto.setEndDate(t.getEndDate());
            dto.setStatus(t.getStatus().toString());
            dto.setHoursDue(t.getHoursDue());
            dto.setSignedByEmployee(t.getSignedByEmployee());
            dto.setSignedBySupervisor(t.getSignedBySupervisor());

            dtos.add(dto);
        }

        return dtos;
    }

    @Override
    public List<TimesheetDTO> findPendingArchivesForSecretary(String emailAddress) {
        List<Timesheet> entities = timesheetDAO.findPendingArchivesForSecretary(emailAddress);

        List<TimesheetDTO> dtos = new ArrayList<>();

        for (Timesheet t : entities) {
            TimesheetDTO dto = new TimesheetDTO();
            dto.setId(t.getId());
            dto.setStartDate(t.getStartDate());
            dto.setEndDate(t.getEndDate());
            dto.setStatus(t.getStatus().toString());
            dto.setHoursDue(t.getHoursDue());
            dto.setSignedByEmployee(t.getSignedByEmployee());
            dto.setSignedBySupervisor(t.getSignedBySupervisor());

            dtos.add(dto);
        }

        return dtos;
    }

    @Override
    public void archiveTimesheet(Long timesheetId) {
        Timesheet entity = timesheetDAO.findById(timesheetId);

        if (entity != null) {
            if (entity.getStatus() == TimesheetStatus.SIGNED_BY_SUPERVISOR) {
                entity.setStatus(TimesheetStatus.ARCHIVED);
                timesheetDAO.updateTimesheet(entity);

                Contract contract = entity.getContract();
                boolean allArchived = true;
                for (Timesheet t : contract.getTimesheet()) {
                    if (t.getStatus() != TimesheetStatus.ARCHIVED) {
                        allArchived = false;
                        break;
                    }
                }

                if (allArchived) {
                    contract.setStatus(ContractStatus.ARCHIVED);
                    contractsDao.UpdateContract(contract);
                }
            } else {
                throw new IllegalStateException("Timesheet must be SIGNED_BY_SUPERVISOR to be archived.");
            }
        }
    }

}
