package tss.web.Bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class MockUserBean implements Serializable {

    private String firstName = "Max";
    private String lastName = "Mustermann";
    private String email = "max.mustermann@example.com";
    private String role = "EMPLOYEE";

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasRole(String role) {
        return this.role.equals(role);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    public String[] getRoles() {
    return new String[]{
        "EMPLOYEE",
        "SUPERVISOR",
        "ASSISTANT",
        "SECRETARY",
        "ADMIN"
    };
}
}