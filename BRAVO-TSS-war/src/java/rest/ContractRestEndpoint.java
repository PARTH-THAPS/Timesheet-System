package rest;

import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tss.logic.ContractLogic;
import tss.entity.Contract;
import tss.entity.Person;
import java.util.logging.Logger;
import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.entity.ContractStatus;
import tss.logic.PersonLogic;

@Stateless
@LocalBean
@Path("v1/contract")
public class ContractRestEndpoint {

    @EJB
    ContractLogic contractlogic;
    @EJB
    PersonLogic personLogic;

    private static final Logger LOGGER = Logger.getLogger(ContractRestEndpoint.class.getName());

    @POST
    @Path("/createContract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    public ContractDTO createContract(ContractDTO contractDto) {

        PersonDTO persondto = personLogic.findPerson(contractDto.getPersonId());
        return contractlogic.createContract(
                contractDto.getName(),
                contractDto.getStartDate(),
                contractDto.getEndDate(),
                contractDto.getFrequency(),
                contractDto.getHoursPerWeek(),
                contractDto.getHoursDue(),
                contractDto.getWorkingDaysPerWeek(),
                contractDto.getVacationDaysPerYear(),
                persondto,
                contractDto.getState()
        );

    }

    @POST
    @Path("/{contractId}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ContractDTO updateContractStatus(@PathParam("contractId") Long contractId) {
        return contractlogic.updateContractStatus(contractId, ContractStatus.STARTED);

    }

}
