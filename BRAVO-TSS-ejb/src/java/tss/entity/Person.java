package tss.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


@NamedQueries({@NamedQuery( name ="getUserByEmail",query="SELECT p from Person p WHERE p.emailAddress=:emailAddress ")})
@Entity
public class Person extends AbstractEntity  implements Serializable  {
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String emailAddress;
    private boolean consent;
    
    
    @Column(nullable=false)
    private String password; 

    @Enumerated(EnumType.STRING)
    private Role role;
   
    public Person() {
     
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
    public String getEmailAddress() {
        return emailAddress;
    }
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    public boolean isConsent() {
        return consent;
    }
    public void setConsent(boolean consent) {
        this.consent = consent;
    }
   
    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    
    public String getPassword(){
    return password;
}

public void setPassword(String password){
    this.password = password;
}
}