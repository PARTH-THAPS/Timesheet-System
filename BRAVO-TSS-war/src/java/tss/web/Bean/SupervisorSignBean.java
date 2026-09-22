package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

import tss.dto.TimesheetDTO;
import tss.logic.TimesheetLogic;

@Named("supervisorSignBean")
@ViewScoped
public class SupervisorSignBean
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TimesheetLogic timesheetLogic;

    private List<TimesheetDTO> timesheetList =Collections.emptyList();

    @PostConstruct
    public void init() {
        reload();
    }

    public void loadPendingTimesheets(
            String emailAddress) {

        List<TimesheetDTO> result =timesheetLogic.findPendingSignaturesForSupervisor(emailAddress);

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
            showError(
                    "Could not request changes","No timesheet was selected.");
            return;
        }

        try {
            timesheetLogic.requestChanges(timesheetId);

            showInfo(
                    "Changes requested","The employee can edit this timesheet again.");

            reload();

        } catch (IllegalArgumentException
                | IllegalStateException e) {

            showError(
                    "Could not request changes",messageOf(e));
        }
    }

    private void reload() {
        Principal principal = FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal();

        if (principal != null) {
            loadPendingTimesheets(
                    principal.getName()
            );
        } else {
            timesheetList = Collections.emptyList();
        }
    }

    private String messageOf(Exception e) {
        return e.getMessage() != null
                ? e.getMessage()
                : "The operation could not be completed.";
    }

    private void showInfo(
            String summary,
            String detail) {

        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        summary,
                        detail
                )
        );
    }

    private void showError(
            String summary,
            String detail) {

        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        summary,
                        detail
                )
        );
    }

    public List<TimesheetDTO> getTimesheetList() {
        return timesheetList;
    }
}