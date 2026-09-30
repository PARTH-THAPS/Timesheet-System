package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import tss.dto.TimesheetDTO;
import tss.dto.User;
import tss.entity.TimesheetStatus;
import tss.logic.TimesheetLogic;

@Named
@ViewScoped
public class HomeDashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TimesheetLogic timesheetLogic;

    @Inject
    private loginBean loginBean;

    private List<TimesheetDTO> currentEmployeeTimesheets
            = Collections.emptyList();

    private List<TimesheetDTO> supervisorTimesheets
            = Collections.emptyList();

    private List<TimesheetDTO> secretaryPendingArchives
            = Collections.emptyList();

    @PostConstruct
    public void init() {

        User user = loginBean.getUser();

        if (user == null || loginBean.getActiveRole() == null) {
            return;
        }

        switch (loginBean.getActiveRole()) {

            case EMPLOYEE ->
                loadEmployeeDashboard(user);

            case SUPERVISOR ->
                loadSupervisorDashboard(user);

            case SECRETARY ->
                loadSecretaryDashboard();

            default -> {
                // No additional data required yet.
            }
        }
    }

    private void loadEmployeeDashboard(User user) {

        LocalDate today = LocalDate.now();

        List<TimesheetDTO> timesheets
                = timesheetLogic.findByEmployeeId(user.getId());

        if (timesheets == null) {
            currentEmployeeTimesheets = Collections.emptyList();
            return;
        }

        currentEmployeeTimesheets = timesheets.stream()
                .filter(timesheet
                        -> timesheet.getStartDate() != null
                && timesheet.getEndDate() != null
                && !today.isBefore(timesheet.getStartDate())
                && !today.isAfter(timesheet.getEndDate())
                )
                .toList();
    }

    private void loadSupervisorDashboard(User user) {

        List<TimesheetDTO> timesheets
                = timesheetLogic.findTimesheetsForSupervisor(
                        user.getId()
                );

        if (timesheets == null) {
            supervisorTimesheets = Collections.emptyList();
            return;
        }

        supervisorTimesheets = timesheets.stream()
                .filter(timesheet
                        -> timesheet.getStatus()
                == TimesheetStatus.SIGNED_BY_EMPLOYEE
                )
                .toList();
    }

    private void loadSecretaryDashboard() {

        Principal principal = FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal();

        if (principal == null) {
            secretaryPendingArchives
                    = Collections.emptyList();
            return;
        }

        List<TimesheetDTO> result
                = timesheetLogic.findPendingArchivesForSecretary(
                        principal.getName()
                );

        secretaryPendingArchives = result != null
                ? result
                : Collections.emptyList();
    }

    public List<TimesheetDTO> getCurrentEmployeeTimesheets() {
        return currentEmployeeTimesheets;
    }

    public boolean isHasCurrentEmployeeTimesheets() {
        return !currentEmployeeTimesheets.isEmpty();
    }

    public List<TimesheetDTO> getSupervisorTimesheets() {
        return supervisorTimesheets;
    }

    public int getPendingSupervisorCount() {
        return supervisorTimesheets.size();
    }

    public List<TimesheetDTO> getSecretaryPendingArchives() {
        return secretaryPendingArchives;
    }

    public int getPendingArchiveCount() {
        return secretaryPendingArchives.size();
    }
}
