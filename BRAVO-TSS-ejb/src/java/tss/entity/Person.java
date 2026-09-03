package tss.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

@NamedQueries({
    @NamedQuery(name = "getUserByEmail", query = "SELECT p from Person p WHERE p.emailAddress=:emailAddress ")})
@Entity
public class Person extends AbstractEntity implements Serializable {

    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String emailAddress;
    private boolean consent;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PERSON_ROLE", joinColumns = @JoinColumn(name = "PERSON_ID"))
    private Set<Role> roles;

    @OneToMany(mappedBy = "employee")
    private Set<Contract> employeeContract;

    @OneToMany(mappedBy = "supervisor")
    private Set<Contract> supervisorContract;

    @ManyToMany(mappedBy = "secretaries")
    private Set<Contract> secretaryContract;

    @ManyToMany(mappedBy = "assistants")
    private Set<Contract> assistantContract;

    public Person() {
        this.roles = new HashSet<>();
        this.employeeContract = new HashSet<>();
        this.supervisorContract = new HashSet<>();
        this.secretaryContract = new HashSet<>();
        this.assistantContract = new HashSet<>();
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

    public Set<Role> getRole() {
        return roles;
    }

    public void setRoles(Role... role) {
        this.roles.addAll(Arrays.asList(role));
    }

    public void setRoles(Set<Role> role) {
        this.roles.addAll(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public String getPassword() {
        return password;
    }

    public Set<Contract> getEmployeeContract() {
        return this.employeeContract;
    }

    public void setEmployeeContract(Set<Contract> contract) {
        this.employeeContract = contract;
    }

    public Set<Contract> getSupervisorContract() {
        return this.supervisorContract;
    }

    public void setSupervisorContract(Set<Contract> contract) {
        this.supervisorContract = contract;
    }

    public Set<Contract> getSecretaryContract() {
        return secretaryContract;
    }

    public void setSecretaryContract(Set<Contract> secretaryContract) {
        this.secretaryContract = secretaryContract;
    }

    public Set<Contract> getAssistantContract() {
        return assistantContract;
    }

    public void setAssistantContract(Set<Contract> assistantContract) {
        this.assistantContract = assistantContract;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
