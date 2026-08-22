package tss.logic;

import jakarta.ejb.Remote;
import java.time.LocalDate;
import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.entity.ContractStatus;
import tss.entity.TimesheetFrequency;
import tss.entity.Person;
import tss.entity.Contract;

@Remote
public interface ContractLogic {

    ContractDTO createContract(String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency timesheetFrequency, double hoursPerWeek, double hoursDue, int workingDaysPerWeek, int vacationDaysPerYear, PersonDTO person, String state);

    ContractDTO updateContract(Contract updatedContract);
    
    void deleteContract(Long contractId);
    
    ContractDTO searchContract(Long contractId);
    
    ContractDTO updateContractStatus(Long contrcatId,ContractStatus contractStatus);
    
    void CheckForArchivedTimesheet (Contract contract);
    
//    Contract PrintContract();

}
