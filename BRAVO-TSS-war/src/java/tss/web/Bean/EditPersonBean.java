package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import tss.dto.PersonDTO;
import tss.entity.Person;
import tss.logic.PersonLogic;

@Named("EditPersonBean")
@ViewScoped
public class EditPersonBean implements Serializable {

    @EJB
    private PersonLogic personLogic;
    private Long id;
    private PersonDTO personDto;

    @PostConstruct
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
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Person updated", "Changes saved successfully."));
            return "personList?faces-redirect=true";
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating person", e.getMessage()));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating person", "An unexpected error occurred."));
            return null;
        }
    }

    public String delete() {
        try {
            personLogic.deletePerson(personDto);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Person deleted", "Deleted successfully."));
            return "personList?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error deleting person", e.getMessage()));
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
