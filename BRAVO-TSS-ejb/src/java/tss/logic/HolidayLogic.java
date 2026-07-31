package tss.logic;

import jakarta.ejb.Remote;
import java.util.List;
import tss.entity.Holiday;

@Remote
public interface HolidayLogic {

   
   List<Holiday> createHoliday(List<Holiday> holidays);
   List<Holiday> findByState(String state);

}