package tss.logic;

import jakarta.ejb.Remote;
import java.time.LocalDate;
import java.util.List;
import tss.dto.HolidayDTO;
import tss.entity.FederalState;
import tss.entity.Holiday;

@Remote
public interface HolidayLogic {

    List<HolidayDTO> createHoliday(List<Holiday> holidays);

    HolidayDTO createHoliday(String day, LocalDate date, String holiday, FederalState state, int year);

    HolidayDTO findHoliday(Long id);

    HolidayDTO updateHoliday(HolidayDTO dto);

    void deleteHoliday(HolidayDTO dto);

    List<HolidayDTO> findByStateAndRange(FederalState state, LocalDate StartDate, LocalDate endDate);

    List<HolidayDTO> findByState(FederalState state);

    List<HolidayDTO> findAllHoliday();
}