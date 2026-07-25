package tss.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import tss.entity.Person;

@Stateless
public class PersonDao {

    @PersistenceContext(unitName = "BRAVO-TSS-ejbPU")
    private EntityManager em;

    public void createPerson(Person person) {
        em.persist(person);
    }
    
    public Person getPerson(String emailAddress)
    {
     try{
     return em.createNamedQuery("getUserByEmail", Person.class)
                    .setParameter("emailAddress", emailAddress)
                    .getSingleResult();
     }
     catch (NoResultException e)
     {
     return null;
     }
    }

    public Person findPerson(Long id) {
        return em.find(Person.class, id);
    }
}