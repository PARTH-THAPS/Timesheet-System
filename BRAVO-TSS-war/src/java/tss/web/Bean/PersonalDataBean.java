package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.security.Principal;
import java.time.LocalDate;
import tss.dto.PersonDTO;
import tss.dto.User;
import tss.entity.Language;
import tss.entity.Role;
import tss.logic.PersonLogic;
import tss.logic.UserLogic;
import tss.web.i18n.Messages;

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

    @Inject
    private LocaleBean localeBean;

    private String oldPrincipalName = null;

    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

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
            personDTO = personLogic.updatePerson(personDTO);

            if (personDTO.getPreferredLanguage() != null) {
                localeBean.applyPreferredLanguage(
                        personDTO.getPreferredLanguage()
                );
            }

            ctx.addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            Messages.get("message.personalInfo.saved"),
                            null
                    )
            );

        } catch (EJBException | IllegalArgumentException e) {

            ctx.addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            Messages.get(
                                    "message.personalInfo.saveFailed",
                                    e.getMessage()
                            ),
                            null
                    )
            );
        }

        return null;
    }

    public LocalDate getToday() {
        return LocalDate.now();
    }

    public boolean isDateOfBirthRequired() {
        return personDTO != null
                && personDTO.getRole() != null
                && personDTO.getRole().contains(Role.EMPLOYEE);
    }

    public String getCurrentPassword() {
        return this.currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return this.newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return this.confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void changePassword() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        try {
            if (newPassword == null
                    || !newPassword.equals(confirmPassword)) {

                throw new IllegalArgumentException(
                        Messages.get("message.password.mismatch")
                );
            }

            User user = getUser();

            if (user == null) {
                throw new IllegalStateException(
                        Messages.get("message.auth.noUser")
                );
            }

            personLogic.changePassword(
                    user.getId(),
                    currentPassword,
                    newPassword
            );

            currentPassword = null;
            newPassword = null;
            confirmPassword = null;

            ctx.addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            Messages.get("message.password.changed.summary"),
                            Messages.get("message.password.changed.detail")
                    )
            );

        } catch (EJBException | IllegalArgumentException | IllegalStateException e) {

            ctx.addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            Messages.get("message.password.changeFailed"),
                            e.getMessage()
                    )
            );
        }
    }
}
