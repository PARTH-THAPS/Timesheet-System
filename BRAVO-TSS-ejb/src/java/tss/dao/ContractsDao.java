package tss.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import tss.entity.Contract;
import tss.entity.ContractStatus;

@Stateless
public class ContractsDao {

    @PersistenceContext(unitName = "BRAVO-TSS-ejbPU")
    private EntityManager em;

    public void createContract(Contract contract) {
        em.persist(contract);
    }

    public Contract findContract(long id) {
        return em.find(Contract.class, id);
    }

    public Contract UpdateContract(Contract contract) {
        return em.merge(contract);
    }

    public void deleteContract(Contract contract) {
        em.remove(contract);
    }

    public List<Contract> findAllContracts() {
        return em.createNamedQuery("getAllContracts", Contract.class).getResultList();
    }

    public List<Contract> findAllArchivedContracts() {
        return em.createQuery(
                "SELECT c FROM Contract c WHERE c.status = :status",
                Contract.class
        )
                .setParameter("status", ContractStatus.ARCHIVED)
                .getResultList();
    }

    public List<Contract> findAllArchivedContractsForSupervisor(long id) {
        return em.createQuery(
                "SELECT c FROM Contract c WHERE c.status = :status AND c.supervisor.id = :supId",
                Contract.class
        )
                .setParameter("status", ContractStatus.ARCHIVED)
                .setParameter("supId", id)
                .getResultList();
    }

    public long countContractsByEmployee(Long personId) {
        return em.createQuery(
                "SELECT COUNT(c) FROM Contract c WHERE c.employee.id = :id", Long.class)
                .setParameter("id", personId)
                .getSingleResult();
    }
}
