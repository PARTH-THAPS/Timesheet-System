package tss.logic;

import jakarta.ejb.Remote;
import tss.entity.Person;
import tss.dto.User;

@Remote
public interface UserLogic {
    public static final String USER_ROLE="USER" ;
    public static final String ADMIN_ROLE="ADMIN" ;
    public static final String ASSISTANT_ROLE="ASSISTANT";
    public static final String SECREATRY_ROLE="SECRETARY";
    public static final String GUEST_ROLE="GUEST";
    public static final String SUPERVISOR_ROLE="SUPERVISOR";
     
     public User getCurrentUser();
}
