package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import tss.dao.HolidayDao;
import tss.dto.HolidayDTO;
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
    public List<HolidayDTO> findByState(String state) {
        return toDtoList(holidayDao.findByState(state));
    }

    @Override
    public List<HolidayDTO> findByStateAndRange(String state, LocalDate startDate, LocalDate endDate) {
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