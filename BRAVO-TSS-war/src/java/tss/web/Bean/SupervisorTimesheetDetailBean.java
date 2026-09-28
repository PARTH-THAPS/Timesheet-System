package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.security.Principal;

import tss.dto.TimesheetDTO;
import tss.dto.TimesheetEntryDTO;
import tss.dto.User;
import tss.logic.TimesheetLogic;

@Named("supervisorTimesheetDetailBean")
@ViewScoped
public class SupervisorTimesheetDetailBean
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TimesheetLogic timesheetLogic;
    @Inject
    private loginBean loginBean;


    private Long id;
    private TimesheetDTO timesheet;

    public void init() {
        if (timesheet != null) {
            return;
        }

        if (id == null) {
            showError(
                    "Could not load timesheet","No timesheet id was provided.");
            return;
        }

        User user = loginBean.getUser();

        if (user == null) {
            showError("Could Not Load Timesheet", "No authenticated supervisor was found.");
            return;
        }

        try {
            timesheet =timesheetLogic.getTimesheetForSupervisor(id,user.getId());
        } catch (IllegalArgumentException e) {
            showError("Could not load timesheet",e.getMessage()
            );
        }
    }

    public void signBySupervisor() {
        if (timesheet == null || timesheet.getId() == null) {
            showError("Could not sign","No timesheet is loaded.");
            return;
        }

        try {
            timesheet =timesheetLogic.signBySupervisor(timesheet.getId());
            showInfo("Timesheet signed","The timesheet was signed successfully.");
        } catch (IllegalArgumentException
                | IllegalStateException e) {
            showError("Could not sign",messageOf(e));
        }
    }

    public void requestChanges() {
        if (timesheet == null || timesheet.getId() == null) {
            showError("Could Not Request Changes", "No timesheet is loaded.");
            return;
        }

        User user = loginBean.getUser();

        if (user == null) {
            showError("Could Not Request Changes", "No authenticated supervisor was found.");
            return;
        }

        try {
            timesheet = timesheetLogic.requestChangesBySupervisor(timesheet.getId(), user.getId());
            showInfo("Changes Requested", "The employee can edit this timesheet again.");
        } 
        catch (IllegalArgumentException| IllegalStateException e) {
            showError("Could Not Request Changes", messageOf(e));
        }
    }
    
    public boolean isEmployeeSigned() {
    return timesheet != null&& timesheet.getSignedByEmployee() != null;
    }

    public boolean isSupervisorSigned() {
        return timesheet != null&& timesheet.getSignedBySupervisor() != null;
    }

    public boolean isEmployeeReviewActive() {
        return !isEmployeeSigned();
    }

    public boolean isSupervisorReviewActive() {
        return isEmployeeSigned()&& !isSupervisorSigned();
    }

    public double getReportedHours() {
        if (timesheet == null|| timesheet.getEntries() == null) {
            return 0.0;
        }

        return timesheet.getEntries()
                .stream()
                .mapToDouble(TimesheetEntryDTO::getHours)
                .sum();
    }

    public double getBalance() {
        if (timesheet == null) {
            return 0.0;
        }

        return getReportedHours()- timesheet.getHoursDue();
    }

    public boolean canSign() {
        return timesheet != null&& timesheet.getStatus()== tss.entity.TimesheetStatus.SIGNED_BY_EMPLOYEE;
    }

    public boolean canRequestChanges() {
        return canSign();
    }

    private String messageOf(Exception e) {
        return e.getMessage() != null
                ? e.getMessage()
                : "The operation could not be completed.";
    }

    private void showInfo(String summary, String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO,summary,detail)
                );
    }

    private void showError(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,summary,detail));
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
}