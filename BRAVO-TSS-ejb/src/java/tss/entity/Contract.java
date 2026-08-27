package tss.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.*;

@Entity
@NamedQueries({@NamedQuery( name ="getAllContracts",query="SELECT c FROM Contract c")})
public class Contract extends AbstractEntity {
 
    @Enumerated(EnumType.STRING)
    private ContractStatus status; 
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate terminationDate;
    private double hoursPerWeek;
    @Enumerated(EnumType.STRING)
    private TimesheetFrequency frequency;
    private double vacationHours;
    private double hoursDue;
    private int workingDaysPerWeek;
    private int vacationDaysPerYear;
    @Enumerated(EnumType.STRING)
    private FederalState state;
       
    @ManyToOne
    @JoinColumn(name = "EMPLOYEE_ID")
    private Person employee;

    @ManyToOne
    @JoinColumn(name = "SUPERVISOR_ID")
    private Person supervisor;

    @ManyToMany
    private Set<Person> secretaries;

    @ManyToMany
    private Set<Person> assistants;

    @OneToMany(mappedBy = "contract")
    private List<Timesheet> timesheet;

    public List<Timesheet> getTimesheet() {
        return timesheet;
    }

    public void setTimesheet(List<Timesheet> timesheet) {
        this.timesheet = timesheet;
    }
    

    public double getVacationHours() {
        return vacationHours;
    }

    public void setVacationHours(double vacationHours) {
        this.vacationHours = vacationHours;
    }

    public Contract()
    {
        this.secretaries = new HashSet<>();
        this.assistants =  new HashSet<>();
        this.timesheet = new ArrayList<>();
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public Person getEmployee() {
        return employee;
    }

    public void setEmployee(Person person) {
        this.employee = person;
    }
    
    public Person getSupervisor() { return supervisor; }

    public void setSupervisor(Person supervisor) { this.supervisor = supervisor; }

    public void addSecretary(Collection<Person> persons) { this.secretaries.addAll(persons); }

    public void removeSecretary(Person... persons) {
        Set<Person> toRemove = new HashSet<>(Arrays.asList(persons));
        this.secretaries.removeAll(toRemove);
    }

    public Set<Person> getSecretaries() {
        return Collections.unmodifiableSet(this.secretaries);
    }

    public void addAssistant(Person... persons) { this.assistants.addAll(Arrays.asList(persons)); }

    public void removeAssistant(Person... persons) {
        Set<Person> toRemove = new HashSet<>(Arrays.asList(persons));
        this.assistants.removeAll(toRemove);
    }

    public Set<Person> getAssistants() {
        return Collections.unmodifiableSet(this.assistants);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public double getHoursPerWeek() {
        return hoursPerWeek;
    }

    public void setHoursPerWeek(double hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }

    public TimesheetFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(TimesheetFrequency frequency) {
        this.frequency = frequency;
    }

    public double getHoursDue() {
        return hoursDue;
    }

    public void setHoursDue(double hoursDue) {
        this.hoursDue = hoursDue;
    }

    public int getWorkingDaysPerWeek() {
        return workingDaysPerWeek;
    }

    public void setWorkingDaysPerWeek(int workingDaysPerWeek) {
        this.workingDaysPerWeek = workingDaysPerWeek;
    }

    public int getVacationDaysPerYear() {
        return vacationDaysPerYear;
    }

    public void setVacationDaysPerYear(int vacationDaysPerYear) {
        this.vacationDaysPerYear = vacationDaysPerYear;
    }
    
    public FederalState getState() {
        return state;
    }

    public void setState(FederalState state) {
        this.state = state;
    }
    
}
