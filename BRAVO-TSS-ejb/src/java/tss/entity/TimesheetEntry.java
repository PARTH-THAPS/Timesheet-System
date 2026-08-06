package tss.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.time.Duration;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class TimesheetEntry extends AbstractEntity {
    
    @Enumerated(EnumType.STRING)
    private ReportType type;
    private String description;       
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
        
    }
    
    @Transient
    public double getHours() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime).toMinutes() / 60.0;
        }
        return 0.0;
    }

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        if (type == null) {
            throw new IllegalArgumentException("Entry type must not be null");
        }
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("startTime must not be null");
        }
        if (endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("startTime cannot be after endTime");
        }
        this.startTime = startTime;   
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        if (endTime == null) {
            throw new IllegalArgumentException("endTime must not be null");
        }
        if (startTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("endTime cannot be before startTime");
        }
        this.endTime = endTime;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        if (entryDate == null) {
            throw new IllegalArgumentException("entryDate must not be null");
        }
        this.entryDate = entryDate;
    }

    public Timesheet getTimesheet() {
        return timesheet;
    }

    public void setTimesheet(Timesheet timesheet) {
        if (timesheet == null) {
            throw new IllegalArgumentException("TimesheetEntry must belong to a Timesheet");
        }
        this.timesheet = timesheet;
    }
    
}
