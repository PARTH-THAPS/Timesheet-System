package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import tss.dao.HolidayDao;
import tss.dto.HolidayDTO;
import tss.entity.FederalState;
import tss.entity.Holiday;
import tss.logic.HolidayLogic;

/**
 * Implementation of the {@link HolidayLogic} interface.
 * Manages operations on regional public holidays.
 */
@Stateless
public class HolidayLogicImp implements HolidayLogic {

    @EJB
    private HolidayDao holidayDao;

    /**
     * Creates multiple holiday entries from a list.
     *
     * @param holidays The list of Holiday entities to persist.
     * @return A list of created HolidayDTOs.
     */
    @Override
    public List<HolidayDTO> createHoliday(List<Holiday> holidays) {
        return toDtoList(holidayDao.createHoliday(holidays));
    }

    /**
     * Creates a single holiday entry.
     *
     * @param day The name or descriptor of the day.
     * @param date The date of the holiday.
     * @param holiday The type/name of the holiday.
     * @param state The federal state where it applies.
     * @param year The year of the holiday.
     * @return The created HolidayDTO.
     */
    @Override
    public HolidayDTO createHoliday(String day, LocalDate date, String holiday, FederalState state, int year) {
        Holiday h = new Holiday();
        h.setDay(day);
        h.setDate(date);
        h.setHoliday(holiday);
        h.setState(state);
        h.setYear(year);

        holidayDao.createHoliday(List.of(h));

        return toDTO(h);
    }

    /**
     * Finds a holiday by its ID.
     *
     * @param id The ID of the holiday.
     * @return The HolidayDTO.
     */
    @Override
    public HolidayDTO findHoliday(Long id) {
        Holiday h = holidayDao.findById(id);
        if (h == null) {
            throw new IllegalArgumentException("No Holiday found with id: " + id);
        }
        return toDTO(h);
    }

    /**
     * Updates an existing holiday.
     *
     * @param dto The HolidayDTO with updated values.
     * @return The updated HolidayDTO.
     */
    @Override
    public HolidayDTO updateHoliday(HolidayDTO dto) {
        Holiday h = holidayDao.findById(dto.getId());
        if (h == null) {
            throw new IllegalArgumentException("No Holiday found with id: " + dto.getId());
        }

        h.setDay(dto.getDay());
        h.setDate(dto.getDate());
        h.setHoliday(dto.getHoliday());
        h.setState(dto.getState());
        h.setYear(dto.getYear());

        return toDTO(holidayDao.updateHoliday(h));
    }

    /**
     * Deletes a holiday.
     *
     * @param dto The HolidayDTO identifying the holiday to delete.
     */
    @Override
    public void deleteHoliday(HolidayDTO dto) {
        Holiday h = holidayDao.findById(dto.getId());
        if (h == null) {
            throw new IllegalArgumentException("No Holiday found with id: " + dto.getId());
        }
        holidayDao.deleteHoliday(h);
    }

    /**
     * Retrieves all holidays for a specific state.
     *
     * @param state The federal state.
     * @return A list of holidays in the specified state.
     */
    @Override
    public List<HolidayDTO> findByState(FederalState state) {
        return toDtoList(holidayDao.findByState(state));
    }

    /**
     * Retrieves holidays for a specific state within a date range.
     *
     * @param state The federal state.
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     * @return A list of holidays within the range and state.
     */
    @Override
    public List<HolidayDTO> findByStateAndRange(FederalState state, LocalDate startDate, LocalDate endDate) {
        return toDtoList(holidayDao.findByStateAndDateRange(state, startDate, endDate));
    }

    /**
     * Retrieves all holidays in the system.
     *
     * @return A list of all HolidayDTOs.
     */
    @Override
    public List<HolidayDTO> findAllHoliday() {
        return toDtoList(holidayDao.findAllHoliday());
    }

    private List<HolidayDTO> toDtoList(List<Holiday> holidays) {
        List<HolidayDTO> dtoList = new ArrayList<>();
        for (Holiday h : holidays) {
            dtoList.add(toDTO(h));
        }
        return dtoList;
    }

    private HolidayDTO toDTO(Holiday h) {
        HolidayDTO dto = new HolidayDTO();
        dto.setId(h.getId());
        dto.setUuid(h.getUuid());
        dto.setJpaVersion(h.getJpaVersion());
        dto.setDate(h.getDate());
        dto.setState(h.getState());
        dto.setHoliday(h.getHoliday());
        dto.setDay(h.getDay());
        dto.setYear(h.getYear());
        return dto;
    }
}