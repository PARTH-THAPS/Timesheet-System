package tss.dto;

import java.time.LocalDate;

public class HolidayDTO extends AbstractDTO {

    private String day;
    private LocalDate date;
    private String holiday;
    private String state;
    private int year;

    public HolidayDTO() {
    }

    public HolidayDTO(String day, LocalDate date, String holiday, String state, int year) {
        this.day = day;
        this.date = date;
        this.holiday = holiday;
        this.state = state;
        this.year = year;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
