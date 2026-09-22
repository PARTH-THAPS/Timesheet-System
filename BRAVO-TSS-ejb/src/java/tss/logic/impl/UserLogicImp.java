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