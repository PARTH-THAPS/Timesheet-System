package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import tss.dto.PersonDTO;
import tss.entity.Role;
import tss.logic.PersonLogic;
import tss.web.i18n.Messages;

@Named
@ViewScoped
public class AdminUserBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private PersonLogic personLogic;

    private Long id;
    private PersonDTO person;

    private Set<Role> selectedRoleNames = new HashSet<>();

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
            selectedRoleNames = new HashSet<>(person.getRole());
        }
    }

    public String save() {

        try {

            person.setRole(new HashSet<>(selectedRoleNames));

            if (id == null) {
                personLogic.createPerson(
                        person.getFirstName(),
                        person.getLastName(),
                        person.getEmailAddress(),
                        false, // Consent
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
                            Messages.get("message.user.saved.summary"),
                            Messages.get("message.user.saved.detail")
                    )
            );

            return "/views/admin/usersmgmt.xhtml?faces-redirect=true";

        } catch (Exception e) {

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            Messages.get("message.user.saveFailed"),
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
                            Messages.get("message.user.deleteFailed"),
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

    public Set<Role> getSelectedRoleNames() {
        return selectedRoleNames;
    }

    public void setSelectedRoleNames(Set<Role> selectedRoleNames) {
        this.selectedRoleNames = selectedRoleNames;
    }

    public void revokeConsent() {
        if (person == null || person.getId() == null) {
            return;
        }

        try {
            personLogic.revokeConsent(person.getId());
            person.setConsent(false);

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            Messages.get("message.user.consentRevoked.summary"),
                            Messages.get("message.user.consentRevoked.detail")
                    )
            );

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            Messages.get("message.user.consentRevokeFailed"),
                            e.getMessage()
                    )
            );
        }
    }
}
