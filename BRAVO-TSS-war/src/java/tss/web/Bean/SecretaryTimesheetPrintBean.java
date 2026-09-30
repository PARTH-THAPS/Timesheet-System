package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import tss.dto.TimesheetDTO;
import tss.dto.User;
import tss.logic.TimesheetLogic;

@Named("secretaryTimesheetPrintBean")
@ViewScoped
public class SecretaryTimesheetPrintBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TimesheetLogic timesheetLogic;

    @Inject
    private loginBean loginBean;

    private Long id;

    private List<TimesheetDTO> timesheets = Collections.emptyList();

    private boolean initialized;

    public void init() {

        if (initialized) {
            return;
        }

        initialized = true;

        User user = loginBean.getUser();

        if (user == null) {
            return;
        }

        if (id != null) {

            TimesheetDTO timesheet =
                    timesheetLogic.getTimesheetForSecretary(
                            id,
                            user.getId()
                    );

            if (timesheet != null) {
                timesheets = List.of(timesheet);
            }

            return;
        }

        List<TimesheetDTO> result =
                timesheetLogic.findTimesheetsForSecretary(
                        user.getId()
                );

        timesheets = result != null
                ? result
                : Collections.emptyList();
    }

    public double getReportedHours(TimesheetDTO timesheet) {

        if (timesheet == null
                || timesheet.getEntries() == null) {
            return 0.0;
        }

        return timesheet.getEntries()
                .stream()
                .mapToDouble(entry -> entry.getHours())
                .sum();
    }

    public double getBalance(TimesheetDTO timesheet) {

        if (timesheet == null) {
            return 0.0;
        }

        return getReportedHours(timesheet)
                - timesheet.getHoursDue();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<TimesheetDTO> getTimesheets() {
        return timesheets;
    }

    public boolean isSingleTimesheet() {
        return id != null;
    }
}