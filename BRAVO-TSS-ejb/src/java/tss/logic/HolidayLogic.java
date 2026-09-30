package tss.logic;

import jakarta.ejb.Remote;
import java.time.LocalDate;
import java.util.List;
import tss.dto.HolidayDTO;
import tss.entity.FederalState;
import tss.entity.Holiday;

/**
 * Remote interface for managing public holidays across different federal states.
 */
@Remote
public interface HolidayLogic {

    /**
     * Bulk creates holidays from a provided list.
     * @param holidays A list of holiday entities to create.
     * @return A list of the newly created holidays.
     */
    List<HolidayDTO> createHoliday(List<Holiday> holidays);

    /**
     * Creates a single holiday entry.
     * @param day The name or description of the day.
     * @param date The date of the holiday.
     * @param holiday The type or title of the holiday.
     * @param state The federal state where the holiday applies.
     * @param year The year of the holiday.
     * @return The newly created holiday.
     */
    HolidayDTO createHoliday(String day, LocalDate date, String holiday, FederalState state, int year);

    /**
     * Finds a holiday by its ID.
     * @param id The ID of the holiday.
     * @return The found holiday.
     */
    HolidayDTO findHoliday(Long id);

    /**
     * Updates an existing holiday.
     * @param dto The updated holiday data.
     * @return The updated holiday.
     */
    HolidayDTO updateHoliday(HolidayDTO dto);

    /**
     * Deletes a specific holiday.
     * @param dto The holiday to delete.
     */
    void deleteHoliday(HolidayDTO dto);

    /**
     * Finds holidays for a specific federal state within a given date range.
     * @param state The federal state.
     * @param StartDate The start date of the range.
     * @param endDate The end date of the range.
     * @return A list of holidays within the specified parameters.
     */
    List<HolidayDTO> findByStateAndRange(FederalState state, LocalDate StartDate, LocalDate endDate);

    /**
     * Finds all holidays for a specific federal state.
     * @param state The federal state.
     * @return A list of holidays for the state.
     */
    List<HolidayDTO> findByState(FederalState state);

    /**
     * Retrieves all holidays in the system.
     * @return A list of all holidays.
     */
    List<HolidayDTO> findAllHoliday();
}