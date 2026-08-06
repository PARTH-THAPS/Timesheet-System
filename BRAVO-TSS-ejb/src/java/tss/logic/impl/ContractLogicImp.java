package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDate;
import tss.dao.ContractsDao;
import tss.entity.ContractStatus;
import tss.entity.TimesheetFrequency;
import tss.logic.ContractLogic;
import tss.entity.Contract;
import tss.entity.Person;


@Stateless
public class ContractLogicImp implements ContractLogic {

    @EJB
    private ContractsDao contractsDao;

    @Override
    public Contract createContract(String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency timesheetFrequency, LocalDate terminationDate, double hoursPerWeek, double vacationHours, double hoursDue, int workingDaysPerWeek, int vacationDaysPerYear, Person person) {
        Contract contract = new Contract();
        contract.setStatus(ContractStatus.PREPARED);
        contract.setName(name);
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setFrequency(timesheetFrequency);
        contract.setTerminationDate(terminationDate);
        contract.setHoursPerWeek(hoursPerWeek);
        contract.setVacationHours(vacationHours);
        contract.setHoursDue(hoursDue);
        contract.setWorkingDaysPerWeek(workingDaysPerWeek);
        contract.setVacationDaysPerYear(vacationDaysPerYear);
        contract.setPerson(person);
        contractsDao.createContract(contract);
        return contract;
    }

    @Override
    public Contract updateContract(Contract updatedContract) {
         Contract contract = contractsDao.findContract(updatedContract.getId());

    if (contract == null) {
        throw new IllegalArgumentException(
            "No contract found with id: " + updatedContract.getId());
    }

    if (contract.getStatus() != ContractStatus.PREPARED) {
        throw new IllegalStateException(
            "Contract can only be updated when status is PREPARED");
    }

    contract.setStartDate(updatedContract.getStartDate());
    contract.setEndDate(updatedContract.getEndDate());
    contract.setFrequency(updatedContract.getFrequency());
    contract.setHoursPerWeek(updatedContract.getHoursPerWeek());
    contract.setVacationHours(updatedContract.getVacationHours());
    contract.setHoursDue(updatedContract.getHoursDue());
    contract.setWorkingDaysPerWeek(updatedContract.getWorkingDaysPerWeek());
    contract.setVacationDaysPerYear(updatedContract.getVacationDaysPerYear());

    contractsDao.UpdateContract(contract);

    return contract;
    }

    
    
    @Override
    public void deleteContract(Long contractId) {

        Contract contract = contractsDao.findContract(contractId);


        if(contract == null) {

            throw new IllegalArgumentException(
                "No contract found with id: " + contractId
            );
        }


        if(contract.getStatus() != ContractStatus.PREPARED) {

            throw new IllegalStateException(
                "Contract can only be deleted when status is PREPARED"
            );
        }


        contractsDao.deleteContract(contract);
    }
    
    
   @Override
    public Contract searchContract(Long contractId)
    {
    Contract contract = contractsDao.findContract(contractId); 
            
    if(contract == null) {

            throw new IllegalArgumentException(
                "No contract found with id: " + contractId
            );
        }
    
        return contract;
  
    }
    
    
    @Override
    public Contract updateContractStatus(Long contractId,ContractStatus newStatus)
    {
    Contract contract = contractsDao.findContract(contractId);

    if (contract == null) {
        throw new IllegalArgumentException(
            "No contract found with id: " + contractId
        );
    }

    ContractStatus currentStatus = contract.getStatus();

    if (currentStatus == ContractStatus.PREPARED
            && newStatus == ContractStatus.STARTED) {

        contract.setStatus(newStatus);

    } else if (currentStatus == ContractStatus.STARTED 
            && newStatus == ContractStatus.TERMINATED) {

        contract.setStatus(newStatus);

    } 
    else if (currentStatus == ContractStatus.STARTED 
            && newStatus == ContractStatus.ARCHIVED) {

        contract.setStatus(newStatus);

    }
    else if (currentStatus == ContractStatus.TERMINATED 
            && newStatus == ContractStatus.ARCHIVED) {

        contract.setStatus(newStatus);

    }
    
    else {
        throw new IllegalStateException(
            "Invalid status transition: "
            + currentStatus + "From" + newStatus
        );
    }

    contractsDao.UpdateContract(contract);

    return contract;
    }

}
