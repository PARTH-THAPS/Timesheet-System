package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;
import tss.dto.PersonDTO;
import tss.entity.Role;
import tss.logic.PersonLogic;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import tss.web.i18n.Messages;

@Named("CreatePersonBean")
@RequestScoped
public class createPersonBean {

    private static final Logger LOG = Logger.getLogger(createPersonBean.class.getName());

    @EJB
    private PersonLogic personLogic;

    private PersonDTO personDto = new PersonDTO();

    public String createPerson() {
        try {
            Set<Role> roles = new HashSet<>();
            roles.add(Role.GUEST);

            PersonDTO created = personLogic.createPerson(
                    personDto.getFirstName(),
                    personDto.getLastName(),
                    personDto.getEmailAddress(),
                    personDto.isConsent(),
                    personDto.getPassword(),
                    roles
            );

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            Messages.get("message.signup.created.summary"),
                            Messages.get(
                                    "message.signup.created.detail",
                                    created.getFirstName()
                            )
                    )
            );

            return "/views/login.xhtml?faces-redirect=true";

        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Validation error creating person", e);
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            Messages.get("message.signup.createFailed"),
                            e.getMessage()
                    )
            );
            return null;

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unexpected error creating person", e);
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            Messages.get("message.signup.createFailed"),
                            Messages.get("message.signup.createFailed.detail")
                    )
            );
            return null;
        }
    }

    public PersonDTO getPersonDto() {
        return personDto;
    }

    public void setPersonDto(PersonDTO personDto) {
        this.personDto = personDto;
    }

    public void validateConsent(
            FacesContext context,
            UIComponent component,
            Object value
    ) {
        if (!(value instanceof Boolean) || !((Boolean) value)) {
            throw new ValidatorException(
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            Messages.get("signup.consent.required"),
                            null
                    )
            );
        }
    }
}
