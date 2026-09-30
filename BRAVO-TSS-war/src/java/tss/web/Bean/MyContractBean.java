package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tss.dto.ContractDTO;
import tss.dto.ContractStatisticsDTO;
import tss.dto.User;
import tss.logic.ContractLogic;

@Named
@ViewScoped
public class MyContractBean implements Serializable {

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
            contracts = new ArrayList<>();
            return;
        }

        Long currentPersonId = currentUser.getId();

        contracts = new ArrayList<>(
                contractLogic.findAllContracts()
                        .stream()
                        .filter(c -> c.getPersonId() != null
                        && c.getPersonId().equals(currentPersonId))
                        .toList()
        );
    }

    public List<ContractDTO> getContracts() {
        return contracts;
    }

    public ContractDTO getSelectedContract() {
        return selectedContract;
    }

    public void setSelectedContract(ContractDTO selectedContract) {

        this.selectedContract = selectedContract;

        if (selectedContract == null
                || selectedContract.getId() == null) {

            selectedStatistics = null;
            return;
        }

        selectedStatistics
                = contractLogic.getContractStatistics(
                        selectedContract.getId()
                );
    }

    public ContractStatisticsDTO getSelectedStatistics() {
        return selectedStatistics;
    }
}
