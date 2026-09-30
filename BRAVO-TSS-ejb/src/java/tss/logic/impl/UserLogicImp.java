package tss.logic.impl;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBContext;
import jakarta.ejb.Stateless;

import java.security.Principal;

import tss.dao.PersonDao;
import tss.dto.User;
import tss.entity.Person;
import tss.logic.UserLogic;

/**
 * Stateless session bean implementation of the {@link UserLogic} interface.
 * Handles user role declarations and retrieval of the currently authenticated user.
 */
@Stateless
@DeclareRoles({
   UserLogic.USER_ROLE,
   UserLogic.ADMIN_ROLE,
   UserLogic.ASSISTANT_ROLE,
   UserLogic.EMPLOYEE_ROLE,
   UserLogic.GUEST_ROLE,
   UserLogic.SECREATRY_ROLE,
   UserLogic.SUPERVISOR_ROLE
})
public class UserLogicImp implements UserLogic {

   @EJB
   private PersonDao dao;

   @Resource
   private EJBContext ejbContext;

    /**
     * Retrieves the currently authenticated user from the EJB context.
     *
     * @return A {@link User} DTO representing the currently logged-in user.
     */
   @Override
   @RolesAllowed({
       USER_ROLE,
       ADMIN_ROLE,
       ASSISTANT_ROLE,
       EMPLOYEE_ROLE,
       GUEST_ROLE,
       SECREATRY_ROLE,
       SUPERVISOR_ROLE
   })
   public User getCurrentUser() {

       Principal principal = ejbContext.getCallerPrincipal();

       Person person = dao.getPerson(principal.getName());

       return createDTO(person);
   }

    /**
     * Converts a {@link Person} entity into a {@link User} DTO.
     *
     * @param p The Person entity to convert.
     * @return The User DTO, or null if the input person is null.
     */
   public User createDTO(Person p) {

       if (p == null) {
           return null;
       }

       return new User(
           p.getId(),
           p.getUuid(),
           p.getJpaVersion(),
           p.getEmailAddress(),
           p.getFirstName(),
           p.getLastName(),
           p.getRole()
       );
   }
}