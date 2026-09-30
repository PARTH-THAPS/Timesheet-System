package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import tss.dto.PersonDTO;
import tss.logic.PersonLogic;
import tss.web.i18n.Messages;

@Named("EditPersonBean")
@ViewScoped
public class EditPersonBean implements Serializable {

    @EJB
    private PersonLogic personLogic;
    private Long id;
    private PersonDTO personDto;

    public void init() {
        if (id != null) {
            PersonDTO person = personLogic.findPerson(id);
            personDto = person;
        } else {
            personDto = new PersonDTO();
        }
    }

    public String save() {
        try {
            personLogic.updatePerson(personDto);
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            Messages.get("message.person.updated.summary"),
                            Messages.get("message.person.updated.detail")
                    )
            );
            return "personList?faces-redirect=true";
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            Messages.get("message.person.updateFailed"),
                            e.getMessage()
                    )
            );
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            Messages.get("message.person.updateFailed"),
                            Messages.get("message.common.unexpectedError")
                    )
            );
            return null;
        }
    }

    public String delete() {
        try {
            personLogic.deletePerson(personDto);
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            Messages.get("message.person.deleted.summary"),
                            Messages.get("message.person.deleted.detail")
                    )
            );
            return "personList?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            Messages.get("message.person.deleteFailed"),
                            e.getMessage()
                    )
            );
            return null;
        }
    }

    public String cancel() {
        return "personList?faces-redirect=true";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonDTO getPersonDto() {
        return personDto;
    }

    public void setPersonDto(PersonDTO personDto) {
        this.personDto = personDto;
    }
}
