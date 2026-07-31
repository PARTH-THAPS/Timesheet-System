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
    public Contract updateContract(Long contractId, ContractStatus contractStatus, String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency timesheetFrequency, LocalDate terminationDate, double hoursPerWeek, double vacationHours, double hoursDue, int workingDaysPerWeek, int vacationDaysPerYear, Person person) {
        Contract contract = contractsDao.findContract(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("No contract found with id: " + contractId);
        }
        
         if (contract.getStatus() != ContractStatus.PREPARED) {
            throw new IllegalStateException(
                "Contract can only be updated when status is PREPARED"
            );
        }

        contract.setStatus(contractStatus);
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

}
