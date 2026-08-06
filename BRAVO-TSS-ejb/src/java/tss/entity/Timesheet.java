package tss.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Timesheet extends AbstractEntity {
    
    @Enumerated(EnumType.STRING)
    private TimesheetStatus status;
    private LocalDate startDate;
    private LocalDate endDate;              
    private LocalDate signedByEmployee;
    private LocalDate signedBySupervisor;
    
    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimesheetEntry> entries;
    
    @ManyToOne
    private Contract contract;
    
    public Timesheet() {
        this.entries = new ArrayList<>();
    }
    
    public Timesheet(LocalDate startDate, LocalDate endDate, Contract contract){
            this.status = TimesheetStatus.IN_PROGRESS;
            this.startDate = startDate;
            this.endDate = endDate;
            this.contract = contract;
            this.signedByEmployee = null;
            this.signedBySupervisor = null;
            this.entries = new ArrayList<>();
            
    }

    @Transient
    public double getHoursDue() {
        return calculateHoursDue();
    }
    
    private double calculateHoursDue() {
        if (contract == null) {
            throw new IllegalStateException("Timesheet must be linked to a contract");
        }
        int workingDaysPerWeek = contract.getWorkingDaysPerWeek();
        if (workingDaysPerWeek <= 0) {
            throw new IllegalStateException("Contract workingDaysPerWeek must be greater than zero");
        }

        int workingDaysInPeriod = countWorkingDays(startDate, endDate);
        int publicHolidaysInPeriod = 0; // TODO: Compute Public Holiday (CN4d/CN4e)

        double hoursPerWeek = contract.getHoursPerWeek();
        return (workingDaysInPeriod - publicHolidaysInPeriod) * hoursPerWeek / workingDaysPerWeek;
    }

    private int countWorkingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalStateException("startDate and endDate must be set before calculating hours due");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }

        int count = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (isWorkingDay(current)) count++;
            current = current.plusDays(1);
        }
        return count;
    }

    private boolean isWorkingDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    public void validateModifiable() {
        if (status != TimesheetStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                "Cannot modify entries: Timesheet status is " + status + ", must be IN_PROGRESS");
        }
        if (contract == null) {
            throw new IllegalStateException("Cannot modify entries: Timesheet has no contract");
        }
        if (contract.getStatus() != ContractStatus.STARTED) {
            throw new IllegalStateException(
                "Cannot modify entries: Contract status is " + contract.getStatus() + ", must be STARTED");
        }
    }
    
    public TimesheetStatus getStatus() {
        return status;
    }

    public void setStatus(TimesheetStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getSignedByEmployee() {
        return signedByEmployee;
    }

    public void setSignedByEmployee(LocalDate signedByEmployee) {
        this.signedByEmployee = signedByEmployee;
    }

    public LocalDate getSignedBySupervisor() {
        return signedBySupervisor;
    }

    public void setSignedBySupervisor(LocalDate signedBySupervisor) {
        this.signedBySupervisor = signedBySupervisor;
    }
    
    public Contract getContract() {
        return contract;
    }

    public List<TimesheetEntry> getEntries() {
        return entries;
    }
    
    public List<TimesheetEntry> addEntry(TimesheetEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null");
        }
        validateModifiable();
        entry.setTimesheet(this);
        this.entries.add(entry);
        return entries;
    }

    public List<TimesheetEntry> removeEntry(TimesheetEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null");
        }
        validateModifiable();
        boolean removed = this.entries.remove(entry);
        if (!removed) {
            throw new IllegalArgumentException("entry not found on this timesheet");
        }
        entry.setTimesheet(null);
        return entries;
    }
    


}
