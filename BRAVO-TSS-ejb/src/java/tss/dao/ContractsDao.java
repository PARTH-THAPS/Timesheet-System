
package tss.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import tss.entity.Contract;

@Stateless
public class ContractsDao {
    
    @PersistenceContext(unitName = "BRAVO-TSS-ejbPU")
    private EntityManager em;
    
    public void createContract(Contract contract)
    {
      em.persist(contract);
    }
    
    public Contract findContract(long id)
    {
    return em.find(Contract.class,id);
    }
    
    public Contract UpdateContract(Contract contract)
    {
          return em.merge(contract);
    }
    
    public void deleteContract(Contract contract)
    {
      em.remove(contract);
    }
    
    public List<Contract> findAllContracts() {
        return em.createNamedQuery("getAllContracts", Contract.class).getResultList();
    }       
}
