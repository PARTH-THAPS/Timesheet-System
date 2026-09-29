package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import tss.dto.ContractDTO;
import tss.dto.TimesheetDTO;
import tss.dto.TimesheetEntryDTO;
import tss.entity.ReportType;
import tss.entity.TimesheetStatus;
import tss.logic.ContractLogic;
import tss.logic.TimesheetLogic;

@Named
@ViewScoped
public class TimesheetDetailBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TimesheetLogic timesheetLogic;
    
    @EJB
    private ContractLogic contractLogic;

    private Long id;

    private TimesheetDTO timesheet;

    private TimesheetEntryDTO entry = new TimesheetEntryDTO();
    
    private Long editingEntryId;
    
    private Double vacationHoursTotal;
    private Double vacationHoursUsed;

    public void init() {
        if (timesheet != null) {
            return;
        }

        if (id == null) {
            throw new IllegalArgumentException(
                    "No timesheet id provided."
            );
        }

        timesheet = timesheetLogic.getTimesheetById(id);

        if (timesheet == null) {
            throw new IllegalArgumentException(
                    "No timesheet found with id: " + id
            );
        }
        
        loadVacationSummary();
    }

    public void prepareNewEntry() {
        entry = new TimesheetEntryDTO();
        editingEntryId = null;
    }

    public void prepareEditEntry(TimesheetEntryDTO selected) {
        if (selected == null) {
            return;
        }

        editingEntryId = selected.getId();

        entry = new TimesheetEntryDTO();
        entry.setId(selected.getId());
        entry.setEntryDate(selected.getEntryDate());
        entry.setStartTime(selected.getStartTime());
        entry.setEndTime(selected.getEndTime());
        entry.setDescription(selected.getDescription());
        entry.setType(selected.getType());
    }

    public void saveEntry() {
        try {
            if (editingEntryId != null) {
                timesheetLogic.updateEntry(
                        timesheet.getId(),
                        editingEntryId,
                        entry
                );

                showInfo(
                        "Entry updated",
                        "The entry was updated successfully."
                );
            } else {
                timesheetLogic.addEntry(
                        timesheet.getId(),
                        entry
                );

                showInfo(
                        "Entry added",
                        "The entry was added successfully."
                );
            }

            reload();
            prepareNewEntry();

        } catch (Exception e) {
            showError(
                    "Could not save entry",
                    e.getMessage()
            );
        }
    }

    public void deleteEntry(TimesheetEntryDTO entry) {
        try {
            timesheetLogic.removeEntry(timesheet.getId(),entry.getId());

            reload();
            showInfo("Entry removed", "The entry was removed successfully.");

        } catch (Exception e) {
            showError(
                    "Could not delete entry",
                    e.getMessage()
            );
        }
    }
    
    private void loadVacationSummary() {
        if (timesheet == null || timesheet.getContractId() == null) {
            vacationHoursTotal = 0.0;
            vacationHoursUsed = 0.0;
            return;
        }

        ContractDTO contract = contractLogic.searchContract(timesheet.getContractId());
        vacationHoursTotal = contract != null ? contract.getVacationHours() : 0.0;
        vacationHoursUsed = timesheetLogic.getUsedVacationHours(timesheet.getContractId());
    }
    
    public void signByEmployee() {
    if (timesheet == null || timesheet.getId() == null) {
        showError("Could not sign", "No timesheet is loaded.");
        return;
    }

    try {
        timesheet = timesheetLogic.signByEmployee(timesheet.getId());

        showInfo(
                "Timesheet signed",
                "The timesheet was signed successfully."
        );
    } catch (IllegalArgumentException | IllegalStateException e) {
        showError("Could not sign", messageOf(e));
    }
}

    public void revokeEmployeeSignature() {
    if (timesheet == null || timesheet.getId() == null) {
        showError("Could not revoke signature","No timesheet is loaded.");
        return;
    }

    try {
        timesheet = timesheetLogic.revokeEmployeeSignature(timesheet.getId());

        showInfo(
                "Signature revoked",
                "The employee signature was revoked."
        );
    } catch (IllegalArgumentException | IllegalStateException e) {
        showError("Could not revoke signature", messageOf(e)
        );
    }
}

    private String messageOf(Exception e) {
        return e.getMessage() != null
            ? e.getMessage()
            : "The operation could not be completed.";
    }

    private void reload() {
        timesheet = timesheetLogic.getTimesheetById(id);
        loadVacationSummary();
    }

   
    public double getReportedHours() {
        if (timesheet == null || timesheet.getEntries() == null) {
            return 0;
        }

        return timesheet.getEntries()
                .stream()
                .mapToDouble(TimesheetEntryDTO::getHours)
                .sum();
    }

    public double getBalance() {
        if (timesheet == null) {
            return 0;
        }

        return timesheet.getHoursDue()- getReportedHours();
    }

    public boolean isEditable() {
        return timesheet != null&& timesheet.getStatus() == TimesheetStatus.IN_PROGRESS&& !isFuturePeriod();
    }
    
    public boolean isEditingEntry() {
        return editingEntryId != null;
    }
    
    public boolean isCanSignByEmployee() {
    return timesheet != null&& timesheet.getStatus() == TimesheetStatus.IN_PROGRESS&& !isFuturePeriod();
}


    public boolean isCanRevokeEmployeeSignature() {
        return timesheet != null
                && timesheet.getStatus()
                        == TimesheetStatus.SIGNED_BY_EMPLOYEE;
    }


    public boolean isEmployeeSigned() {
        return timesheet != null
                && timesheet.getSignedByEmployee() != null;
    }

    public boolean isSupervisorSigned() {
        return timesheet != null
                && timesheet.getSignedBySupervisor() != null;
    }
    
    public ReportType[] getReportTypes() {
        return ReportType.values();
    }
    
    private void showInfo(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
    }

    private void showError(
            String summary,
            String detail) {

        FacesContext.getCurrentInstance()
                .addMessage(
                        null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_ERROR,
                                summary,
                                detail
                        )
                );
    }
    
    private boolean isFuturePeriod() {
    if (timesheet == null || timesheet.getStartDate() == null) {
        return false;
    }
    return java.time.LocalDate.now().isBefore(timesheet.getStartDate());
}
    
    
    public double calculateEntryHours(TimesheetEntryDTO entry) {
    if (entry == null
            || entry.getStartTime() == null
            || entry.getEndTime() == null) {
        return 0;
    }
    long minutes = java.time.Duration.between(
            entry.getStartTime(),
            entry.getEndTime()
    ).toMinutes();

    
    return minutes / 60.0;
}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TimesheetDTO getTimesheet() {
        return timesheet;
    }

    public TimesheetEntryDTO getEntry() {
        return entry;
    }
    
    public double getVacationHoursTotal() {
        return vacationHoursTotal != null ? vacationHoursTotal : 0.0;
    }

    public double getVacationHoursUsed() {
        return vacationHoursUsed != null ? vacationHoursUsed : 0.0;
    }

    public double getVacationHoursRemaining() {
        return getVacationHoursTotal() - getVacationHoursUsed();
    }
}
