package tss.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
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
        try {
            return em.createNamedQuery("getHolidayByState", Holiday.class).setParameter("State", state).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}
