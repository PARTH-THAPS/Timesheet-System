package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import tss.dto.TimesheetDTO;
import tss.dto.TimesheetEntryDTO;
import tss.entity.ReportType;
import tss.entity.TimesheetStatus;
import tss.logic.TimesheetLogic;

@Named
@ViewScoped
public class TimesheetDetailBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TimesheetLogic timesheetLogic;

    private Long id;

    private TimesheetDTO timesheet;

    private TimesheetEntryDTO entry;
    
    

    public void init() {
        if (timesheet != null) {
            return;
        }

        if (id == null) {
            throw new IllegalArgumentException(
                    "No timesheet id provided."
            );
        }

        timesheet = timesheetLogic.getTimesheetById(id);

        if (timesheet == null) {
            throw new IllegalArgumentException(
                    "No timesheet found with id: " + id
            );
        }
    }

    public void prepareNewEntry() {
        entry = new TimesheetEntryDTO();
    }

    public void saveEntry() {
        try {
            timesheetLogic.addEntry(timesheet.getId(),entry);
            showInfo("Entry added", "The entry was added successfully.");;

            reload();
            prepareNewEntry();

        } catch (Exception e) {
            showError(
                    "Could not save entry",
                    e.getMessage()
            );
        }
    }

    public void deleteEntry(TimesheetEntryDTO entry) {
        try {
            timesheetLogic.removeEntry(timesheet.getId(),entry.getId());

            reload();
            showInfo("Entry removed", "The entry was removed successfully.");

        } catch (Exception e) {
            showError(
                    "Could not delete entry",
                    e.getMessage()
            );
        }
    }
    
    public void signByEmployee() {
        try {
            timesheetLogic.signByEmployee(timesheet.getId());
            reload();
            showInfo("Timesheet signed", "You signed this timesheet.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError("Could not sign", e.getMessage());
        }
    }

    public void revokeEmployeeSignature() {
        try {
            timesheetLogic.revokeEmployeeSignature(timesheet.getId());
            reload();
            showInfo("Signature revoked", "Your signature was revoked.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError("Could not revoke signature", e.getMessage());
        }
    }

    private void reload() {
        timesheet = timesheetLogic.getTimesheetById(id);
    }

   
    public double getReportedHours() {
        if (timesheet == null || timesheet.getEntries() == null) {
            return 0;
        }

        return timesheet.getEntries()
                .stream()
                .mapToDouble(TimesheetEntryDTO::getHours)
                .sum();
    }

    public double getBalance() {
        if (timesheet == null) {
            return 0;
        }

        return getReportedHours() - timesheet.getHoursDue();
    }

    public boolean isEditable() {
        return timesheet != null
                && timesheet.getStatus()
                == TimesheetStatus.IN_PROGRESS;
    }

    public ReportType[] getReportTypes() {
        return ReportType.values();
    }
    
    private void showInfo(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
    }

    private void showError(
            String summary,
            String detail) {

        FacesContext.getCurrentInstance()
                .addMessage(
                        null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_ERROR,
                                summary,
                                detail
                        )
                );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TimesheetDTO getTimesheet() {
        return timesheet;
    }

    public TimesheetEntryDTO getEntry() {
        return entry;
    }
}
