package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;
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

        } else {
            contract = contractLogic.searchContract(id);
        }
    }

    public void save() {
        try {
            if (isNewContract()) {

                PersonDTO person = findSelectedPerson();

                contractLogic.createContract(
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

            } else {
                contractLogic.updateContract(contract);
            }

            redirectToContracts();

        } catch (Exception e) {
            showError(
                    "Could not save contract",
                    e.getMessage()
            );
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