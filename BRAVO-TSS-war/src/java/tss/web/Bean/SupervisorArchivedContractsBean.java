package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.io.Serial;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.entity.ContractStatus;
import tss.logic.ContractLogic;
import tss.logic.PersonLogic;

@Named("supervisorArchivedContractsBean")
@ViewScoped
public class SupervisorArchivedContractsBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EJB
    private ContractLogic contractLogic;

    @EJB
    private PersonLogic personLogic;

    private List<ContractDTO> archivedContracts = List.of();
    private final Map<Long, String> personNames = new HashMap<>();

    @PostConstruct
    public void init() {
        loadPersonNames();

        Principal principal = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getUserPrincipal();

        if (principal == null) {
            archivedContracts = List.of();
            return;
        }

        Long supervisorId = findPersonIdByEmail(principal.getName());
        if (supervisorId == null) {
            archivedContracts = List.of();
            return;
        }

        archivedContracts = contractLogic.findAllArchivedContractsForSupervisor(supervisorId);
    }

    private void loadPersonNames() {
        for (PersonDTO person : personLogic.findAllPersons()) {
            personNames.put(
                    person.getId(),
                    person.getFirstName() + " " + person.getLastName()
            );
        }
    }

    private Long findPersonIdByEmail(String emailAddress) {
        return personLogic.findAllPersons().stream()
                .filter(person -> emailAddress.equals(person.getEmailAddress()))
                .map(PersonDTO::getId)
                .findFirst()
                .orElse(null);
    }

    public String getPersonName(Long personId) {
        return personNames.getOrDefault(personId, "Unknown");
    }

    public List<ContractDTO> getArchivedContracts() {
        return archivedContracts;
    }
    
    public ContractStatus[] getStatuses() {
        return ContractStatus.values();
    }
}


