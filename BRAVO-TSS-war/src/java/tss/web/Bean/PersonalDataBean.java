package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.security.Principal;
import tss.dto.PersonDTO;
import tss.dto.User;
import tss.entity.Language;   // adjust to the real package of your enum
import tss.logic.PersonLogic;
import tss.logic.UserLogic;

@Named
@SessionScoped
public class PersonalDataBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private User currentUser;
    private PersonDTO personDTO;

    @EJB
    private UserLogic userLogic;

    @EJB
    private PersonLogic personLogic;

    private String oldPrincipalName = null;   // was Principal (not serializable)

    public User getUser() {
        Principal p = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal();

        if (p == null) {
            currentUser = null;
            personDTO = null;
            oldPrincipalName = null;
        } else {
            if (oldPrincipalName == null || !p.getName().equals(oldPrincipalName)) {
                currentUser = userLogic.getCurrentUser();
                personDTO = personLogic.findPerson(currentUser.getId());
            }
            oldPrincipalName = p.getName();
        }
        return currentUser;
    }

    public PersonDTO getPersonDTO() {
        return personDTO;
    }

    public Language[] getLanguages() {         
        return Language.values();
    }

    public String save() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            personDTO = personLogic.updatePerson(personDTO);   // use the persisted result
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Saved successfully!", null));
        } catch (EJBException | IllegalArgumentException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Saving failed: " + e.getMessage(), null));
        }
        return null;
    }
}