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

/**
 *
 * @author parth
 */
@Remote
public interface ContractLogic {

    Contract createContract(String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency timesheetFrequency, LocalDate terminationDate, double hoursPerWeek, double vacationHours, double hoursDue, int workingDaysPerWeek, int vacationDaysPerYear, Person person);

    Contract updateContract(long contractId, ContractStatus contractStatus, String name, LocalDate startDate, LocalDate endDate, TimesheetFrequency timesheetFrequency, LocalDate terminationDate, double hoursPerWeek, double vacationHours, double hoursDue, int workingDaysPerWeek, int vacationDaysPerYear, Person person);
}
