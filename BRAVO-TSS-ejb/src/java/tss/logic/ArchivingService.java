package tss.logic;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import tss.dao.TimesheetDao;

import java.time.LocalDateTime;

@Singleton
@Startup
public class ArchivingService {
    @EJB
    private TimesheetDao timesheetDAO;

    @Schedule(hour = "1", minute = "0", second = "0", persistent = false)
    public void archiveOldData() {
        System.out.println("Starting daily archival process...");

        int archivedCount = timesheetDAO.deleteArchiveOldRecords();

        System.out.println("Archival complete. Items moved: " + archivedCount);
    }
}