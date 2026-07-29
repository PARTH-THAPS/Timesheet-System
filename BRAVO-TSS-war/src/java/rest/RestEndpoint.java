/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

/**
 *
 * @author parth
 */


//Test API 
@Stateless
@LocalBean
@Path("v1/person")
public class RestEndpoint {
    @EJB
    private PersonLogic personLogic;

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Person createPerson(PersonDTO  request) {

        return personLogic.CreatePerson(
                request.getFirstName(),
                request.getLastName(),
                request.getEmailAddress(),
                request.isConsent(),
                request.getPassword(),
                Role.valueOf(request.getRole())
        );
    }
}