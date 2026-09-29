package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.entity.ContractStatus;
import tss.entity.FederalState;
import tss.entity.Role;
import tss.entity.TimesheetFrequency;
import tss.logic.ContractLogic;
import tss.logic.PersonLogic;
import tss.logic.TerminationBlockedException;

@Named
@ViewScoped
public class ContractEditBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ContractLogic contractLogic;

    @EJB
    private PersonLogic personLogic;

    @Inject
    private loginBean loginBean;

    private Long id;
    private ContractDTO contract;
    private List<PersonDTO> persons;
    private List<Long> originalSecretaryIds;
    private List<Long> originalAssistantIds;

    // Termination flags, loaded once per state change instead of on every JSF render
    private boolean terminateBlocked;
    private boolean hasUnresolvedInProgress;
    private boolean hasEmptyInProgress;

    public void init() {
        if (contract != null) {
            return;
        }
        persons = personLogic.findAllPersons();

        if (id == null) {
            contract = new ContractDTO();
            contract.setFrequency(TimesheetFrequency.MONTHLY);
            contract.setWorkingDaysPerWeek(5);
            contract.setVacationDaysPerYear(20);
            contract.setState(FederalState.RLP);
            contract.setSecretaryIds(new ArrayList<>());
            contract.setAssistantIds(new ArrayList<>());
        } else {
            contract = contractLogic.searchContract(id);
            if (!isAuthorizedForContract(contract)) {
                denyAccess();
                return;
            }
        }
        originalSecretaryIds = contract.getSecretaryIds() != null
                ? new ArrayList<>(contract.getSecretaryIds())
                : new ArrayList<>();
        originalAssistantIds = contract.getAssistantIds() != null
                ? new ArrayList<>(contract.getAssistantIds())
                : new ArrayList<>();

        refreshTerminationFlags();
    }

    private boolean isAuthorizedForContract(ContractDTO contract) {
        Long currentPersonId = loginBean.getUser().getId();
        if (loginBean.hasRole("SUPERVISOR")) {
            return currentPersonId.equals(contract.getSupervisorId());
        }
        if (loginBean.hasRole("ASSISTANT")) {
            return contract.getAssistantIds() != null
                    && contract.getAssistantIds().contains(currentPersonId);
        }
        return true; // e.g. ADMIN or other unrestricted roles
    }

    private void denyAccess() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            String contextPath = facesContext.getExternalContext().getRequestContextPath();
            facesContext.getExternalContext().redirect(contextPath + "/views/access-denied.xhtml");
            facesContext.responseComplete();
        } catch (IOException e) {
            showError("Access denied", "You are not authorized to view this contract.");
        }
    }

    

    public void save() {
        try {
            validateNoOverlap();

            Long contractId;

            if (isNewContract()) {
                PersonDTO person = findSelectedPerson();

                ContractDTO created = contractLogic.createContract(
                        contract.getName(),
                        contract.getStartDate(),
                        contract.getEndDate(),
                        contract.getFrequency(),
                        contract.getHoursPerWeek(),
                        contract.getHoursDue(),
                        contract.getWorkingDaysPerWeek(),
                        contract.getVacationDaysPerYear(),
                        person,
                        contract.getState()
                );

                contractId = created.getId();

                if (contract.getSupervisorId() != null) {
                    contractLogic.addSupervisor(contractId, contract.getSupervisorId());
                }
            } else {
                contractLogic.updateContract(contract);
                contractId = contract.getId();

                if (contract.getSupervisorId() != null) {
                    contractLogic.addSupervisor(contractId, contract.getSupervisorId());
                } else {
                    contractLogic.removeSupervisor(contractId);
                }
            }

            syncSecretariesAndAssistants(contractId);
            redirectToContracts();

        } catch (Exception e) {
            showError("Could not save contract", e.getMessage());
        }
    }

    private void validateNoOverlap() {
        Long employeeId = contract.getPersonId();
        Long supervisorId = contract.getSupervisorId();
        List<Long> secretaryIds = contract.getSecretaryIds() != null
                ? contract.getSecretaryIds() : List.of();
        List<Long> assistantIds = contract.getAssistantIds() != null
                ? contract.getAssistantIds() : List.of();

        if (secretaryIds.isEmpty()) {
            throw new IllegalArgumentException("At least one secretary is required.");
        }

        // Collect every (role label, personId) pair that was actually assigned
        List<Map.Entry<String, Long>> assignments = new ArrayList<>();
        if (employeeId != null) {
            assignments.add(Map.entry("employee", employeeId));
        }
        if (supervisorId != null) {
            assignments.add(Map.entry("supervisor", supervisorId));
        }
        for (Long id : secretaryIds) {
            assignments.add(Map.entry("secretary", id));
        }
        for (Long id : assistantIds) {
            assignments.add(Map.entry("assistant", id));
        }

        Set<Long> seen = new HashSet<>();
        for (Map.Entry<String, Long> entry : assignments) {
            if (!seen.add(entry.getValue())) {
                throw new IllegalArgumentException(
                        "Each person may only have one role on this contract. "
                        + personNameOrId(entry.getValue())
                        + " is assigned to more than one role."
                );
            }
        }
    }

    private String personNameOrId(Long id) {
        return persons.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .map(p -> p.getFirstName() + " " + p.getLastName())
                .orElse("Person #" + id);
    }

    private void syncSecretariesAndAssistants(Long contractId) {
        List<Long> selectedSecretaries = contract.getSecretaryIds() != null
                ? contract.getSecretaryIds() : List.of();

        List<Long> secretariesToAdd = selectedSecretaries.stream()
                .filter(pid -> !originalSecretaryIds.contains(pid))
                .toList();

        List<Long> secretariesToRemove = originalSecretaryIds.stream()
                .filter(pid -> !selectedSecretaries.contains(pid))
                .toList();

        if (!secretariesToAdd.isEmpty()) {
            contractLogic.addSecretary(contractId, secretariesToAdd);
        }
        if (!secretariesToRemove.isEmpty()) {
            contractLogic.removeSecretary(contractId, secretariesToRemove);
        }

        List<Long> selectedAssistants = contract.getAssistantIds() != null
                ? contract.getAssistantIds() : List.of();

        List<Long> assistantsToAdd = selectedAssistants.stream()
                .filter(pid -> !originalAssistantIds.contains(pid))
                .toList();

        List<Long> assistantsToRemove = originalAssistantIds.stream()
                .filter(pid -> !selectedAssistants.contains(pid))
                .toList();

        if (!assistantsToAdd.isEmpty()) {
            contractLogic.addAssistant(contractId, assistantsToAdd);
        }
        if (!assistantsToRemove.isEmpty()) {
            contractLogic.removeAssistant(contractId, assistantsToRemove);
        }
    }

    public void start() {
        try {
            contract = contractLogic.updateContractStatus(
                    contract.getId(),
                    ContractStatus.STARTED
            );
            refreshTerminationFlags();
        } catch (Exception e) {
            showError(
                    "Could not start contract",
                    e.getMessage()
            );
        }
    }
private void refreshTerminationFlags() {
    if (contract == null || contract.getId() == null
            || contract.getStatus() != ContractStatus.STARTED) {
        terminateBlocked = false;
        hasUnresolvedInProgress = false;
        hasEmptyInProgress = false;
        return;
    }
    Long contractId = contract.getId();

    // Only an employee-signed timesheet blocks the button
    terminateBlocked = contractLogic.hasTimesheetsPendingSupervisorSignature(contractId);

    // These two only choose the text of the confirm dialog
    hasUnresolvedInProgress = contractLogic.hasUnresolvedInProgressTimesheets(contractId);
    hasEmptyInProgress = contractLogic.hasEmptyInProgressTimesheets(contractId);
}
    

    public void terminate() {
   
    if (!isStarted()) {
        showError("Could not terminate contract",
                "Only a started contract can be terminated.");
        return;
    }
    try {
        // The confirm dialog (contract.terminate.confirm.*) has already been accepted
        // by the user, so deleting IN_PROGRESS timesheets is confirmed here.
        contract = contractLogic.terminateContract(contract.getId(), true);
    } catch (TerminationBlockedException e) {
        // A timesheet was signed by the employee after the page was rendered
        showError("Contract cannot be terminated", e.getMessage());
    }  catch (Exception e) {
        showError("Could not terminate contract", e.getMessage());
    } finally {
        
        refreshTerminationFlags();
    }
}

    public void delete() {
        try {
            contractLogic.deleteContract(contract.getId());
            redirectToContracts();
        } catch (Exception e) {
            showError(
                    "Could not delete contract",
                    e.getMessage()
            );
        }
    }

    public void cancel() {
        try {
            redirectToContracts();
        } catch (IOException e) {
            showError(
                    "Could not return to contracts",
                    e.getMessage()
            );
        }
    }

    private PersonDTO findSelectedPerson() {
        if (contract.getPersonId() == null) {
            throw new IllegalArgumentException(
                    "Please select an employee."
            );
        }

        return persons.stream()
                .filter(person
                        -> contract.getPersonId().equals(person.getId())
                )
                .findFirst()
                .orElseThrow(()
                        -> new IllegalArgumentException(
                        "Selected employee could not be found."
                )
                );
    }

    private void redirectToContracts() throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String contextPath
                = facesContext.getExternalContext().getRequestContextPath();

        String section = loginBean.hasRole("SUPERVISOR")
                ? "supervisor"
                : "assistant";

        facesContext.getExternalContext().redirect(
                contextPath + "/views/" + section + "/contracts.xhtml"
        );
        facesContext.responseComplete();
    }

    private void showError(
            String summary,
            String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        summary,
                        detail
                )
        );
    }

    public boolean isNewContract() {
        return id == null;
    }

    public boolean isPrepared() {
        return contract != null
                && contract.getStatus()
                == ContractStatus.PREPARED;
    }

    public boolean isStarted() {
        return contract != null
                && contract.getStatus()
                == ContractStatus.STARTED;
    }

    // Cached values, so the getters below do not hit the database on every render
    public boolean isHasUnresolvedInProgressTimesheets() {
        return hasUnresolvedInProgress;
    }

    public boolean isTerminateBlocked() {
        return terminateBlocked;
    }

    public String getTerminateConfirmMessageKey() {
        if (hasUnresolvedInProgress) {
            return "contract.terminate.confirm.warning";
        }
        if (hasEmptyInProgress) {
            return "contract.terminate.confirm.emptyWillBeDeleted";
        }
        return "contract.terminate.confirm";
    }

    public List<PersonDTO> getSupervisors() {
        return persons.stream()
                .filter(p -> p.getRole() != null && p.getRole().contains(Role.SUPERVISOR))
                .toList();
    }

    public List<PersonDTO> getSecretaries() {
        return persons.stream()
                .filter(p -> p.getRole() != null && p.getRole().contains(Role.SECRETARY))
                .toList();
    }

    public List<PersonDTO> getAssistants() {
        return persons.stream()
                .filter(p -> p.getRole() != null && p.getRole().contains(Role.ASSISTANT))
                .toList();
    }

    public boolean isEditable() {
        return isNewContract() || isPrepared();
    }

    public TimesheetFrequency[] getFrequencies() {
        return TimesheetFrequency.values();
    }

    public FederalState[] getStates() {
        return FederalState.values();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContractDTO getContract() {
        return contract;
    }

    public List<PersonDTO> getPersons() {
        return persons;
    }
}