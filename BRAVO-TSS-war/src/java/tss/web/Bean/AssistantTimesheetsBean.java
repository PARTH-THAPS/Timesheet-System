package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
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

@Named("assistantTimesheetsBean")
@ViewScoped
public class AssistantTimesheetsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TimesheetLogic timesheetLogic;
    @Inject
    private loginBean loginBean;

    private List<TimesheetDTO> timesheetList = Collections.emptyList();

    @PostConstruct
    public void init() {
        User user = loginBean.getUser();

        if (user != null) {
            List<TimesheetDTO> result =
                    timesheetLogic.findTimesheetsForAssistant(user.getId());
            timesheetList = result != null ? result : Collections.emptyList();
        }
    }

    public List<TimesheetDTO> getTimesheetList() {
        return timesheetList;
    }
}