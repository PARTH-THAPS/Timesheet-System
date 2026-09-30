package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.entity.ContractStatus;
import tss.logic.ContractLogic;
import tss.logic.PersonLogic;

@Named("archiveContractsBean")
@ViewScoped
public class ArchiveContractsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ContractLogic contractLogic;

    @EJB
    private PersonLogic personLogic;

    private List<ContractDTO> archivedContracts;
    private final Map<Long, String> personNames = new HashMap<>();

    @PostConstruct
    public void init() {
        archivedContracts = contractLogic.findAllContracts().stream()
                .filter(contract -> contract.getStatus() == ContractStatus.ARCHIVED)
                .toList();

        for (PersonDTO person : personLogic.findAllPersons()) {
            personNames.put(person.getId(), person.getFirstName() + " " + person.getLastName());
        }
    }

    public List<ContractDTO> getArchivedContracts() {
        return archivedContracts;
    }

    public String getPersonName(Long personId) {
        return personNames.getOrDefault(personId, "Unknown");
    }
    
    public ContractStatus[] getStatuses() {
        return ContractStatus.values();
    }
}