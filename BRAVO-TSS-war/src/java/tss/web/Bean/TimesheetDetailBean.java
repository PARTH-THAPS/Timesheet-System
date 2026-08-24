package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import tss.dto.TimesheetDTO;
import tss.dto.TimesheetEntryDTO;
import tss.logic.TimesheetLogic;

@Named("timesheetDetailBean")
@ViewScoped
public class TimesheetDetailBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TimesheetLogic tl;
    
    private Long timesheetId;

    private TimesheetDTO timesheet;

    private TimesheetEntryDTO newEntry = new TimesheetEntryDTO();

    public void init() {
        if (timesheetId != null && timesheet == null) {
            loadTimesheet();
        }
    }

    private void loadTimesheet() {
        timesheet = tl.getTimesheetById(timesheetId);
    }


    public String addEntry() {
        try {
            timesheet = tl.addEntry(timesheetId, newEntry);
            newEntry = new TimesheetEntryDTO(); 
            addMessage(FacesMessage.SEVERITY_INFO, "Entry added", "The entry was added successfully.");
            return null; 
        } catch (IllegalArgumentException | IllegalStateException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Could not add entry", e.getMessage());
            return null;
        }
    }

    public String updateEntry(Long entryId, TimesheetEntryDTO updatedEntry) {
        try {
            timesheet = tl.updateEntry(timesheetId, entryId, updatedEntry);
            addMessage(FacesMessage.SEVERITY_INFO, "Entry updated", "The entry was updated successfully.");
            return null;
        } catch (IllegalArgumentException | IllegalStateException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Could not update entry", e.getMessage());
            return null;
        }
    }

    public String removeEntry(Long entryId) {
        try {
            tl.removeEntry(timesheetId, entryId);
            loadTimesheet(); 
            addMessage(FacesMessage.SEVERITY_INFO, "Entry removed", "The entry was removed successfully.");
            return null;
        } catch (IllegalArgumentException | IllegalStateException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Could not remove entry", e.getMessage());
            return null;
        }
    }


    public String signByEmployee() {
        return doTransition(() -> tl.signByEmployee(timesheetId), "Timesheet signed", "You signed this timesheet.");
    }

    public String revokeEmployeeSignature() {
        return doTransition(() -> tl.revokeEmployeeSignature(timesheetId), "Signature revoked", "Your signature was revoked.");
    }

    public String signBySupervisor() {
        return doTransition(() -> tl.signBySupervisor(timesheetId), "Timesheet approved", "You signed this timesheet as supervisor.");
    }

    public String requestChanges() {
        return doTransition(() -> tl.requestChanges(timesheetId), "Changes requested", "The employee can now edit this timesheet again.");
    }

    
    private String doTransition(java.util.function.Supplier<TimesheetDTO> action, String summary, String detail) {
        try {
            timesheet = action.get();
            addMessage(FacesMessage.SEVERITY_INFO, summary, detail);
            return null;
        } catch (IllegalArgumentException | IllegalStateException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Action failed", e.getMessage());
            return null;
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public Long getTimesheetId() {
        return timesheetId;
    }

    public void setTimesheetId(Long timesheetId) {
        this.timesheetId = timesheetId;
    }

    public TimesheetDTO getTimesheet() {
        return timesheet;
    }

    public TimesheetEntryDTO getNewEntry() {
        return newEntry;
    }

    public void setNewEntry(TimesheetEntryDTO newEntry) {
        this.newEntry = newEntry;
    }
}
