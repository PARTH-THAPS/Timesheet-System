/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tss.logic.imp;

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
import tss.dao.PersonDao;
import tss.entity.Person;
import tss.logic.UserLogic;
import tss.dto.User;



@Stateless
@DeclareRoles({UserLogic.USER_ROLE,UserLogic.ADMIN_ROLE})
public class UserLogicImp implements UserLogic {
    
    private static final Logger LOG= Logger.getLogger(UserLogicImp.class.getName());
    
    @EJB
    private PersonDao dao; 
    
    private Person caller;
    
    @Resource
    private EJBContext ejbContext;
    
    @AroundInvoke
    private Object getCaller(InvocationContext ctx)throws Exception
    {
        Principal p =ejbContext.getCallerPrincipal();
        if(p!=null)
        {
        caller= dao.getPerson(p.getName());
        }
         return ctx.proceed();
    }
    
    @Override
    @RolesAllowed(USER_ROLE)
    public User getCurrentUser() {
        return createDTO(caller,
                ejbContext.isCallerInRole(USER_ROLE),
                ejbContext.isCallerInRole(ADMIN_ROLE));
    }
    
    
    
    public User createDTO(Person p,boolean isInUserRole, boolean isInAdminRole)
    {
    if (p==null){
    return null;
    }
    return new User(p.getUuid(), p.getJpaVersion(),p.getEmailAddress(),p.getFirstName(), p.getLastName(), isInUserRole, isInAdminRole);
    }
   
    }
            

            
            
    
//    Need to write code for 
    
//    @RolesAllowed
    
    

