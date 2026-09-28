package tss.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;

import tss.entity.Contract;
import tss.entity.Timesheet;
import tss.entity.TimesheetStatus;

@Stateless
public class TimesheetDao {

    @PersistenceContext(unitName = "BRAVO-TSS-ejbPU")
    private EntityManager em;

    /**
     * Only gets called when creating a contract
     */
    public void createTimesheet(Timesheet timesheet) {
        if (timesheet == null) {
            throw new IllegalArgumentException("timesheet must not be null");
        }
        em.persist(timesheet);
    }

    public Timesheet updateTimesheet(Timesheet timesheet) {
        if (timesheet == null) {
            throw new IllegalArgumentException("timesheet must not be null");
        }
        return em.merge(timesheet);
    }

    public void deleteTimesheet(Timesheet timesheet) {
        if (timesheet == null) {
            throw new IllegalArgumentException("timesheet must not be null");
        }
        Timesheet managed = em.find(Timesheet.class, timesheet.getId());
        if (managed != null) {
            em.remove(managed);
        }
    }

    public Timesheet findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        Timesheet ts = em.find(Timesheet.class, id);
        if (ts == null) {
            throw new NoResultException("No Timesheet found with id " + id);
        }
        return ts;
    }

    public void deleteById(Long id) {
        Timesheet ts = findById(id);
        em.remove(ts);
    }

    public List<Timesheet> findByContract(Contract contract) {
        try {
            return em.createQuery(
                            "SELECT t FROM Timesheet t WHERE t.contract = :contract ORDER BY t.startDate",
                            Timesheet.class)
                    .setParameter("contract", contract)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Timesheet> findByContractAndStatus(Contract contract, TimesheetStatus status) {
        try {
            return em.createQuery(
                            "SELECT t FROM Timesheet t WHERE t.contract = :contract AND t.status = :status ORDER BY t.startDate",
                            Timesheet.class)
                    .setParameter("contract", contract)
                    .setParameter("status", status)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Timesheet> findByStatus(TimesheetStatus status) {
        try {
            return em.createQuery(
                            "SELECT t FROM Timesheet t WHERE t.status = :status ORDER BY t.startDate",
                            Timesheet.class)
                    .setParameter("status", status)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Timesheet> findByEmployeeUsername(String emailAddress) {
        return em.createQuery(
                        "SELECT DISTINCT t FROM Timesheet t " +
                                "LEFT JOIN FETCH t.entries " +
                                "JOIN t.contract c " +
                                "JOIN c.employee p " +
                                "WHERE p.emailAddress = :email " +
                                "ORDER BY t.startDate",
                        Timesheet.class)
                .setParameter("email", emailAddress)
                .getResultList();
    }

    public List<Timesheet> findPendingArchivesForSecretary(String emailAddress) {
        return em.createQuery(
                        "SELECT DISTINCT t FROM Timesheet              t " +
                                "JOIN t.contract c " +
                                "JOIN c.secretaries s " +
                                "WHERE s.emailAddress = :emailAddress " +
                                "AND t.status = tss.entity.TimesheetStatus.SIGNED_BY_SUPERVISOR " +
                                "ORDER BY t.startDate",
                        Timesheet.class)
                .setParameter("emailAddress", emailAddress)
                .getResultList();
    }

    public List<Timesheet> findInProgressOnLastDay() {
        return em.createQuery(
                        "SELECT t FROM Timesheet t WHERE t.endDate = CURRENT_DATE ORDER BY t.startDate",
                        Timesheet.class)
                .getResultList();
    }

    public List<Timesheet> findByStatusOnLastDay(TimesheetStatus status) {
        try {
            return em.createQuery(
                            "SELECT t FROM Timesheet t WHERE t.status = :status AND t.endDate = CURRENT_DATE ORDER BY t.startDate",
                            Timesheet.class)
                    .setParameter("status", status)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public int deleteArchiveOldRecords() {
        return em.createQuery(
                        "DELETE FROM Timesheet t " +
                                "WHERE t.status = :status " +
                                "AND t.endDate < :cutoffDate")
                .setParameter("status", TimesheetStatus.ARCHIVED)
                .setParameter("cutoffDate", LocalDateTime.now().minusYears(2).toLocalDate())
                .executeUpdate();
    }
    
    public List<Timesheet> findPendingSignaturesForSupervisor(String emailAddress) {
        return em.createQuery(
                "SELECT DISTINCT t "
                + "FROM Timesheet t "
                + "JOIN FETCH t.contract c "
                + "JOIN FETCH c.employee e "
                + "JOIN c.supervisor s "
                + "WHERE s.emailAddress = :emailAddress "
                + "AND t.status = "
                + "tss.entity.TimesheetStatus.SIGNED_BY_EMPLOYEE "
                + "ORDER BY t.startDate",
                Timesheet.class
        )
        .setParameter("emailAddress", emailAddress)
        .getResultList();
    }
    
    public List<Timesheet> findByEmployeeId(Long personId) {
        return em.createQuery(
                "SELECT DISTINCT t "
                + "FROM Timesheet t "
                + "LEFT JOIN FETCH t.entries "
                + "JOIN t.contract c "
                + "JOIN c.employee p "
                + "WHERE p.id = :personId "
                + "ORDER BY t.startDate",
                Timesheet.class
        )
        .setParameter("personId", personId)
        .getResultList();
    }

    public List<Timesheet> findBySupervisorId(Long supervisorId) {
        return em.createQuery(
                "SELECT DISTINCT t "
                + "FROM Timesheet t "
                + "JOIN FETCH t.contract c "
                + "JOIN FETCH c.employee e "
                + "WHERE c.supervisor.id = :supervisorId "
                + "ORDER BY t.startDate DESC",
                Timesheet.class
        )
        .setParameter("supervisorId", supervisorId)
        .getResultList();
    }

    public List<Timesheet> findByAssistantId(Long assistantId) {
        return em.createQuery(
                "SELECT DISTINCT t "
                + "FROM Timesheet t "
                + "JOIN FETCH t.contract c "
                + "JOIN FETCH c.employee e "
                + "JOIN c.assistants a "
                + "WHERE a.id = :assistantId "
                + "ORDER BY t.startDate DESC",
                Timesheet.class
        )
        .setParameter("assistantId", assistantId)
        .getResultList();
    }

    public List<Timesheet> findBySecretaryId(Long secretaryId) {
        return em.createQuery(
                "SELECT DISTINCT t "
                + "FROM Timesheet t "
                + "JOIN FETCH t.contract c "
                + "JOIN FETCH c.employee e "
                + "JOIN c.secretaries s "
                + "WHERE s.id = :secretaryId "
                + "ORDER BY t.startDate DESC",
                Timesheet.class
        )
        .setParameter("secretaryId", secretaryId)
        .getResultList();
    }

    public Timesheet findByIdForSupervisorId(Long timesheetId, Long supervisorId) {
        try {
            return em.createQuery(
                    "SELECT DISTINCT t "
                    + "FROM Timesheet t "
                    + "LEFT JOIN FETCH t.entries "
                    + "JOIN FETCH t.contract c "
                    + "JOIN FETCH c.employee e "
                    + "WHERE t.id = :timesheetId "
                    + "AND c.supervisor.id = :supervisorId",
                    Timesheet.class
            )
            .setParameter("timesheetId", timesheetId)
            .setParameter("supervisorId", supervisorId)
            .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Timesheet findByIdForAssistantId(Long timesheetId, Long assistantId) {
        try {
            return em.createQuery(
                    "SELECT DISTINCT t "
                    + "FROM Timesheet t "
                    + "LEFT JOIN FETCH t.entries "
                    + "JOIN FETCH t.contract c "
                    + "JOIN FETCH c.employee e "
                    + "JOIN c.assistants a "
                    + "WHERE t.id = :timesheetId "
                    + "AND a.id = :assistantId",
                    Timesheet.class
            )
            .setParameter("timesheetId", timesheetId)
            .setParameter("assistantId", assistantId)
            .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Timesheet findByIdForSecretaryId(Long timesheetId, Long secretaryId) {
        try {
            return em.createQuery(
                    "SELECT DISTINCT t "
                    + "FROM Timesheet t "
                    + "LEFT JOIN FETCH t.entries "
                    + "JOIN FETCH t.contract c "
                    + "JOIN FETCH c.employee e "
                    + "JOIN c.secretaries s "
                    + "WHERE t.id = :timesheetId "
                    + "AND s.id = :secretaryId",
                    Timesheet.class
            )
            .setParameter("timesheetId", timesheetId)
            .setParameter("secretaryId", secretaryId)
            .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
   
    public Timesheet findByIdWithEntries(Long id) {
    if (id == null) {
        throw new IllegalArgumentException(
                "id must not be null"
        );
    }

    return em.createQuery(
            "SELECT DISTINCT t "
            + "FROM Timesheet t "
            + "LEFT JOIN FETCH t.entries "
            + "WHERE t.id = :id",
            Timesheet.class
    )
    .setParameter("id", id)
    .getSingleResult();
}
}

