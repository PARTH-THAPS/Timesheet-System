package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;
import java.security.Principal;

import tss.dto.PersonDTO;
import tss.dto.User;
import tss.logic.PersonLogic;
import tss.logic.UserLogic;

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

   private Principal oldPrincipal = null;

   public User getUser() {

       Principal p = FacesContext.getCurrentInstance()
               .getExternalContext()
               .getUserPrincipal();

       if (p == null) {

           currentUser = null;
           personDTO = null;

       } else {

           if (oldPrincipal == null
                   || !p.getName().equals(oldPrincipal.getName())) {

               currentUser = userLogic.getCurrentUser();

               personDTO = personLogic.findPerson(
                       currentUser.getId()
               );
           }
       }

       oldPrincipal = p;

       return currentUser;
   }

   public PersonDTO getPersonDTO() {
       return personDTO;
   }

   public String save() {

       personLogic.updatePerson(personDTO);

       FacesContext.getCurrentInstance()
               .addMessage(
                       null,
                       new FacesMessage("Saved successfully!")
               );

       return null;
   }
}