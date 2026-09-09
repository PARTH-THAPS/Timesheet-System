package tss.logic;

import jakarta.ejb.Remote;
import java.time.LocalDate;
import java.util.List;
import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.entity.ContractStatus;
import tss.entity.TimesheetFrequency;
import tss.entity.Contract;
import tss.entity.FederalState;

@Remote
public interface ContractLogic {

    ContractDTO createContract(String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency timesheetFrequency, double hoursPerWeek, double hoursDue, int workingDaysPerWeek, int vacationDaysPerYear, PersonDTO person, FederalState state);

    ContractDTO updateContract(ContractDTO updatedContract);

    void deleteContract(Long contractId);

    ContractDTO searchContract(Long contractId);

    List<ContractDTO> findAllContracts();

    ContractDTO updateContractStatus(Long contrcatId, ContractStatus contractStatus);

    void CheckForArchivedTimesheet(Contract contract);

    void addSecretary(Long contractId, List<Long> personIds);

    void removeSecretary(Long contractId, List<Long> personIds);

    void addAssistant(Long contractId, List<Long> personIds);

    void removeAssistant(Long contractId, List<Long> personIds);

    void addSupervisor(Long contractId, Long personId);

    void removeSupervisor(Long contractId);

//    Contract PrintContract();
}
