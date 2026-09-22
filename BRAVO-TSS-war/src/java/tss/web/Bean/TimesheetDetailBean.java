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

    private TimesheetEntryDTO entry = new TimesheetEntryDTO();

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
    if (timesheet == null || timesheet.getId() == null) {
        showError("Could not sign", "No timesheet is loaded.");
        return;
    }

    try {
        timesheet = timesheetLogic.signByEmployee(timesheet.getId());

        showInfo(
                "Timesheet signed",
                "The timesheet was signed successfully."
        );
    } catch (IllegalArgumentException | IllegalStateException e) {
        showError("Could not sign", messageOf(e));
    }
}

    public void revokeEmployeeSignature() {
    if (timesheet == null || timesheet.getId() == null) {
        showError("Could not revoke signature","No timesheet is loaded.");
        return;
    }

    try {
        timesheet = timesheetLogic.revokeEmployeeSignature(timesheet.getId());

        showInfo(
                "Signature revoked",
                "The employee signature was revoked."
        );
    } catch (IllegalArgumentException | IllegalStateException e) {
        showError("Could not revoke signature", messageOf(e)
        );
    }
}

    private String messageOf(Exception e) {
        return e.getMessage() != null
            ? e.getMessage()
            : "The operation could not be completed.";
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
    
    public boolean isCanSignByEmployee() {
    return timesheet != null
            && timesheet.getStatus()
                    == TimesheetStatus.IN_PROGRESS;
}

    public boolean isCanRevokeEmployeeSignature() {
        return timesheet != null
                && timesheet.getStatus()
                        == TimesheetStatus.SIGNED_BY_EMPLOYEE;
    }


    public boolean isEmployeeSigned() {
        return timesheet != null
                && timesheet.getSignedByEmployee() != null;
    }

    public boolean isSupervisorSigned() {
        return timesheet != null
                && timesheet.getSignedBySupervisor() != null;
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
    
    
    public double calculateEntryHours(TimesheetEntryDTO entry) {
    if (entry == null
            || entry.getStartTime() == null
            || entry.getEndTime() == null) {
        return 0;
    }
    long minutes = java.time.Duration.between(
            entry.getStartTime(),
            entry.getEndTime()
    ).toMinutes();

    
    return minutes / 60.0;
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
