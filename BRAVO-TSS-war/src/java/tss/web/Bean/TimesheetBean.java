package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;

import tss.dto.PersonDTO;
import tss.dto.TimesheetDTO;
import tss.dto.User;
import tss.logic.PersonLogic;
import tss.logic.TimesheetLogic;

@ViewScoped
@Named("timesheetBean")
public class TimesheetBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TimesheetLogic tl;
    @Inject
    private loginBean loginBean;

    private List<TimesheetDTO> timesheetList = List.of();

    @PostConstruct
    public void init() {
        User user = loginBean.getUser();

        if (user != null) {
            loadTimesheetsForUser(user.getId());
        }

        // Temporary development functionality
        persons = personLogic.findAllPersons();
    }

    public void loadTimesheetsForUser(Long employeeId) {
        timesheetList = tl.findByEmployeeId(employeeId);
    }

    public List<TimesheetDTO> getTimesheetList() {
        return timesheetList;
    }


    // Development only

    @EJB
    private PersonLogic personLogic;

    private List<PersonDTO> persons;

    private Long selectedEmployeeId;

    public void loadSelectedEmployee() {
        if (selectedEmployeeId == null) {
            timesheetList = List.of();
            return;
        }

        PersonDTO person = persons.stream()
                .filter(p -> selectedEmployeeId.equals(p.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Selected employee could not be found."
                        )
                );

        loadTimesheetsForUser(person.getId());
    }

    public List<PersonDTO> getPersons() {
        return persons;
    }

    public Long getSelectedEmployeeId() {
        return selectedEmployeeId;
    }

    public void setSelectedEmployeeId(Long selectedEmployeeId) {
        this.selectedEmployeeId = selectedEmployeeId;
    }
}