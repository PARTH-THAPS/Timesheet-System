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

    TimesheetDTO signByEmployee(Long timesheetId);

    void archiveTimesheet(Long timesheetId);

    List<TimesheetDTO> findByEmployeeUsername(String emailAddress);

    List<TimesheetDTO> findPendingArchivesForSecretary(String emailAddress);

    TimesheetDTO revokeEmployeeSignature(Long timesheetId);

    TimesheetDTO signBySupervisor(Long timesheetId);

    TimesheetDTO requestChanges(Long timesheetId);

}