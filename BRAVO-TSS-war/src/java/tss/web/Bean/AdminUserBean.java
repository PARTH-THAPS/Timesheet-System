package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import tss.dto.PersonDTO;
import tss.entity.Role;
import tss.logic.PersonLogic;

@Named
@ViewScoped
public class AdminUserBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private PersonLogic personLogic;

    private Long id;
    private PersonDTO person;

   
    private Set<String> selectedRoleNames = new HashSet<>();

    public void init() {

        if (person != null) {
            return;
        }

        if (id == null) {
            person = new PersonDTO();
            person.setRole(new HashSet<>()); 
        } else {
            person = personLogic.findPerson(id);
        }

        if (person.getRole() != null) {
            selectedRoleNames = person.getRole().stream()
                    .map(Role::name)
                    .collect(Collectors.toSet());
        }
    }

    public String save() {

        try {

            Set<Role> roles = selectedRoleNames.stream()
                    .map(Role::valueOf)
                    .collect(Collectors.toSet());
            person.setRole(roles);

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

    public Set<String> getSelectedRoleNames() {
        return selectedRoleNames;
    }

    public void setSelectedRoleNames(Set<String> selectedRoleNames) {
        this.selectedRoleNames = selectedRoleNames;
    }
}