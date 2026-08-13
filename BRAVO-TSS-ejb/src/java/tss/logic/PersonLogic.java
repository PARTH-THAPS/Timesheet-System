package tss.logic;

import jakarta.ejb.Remote;
import tss.entity.Person;
import tss.entity.Role;

@Remote
public interface PersonLogic {
     Person createPerson(String firstName,String lastName, String emailAddress,boolean consent,String password,Role role);
     Person findPerson(Long id);
     void deletePerson(Person person);
     Person updatePerson(Person person);
}
