package tss.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Timesheet extends AbstractEntity {
    
    @Enumerated(EnumType.STRING)
    private TimesheetStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private double hoursDue;                // Calculated
    private LocalDate signedByEmployee;
    private LocalDate signedBySupervisor;
    
    @OneToMany(mappedBy = "timesheet")
    private List<TimesheetEntry> entries;
    
    @ManyToOne
    private Contract contract;
    
    public Timesheet(LocalDate startDate, LocalDate endDate, Contract contract){
            this.status = TimesheetStatus.IN_PROGRESS;
            this.startDate = startDate;
            this.endDate = endDate;
            this.hoursDue = calculateHoursDue();
            this.signedByEmployee = null;
            this.signedBySupervisor = null;
            this.entries = new ArrayList<>();
            this.contract = contract;
    }
    
    private double calculateHoursDue() {
        //TODO: Implement
        return 10;
    }

    public TimesheetStatus getStatus() {
        return status;
    }

    public void setStatus(TimesheetStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public double getHoursDue() {
        return hoursDue;
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

    public List<TimesheetEntry> getEntries() {
        return entries;
    }

    public List<TimesheetEntry> addEntry(TimesheetEntry entry) {
        this.entries.add(entry);
        return entries;
    }
    
    public List<TimesheetEntry> removeEntry(TimesheetEntry entry) {
        this.entries.remove(entry);
        return entries;
    }
}
