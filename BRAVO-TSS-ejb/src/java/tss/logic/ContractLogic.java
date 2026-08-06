/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tss.logic;

import jakarta.ejb.Remote;
import java.time.LocalDate;
import tss.entity.ContractStatus;
import tss.entity.TimesheetFrequency;
import tss.entity.Person;
import tss.entity.Contract;

@Remote
public interface ContractLogic {

    Contract createContract(String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency timesheetFrequency, double hoursPerWeek, double hoursDue, int workingDaysPerWeek, int vacationDaysPerYear, Person person);

    Contract updateContract(Contract updatedContract);
    
    void deleteContract(Long contractId);
    
    Contract searchContract(Long contractId);
    
    Contract updateContractStatus(Long contrcatId,ContractStatus contractStatus);
    
    void CheckForArchivedTimesheet (Contract contract);
    
//    Contract PrintContract();

}
