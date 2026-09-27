package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.logic.ContractLogic;
import tss.logic.PersonLogic;

@Named
@ViewScoped
public class ContractPrintBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ContractPrintBean.class.getName());

    @EJB
    private ContractLogic contractLogic;

    @EJB
    private PersonLogic personLogic;

    @Inject
    private loginBean loginBean;

    private Long contractId;
    private ContractDTO contract;
    private PersonDTO employee;
    private PersonDTO supervisor;

    public void loadContract() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String rawId = fc.getExternalContext().getRequestParameterMap().get("id");

        if (rawId == null || rawId.isBlank()) {
            LOGGER.warning("loadContract: no 'id' request parameter present");
            forbid();
            return;
        }

        try {
            contractId = Long.valueOf(rawId.trim());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "loadContract: 'id' parameter not a valid Long: " + rawId, e);
            forbid();
            return;
        }

        try {
            contract = contractLogic.searchContract(contractId);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Contract search failed for id " + contractId, e);
            forbid();
            return;
        }

        if (contract == null) {
            LOGGER.warning("Contract not found for id " + contractId);
            forbid();
            return;
        }

        if (loginBean.getUser() == null) {
            LOGGER.warning("loadContract called with no authenticated user");
            forbid();
            return;
        }

        Long currentPersonId = loginBean.getUser().getId();

        boolean isAssignedSecretary =
                contract.getSecretaryIds() != null
                && contract.getSecretaryIds().contains(currentPersonId);

        if (!isAssignedSecretary) {
            LOGGER.warning("Access denied: person " + currentPersonId
                    + " is not an assigned secretary for contract " + contractId);
            forbid();
            return;
        }

        if (contract.getPersonId() != null) {
            employee = safeFindPerson(contract.getPersonId());
        }
        if (contract.getSupervisorId() != null) {
            supervisor = safeFindPerson(contract.getSupervisorId());
        }
    }

    private PersonDTO safeFindPerson(Long personId) {
        try {
            return personLogic.findPerson(personId);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Person lookup failed for id " + personId, e);
            return null;
        }
    }

    private void forbid() {
        contract = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            facesContext.getExternalContext()
                    .responseSendError(403, "You are not authorized to access this contract.");
            facesContext.responseComplete();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to send 403 for contract " + contractId, e);
        }
    }

    public Long getContractId() { return contractId; }
    public ContractDTO getContract() { return contract; }
    public PersonDTO getEmployee() { return employee; }
    public PersonDTO getSupervisor() { return supervisor; }
}