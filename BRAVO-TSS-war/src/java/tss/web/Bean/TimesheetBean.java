package tss.web.Bean;


import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import tss.dto.TimesheetDTO;
import tss.logic.TimesheetLogic;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;

@ViewScoped
@Named("timesheetBean")
public class TimesheetBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private TimesheetLogic tl;

    private List<TimesheetDTO> timesheetList;

    @PostConstruct
    public void init() {
        Principal principal = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal();

       if (principal != null) {
            String emailAddress = principal.getName();
            loadTimesheetsForUser(emailAddress);
        }
    }

    public void loadTimesheetsForUser(String emailAddress) {
        timesheetList = tl.findByEmployeeUsername(emailAddress);
    }

    public List<TimesheetDTO> getTimesheetList() {
        return timesheetList;
    }
};
