package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import tss.dto.TimesheetDTO;
import tss.dto.User;
import tss.entity.TimesheetStatus;
import tss.logic.TimesheetLogic;
import tss.web.i18n.Messages;

@Named("supervisorSignBean")
@ViewScoped
public class SupervisorSignBean
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TimesheetLogic timesheetLogic;
    @Inject
    private loginBean loginBean;

    private List<TimesheetDTO> timesheetList = Collections.emptyList();

    @PostConstruct
    public void init() {
        reload();
    }

    public void loadTimesheets(Long supervisorId) {
        List<TimesheetDTO> result
                = timesheetLogic.findTimesheetsForSupervisor(supervisorId);

        timesheetList = result != null
                ? result
                : Collections.emptyList();
    }

    public void signTimesheet(Long timesheetId) {
        if (timesheetId == null) {
            showError(
                    Messages.get("message.timesheet.signFailed"),
                    Messages.get("message.timesheet.notSelected")
            );
            return;
        }

        try {
            timesheetLogic.signBySupervisor(timesheetId);

            showInfo(
                    Messages.get("message.timesheet.signed.summary"),
                    Messages.get("message.timesheet.signed.detail")
            );

            reload();

        } catch (IllegalArgumentException
                | IllegalStateException e) {

            showError(
                    Messages.get("message.timesheet.signFailed"),
                    messageOf(e)
            );
        }
    }

    public void requestChanges(Long timesheetId) {
        if (timesheetId == null) {
            showError(
                    Messages.get("message.timesheet.requestChangesFailed"),
                    Messages.get("message.timesheet.notSelected")
            );
            return;
        }

        User user = loginBean.getUser();

        if (user == null) {
            showError(
                    Messages.get("message.timesheet.requestChangesFailed"),
                    Messages.get("message.auth.noSupervisor")
            );
            return;
        }

        try {
            timesheetLogic.requestChangesBySupervisor(timesheetId, user.getId());
            showInfo(
                    Messages.get("message.timesheet.changesRequested.summary"),
                    Messages.get("message.timesheet.changesRequested.detail")
            );
            reload();

        } catch (IllegalArgumentException | IllegalStateException e) {
            showError(
                    Messages.get("message.timesheet.requestChangesFailed"),
                    messageOf(e)
            );
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
        return e.getMessage() != null
                ? e.getMessage()
                : Messages.get("message.common.operationFailed");
    }

    private void showInfo(String summary, String detail) {

        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }

    private void showError(String summary, String detail) {

        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail)
        );
    }

    public List<TimesheetDTO> getTimesheetList() {
        return timesheetList;
    }

    public List<TimesheetStatus> getStatuses() {
        return Arrays.asList(TimesheetStatus.values());
    }
}
