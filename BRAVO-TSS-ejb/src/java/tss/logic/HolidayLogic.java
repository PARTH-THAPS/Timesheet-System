package tss.logic;

import jakarta.ejb.Remote;
import java.time.LocalDate;
import java.util.List;
import tss.entity.Holiday;

@Remote
public interface HolidayLogic {

   
   List<Holiday> createHoliday(List<Holiday> holidays);
   List<Holiday> findByStateAndRange(String state,LocalDate StartDate , LocalDate endDate);
   List<Holiday> findByState(String state);

}