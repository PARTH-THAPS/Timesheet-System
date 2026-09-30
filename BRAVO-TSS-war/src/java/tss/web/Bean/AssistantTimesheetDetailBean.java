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
import tss.entity.TimesheetStatus;
import tss.logic.TimesheetLogic;
import tss.web.i18n.Messages;

@Named("assistantTimesheetDetailBean")
@ViewScoped
public class AssistantTimesheetDetailBean implements Serializable {

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
                    Messages.get("message.timesheet.loadFailed"),
                    Messages.get("message.timesheet.noId")
            );
            return;
        }

        User user = loginBean.getUser();

        if (user == null) {
            showError(
                    Messages.get("message.timesheet.loadFailed"),
                    Messages.get("message.auth.noAssistant")
            );
            return;
        }

        try {
            timesheet = timesheetLogic.getTimesheetForAssistant(id, user.getId());
        } catch (IllegalArgumentException e) {
            showError(
                    Messages.get("message.timesheet.loadFailed"),
                    messageOf(e)
            );
        }
    }

    public void requestChanges() {
        if (timesheet == null || timesheet.getId() == null) {
            showError(
                    Messages.get("message.timesheet.requestChangesFailed"),
                    Messages.get("message.timesheet.notLoaded")
            );
            return;
        }

        Principal principal = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal();

        if (principal == null) {
            showError(
                    Messages.get("message.timesheet.requestChangesFailed"),
                    Messages.get("message.auth.noAssistant")
            );
            return;
        }

        User user = loginBean.getUser();

        if (user == null) {
            showError(
                    Messages.get("message.timesheet.requestChangesFailed"),
                    Messages.get("message.auth.noAssistant")
            );
            return;
        }

        try {
            timesheet = timesheetLogic.requestChanges(timesheet.getId(), user.getId());
            showInfo(
                    Messages.get("message.timesheet.changesRequested.summary"),
                    Messages.get("message.timesheet.changesRequested.detail")
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError(
                    Messages.get("message.timesheet.requestChangesFailed"),
                    messageOf(e)
            );
        }
    }

    public boolean canRequestChanges() {
        return timesheet != null
                && timesheet.getStatus() == TimesheetStatus.SIGNED_BY_EMPLOYEE;
    }

    public double getReportedHours() {
        if (timesheet == null || timesheet.getEntries() == null) {
            return 0.0;
        }
        return timesheet.getEntries().stream()
                .mapToDouble(TimesheetEntryDTO::getHours)
                .sum();
    }

    public double getBalance() {
        if (timesheet == null) {
            return 0.0;
        }
        return getReportedHours() - timesheet.getHoursDue();
    }

    private String messageOf(Exception e) {
        return e.getMessage() != null
                ? e.getMessage()
                : Messages.get("message.common.operationFailed");
    }

    private void showInfo(String summary, String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
    }

    private void showError(String summary, String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
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
