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
        PersonDTO person = personLogic.createPerson(
                request.getFirstName(),
                request.getLastName(),
                request.getEmailAddress(),
                request.isConsent(),
                request.getPassword(),
                request.getRole()
        );
        return person;
    }

    
}