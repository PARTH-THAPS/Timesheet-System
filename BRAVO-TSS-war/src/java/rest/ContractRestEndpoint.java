
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.logging.Level;
import tss.dao.PersonDao;
import tss.logic.ContractLogic;
import tss.entity.Contract;
import tss.entity.Person;
import java.util.logging.Logger;
import tss.dto.ContractDTO;

/**
 * * * @author Tia Benny
 */
@Stateless
@LocalBean
@Path("v1/contract")
public class ContractRestEndpoint {

    @EJB
    ContractLogic contractlogic;
    @EJB
    PersonDao personDao;
    private static final Logger LOGGER = Logger.getLogger(ContractRestEndpoint.class.getName());

    @POST
    @Path("/createContract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    
    public Contract createContract(ContractDTO contractDto) {
        Person person = personDao.findPerson(contractDto.getPersonId());    
    
        return contractlogic.createContract(
                contractDto.getName(), 
                contractDto.getStartDate(), 
                contractDto.getEndDate(), 
                contractDto.getFrequency(), 
                contractDto.getTerminationDate(), 
                contractDto.getHoursPerWeek(), 
                contractDto.getVacationHours(), 
                contractDto.getHoursDue(), 
                contractDto.getWorkingDaysPerWeek(), 
                contractDto.getVacationDaysPerYear(), 
                person
        );
    }
}
