package tss.logic;

import jakarta.ejb.Remote;
import tss.entity.Person;
import tss.dto.User;

@Remote
public interface UserLogic {
    public static final  String USER_ROLE="User" ;
     public static final  String ADMIN_ROLE="Admin" ;
     
     public User getCurrentUser();
}
