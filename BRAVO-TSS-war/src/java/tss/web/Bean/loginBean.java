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
import tss.logic.UserLogic;

@Named
@SessionScoped
public class loginBean implements Serializable {

   private static final long serialVersionUID = 1L;

   private static final Logger LOG =
           Logger.getLogger(loginBean.class.getName());

   private boolean error;
   private User currentUser;

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

       } else {

           if (oldPrincipal == null
                   || !p.getName().equals(oldPrincipal.getName())) {

               currentUser = u.getCurrentUser();

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

           FacesContext ctx =
                   FacesContext.getCurrentInstance();

           String contextPath =
                   ctx.getExternalContext()
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

               FacesContext ctx =
                       FacesContext.getCurrentInstance();

               String contextPath =
                       ctx.getExternalContext()
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
}