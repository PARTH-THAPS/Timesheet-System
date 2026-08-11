package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDate;
import java.util.List;
import tss.dao.HolidayDao;
import tss.entity.Holiday;
import tss.logic.HolidayLogic;

@Stateless
public class HolidayLogicImp implements HolidayLogic {


    @EJB
    private HolidayDao holidayDao;


    @Override
public List<Holiday> createHoliday(List<Holiday> holidays) {

    return holidayDao.createHoliday(holidays);
}
    
    @Override
    public List<Holiday> findByState(String state)
    {
           return holidayDao.findByState(state);
    }
    
    
    @Override
    public List<Holiday> findByStateAndRange(String state,LocalDate StartDate , LocalDate endDate)
    {
           return holidayDao.findByStateAndDateRange(state, StartDate, endDate);
    }
}