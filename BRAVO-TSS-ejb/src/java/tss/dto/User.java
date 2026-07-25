/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tss.dto;

public class User extends AbstractDTO {
     private static final long serialVersionUID = 282980014285470000L;

    private String email;
    private String firstName;

    private String lastName;

    private final boolean isInUserRole; // read only

    private final boolean isInAdminRole; // read only

    public User(String uuid, int jpaVersion, String email, String firstName, String lastName, boolean isInUserRole, boolean isInAdminRole) {
        super(uuid, jpaVersion);
        this.email=email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isInUserRole = isInUserRole;
        this.isInAdminRole = isInAdminRole;
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

    public boolean isIsInUserRole() {
        return isInUserRole;
    }

    public boolean isIsAdminRole() {
        return isInAdminRole;
    }
}
