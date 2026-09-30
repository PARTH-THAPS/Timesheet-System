package tss.logic;

import jakarta.ejb.Remote;
import java.util.List;
import tss.dto.TimesheetDTO;
import tss.dto.TimesheetEntryDTO;

/**
 * Remote interface managing the business logic for timesheets.
 */
@Remote
public interface TimesheetLogic {

    /**
     * Generates timesheets for a specified contract.
     * @param contractId The ID of the contract.
     */
    void generateTimesheetsForContract(Long contractId);

    /**
     * Adds a new entry to an existing timesheet.
     * @param timesheetId The ID of the timesheet.
     * @param entry The timesheet entry details to add.
     * @return The updated timesheet.
     */
    TimesheetDTO addEntry(Long timesheetId, TimesheetEntryDTO entry);

    /**
     * Updates an existing entry on a timesheet.
     * @param timesheetId The ID of the timesheet.
     * @param entryId The ID of the entry to update.
     * @param entry The updated entry details.
     * @return The updated timesheet.
     */
    TimesheetDTO updateEntry(Long timesheetId, Long entryId, TimesheetEntryDTO entry);

    /**
     * Removes a specific entry from a timesheet.
     * @param timesheetId The ID of the timesheet.
     * @param entryId The ID of the entry to remove.
     */
    void removeEntry(Long timesheetId, Long entryId);

    /**
     * Deletes all in-progress timesheets associated with a contract.
     * @param contractId The ID of the contract.
     */
    void deleteInProgressTimesheets(Long contractId);

    /**
     * Retrieves all timesheets for a given contract.
     * @param contractId The ID of the contract.
     * @return A list of timesheets belonging to the contract.
     */
    List<TimesheetDTO> getTimesheetsForContract(Long contractId);

    /**
     * Retrieves a specific timesheet by its ID.
     * @param timesheetId The ID of the timesheet.
     * @return The requested timesheet.
     */
    TimesheetDTO getTimesheetById(Long timesheetId);

    /**
     * Calculates the total used vacation hours for a contract.
     * @param contractId The ID of the contract.
     * @return The number of vacation hours used.
     */
    double getUsedVacationHours(Long contractId);

    /**
     * Signs the timesheet on behalf of the employee.
     * @param timesheetId The ID of the timesheet.
     * @return The signed timesheet.
     */
    TimesheetDTO signByEmployee(Long timesheetId);

    /**
     * Archives a specific timesheet.
     * @param timesheetId The ID of the timesheet to archive.
     */
    void archiveTimesheet(Long timesheetId);

    /**
     * Archives older timesheet records system-wide.
     * @return The number of archived records.
     */
    int archiveOldRecords();

    /**
     * Finds timesheets pending archival by a secretary.
     * @param emailAddress The email address of the secretary.
     * @return A list of timesheets pending archival.
     */
    List<TimesheetDTO> findPendingArchivesForSecretary(String emailAddress);

    /**
     * Revokes the employee's signature from a timesheet.
     * @param timesheetId The ID of the timesheet.
     * @return The updated timesheet without the signature.
     */
    TimesheetDTO revokeEmployeeSignature(Long timesheetId);

    /**
     * Signs the timesheet on behalf of the supervisor.
     * @param timesheetId The ID of the timesheet.
     * @return The signed timesheet.
     */
    TimesheetDTO signBySupervisor(Long timesheetId);

    /**
     * Finds timesheets pending signature for a specific supervisor.
     * @param emailAddress The email address of the supervisor.
     * @return A list of timesheets awaiting the supervisor's signature.
     */
    List<TimesheetDTO> findPendingSignaturesForSupervisor(String emailAddress);

    /**
     * Retrieves all timesheets for a specific employee.
     * @param personId The ID of the employee.
     * @return A list of the employee's timesheets.
     */
    List<TimesheetDTO> findByEmployeeId(Long personId);

    /**
     * Retrieves all timesheets assigned to a specific supervisor.
     * @param supervisorId The ID of the supervisor.
     * @return A list of timesheets for the supervisor.
     */
    List<TimesheetDTO> findTimesheetsForSupervisor(Long supervisorId);

    /**
     * Retrieves a specific timesheet for a supervisor's review.
     * @param timesheetId The ID of the timesheet.
     * @param supervisorId The ID of the supervisor.
     * @return The requested timesheet.
     */
    TimesheetDTO getTimesheetForSupervisor(Long timesheetId, Long supervisorId);

    /**
     * Requests changes to a timesheet by rejecting it (Supervisor action).
     * @param timesheetId The ID of the timesheet.
     * @param supervisorId The ID of the supervisor requesting changes.
     * @return The updated timesheet with a change request status.
     */
    TimesheetDTO requestChangesBySupervisor(Long timesheetId, Long supervisorId);

    /**
     * Checks if a supervisor has access rights to a specific timesheet.
     * @param timesheetId The ID of the timesheet.
     * @param supervisorId The ID of the supervisor.
     * @return True if access is permitted, false otherwise.
     */
    boolean canSupervisorAccessTimesheet(Long timesheetId, Long supervisorId);

    /**
     * Retrieves timesheets assigned to a specific assistant.
     * @param assistantId The ID of the assistant.
     * @return A list of timesheets for the assistant.
     */
    List<TimesheetDTO> findTimesheetsForAssistant(Long assistantId);

    /**
     * Retrieves a specific timesheet for an assistant's review.
     * @param timesheetId The ID of the timesheet.
     * @param assistantId The ID of the assistant.
     * @return The requested timesheet.
     */
    TimesheetDTO getTimesheetForAssistant(Long timesheetId, Long assistantId);

    /**
     * Requests changes to a timesheet (Assistant action).
     * @param timesheetId The ID of the timesheet.
     * @param assistantId The ID of the assistant requesting changes.
     * @return The updated timesheet.
     */
    TimesheetDTO requestChanges(Long timesheetId, Long assistantId);

    /**
     * Retrieves timesheets assigned to a specific secretary.
     * @param secretaryId The ID of the secretary.
     * @return A list of timesheets for the secretary.
     */
    List<TimesheetDTO> findTimesheetsForSecretary(Long secretaryId);

    /**
     * Retrieves a specific timesheet in the context of a secretary.
     * @param timesheetId The ID of the timesheet.
     * @param secretaryId The ID of the secretary.
     * @return The requested timesheet.
     */
    TimesheetDTO getTimesheetForSecretary(Long timesheetId, Long secretaryId);
}