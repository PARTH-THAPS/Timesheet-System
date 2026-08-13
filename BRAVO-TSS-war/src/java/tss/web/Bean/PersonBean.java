package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import tss.dto.PersonDTO;
import tss.entity.Person;
import tss.entity.Role;
import tss.logic.PersonLogic;

@Named("personBean")
@RequestScoped
public class PersonBean {

    @EJB
    private PersonLogic personLogic;

    private PersonDTO personDto = new PersonDTO();

    public String createPerson() {
        try {
            //Role role = Role.valueOf(personDto.getRole().toUpperCase());

            Person created = personLogic.createPerson(
                    personDto.getFirstName(),
                    personDto.getLastName(),
                    personDto.getEmailAddress(),
                    personDto.isConsent(),
                    personDto.getPassword(),
                    Role.ADMIN
            );

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Account created", "Welcome, " + created.getFirstName()));

            return "success?faces-redirect=true";
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error creating person", e.getMessage()));
            return null;
        }
    }

    public PersonDTO getPersonDto() {
        return personDto;
    }

    public void setPersonDto(PersonDTO personDto) {
        this.personDto = personDto;
    }
}