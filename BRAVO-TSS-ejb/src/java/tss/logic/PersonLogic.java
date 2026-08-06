/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tss.logic;

import jakarta.ejb.Remote;
import tss.entity.Person;
import tss.entity.Role;

@Remote
public interface PersonLogic {
 
     Person createPerson(String firstName,String lastName, String emailAddress,boolean consent,String password,Role role);
     Person findPerson(Long id);


}
