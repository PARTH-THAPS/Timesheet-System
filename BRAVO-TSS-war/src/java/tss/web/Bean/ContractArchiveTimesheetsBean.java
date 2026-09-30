package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tss.dto.ContractDTO;
import tss.dto.PersonDTO;
import tss.dto.TimesheetDTO;
import tss.logic.ContractLogic;
import tss.logic.PersonLogic;
import tss.logic.TimesheetLogic;
import tss.web.i18n.Messages;

@Named("contractArchiveTimesheetsBean")
@ViewScoped
public class ContractArchiveTimesheetsBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EJB
    private ContractLogic contractLogic;

    @EJB
    private TimesheetLogic timesheetLogic;

    @EJB
    private PersonLogic personLogic;

    private Long id;
    private ContractDTO contract;
    private List<TimesheetDTO> timesheets = List.of();

    private final Map<Long, String> personNames = new HashMap<>();

    public void init() {
        loadPersonNames();

        if (id == null) {
            throw new IllegalArgumentException(Messages.get("message.contract.noId"));
        }

        contract = contractLogic.searchContract(id);

        if (contract == null) {
            throw new IllegalArgumentException(Messages.get("message.contract.notFound", id));
        }

        timesheets = timesheetLogic.getTimesheetsForContract(id);
    }

    private void loadPersonNames() {
        for (PersonDTO person : personLogic.findAllPersons()) {
            personNames.put(
                    person.getId(),
                    person.getFirstName() + " " + person.getLastName()
            );
        }
    }

    public String getPersonName(Long personId) {
        return personNames.getOrDefault(
                personId,
                Messages.get("common.unknown")
        );
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

    public List<TimesheetDTO> getTimesheets() {
        return timesheets;
    }
}
