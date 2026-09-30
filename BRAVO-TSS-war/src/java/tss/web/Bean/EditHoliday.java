package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;

import tss.dto.HolidayDTO;
import tss.entity.FederalState;
import tss.logic.HolidayLogic;
import tss.web.i18n.Messages;

@Named("EditHolidayBean")
@ViewScoped
public class EditHoliday implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private HolidayLogic holidayLogic;

    private Long id;
    private HolidayDTO holidaydto;

    public void init() {

        if (holidaydto != null) {
            return;
        }

        if (id == null) {
            holidaydto = new HolidayDTO();
        } else {
            holidaydto = holidayLogic.findHoliday(id);
        }
    }

    public String save() {

        try {

            if (id == null) {
                holidaydto = holidayLogic.createHoliday(
                        holidaydto.getDay(),
                        holidaydto.getDate(),
                        holidaydto.getHoliday(),
                        holidaydto.getState(),
                        holidaydto.getYear()
                );
            } else {
                holidaydto = holidayLogic.updateHoliday(holidaydto);
            }

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            Messages.get("message.holiday.saved.summary"),
                            Messages.get("message.holiday.saved.detail")
                    )
            );

            return "/views/admin/HolidayList.xhtml?faces-redirect=true";

        } catch (Exception e) {

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            Messages.get("message.holiday.saveFailed"),
                            e.getMessage()
                    )
            );

            return null;
        }
    }

    public String delete() {

        if (id == null) {
            return null;
        }

        try {

            holidayLogic.deleteHoliday(holidaydto);

            return "/views/admin/HolidayList.xhtml?faces-redirect=true";

        } catch (Exception e) {

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            Messages.get("message.holiday.deleteFailed"),
                            e.getMessage()
                    )
            );

            return null;
        }
    }

    public String cancel() {
        return "/views/admin/HolidayList.xhtml?faces-redirect=true";
    }

    public boolean isNewHoliday() {
        return id == null;
    }

    public FederalState[] getStates() {
        return FederalState.values();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HolidayDTO getHolidaydto() {
        return holidaydto;
    }
}
