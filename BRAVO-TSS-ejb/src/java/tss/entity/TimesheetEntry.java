package tss.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class TimesheetEntry extends AbstractEntity {
    
    @Enumerated(EnumType.STRING)
    private ReportType type;
    private String description;
    private double hours;           // Calculated
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate entryDate;
    
    @ManyToOne
    private Timesheet timesheet;
    
    public TimesheetEntry() {
    }
    
    public TimesheetEntry(ReportType type, String description, LocalTime startTime, LocalTime endTime, LocalDate entryDate, Timesheet timesheet){
        this.type = type;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.entryDate = entryDate;
        this.timesheet = timesheet;
        calculateHours();
    }
    
    private void calculateHours() {
        if (startTime != null && endTime != null) {
            // TODO: Implement
        } else {
            hours = 0;
        }
    }

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getHours() {
        return hours;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
        calculateHours();
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
        calculateHours();
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public Timesheet getTimesheet() {
        return timesheet;
    }

    public void setTimesheet(Timesheet timesheet) {
        this.timesheet = timesheet;
    }
    
    
    
}
