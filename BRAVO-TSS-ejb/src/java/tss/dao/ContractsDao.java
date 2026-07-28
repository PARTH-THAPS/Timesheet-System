/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tss.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tss.entity.Contract;

/**
 *
 * @author parth
 */
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
    
    public void deleteContract(long id)
    {
      Contract contract=em.find(Contract.class, id);
      if(contract!=null)
      {
      em.remove(contract);
      }
    }
    
    
    
    
    
    
}
