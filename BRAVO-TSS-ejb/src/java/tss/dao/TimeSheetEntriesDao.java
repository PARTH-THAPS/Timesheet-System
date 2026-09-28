package tss.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tss.entity.TimesheetEntry;

@Stateless
public class TimeSheetEntriesDao {
    
    @PersistenceContext(unitName = "BRAVO-TSS-ejbPU")
    private EntityManager em;
    
    public void addTimeSheetEntries(TimesheetEntry timesheetEntries)
    {
     em.persist(timesheetEntries);
    }
    
    public double sumVacationHoursForContract(Long contractId, Long excludingEntryId) {
        String jpql = "SELECT COALESCE(SUM(e.hours), 0.0) "
                + "FROM TimesheetEntry e "
                + "WHERE e.timesheet.contract.id = :contractId "
                + "AND e.type = tss.entity.ReportType.VACATION"
                + (excludingEntryId != null ? " AND e.id <> :excludingEntryId" : "");

        var query = em.createQuery(jpql, Double.class)
                .setParameter("contractId", contractId);
        if (excludingEntryId != null) {
            query.setParameter("excludingEntryId", excludingEntryId);
        }
        return query.getSingleResult();
    }
    
}
