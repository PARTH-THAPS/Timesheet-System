package rest;

import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import tss.dto.HolidayDTO;
import tss.entity.Holiday;
import tss.logic.HolidayLogic;

@Stateless
@LocalBean
@Path("v1/holiday")
public class HolidayRestEndpoint {

    @EJB
    private HolidayLogic holidayLogic;

//@POST
//@Path("create")
//@Consumes(MediaType.APPLICATION_JSON)
//@Produces(MediaType.APPLICATION_JSON)
//public List<Holiday> createHolidayTable(List<HolidayDTO> holidayDTOs) {
//
//    List<Holiday> holidays = new ArrayList<>();
//
//    for (HolidayDTO dto : holidayDTOs) {
//
//        Holiday holiday = new Holiday();
//
//        holiday.setDay(dto.getDay());
//        holiday.setDate(dto.getDate());
//        holiday.setHoliday(dto.getHoliday());
//        holiday.setState(dto.getState());
//        holiday.setYear(dto.getYear());
//
//        holidays.add(holiday);
//    }
//
//    return holidayLogic.createHoliday(holidays);
//}
//
//    @GET
//    @Path("find/{state}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public List <Holiday> getHolidayByYear(@PathParam("state") String State) {
//
//        return holidayLogic.findByState(State);
//    }
    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<HolidayDTO> createHolidayTable(List<HolidayDTO> holidayDTOs) {
        List<Holiday> holidays = new ArrayList<>();
        for (HolidayDTO dto : holidayDTOs) {
            Holiday holiday = new Holiday();
            holiday.setDay(dto.getDay());
            holiday.setDate(dto.getDate());
            holiday.setHoliday(dto.getHoliday());
            holiday.setState(dto.getState());
            holiday.setYear(dto.getYear());
            holidays.add(holiday);
        }
        List<Holiday> created = holidayLogic.createHoliday(holidays);
        return toDTOList(created);
    }

    @GET
    @Path("find/{state}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<HolidayDTO> getHolidayByYear(@PathParam("state") String state) {
        List<Holiday> holidays = holidayLogic.findByState(state);
        return toDTOList(holidays);
    }
    
    
    
    @GET
    @Path("find/{state}/range")
    @Produces(MediaType.APPLICATION_JSON)
    public List<HolidayDTO> getHolidaysByStateAndRange(@PathParam("state") String state,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate) {
        List<Holiday> holidays = holidayLogic.findByStateAndRange(
                state, LocalDate.parse(startDate), LocalDate.parse(endDate));
        return toDTOList(holidays);
    }

    private List<HolidayDTO> toDTOList(List<Holiday> holidays) {
        List<HolidayDTO> dtos = new ArrayList<>();
        for (Holiday holiday : holidays) {
            dtos.add(toDTO(holiday));
        }
        return dtos;
    }

    

    private HolidayDTO toDTO(Holiday holiday) {
        HolidayDTO dto = new HolidayDTO();
        dto.setUuid(holiday.getUuid());
        dto.setJpaVersion(holiday.getJpaVersion());
        dto.setDay(holiday.getDay());
        dto.setDate(holiday.getDate());
        dto.setHoliday(holiday.getHoliday());
        dto.setState(holiday.getState());
        dto.setYear(holiday.getYear());
        return dto;
    }

}
