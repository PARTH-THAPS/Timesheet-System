package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;
import tss.dto.TimesheetDTO;
import tss.dto.TimesheetEntryDTO;
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
import tss.entity.ReportType;
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
    public void generateTimesheetsForContract(Long contractId) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
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
            ts.setHoursDue(calculateHoursDue(startDate, periodEnd, contract.getHoursPerWeek(), contract.getWorkingDaysPerWeek(), contract));
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
    public TimesheetDTO addEntry(Long timesheetId, TimesheetEntryDTO entryDTO) {
        if (entryDTO == null) {
            throw new IllegalArgumentException("Entry cannot be null");
        }
        Timesheet timesheet = timesheetDAO.findById(timesheetId);
        if (timesheet == null) {
            throw new IllegalArgumentException("No timesheet found with id: " + timesheetId);
        }
        validateEntryModification(timesheet);

        TimesheetEntry entry = toEntity(entryDTO);

        if (entry.getType() == ReportType.VACATION) {
            validateVacationCap(timesheet.getContract(), entry.getHours(), null);
        }

        entry.setTimesheet(timesheet);
        timesheet.getEntries().add(entry);

        timesheetDAO.updateTimesheet(timesheet);
        return toDTO(timesheet);
    }

    //Add Timesheet Entries
    public void addTimesheet(Timesheet timeSheet)
    {          
    TimesheetEntry timesheetEntries= new TimesheetEntry();
    List <Holiday>holidays=checkForHolidays(timeSheet,"RLP");
    timesheetEntries.setTimesheet(timeSheet);
    timesheetEntries.setType(null);
    timesheetEntries.setDescription(null);
    timesheetEntries.setEntryDate(null);
    timesheetEntries.setStartTime(null);
    timesheetEntries.setEndTime(null);
    timesheetEntriesDao.addTimeSheetEntries(timesheetEntries);
    }
    //holidays logic
    public List<Holiday> checkForHolidays(Timesheet timeSheet,String State)
    {  
             LocalDate startDate=timeSheet.getStartDate();
             LocalDate endDate=timeSheet.getEndDate();
             return holidayLogic.findByStateAndRange(State, startDate, endDate);
    }


    @Override
    public TimesheetDTO updateEntry(Long timesheetId, Long entryId, TimesheetEntryDTO updatedEntryDTO) {
        if (updatedEntryDTO == null) {
            throw new IllegalArgumentException("Updated entry cannot be null");
        }
        Timesheet timesheet = timesheetDAO.findById(timesheetId);
        if (timesheet == null) {
            throw new IllegalArgumentException("No timesheet found with id: " + timesheetId);
        }
        validateEntryModification(timesheet);

        TimesheetEntry existing = findEntry(timesheet, entryId);

        ReportType updatedType = toReportType(updatedEntryDTO.getType());
        double updatedHours = computeHours(updatedEntryDTO.getStartTime(), updatedEntryDTO.getEndTime());

        if (updatedType == ReportType.VACATION) {
            validateVacationCap(timesheet.getContract(), updatedHours, entryId);
        }

        existing.setEntryDate(updatedEntryDTO.getEntryDate());
        existing.setStartTime(updatedEntryDTO.getStartTime());
        existing.setEndTime(updatedEntryDTO.getEndTime());
        existing.setDescription(updatedEntryDTO.getDescription());
        existing.setType(updatedType);

        timesheetDAO.updateTimesheet(timesheet);
        return toDTO(timesheet);
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
    public void deleteInProgressTimesheets(Long contractId) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
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
    public List<TimesheetDTO> getTimesheetsForContract(Long contractId) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }
        List<Timesheet> timesheets = timesheetDAO.findByContract(contract);
        if (timesheets == null) {
            return List.of();
        }
        return timesheets.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public TimesheetDTO getTimesheetById(Long timesheetId) {
        Timesheet timesheet = timesheetDAO.findById(timesheetId);
        if (timesheet == null) {
            throw new IllegalArgumentException("No timesheet found with id: " + timesheetId);
        }
        return toDTO(timesheet);
    }

    @Override
    public TimesheetDTO signByEmployee(Long timesheetId) {
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
        return toDTO(timesheet);
    }

    @Override
    public TimesheetDTO revokeEmployeeSignature(Long timesheetId) {
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
        return toDTO(timesheet);
    }

    @Override
    public TimesheetDTO signBySupervisor(Long timesheetId) {
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
        return toDTO(timesheet);
    }

    @Override
    public TimesheetDTO requestChanges(Long timesheetId) {
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
        return toDTO(timesheet);
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

    private void validateVacationCap(Contract contract, double newHours, Long excludingEntryId) {
        if (contract == null) {
            throw new IllegalStateException("Timesheet has no contract; cannot validate vacation cap");
        }

        double usedVacationHours = calculateUsedVacationHours(contract, excludingEntryId);
        double allowed = contract.getVacationHours();
        double totalAfterAdd = usedVacationHours + newHours;

        if (totalAfterAdd > allowed) {
            throw new IllegalStateException(
                    "Vacation hours exceed contract allowance: used=" + usedVacationHours
                    + " + new=" + newHours + " > allowed=" + allowed);
        }
    }

    private double calculateUsedVacationHours(Contract contract, Long excludingEntryId) {
        double total = 0.0;

        if (contract.getTimesheet() == null) {
            return total;
        }

        for (Timesheet timesheet : contract.getTimesheet()) {
            if (timesheet.getEntries() == null) {
                continue;
            }
            for (TimesheetEntry entry : timesheet.getEntries()) {
                if (entry.getType() != ReportType.VACATION) {
                    continue;
                }
                if (isExcluded(entry, excludingEntryId)) {
                    continue;
                }
                total += entry.getHours();
            }
        }
        return total;
    }

    private boolean isExcluded(TimesheetEntry entry, Long excludingEntryId) {
        return excludingEntryId != null && excludingEntryId.equals(entry.getId());
    }

    private double computeHours(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes() / 60.0;
        }
        return 0.0;
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

    private TimesheetDTO toDTO(Timesheet ts) {
        TimesheetDTO dto = new TimesheetDTO();
        dto.setUuid(ts.getUuid());
        dto.setJpaVersion(ts.getJpaVersion());
        dto.setStartDate(ts.getStartDate());
        dto.setEndDate(ts.getEndDate());
        dto.setStatus(ts.getStatus() != null ? ts.getStatus().name() : null);
        dto.setSignedByEmployee(ts.getSignedByEmployee());
        dto.setSignedBySupervisor(ts.getSignedBySupervisor());
        dto.setHoursDue(ts.getHoursDue());
        dto.setContractId(ts.getContract() != null ? ts.getContract().getId() : null);
        if (ts.getEntries() != null) {
            dto.setEntries(ts.getEntries().stream().map(this::toDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private TimesheetEntryDTO toDTO(TimesheetEntry entry) {
        TimesheetEntryDTO dto = new TimesheetEntryDTO();
        dto.setUuid(entry.getUuid());
        dto.setJpaVersion(entry.getJpaVersion());
        dto.setEntryDate(entry.getEntryDate());
        dto.setStartTime(entry.getStartTime());
        dto.setEndTime(entry.getEndTime());
        dto.setHours(entry.getHours());
        dto.setDescription(entry.getDescription());
        dto.setType(entry.getType() != null ? entry.getType().name() : null);
        return dto;
    }

    private TimesheetEntry toEntity(TimesheetEntryDTO dto) {
        TimesheetEntry entry = new TimesheetEntry();
        entry.setEntryDate(dto.getEntryDate());
        entry.setStartTime(dto.getStartTime());
        entry.setEndTime(dto.getEndTime());
        entry.setDescription(dto.getDescription());
        entry.setType(toReportType(dto.getType()));
        return entry;
    }

    private ReportType toReportType(String type) {
        return type != null ? ReportType.valueOf(type) : null;
    }
}
