package tss.dto;

import java.time.LocalDate;
import tss.entity.FederalState;

public class HolidayDTO extends AbstractDTO {

    private String day;
    private LocalDate date;
    private String holiday;
    private FederalState state;
    private int year;
    private Long id;

    public HolidayDTO() {
    }

    public HolidayDTO(String day, LocalDate date, String holiday, FederalState state, int year) {
        this.day = day;
        this.date = date;
        this.holiday = holiday;
        this.state = state;
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getHoliday() {
        return holiday;
    }

    public void setHoliday(String holiday) {
        this.holiday = holiday;
    }

    public FederalState getState() {
        return state;
    }

    public void setState(FederalState state) {
        this.state = state;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
