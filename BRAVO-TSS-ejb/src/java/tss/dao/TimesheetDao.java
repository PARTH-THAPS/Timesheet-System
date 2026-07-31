package tss.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Contains all methods concerning Timesheet and TimesheetEntry
 */
@Stateless
public class TimesheetDao {
    
    @PersistenceContext(unitName = "BRAVO-TSS-ejbPU")
    private EntityManager em;
    
    /**
     * Only gets called when creating a contract
     */
    public void createTimesheet() {
        
    }
    
    public void getTimesheet() {
        
    }
    
    public void addEntry() {
        
    }
    
    public void removeEntry() {
        
    }
}
