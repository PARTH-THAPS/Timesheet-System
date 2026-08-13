package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.security.Principal;
import tss.dto.User;
import java.util.logging.Level;
import java.util.logging.Logger;
import tss.logic.UserLogic;

@Named
@SessionScoped
public class loginBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(loginBean.class.getName());

    private User currentUser;
    
    @EJB
    private UserLogic u;

    public boolean isLoggedIn() {
        return FacesContext.getCurrentInstance()
            .getExternalContext()
            .getUserPrincipal() != null;
    }

    private Principal oldPrincipal = null;

    public User getUser() {
        Principal p = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal();

        if (p == null) {
            currentUser = null;
        } else {
            if (oldPrincipal == null || !p.getName().equals(oldPrincipal.getName())) {
                currentUser = u.getCurrentUser();
                LOG.log(Level.INFO, "Contacts: LOGIN user {0}", p.getName());
            }
        }
        oldPrincipal = p;
        return currentUser;
    }

    public void invalidateSession() {
        LOG.log(Level.INFO, "invalidateSession()");
        Principal p = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal();
        if (p != null) {
            LOG.log(Level.INFO, "Contacts: LOGOUT user {0}", p.getName());
        }
        currentUser = null;
        oldPrincipal = null;
        FacesContext.getCurrentInstance()
                .getExternalContext()
                .invalidateSession();
    }

    public void logout() {
        invalidateSession();
        FacesContext.getCurrentInstance().responseComplete();

    }
}
