package tss.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import tss.entity.Holiday;

@Stateless
public class HolidayDao {
    @PersistenceContext(unitName = "BRAVO-TSS-ejbPU")
    private EntityManager em;

    public List<Holiday> createHoliday(List<Holiday> holidays) {
        for (Holiday holiday : holidays) {
            em.persist(holiday);
        }
        return holidays;
    }

    public List<Holiday> findByState(String state) {
        return em.createNamedQuery("getHolidayByState", Holiday.class)
                 .setParameter("State", state)
                 .getResultList();
    }

    public List<Holiday> findByStateAndDateRange(String state, LocalDate startDate, LocalDate endDate) {
        return em.createNamedQuery("getHolidayByStateAndDateRange", Holiday.class)
                 .setParameter("State", state)
                 .setParameter("startDate", startDate)
                 .setParameter("endDate", endDate)
                 .getResultList();
    }
}