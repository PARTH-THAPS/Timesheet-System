package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import tss.dao.PersonDao;
import tss.dto.PersonDTO;
import tss.entity.Person;
import tss.entity.Role;
import tss.logic.PersonLogic;
import tss.util.PasswordHash;

@Stateless
public class PersonLogicImp implements PersonLogic {

    @EJB
    private PersonDao personDao;

    @Override
    public PersonDTO createPerson(String firstName, String lastName, String emailAddress, boolean consent, String password, Set<Role> role) {
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmailAddress(emailAddress);
        person.setPassword(PasswordHash.hashPassword(password));
        person.setConsent(consent);
        person.setRoles(role);

        personDao.createPerson(person);

        return personDto(person);
    }

    @Override
    public PersonDTO findPerson(Long id) {

        Person person = personDao.findPersonById(id);

        if (person == null) {
            throw new IllegalArgumentException(
                    "No Person found with id: " + id
            );
        }

        return personDto(person);
    }

    @Override
    public PersonDTO updatePerson(PersonDTO dto) {
    Person person = personDao.findPersonById(dto.getId());  
    
    
    if (person == null) {
        throw new IllegalArgumentException("No Person found with id: " + dto.getId());
    }
    person.setFirstName(dto.getFirstName());
    person.setLastName(dto.getLastName());
    person.setEmailAddress(dto.getEmailAddress());
    person.setConsent(dto.isConsent());
    person.setRoles(dto.getRole());
    return personDto(personDao.updatePerson(person));
    }

    @Override
    public void deletePerson(PersonDTO dto) {
    Person person = personDao.findPersonById(dto.getId());
    if (person == null) {
        throw new IllegalArgumentException("No Person found with id: " + dto.getId());
    }
    personDao.deletePerson(person);
}

    @Override
    public List<PersonDTO> findAllPersons() {
          List<Person> persons = personDao.findAllPersons();
    return toDtoList(persons);
    }
    
    private List<PersonDTO> toDtoList(List<Person> persons) {
    List<PersonDTO> dtoList = new ArrayList<>();

    for (Person p : persons) {
        dtoList.add(personDto(p));
    }

    return dtoList;
}
    
  private PersonDTO personDto(Person p) {
    PersonDTO dto = new PersonDTO();
    dto.setId(p.getId());
    dto.setUuid(p.getUuid());
    dto.setFirstName(p.getFirstName());
    dto.setLastName(p.getLastName());
    dto.setEmailAddress(p.getEmailAddress());
    dto.setConsent(p.isConsent());
    dto.setRole(p.getRole());
    return dto;
}

}
