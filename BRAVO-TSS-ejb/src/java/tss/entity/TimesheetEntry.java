package tss.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
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
    @JoinColumn(name = "TIMESHEET_ID", nullable = false)
    private Timesheet timesheet;
    
    public TimesheetEntry() {
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
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
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
