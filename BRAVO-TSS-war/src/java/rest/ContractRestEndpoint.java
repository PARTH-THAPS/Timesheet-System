
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
        
       Person person= personLogic.findPerson(contractDto.getPersonId());
        Contract contrcat= contractlogic.createContract(
                contractDto.getName(), 
                contractDto.getStartDate(), 
                contractDto.getEndDate(), 
                contractDto.getFrequency(), 
                contractDto.getHoursPerWeek(), 
                contractDto.getHoursDue(), 
                contractDto.getWorkingDaysPerWeek(), 
                contractDto.getVacationDaysPerYear(), 
                person
        );
        
        return  toDTO(contrcat);
    }
    
   private ContractDTO toDTO(Contract contract) {
    ContractDTO dto = new ContractDTO();
    dto.setUuid(contract.getUuid());                    
    dto.setJpaVersion(contract.getJpaVersion());           //
    dto.setName(contract.getName());
    dto.setStartDate(contract.getStartDate());
    dto.setEndDate(contract.getEndDate());
    dto.setFrequency(contract.getFrequency());          // direct assign, same enum type
    dto.setTerminationDate(contract.getTerminationDate());
    dto.setHoursPerWeek(contract.getHoursPerWeek());
    dto.setVacationHours(contract.getVacationHours());
    dto.setHoursDue(contract.getHoursDue());
    dto.setWorkingDaysPerWeek(contract.getWorkingDaysPerWeek());
    dto.setVacationDaysPerYear(contract.getVacationDaysPerYear());
    dto.setPersonId(contract.getPerson().getId());
    return dto;
}
    
    
}
