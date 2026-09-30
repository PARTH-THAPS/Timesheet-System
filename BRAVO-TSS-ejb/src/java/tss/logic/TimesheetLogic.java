package tss.logic;

import jakarta.ejb.Remote;
import java.util.List;
import tss.dto.TimesheetDTO;
import tss.dto.TimesheetEntryDTO;

@Remote
public interface TimesheetLogic {

    void generateTimesheetsForContract(Long contractId);

    TimesheetDTO addEntry(Long timesheetId, TimesheetEntryDTO entry);

    TimesheetDTO updateEntry(Long timesheetId, Long entryId, TimesheetEntryDTO entry);

    void removeEntry(Long timesheetId, Long entryId);

    void deleteInProgressTimesheets(Long contractId);

    List<TimesheetDTO> getTimesheetsForContract(Long contractId);

    TimesheetDTO getTimesheetById(Long timesheetId);
    
    double getUsedVacationHours(Long contractId);

    TimesheetDTO signByEmployee(Long timesheetId);

    void archiveTimesheet(Long timesheetId);
    
    int archiveOldRecords();

    List<TimesheetDTO> findPendingArchivesForSecretary(String emailAddress);

    TimesheetDTO revokeEmployeeSignature(Long timesheetId);

    TimesheetDTO signBySupervisor(Long timesheetId);
    
    List<TimesheetDTO> findPendingSignaturesForSupervisor(String emailAddress);

    List<TimesheetDTO> findByEmployeeId(Long personId);

    List<TimesheetDTO> findTimesheetsForSupervisor(Long supervisorId);
    
    TimesheetDTO getTimesheetForSupervisor(Long timesheetId, Long supervisorId);
    
    TimesheetDTO requestChangesBySupervisor(Long timesheetId, Long supervisorId);
    
    boolean canSupervisorAccessTimesheet(Long timesheetId, Long supervisorId);

    List<TimesheetDTO> findTimesheetsForAssistant(Long assistantId);
    
    TimesheetDTO getTimesheetForAssistant(Long timesheetId, Long assistantId);
    
    TimesheetDTO requestChanges(Long timesheetId, Long assistantId);

    List<TimesheetDTO> findTimesheetsForSecretary(Long secretaryId);
    
    TimesheetDTO getTimesheetForSecretary(Long timesheetId, Long secretaryId);
}