package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
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
import tss.dto.HolidayDTO;
import tss.entity.FederalState;
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
        double totalHoursDue = 0.0;

        while (!startDate.isAfter(endDate)) {
            LocalDate periodEnd = calculatePeriodEnd(startDate, contract.getFrequency(), endDate);

            Timesheet ts = new Timesheet();
            ts.setStatus(TimesheetStatus.IN_PROGRESS);
            ts.setStartDate(startDate);
            ts.setEndDate(periodEnd);
            ts.setContract(contract);
            double timesheetHoursDue = calculateHoursDue(startDate, periodEnd, contract.getHoursPerWeek(),contract.getWorkingDaysPerWeek(), contract);
            ts.setHoursDue(timesheetHoursDue);
            totalHoursDue += timesheetHoursDue;

            ts.setSignedByEmployee(null);
            ts.setSignedBySupervisor(null);

            timesheetDAO.createTimesheet(ts);

            startDate = periodEnd.plusDays(1);
        }

        contract.setHoursDue(totalHoursDue);
        contractsDao.UpdateContract(contract);
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
            throw new IllegalArgumentException(
                    "No timesheet found with id: " + timesheetId
            );
        }

        LocalDate startDate = timesheet.getStartDate();
        LocalDate endDate = timesheet.getEndDate();

        validateEntryModification(timesheet);

        TimesheetEntry entry = toEntity(entryDTO);

        double computedHours = computeHours(entry.getStartTime(), entry.getEndTime());
        entry.setHours(computedHours);

        if (entry.getEntryDate().isBefore(startDate) || entry.getEntryDate().isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "The entry date must fall between " + startDate + " and " + endDate + "."
            );
        }

        validateNoOverlap(timesheet, entry, null);

        if (entry.getType() == ReportType.VACATION) {
            validateVacationCap(timesheet.getContract(), computedHours, null);
        }

        entry.setTimesheet(timesheet);
        timesheet.getEntries().add(entry);
        timesheetDAO.updateTimesheet(timesheet);

        return toDTO(timesheet);
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

        TimesheetEntry candidate = new TimesheetEntry();
        candidate.setEntryDate(updatedEntryDTO.getEntryDate());
        candidate.setStartTime(updatedEntryDTO.getStartTime());
        candidate.setEndTime(updatedEntryDTO.getEndTime());

        if (candidate.getEntryDate().isBefore(timesheet.getStartDate())
                || candidate.getEntryDate().isAfter(timesheet.getEndDate())) {

            throw new IllegalArgumentException(
                    "The entry date must fall between "
                    + timesheet.getStartDate()
                    + " and "
                    + timesheet.getEndDate()
                    + "."
            );
        }

        validateNoOverlap(timesheet, candidate, entryId);

        if (updatedType == ReportType.VACATION) {
            validateVacationCap(timesheet.getContract(), updatedHours, entryId);
        }

        existing.setEntryDate(updatedEntryDTO.getEntryDate());
        existing.setStartTime(updatedEntryDTO.getStartTime());
        existing.setEndTime(updatedEntryDTO.getEndTime());
        existing.setHours(updatedHours);
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
    List<Timesheet> timesheets = timesheetDAO.findByContractId(contractId);

    List<Timesheet> toDelete = timesheets.stream()
            .filter(t -> t.getStatus() == TimesheetStatus.IN_PROGRESS)
            .toList();

    for (Timesheet ts : toDelete) {
        Contract contract = ts.getContract();
        if (contract != null && contract.getTimesheet() != null) {
            contract.getTimesheet().remove(ts);
        }
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
        Timesheet timesheet = timesheetDAO.findByIdWithEntries(timesheetId);
        if (timesheet == null) {
            throw new IllegalArgumentException("No timesheet found with id: " + timesheetId);
        }
        return toDTO(timesheet);
    }

    @Override
    public double getUsedVacationHours(Long contractId) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }
        return calculateUsedVacationHours(contract, null);
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
        
        if (LocalDate.now().isBefore(timesheet.getStartDate())) {
            throw new IllegalStateException("Timesheet cannot be signed for a future period (starts "+ timesheet.getStartDate() + ").");
        }

        timesheet.setSignedBySupervisor(LocalDate.now());
        timesheet.setStatus(TimesheetStatus.SIGNED_BY_SUPERVISOR);
        timesheetDAO.updateTimesheet(timesheet);

        return toDTO(timesheet);
    }

    @Override
    public List<TimesheetDTO> findPendingSignaturesForSupervisor(String emailAddress) {

        List<Timesheet> entities = timesheetDAO.findPendingSignaturesForSupervisor(emailAddress);

        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimesheetDTO> findByEmployeeId(Long personId) {
        List<Timesheet> entities = timesheetDAO.findByEmployeeId(personId);
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimesheetDTO> findTimesheetsForSupervisor(Long supervisorId) {
        List<Timesheet> entities = timesheetDAO.findBySupervisorId(supervisorId);
        if (entities == null) 
            return List.of();
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TimesheetDTO getTimesheetForSupervisor(Long timesheetId, Long supervisorId) {
        Timesheet timesheet = timesheetDAO.findByIdForSupervisorId(timesheetId, supervisorId);
        return toDTO(timesheet);
    }

    @Override
    public TimesheetDTO requestChangesBySupervisor(Long timesheetId, Long supervisorId) {
        Timesheet timesheet = timesheetDAO.findByIdForSupervisorId(timesheetId, supervisorId);

        if (timesheet.getStatus() != TimesheetStatus.SIGNED_BY_EMPLOYEE) {
            throw new IllegalStateException(
                    "Changes can only be requested when status is SIGNED_BY_EMPLOYEE"
            );
        }

        timesheet.setSignedByEmployee(null);
        timesheet.setStatus(TimesheetStatus.IN_PROGRESS);
        timesheetDAO.updateTimesheet(timesheet);

        return toDTO(timesheet);
    }

    @Override
    public boolean canSupervisorAccessTimesheet(Long timesheetId, Long supervisorId) {
        if (timesheetId == null || supervisorId == null) {
            return false;
        }

        try {
            timesheetDAO.findByIdForSupervisorId(timesheetId, supervisorId);
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public List<TimesheetDTO> findTimesheetsForAssistant(Long assistantId) {
        List<Timesheet> entities = timesheetDAO.findByAssistantId(assistantId);
        return entities
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TimesheetDTO getTimesheetForAssistant(Long timesheetId, Long assistantId) {
        Timesheet timesheet = timesheetDAO.findByIdForAssistantId(timesheetId, assistantId);
        return toDTO(timesheet);
    }

    @Override
    public TimesheetDTO requestChanges(Long timesheetId, Long assistantId) {
        Timesheet timesheet = timesheetDAO.findByIdForAssistantId(timesheetId, assistantId);

        if (timesheet.getStatus() != TimesheetStatus.SIGNED_BY_EMPLOYEE) {
            throw new IllegalStateException(
                    "Changes can only be requested when status is SIGNED_BY_EMPLOYEE"
            );
        }

        timesheet.setSignedByEmployee(null);
        timesheet.setStatus(TimesheetStatus.IN_PROGRESS);
        timesheetDAO.updateTimesheet(timesheet);

        return toDTO(timesheet);
    }

    @Override
    public List<TimesheetDTO> findTimesheetsForSecretary(Long secretaryId) {
        List<Timesheet> entities = timesheetDAO.findBySecretaryId(secretaryId);
        return entities
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TimesheetDTO getTimesheetForSecretary(Long timesheetId, Long secretaryId) {
        Timesheet timesheet = timesheetDAO.findByIdForSecretaryId(timesheetId, secretaryId);
        return toDTO(timesheet);
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
            dto.setStatus(t.getStatus());
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

    @Override
    public int archiveOldRecords() {
        return timesheetDAO.deleteArchiveOldRecords();
    }

    private double calculateHoursDue(LocalDate startDate, LocalDate endDate, double hoursPerWeek,int workingDaysPerWeek, Contract contract) {
        if (contract == null) {
            throw new IllegalStateException("Timesheet must be linked to a contract");
        }
        if (workingDaysPerWeek <= 0) {
            throw new IllegalStateException("Contract workingDaysPerWeek must be greater than zero");
        }

        int workingDaysInPeriod = countWorkingDays(startDate, endDate, workingDaysPerWeek);

        List<HolidayDTO> holidays = checkForHolidays(startDate, endDate, contract.getState());
        List<HolidayDTO> holidaysInWeekdays = holidays.stream()
                .filter(h -> isWorkingDay(h.getDate(), workingDaysPerWeek))
                .toList();

        int publicHolidaysInPeriod = holidaysInWeekdays.size();

        return (workingDaysInPeriod - publicHolidaysInPeriod) * hoursPerWeek / workingDaysPerWeek;
    }

    private int countWorkingDays(LocalDate startDate, LocalDate endDate, int workingDaysPerWeek) {
        if (startDate == null || endDate == null) {
            throw new IllegalStateException("startDate and endDate must be set before calculating hours due");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }

        int count = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (isWorkingDay(current, workingDaysPerWeek)) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    private boolean isWorkingDay(LocalDate date, int workingDaysPerWeek) {
        int dayOfWeekValue = date.getDayOfWeek().getValue();
        return dayOfWeekValue <= workingDaysPerWeek;
    }
    
    public List<HolidayDTO> checkForHolidays(LocalDate startDate, LocalDate endDate, FederalState State) {
        return holidayLogic.findByStateAndRange(State, startDate, endDate);
    }

    private void validateEntryModification(Timesheet timesheet) {
        if (timesheet.getStatus() != TimesheetStatus.IN_PROGRESS) {
            throw new IllegalStateException("Entries can only be modified when Timesheet is IN_PROGRESS");
        }

        Contract contract = timesheet.getContract();
        if (contract == null || contract.getStatus() != ContractStatus.STARTED) {
            throw new IllegalStateException("Entries can only be modified when Contract is STARTED");
        }
        LocalDate today = LocalDate.now();
        if (today.isBefore(timesheet.getStartDate())) {
            throw new IllegalStateException(
                    "Entries can only be modified for the current period ("+ timesheet.getStartDate() + " - " + timesheet.getEndDate() + ").");
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

    private void validateNoOverlap(Timesheet timesheet, TimesheetEntry newEntry, Long excludingEntryId) {
        if (newEntry.getEntryDate() == null || newEntry.getStartTime() == null || newEntry.getEndTime() == null) {
            return;
        }

        if (!newEntry.getStartTime().isBefore(newEntry.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        for (TimesheetEntry existing : timesheet.getEntries()) {
            if (isExcluded(existing, excludingEntryId)) {
                continue;
            }

            if (existing.getEntryDate() == null || existing.getStartTime() == null || existing.getEndTime() == null) {
                continue;
            }

            if (!existing.getEntryDate().equals(newEntry.getEntryDate())) {
                continue;
            }

            boolean overlaps
                    = newEntry.getStartTime().isBefore(existing.getEndTime())
                    && existing.getStartTime().isBefore(newEntry.getEndTime());

            if (overlaps) {
                throw new IllegalStateException("This entry overlaps with an existing entry from "
                        + existing.getStartTime()
                        + " to "
                        + existing.getEndTime()
                        + " on "
                        + existing.getEntryDate()
                        + ".");
            }
        }
    }

    private double calculateUsedVacationHours(Contract contract, Long excludingEntryId) {
        return timesheetEntriesDao.sumVacationHoursForContract(contract.getId(), excludingEntryId);
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
        dto.setId(ts.getId());
        dto.setJpaVersion(ts.getJpaVersion());
        dto.setStartDate(ts.getStartDate());
        dto.setEndDate(ts.getEndDate());
        dto.setStatus(ts.getStatus() != null ? ts.getStatus() : null);
        dto.setSignedByEmployee(ts.getSignedByEmployee());
        dto.setSignedBySupervisor(ts.getSignedBySupervisor());
        dto.setHoursDue(ts.getHoursDue());
        dto.setContractId(ts.getContract() != null ? ts.getContract().getId() : null);
        if (ts.getEntries() != null) {
            List<TimesheetEntryDTO> sortedEntries = ts.getEntries().stream().map(this::toDTO).sorted(
                    Comparator.comparing(TimesheetEntryDTO::getEntryDate, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(
                            TimesheetEntryDTO::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList());

            dto.setEntries(sortedEntries);
        }
        if (ts.getContract() != null && ts.getContract().getEmployee() != null) {
            dto.setEmployeeFirstName(ts.getContract().getEmployee().getFirstName());
            dto.setEmployeeLastName(ts.getContract().getEmployee().getLastName());
        }
        return dto;
    }

    private TimesheetEntryDTO toDTO(TimesheetEntry entry) {
        TimesheetEntryDTO dto = new TimesheetEntryDTO();
        dto.setId(entry.getId());
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
