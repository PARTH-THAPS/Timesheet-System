package tss.dto;

public class ContractStatisticsDTO extends AbstractDTO {

    private static final long serialVersionUID = 1L;

    private double totalWorkingHours;
    private double totalHoursWorked;
    private double totalVacationHours;
    private double totalVacationHoursLeft;
    private double totalHoursDue;
    private double balance;

    public double getTotalWorkingHours() {
        return totalWorkingHours;
    }

    public void setTotalWorkingHours(double totalWorkingHours) {
        this.totalWorkingHours = totalWorkingHours;
    }

    public double getTotalHoursWorked() {
        return totalHoursWorked;
    }

    public void setTotalHoursWorked(double totalHoursWorked) {
        this.totalHoursWorked = totalHoursWorked;
    }

    public double getTotalVacationHours() {
        return totalVacationHours;
    }

    public void setTotalVacationHours(double totalVacationHours) {
        this.totalVacationHours = totalVacationHours;
    }

    public double getTotalVacationHoursLeft() {
        return totalVacationHoursLeft;
    }

    public void setTotalVacationHoursLeft(double totalVacationHoursLeft) {
        this.totalVacationHoursLeft = totalVacationHoursLeft;
    }

    public double getTotalHoursDue() {
        return totalHoursDue;
    }

    public void setTotalHoursDue(double totalHoursDue) {
        this.totalHoursDue = totalHoursDue;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}