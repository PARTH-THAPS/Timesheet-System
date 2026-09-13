package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.entity.ContractStatus;
import tss.entity.FederalState;
import tss.entity.TimesheetFrequency;
import tss.logic.ContractLogic;
import tss.logic.PersonLogic;

@Named
@ViewScoped
public class ContractEditBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ContractLogic contractLogic;

    @EJB
    private PersonLogic personLogic;

    private Long id;
    private ContractDTO contract;
    private List<PersonDTO> persons;

    private List<Long> originalSecretaryIds;
    private List<Long> originalAssistantIds;

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
        }

        originalSecretaryIds = contract.getSecretaryIds() != null
                ? new ArrayList<>(contract.getSecretaryIds())
                : new ArrayList<>();
        originalAssistantIds = contract.getAssistantIds() != null
                ? new ArrayList<>(contract.getAssistantIds())
                : new ArrayList<>();
    }

    public void save() {
        try {
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
                    contractLogic.addSupervisor(
                            contractId,
                            contract.getSupervisorId()
                    );
                }
            } else {
                contractLogic.updateContract(contract);
                contractId = contract.getId();
                if (contract.getSupervisorId() != null) {
                    contractLogic.addSupervisor(
                            contractId,
                            contract.getSupervisorId()
                    );
                } else {
                    contractLogic.removeSupervisor(contractId);
                }
            }

            syncSecretariesAndAssistants(contractId);

            redirectToContracts();
        } catch (Exception e) {
            showError(
                    "Could not save contract",
                    e.getMessage()
            );
        }
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
        } catch (Exception e) {
            showError(
                    "Could not start contract",
                    e.getMessage()
            );
        }
    }

    public void terminate() {
        try {
            contract = contractLogic.updateContractStatus(
                    contract.getId(),
                    ContractStatus.TERMINATED
            );
        } catch (Exception e) {
            showError(
                    "Could not terminate contract",
                    e.getMessage()
            );
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
                .filter(person ->
                        contract.getPersonId().equals(person.getId())
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Selected employee could not be found."
                        )
                );
    }

    private void redirectToContracts() throws IOException {
        FacesContext facesContext =
                FacesContext.getCurrentInstance();
        String contextPath =
                facesContext
                        .getExternalContext()
                        .getRequestContextPath();
        facesContext
                .getExternalContext()
                .redirect(
                        contextPath
                                + "/views/assistant/contracts.xhtml"
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