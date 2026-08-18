package tss.logic;

import jakarta.ejb.Remote;
import tss.dto.TimesheetDTO;
import tss.entity.Contract;
import java.util.List;
import tss.entity.Timesheet;
import tss.entity.TimesheetEntry;
import tss.entity.TimesheetStatus;

@Remote
public interface TimesheetLogic {

    void generateTimesheetsForContract(Contract contract);

    Timesheet addEntry(Long timesheetId, TimesheetEntry entry);

    Timesheet updateEntry(Long timesheetId, Long entryId, TimesheetEntry entry);

    void removeEntry(Long timesheetId, Long entryId);

    void deleteInProgressTimesheets(Contract contract);
    
    List<Timesheet> getTimesheetsForContract(Long contractId);

    Timesheet getTimesheetById(Long timesheetId);
    
    Timesheet signByEmployee(Long timesheetId);

    Timesheet revokeEmployeeSignature(Long timesheetId);

    Timesheet signBySupervisor(Long timesheetId);

    Timesheet requestChanges(Long timesheetId);

    List<TimesheetDTO> findByEmployeeUsername(String emailAddress);
}