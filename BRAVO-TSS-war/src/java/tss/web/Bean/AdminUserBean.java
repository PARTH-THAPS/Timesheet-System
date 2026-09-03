package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;

import tss.dto.PersonDTO;
import tss.entity.Role;
import tss.logic.PersonLogic;
import java.util.Set;

@Named
@ViewScoped
public class AdminUserBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private PersonLogic personLogic;

    private Long id;
    private PersonDTO person;

    public void init() {

        if (person != null) {
            return;
        }

        if (id == null) {
            person = new PersonDTO();
            person.setRole(Set.of(Role.EMPLOYEE));
        } else {
            person = personLogic.findPerson(id);
        }
    }

    public String save() {

        try {

            if (id == null) {
                personLogic.createPerson(
                        person.getFirstName(),
                        person.getLastName(),
                        person.getEmailAddress(),
                        person.isConsent(),
                        person.getPassword(),
                        person.getRole()
                );
            } else {
                personLogic.updatePerson(person);
            }

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "User saved",
                            "The user was saved successfully."
                    )
            );

            return "/views/admin/usersmgmt.xhtml?faces-redirect=true";

        } catch (Exception e) {

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Could not save user",
                            e.getMessage()
                    )
            );

            return null;
        }
    }

    public String delete() {

        if (id == null) {
            return null;
        }

        try {

            personLogic.deletePerson(person);

            return "/views/admin/usersmgmt.xhtml?faces-redirect=true";

        } catch (Exception e) {

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Could not delete user",
                            e.getMessage()
                    )
            );

            return null;
        }
    }

    public String cancel() {
        return "/views/admin/usersmgmt.xhtml?faces-redirect=true";
    }

    public boolean isNewUser() {
        return id == null;
    }

    public Role[] getRoles() {
        return Role.values();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonDTO getPerson() {
        return person;
    }
}