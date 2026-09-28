package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

import tss.dto.TimesheetDTO;
import tss.dto.User;
import tss.logic.TimesheetLogic;

@Named("supervisorSignBean")
@ViewScoped
public class SupervisorSignBean
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TimesheetLogic timesheetLogic;
    @Inject
    private loginBean loginBean;

    private List<TimesheetDTO> timesheetList =Collections.emptyList();

    @PostConstruct
    public void init() {
        reload();
    }

    public void loadTimesheets(Long supervisorId) {
        List<TimesheetDTO> result =
                timesheetLogic.findTimesheetsForSupervisor(supervisorId);

        timesheetList = result != null
                ? result
                : Collections.emptyList();
    }

    public void signTimesheet(Long timesheetId) {
        if (timesheetId == null) {
            showError("Could not sign timesheet","No timesheet was selected.");
            return;
        }

        try {
            timesheetLogic.signBySupervisor(timesheetId);

            showInfo(
                    "Timesheet signed","The timesheet was signed successfully.");

            reload();

        } catch (IllegalArgumentException
                | IllegalStateException e) {

            showError(
                    "Could not sign timesheet",messageOf(e));
        }
    }

  public void requestChanges(Long timesheetId) {
        if (timesheetId == null) {
            showError("Could Not Request Changes", "No timesheet was selected.");
            return;
        }

        User user = loginBean.getUser();

        if (user == null) {
            showError("Could Not Request Changes", "No authenticated supervisor was found.");
            return;
        }

        try {
            timesheetLogic.requestChangesBySupervisor(timesheetId, user.getId());
            showInfo("Changes Requested", "The employee can edit this timesheet again.");
            reload();

        } catch (IllegalArgumentException| IllegalStateException e) {
            showError("Could Not Request Changes", messageOf(e));
        }
    }


     private void reload() {
        User user = loginBean.getUser();

        if (user != null) {
            loadTimesheets(user.getId());
        } else {
            timesheetList = Collections.emptyList();
        }
    }

    private String messageOf(Exception e) {
        return e.getMessage() != null ? e.getMessage(): "The operation could not be completed.";
    }

    private void showInfo(String summary,String detail) {

        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,summary,detail)
        );
    }

    private void showError(String summary,String detail) {

        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,summary,detail)
        );
    }

    public List<TimesheetDTO> getTimesheetList() {
        return timesheetList;
    }
}