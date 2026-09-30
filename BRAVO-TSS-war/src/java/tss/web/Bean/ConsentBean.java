package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

import tss.dto.PersonDTO;
import tss.dto.User;
import tss.logic.PersonLogic;
import tss.logic.UserLogic;

@Named
@ViewScoped
public class ConsentBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private UserLogic userLogic;

    @EJB
    private PersonLogic personLogic;

    private PersonDTO person;

    public void init() {

        if (person != null) {
            return;
        }

        User user = userLogic.getCurrentUser();

        if (user == null) {
            return;
        }

        person = personLogic.findPerson(user.getId());

        if (person.isConsent()) {
            redirectHome();
        }
    }

    public void accept() {

        try {
            User user = userLogic.getCurrentUser();

            if (user == null) {
                throw new IllegalStateException(
                        "No authenticated user found."
                );
            }

            personLogic.acceptConsent(user.getId());

            redirectHome();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Could not save consent",
                            e.getMessage()
                    )
            );
        }
    }

    private void redirectHome() {
        try {
            FacesContext context
                    = FacesContext.getCurrentInstance();

            String contextPath
                    = context.getExternalContext()
                            .getRequestContextPath();

            context.getExternalContext().redirect(
                    contextPath + "/views/portal/home.xhtml"
            );

            context.responseComplete();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Could not continue",
                            e.getMessage()
                    )
            );
        }
    }

    public PersonDTO getPerson() {
        return person;
    }
}
