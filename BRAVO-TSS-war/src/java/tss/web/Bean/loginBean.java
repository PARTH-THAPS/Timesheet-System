package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import tss.dto.User;
import tss.entity.Role;
import tss.logic.UserLogic;
import tss.web.i18n.Messages;

@Named
@SessionScoped
public class loginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG
            = Logger.getLogger(loginBean.class.getName());

    private boolean error;
    private User currentUser;

    private Role activeRole;

    @EJB
    private UserLogic u;

    private Principal oldPrincipal = null;

    public boolean isLoggedIn() {

        return FacesContext.getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal() != null;
    }

    public User getUser() {

        Principal p = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal();

        if (p == null) {

            currentUser = null;
            activeRole = null;

        } else {

            if (oldPrincipal == null
                    || !p.getName().equals(oldPrincipal.getName())) {

                currentUser = u.getCurrentUser();

                initializeActiveRole();

                LOG.log(
                        Level.INFO,
                        "Contacts: LOGIN user {0}",
                        p.getName()
                );
            }
        }

        oldPrincipal = p;

        return currentUser;
    }

    public void invalidateSession() {

        LOG.log(Level.INFO, "invalidateSession()");

        currentUser = null;
        oldPrincipal = null;
        activeRole = null;

        Principal p = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal();

        if (p != null) {

            LOG.log(
                    Level.INFO,
                    "Person: LOGOUT user {0}",
                    p.getName()
            );
        }

        currentUser = null;
        oldPrincipal = null;

        FacesContext.getCurrentInstance()
                .getExternalContext()
                .invalidateSession();
    }

    public void logout() {

        invalidateSession();

        try {

            FacesContext ctx
                    = FacesContext.getCurrentInstance();

            String contextPath
                    = ctx.getExternalContext()
                            .getRequestContextPath();

            ctx.getExternalContext()
                    .redirect(
                            contextPath + "/views/login.xhtml"
                    );

        } catch (IOException e) {

            LOG.log(
                    Level.SEVERE,
                    "Error redirecting after logout",
                    e
            );
        }
    }

    public void redirectIfLoggedIn() {

        if (isLoggedIn()) {

            try {

                FacesContext ctx
                        = FacesContext.getCurrentInstance();

                String contextPath
                        = ctx.getExternalContext()
                                .getRequestContextPath();

                ctx.getExternalContext()
                        .redirect(
                                contextPath
                                + "/views/portal/home.xhtml"
                        );

            } catch (IOException e) {

                LOG.log(
                        Level.SEVERE,
                        "Error redirecting from index",
                        e
                );
            }
        }
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean hasRole(String roleName) {

        User user = getUser();

        if (user == null || user.getRoles() == null) {
            return false;
        }

        return user.getRoles()
                .stream()
                .anyMatch(
                        r -> r.name()
                                .equalsIgnoreCase(roleName)
                );
    }

    public boolean hasOnlyRole(String roleName) {

        User user = getUser();

        if (user == null
                || user.getRoles() == null
                || user.getRoles().isEmpty()) {

            return false;
        }

        return user.getRoles().size() == 1
                && user.getRoles()
                        .stream()
                        .anyMatch(
                                r -> r.name()
                                        .equalsIgnoreCase(roleName)
                        );
    }

    private void initializeActiveRole() {

        if (currentUser == null
                || currentUser.getRoles() == null
                || currentUser.getRoles().isEmpty()) {

            activeRole = null;
            return;
        }

        if (activeRole != null
                && currentUser.getRoles().contains(activeRole)) {
            return;
        }

        activeRole = currentUser.getRoles()
                .stream()
                .sorted()
                .findFirst()
                .orElse(null);
    }

    public Role getActiveRole() {
        return activeRole;
    }

    public void setActiveRole(Role activeRole) {

        if (activeRole == null) {
            return;
        }

        User user = getUser();

        if (user == null
                || user.getRoles() == null
                || !user.getRoles().contains(activeRole)) {

            throw new IllegalArgumentException(
                    Messages.get("message.role.notAssigned")
            );
        }

        this.activeRole = activeRole;
    }

    public boolean isActiveRole(String roleName) {
        return activeRole != null && activeRole.name().equalsIgnoreCase(roleName);
    }

    public String changeActiveRole() {

        User user = getUser();

        if (user == null
                || user.getRoles() == null
                || activeRole == null
                || !user.getRoles().contains(activeRole)) {

            return "/views/portal/home.xhtml?faces-redirect=true";
        }

        return "/views/portal/home.xhtml?faces-redirect=true";
    }

    public void roleChanged() {

        User user = getUser();

        if (user == null
                || user.getRoles() == null
                || activeRole == null
                || !user.getRoles().contains(activeRole)) {

            activeRole = null;
            initializeActiveRole();
        }
    }
}
