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

@Stateless
public class HolidayLogicImp implements HolidayLogic {

    @EJB
    private HolidayDao holidayDao;

    @Override
    public List<HolidayDTO> createHoliday(List<Holiday> holidays) {
        return toDtoList(holidayDao.createHoliday(holidays));
    }

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

    @Override
    public HolidayDTO findHoliday(Long id) {
        Holiday h = holidayDao.findById(id);
        if (h == null) {
            throw new IllegalArgumentException("No Holiday found with id: " + id);
        }
        return toDTO(h);
    }

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

    @Override
    public void deleteHoliday(HolidayDTO dto) {
        Holiday h = holidayDao.findById(dto.getId());
        if (h == null) {
            throw new IllegalArgumentException("No Holiday found with id: " + dto.getId());
        }
        holidayDao.deleteHoliday(h);
    }

    @Override
    public List<HolidayDTO> findByState(FederalState state) {
        return toDtoList(holidayDao.findByState(state));
    }

    @Override
    public List<HolidayDTO> findByStateAndRange(FederalState state, LocalDate startDate, LocalDate endDate) {
        return toDtoList(holidayDao.findByStateAndDateRange(state, startDate, endDate));
    }

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