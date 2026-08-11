package rest;

import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tss.dto.PersonDTO;
import tss.entity.Person;
import tss.entity.Role;
import tss.logic.PersonLogic;

@Stateless
@LocalBean
@Path("v1/person")
public class PersonRestEndpoint {

    @EJB
    private PersonLogic personLogic;

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PersonDTO createPerson(PersonDTO request) {
        Person person = personLogic.createPerson(
                request.getFirstName(),
                request.getLastName(),
                request.getEmailAddress(),
                request.isConsent(),
                request.getPassword(),
                Role.valueOf(request.getRole())
        );
        return toDTO(person);
    }

    private PersonDTO toDTO(Person person) {
        PersonDTO dto = new PersonDTO();
        dto.setUuid(person.getUuid());
        dto.setJpaVersion(person.getJpaVersion());
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setEmailAddress(person.getEmailAddress());
        dto.setRole(person.getRole().name());
        return dto;
    }
}