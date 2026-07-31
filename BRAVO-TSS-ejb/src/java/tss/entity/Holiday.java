package tss.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import java.time.LocalDate;

@Entity
@NamedQueries({
    @NamedQuery(name = "getHolidayByState", query = "SELECT h from Holiday h WHERE h.State=:State")})
public class Holiday extends AbstractEntity {

    private String Day;
    private LocalDate Date;
    private String Holiday;
    private String State;
    private int Year;

    public Holiday() {
    }

    public int getYear() {
        return Year;
    }

    public void setYear(int Year) {
        this.Year = Year;
    }

    public String getDay() {
        return Day;
    }

    public void setDay(String Day) {
        this.Day = Day;
    }

    public LocalDate getDate() {
        return Date;
    }

    public void setDate(LocalDate Date) {
        this.Date = Date;
    }

    public String getHoliday() {
        return Holiday;
    }

    public void setHoliday(String Holiday) {
        this.Holiday = Holiday;
    }

    public String getState() {
        return State;
    }

    public void setState(String State) {
        this.State = State;
    }
}
