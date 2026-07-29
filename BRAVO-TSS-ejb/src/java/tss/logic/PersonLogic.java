/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tss.logic;

import jakarta.ejb.Remote;
import tss.entity.Person;
import tss.entity.Role;

/**
 *
 * @author parth
 */
@Remote
public interface PersonLogic {
 
     Person CreatePerson(String firstName,String lastName, String emailAddress,boolean consent,String password,Role role);
}
