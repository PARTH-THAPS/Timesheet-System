package tss.logic.impl;

import jakarta.annotation.Resource;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBContext;
import jakarta.ejb.Stateless;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import tss.dao.PersonDao;
import tss.entity.Person;
import tss.logic.UserLogic;
import tss.dto.User;
import tss.entity.Role;

@Stateless
@DeclareRoles({
    UserLogic.USER_ROLE,
    UserLogic.ADMIN_ROLE,
    UserLogic.ASSISTANT_ROLE,
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
        GUEST_ROLE,
        SECREATRY_ROLE,
        SUPERVISOR_ROLE
    })
    public User getCurrentUser() {

        Principal principal = ejbContext.getCallerPrincipal();

        Person person = dao.getPerson(principal.getName());
//        Set<Role> role = dao.getRole(principal.getName());
        
        return createDTO(
            person
        );
    }

    public User createDTO(
            Person p) {

        if (p == null) {
            return null;
        }

        return new User(
            p.getUuid(),
            p.getJpaVersion(),
            p.getEmailAddress(),
            p.getFirstName(),
            p.getLastName(),
            p.getRole()
        );
    }
}
