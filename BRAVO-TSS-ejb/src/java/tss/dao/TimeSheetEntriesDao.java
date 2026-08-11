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
    
}
