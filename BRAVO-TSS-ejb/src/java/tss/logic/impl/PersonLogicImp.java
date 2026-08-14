package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;
import tss.dao.PersonDao;
import tss.entity.Person;
import tss.entity.Role;
import tss.logic.PersonLogic;
import tss.util.PasswordHash;

@Stateless
public class PersonLogicImp implements PersonLogic {

    @EJB
    private PersonDao personDao;

    @Override
    public Person createPerson(String firstName, String lastName, String emailAddress, boolean consent, String password, Role role) {
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

    @Override
    public Person findPerson(Long id) {

        Person person = personDao.findPersonById(id);

        if (person == null) {
            throw new IllegalArgumentException(
                    "No Person found with id: " + id
            );
        }

        return person;
    }

    @Override
    public Person updatePerson(Person person) {
        return personDao.updatePerson(person);
    }

    @Override
    public void deletePerson(Person person) {
        personDao.deletePerson(person);
    }

    @Override
    public List<Person> findAllPersons() {
        return personDao.findAllPersons();
    }
}
