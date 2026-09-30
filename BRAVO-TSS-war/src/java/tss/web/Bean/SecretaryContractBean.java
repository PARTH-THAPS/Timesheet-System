package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

import tss.dto.ContractDTO;
import tss.dto.ContractStatisticsDTO;
import tss.dto.User;
import tss.entity.ContractStatus;
import tss.logic.ContractLogic;

@Named
@ViewScoped
public class SecretaryContractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ContractLogic contractLogic;

    @Inject
    private loginBean loginBean;

    private List<ContractDTO> contracts;
    private ContractDTO selectedContract;
    private ContractStatisticsDTO selectedStatistics;

    @PostConstruct
    public void init() {
        User currentUser = loginBean.getUser();
        if (currentUser == null) {
            contracts = List.of();
            return;
        }
        Long currentPersonId = currentUser.getId();
        contracts = contractLogic.findAllContracts()
                .stream()
                .filter(c -> c.getSecretaryIds() != null
                && c.getSecretaryIds().contains(currentPersonId))
                .toList();
    }

    public List<ContractDTO> getContracts() {
        return contracts;
    }

    public ContractStatus[] getStatuses() {
        return ContractStatus.values();
    }

    public void setSelectedContract(ContractDTO selectedContract) {
        this.selectedContract = selectedContract;

        if (selectedContract == null || selectedContract.getId() == null) {
            selectedStatistics = null;
            return;
        }

        selectedStatistics
                = contractLogic.getContractStatistics(
                        selectedContract.getId()
                );
    }
    
    public ContractDTO getSelectedContract() {
        return selectedContract;
    }
    
    
    public ContractStatisticsDTO getSelectedStatistics() {
        return selectedStatistics;
    }
}
