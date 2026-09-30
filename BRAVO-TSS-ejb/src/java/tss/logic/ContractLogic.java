package tss.logic;

import jakarta.ejb.Remote;
import java.time.LocalDate;
import java.util.List;
import tss.dto.ContractDTO;
import tss.dto.ContractStatisticsDTO;
import tss.dto.PersonDTO;
import tss.entity.ContractStatus;
import tss.entity.TimesheetFrequency;
import tss.entity.Contract;
import tss.entity.FederalState;

/**
 * Remote interface managing the business logic for employment contracts.
 */
@Remote
public interface ContractLogic {

    /**
     * Creates a new employment contract.
     * @param name The name or title of the contract.
     * @param startDate The start date of the contract.
     * @param endDate The end date of the contract.
     * @param timesheetFrequency The frequency at which timesheets are generated.
     * @param hoursPerWeek The required working hours per week.
     * @param hoursDue The total number of hours due.
     * @param workingDaysPerWeek The number of working days in a week.
     * @param vacationDaysPerYear The annual vacation days allowed.
     * @param person The person assigned to the contract.
     * @param state The federal state governing the contract.
     * @param archiveDuration The retention duration for archiving.
     * @return The newly created contract.
     */
    ContractDTO createContract(String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency timesheetFrequency, double hoursPerWeek, double hoursDue, int workingDaysPerWeek, int vacationDaysPerYear, PersonDTO person, FederalState state, int archiveDuration);

    /**
     * Updates an existing contract.
     * @param updatedContract The updated contract data.
     * @return The updated contract.
     */
    ContractDTO updateContract(ContractDTO updatedContract);

    /**
     * Permanently deletes a contract by its ID.
     * @param contractId The ID of the contract.
     */
    void deleteContract(Long contractId);

    /**
     * Searches for and returns a contract by its ID.
     * @param contractId The ID of the contract.
     * @return The requested contract.
     */
    ContractDTO searchContract(Long contractId);

    /**
     * Retrieves working time statistics for a specific contract.
     * @param contractId The ID of the contract.
     * @return The contract statistics.
     */
    ContractStatisticsDTO getContractStatistics(Long contractId);

    /**
     * Retrieves a list of all active or visible contracts.
     * @return A list of all contracts.
     */
    List<ContractDTO> findAllContracts();

    /**
     * Retrieves all archived contracts associated with a specific supervisor.
     * @param id The ID of the supervisor.
     * @return A list of archived contracts for the supervisor.
     */
    List<ContractDTO> findAllArchivedContractsForSupervisor(long id);

    /**
     * Retrieves all archived contracts in the system.
     * @return A list of all archived contracts.
     */
    List<ContractDTO> findAllArchivedContracts();

    /**
     * Checks if the given contract is eligible or has timesheets ready for archiving.
     * @param contract The contract entity to check.
     */
    void CheckForArchivedTimesheet(Contract contract);

    /**
     * Assigns secretaries to a contract.
     * @param contractId The ID of the contract.
     * @param personIds A list of person IDs to be added as secretaries.
     */
    void addSecretary(Long contractId, List<Long> personIds);

    /**
     * Removes secretaries from a contract.
     * @param contractId The ID of the contract.
     * @param personIds A list of person IDs to remove from the secretary role.
     */
    void removeSecretary(Long contractId, List<Long> personIds);

    /**
     * Assigns assistants to a contract.
     * @param contractId The ID of the contract.
     * @param personIds A list of person IDs to be added as assistants.
     */
    void addAssistant(Long contractId, List<Long> personIds);

    /**
     * Removes assistants from a contract.
     * @param contractId The ID of the contract.
     * @param personIds A list of person IDs to remove from the assistant role.
     */
    void removeAssistant(Long contractId, List<Long> personIds);

    /**
     * Assigns a supervisor to a contract.
     * @param contractId The ID of the contract.
     * @param personId The ID of the supervisor.
     */
    void addSupervisor(Long contractId, Long personId);

    /**
     * Removes the assigned supervisor from a contract.
     * @param contractId The ID of the contract.
     */
    void removeSupervisor(Long contractId);

    /**
     * Checks if there are any unresolved, in-progress timesheets for a contract.
     * @param contractId The ID of the contract.
     * @return True if there are unresolved in-progress timesheets, false otherwise.
     */
    public boolean hasUnresolvedInProgressTimesheets(Long contractId);

    /**
     * Checks if a contract has timesheets waiting for a supervisor's signature.
     * @param contractId The ID of the contract.
     * @return True if there are timesheets pending signature, false otherwise.
     */
    boolean hasTimesheetsPendingSupervisorSignature(Long contractId);

    /**
     * Checks if a contract has any empty in-progress timesheets.
     * @param contractId The ID of the contract.
     * @return True if empty in-progress timesheets exist, false otherwise.
     */
    boolean hasEmptyInProgressTimesheets(Long contractId);

    /**
     * Updates the status of a contract.
     * @param contractId The ID of the contract.
     * @param contractStatus The new contract status.
     * @return The updated contract.
     */
    ContractDTO updateContractStatus(Long contractId, ContractStatus contractStatus);

    /**
     * Terminates a contract, optionally enforcing a confirmation check.
     * @param contractId The ID of the contract.
     * @param confirmed Flag indicating if the termination is confirmed.
     * @return The terminated contract.
     */
    ContractDTO terminateContract(Long contractId, boolean confirmed);

    /**
     * Counts the total number of contracts associated with a specific employee.
     * @param personId The ID of the employee.
     * @return The number of contracts.
     */
    long countContractsByEmployee(Long personId);

}
