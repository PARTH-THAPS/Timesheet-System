package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.entity.ContractStatus;
import tss.logic.ContractLogic;
import tss.logic.PersonLogic;

@Named
@RequestScoped
public class ContractListBean {

    @EJB
    private ContractLogic contractLogic;
    @EJB
    private PersonLogic personLogic;
    @Inject
    private loginBean loginBean;

    private List<ContractDTO> contracts;
    private final Map<Long, String> personNames = new HashMap<>();

    @PostConstruct
    public void init() {
        List<ContractDTO> all = contractLogic.findAllContracts();
        Long currentPersonId = loginBean.getUser().getId();

        if (loginBean.hasRole("SUPERVISOR")) {
            contracts = all.stream()
                    .filter(c -> currentPersonId.equals(c.getSupervisorId()))
                    .toList();
        } else if (loginBean.hasRole("ASSISTANT")) {
            contracts = all.stream()
                    .filter(c -> c.getAssistantIds() != null
                            && c.getAssistantIds().contains(currentPersonId))
                    .toList();
        } else {
            contracts = all;
        }

        for (PersonDTO person : personLogic.findAllPersons()) {
            personNames.put(
                    person.getId(),
                    person.getFirstName() + " " + person.getLastName()
            );
        }
    }

    public List<ContractDTO> getContracts() {
        return contracts;
    }

    public ContractStatus[] getStatuses() {
        return ContractStatus.values();
    }

    public String getPersonName(Long personId) {
        return personNames.getOrDefault(personId, "Unknown");
    }
}