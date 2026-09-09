package tss.dto;

import java.util.Set;
import tss.entity.Role;


public class User extends AbstractDTO {
     private static final long serialVersionUID = 282980014285470000L;

    private String email;
    private String firstName;

    private String lastName;
    private Set<Role> roles; 

 

    public User(String uuid, int jpaVersion, String email, String firstName, String lastName, Set<Role> roles) {
        super(uuid, jpaVersion);
        this.email=email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
