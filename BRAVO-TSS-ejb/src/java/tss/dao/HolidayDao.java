package tss.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import tss.entity.FederalState;
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

    public List<Holiday> findByState(FederalState state) {
        return em.createNamedQuery("getHolidayByState", Holiday.class)
                .setParameter("State", state)
                .getResultList();
    }

    public List<Holiday> findByStateAndDateRange(FederalState state, LocalDate startDate, LocalDate endDate) {
        return em.createNamedQuery("getHolidayByStateAndDateRange", Holiday.class)
                .setParameter("State", state)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<Holiday> findAllHoliday() {
        return em.createQuery("SELECT h FROM Holiday h", Holiday.class).getResultList();
    }

    public Holiday findById(Long id) {
        return em.find(Holiday.class, id);
    }

    public Holiday updateHoliday(Holiday holiday) {
        return em.merge(holiday);
    }

    public void deleteHoliday(Holiday holiday) {
        Holiday managed = em.contains(holiday) ? holiday : em.merge(holiday);
        em.remove(managed);
    }
}
