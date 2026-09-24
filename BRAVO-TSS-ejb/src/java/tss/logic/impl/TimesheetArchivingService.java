package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import tss.logic.TimesheetLogic;

@Singleton
@Startup
public class TimesheetArchivingService {

    @EJB
    private TimesheetLogic timesheetLogic;

    @Schedule(hour = "3", minute = "0", persistent = false)
    public void scheduledArchive() {
        try {
            int removed = timesheetLogic.archiveOldRecords();
            System.out.println("Timesheet archiving cleanup removed " + removed + " records.");
        } catch (Exception e) {
            System.err.println("Error running timesheet archiving: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public int runNow() {
        return timesheetLogic.archiveOldRecords();
    }
}

