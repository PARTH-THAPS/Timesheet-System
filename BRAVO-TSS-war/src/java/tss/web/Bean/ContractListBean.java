package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.entity.ContractStatus;
import tss.logic.ContractLogic;
import tss.logic.PersonLogic;
import tss.web.i18n.Messages;

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

        Set<ContractDTO> visible = new LinkedHashSet<>();

        if (loginBean.hasRole("SUPERVISOR")) {
            visible.addAll(
                    all.stream()
                            .filter(c -> currentPersonId.equals(c.getSupervisorId()))
                            .toList()
            );
        }
        if (loginBean.hasRole("ASSISTANT")) {
            visible.addAll(
                    all.stream()
                            .filter(c -> c.getAssistantIds() != null
                            && c.getAssistantIds().contains(currentPersonId))
                            .toList()
            );
        }

        contracts = visible.stream()
                .filter(c -> c.getStatus() != ContractStatus.ARCHIVED)
                .toList();

        for (PersonDTO person : personLogic.findAllPersons()) {
            personNames.put(person.getId(), person.getFirstName() + " " + person.getLastName());
        }
    }

    public List<ContractDTO> getContracts() {
        return contracts;
    }

    public ContractStatus[] getStatuses() {
        return ContractStatus.values();
    }

    public String getPersonName(Long personId) {
        return personNames.getOrDefault(
                personId,
                Messages.get("common.unknown")
        );
    }
}
