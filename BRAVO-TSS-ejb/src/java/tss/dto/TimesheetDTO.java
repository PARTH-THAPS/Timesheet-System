package tss.dto;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class TimesheetDTO extends AbstractDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDate signedByEmployee;
    private LocalDate signedBySupervisor;
    private double hoursDue;
    private Long contractId;
    private List<TimesheetEntryDTO> entries;

    public TimesheetDTO() {
        this.entries = Collections.emptyList();
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public double getHoursDue() {
        return hoursDue;
    }

    public void setHoursDue(double hoursDue) {
        this.hoursDue = hoursDue;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public List<TimesheetEntryDTO> getEntries() {
        return entries;
    }

    public void setEntries(List<TimesheetEntryDTO> entries) {
        if (entries != null) { 
            this.entries = entries; 
        } 
        else { 
            this.entries = Collections.emptyList(); }
    }
}
    

