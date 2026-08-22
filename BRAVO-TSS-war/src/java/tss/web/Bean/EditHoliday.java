/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tss.web.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import tss.dto.HolidayDTO;
import tss.logic.HolidayLogic;

@Named("EditHolidayBean")
@ViewScoped
public class EditHoliday implements Serializable {

    @EJB
    private HolidayLogic holidayLogic;
    private Long id;
    private HolidayDTO holidaydto;
    
    
    @PostConstruct
    public void init(){
    }

}
