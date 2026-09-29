package tss.web.Bean;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
public class ArchiveTimesheetDetailBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long contractId;

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
}