/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.util.List;
import tss.dto.HolidayDTO;
import tss.logic.HolidayLogic;
import tss.entity.Holiday;

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
}
