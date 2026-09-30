/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import java.util.List;
import tss.dto.HolidayDTO;
import tss.logic.HolidayLogic;
import tss.entity.Holiday;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

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
            message(FacesMessage.SEVERITY_INFO, "Holiday deleted", null);
        } catch (Exception e) {
            message(FacesMessage.SEVERITY_ERROR, "Delete failed", e.getMessage());
        }
    }
 private void message(FacesMessage.Severity severity, String summary, String detail) {
    FacesContext.getCurrentInstance()
            .addMessage(null, new FacesMessage(severity, summary, detail));
}
}
