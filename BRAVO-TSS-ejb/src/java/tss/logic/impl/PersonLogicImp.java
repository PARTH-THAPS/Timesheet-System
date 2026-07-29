/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import tss.dao.PersonDao;
import tss.logic.PersonLogic;
import tss.entity.Person;
import tss.entity.Role;
import util.PasswordHash;

/**
 *
 * @author parth
 */
@Stateless
public class PersonLogicImp implements PersonLogic {
    
    @EJB
    private PersonDao personDao;
    

    @Override
    public Person CreatePerson(String firstName, String lastName, String emailAddress, boolean consent, String password, Role role) {
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmailAddress(emailAddress);
        person.setPassword(PasswordHash.hashPassword(password));
        person.setConsent(consent);
        person.setRole(role);

        personDao.createPerson(person);

        return person;
    }
}
