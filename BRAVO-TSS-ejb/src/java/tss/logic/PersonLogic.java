package tss.logic;

import jakarta.ejb.Remote;
import java.util.List;
import java.util.Set;

import tss.dto.PersonDTO;
import tss.entity.Role;

@Remote
public interface PersonLogic {
     PersonDTO createPerson(String firstName, String lastName, String emailAddress, boolean consent, String password, Set<Role> role);
     PersonDTO findPerson(Long id);
     void deletePerson(PersonDTO person);
     PersonDTO updatePerson(PersonDTO person);
     List<PersonDTO> findAllPersons();
}
