package tss.logic;

import jakarta.ejb.Remote;
import tss.entity.Person;
import tss.dto.User;

/**
 * Remote interface defining operations related to the logged-in user.
 */
@Remote
public interface UserLogic {
    /** Constant representing the User role. */
    public static final String USER_ROLE="USER" ;
    /** Constant representing the Admin role. */
    public static final String ADMIN_ROLE="ADMIN" ;
    /** Constant representing the Assistant role. */
    public static final String ASSISTANT_ROLE="ASSISTANT";
    /** Constant representing the Employee role. */
    public static final String EMPLOYEE_ROLE="EMPLOYEE";
    /** Constant representing the Secretary role. */
    public static final String SECREATRY_ROLE="SECRETARY";
    /** Constant representing the Guest role. */
    public static final String GUEST_ROLE="GUEST";
    /** Constant representing the Supervisor role. */
    public static final String SUPERVISOR_ROLE="SUPERVISOR";

    /**
     * Retrieves the currently authenticated user.
     *
     * @return The User object containing current user information.
     */
     public User getCurrentUser();
}
