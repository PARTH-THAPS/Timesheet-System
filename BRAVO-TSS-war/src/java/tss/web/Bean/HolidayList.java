package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.util.List;
import tss.dto.HolidayDTO;
import tss.logic.HolidayLogic;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import tss.web.i18n.Messages;

@Named("HolidayListBean")
@RequestScoped
public class HolidayList {

    @EJB
    HolidayLogic holidayLogic;
    private List<HolidayDTO> holiday;

    @PostConstruct
    public void init() {
        holiday = holidayLogic.findAllHoliday();
    }

    public List<HolidayDTO> getHoliday() {
        return holiday;
    }

    public void deleteHoliday(HolidayDTO dto) {
        try {
            holidayLogic.deleteHoliday(dto);
            holiday = holidayLogic.findAllHoliday();

            message(
                    FacesMessage.SEVERITY_INFO,
                    Messages.get("message.holiday.deleted.summary"),
                    Messages.get("message.holiday.deleted.detail")
            );

        } catch (Exception e) {
            message(
                    FacesMessage.SEVERITY_ERROR,
                    Messages.get("message.holiday.deleteFailed"),
                    e.getMessage()
            );
        }
    }

    private void message(
            FacesMessage.Severity severity,
            String summary,
            String detail
    ) {
        FacesContext.getCurrentInstance()
                .addMessage(
                        null,
                        new FacesMessage(severity, summary, detail)
                );
    }
}
