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
            Person person = personLogic.findPerson(id);
            personDto = toDto(person);
        } else {
            personDto = new PersonDTO();
        }
    }

    public String save() {
        try {
            Person person = personLogic.findPerson(Long.valueOf(personDto.getUuid()));
            applyDtoToEntity(personDto, person);
            personLogic.updatePerson(person);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Person updated", "Changes saved successfully."));
            return "personList?faces-redirect=true";
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating person", e.getMessage()));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating person", "An unexpected error occurred."));
            return null;
        }
    }

    public String delete() {
        try {
            Person person = personLogic.findPerson(Long.valueOf(personDto.getUuid()));
            personLogic.deletePerson(person);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Person deleted", "Deleted successfully."));
            return "personList?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error deleting person", e.getMessage()));
            return null;
        }
    }

    public String cancel() {
        return "personList?faces-redirect=true";
    }

    private PersonDTO toDto(Person person) {
        PersonDTO dto = new PersonDTO();
        dto.setUuid(String.valueOf(person.getId()));
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setEmailAddress(person.getEmailAddress());
        dto.setRole(person.getRole() != null ? person.getRole().name() : null);
        return dto;
    }

    private void applyDtoToEntity(PersonDTO dto, Person person) {
        person.setFirstName(dto.getFirstName());
        person.setLastName(dto.getLastName());
        person.setEmailAddress(dto.getEmailAddress());
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
