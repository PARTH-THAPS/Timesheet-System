package tss.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import tss.entity.Person;
import tss.entity.Role;

@Stateless
public class PersonDao {

    @PersistenceContext(unitName = "BRAVO-TSS-ejbPU")
    private EntityManager em;

    public void createPerson(Person person) {
        em.persist(person);
    }

    public Person getPerson(String emailAddress) {
        try {
            return em.createNamedQuery("getUserByEmail", Person.class)
                    .setParameter("emailAddress", emailAddress)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Person findPersonById(Long id) {
        return em.find(Person.class, id);
    }

    public void deletePerson(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Person must not be null");
        }

        Person p = em.find(Person.class, person.getId());

        if (p != null) {
            em.remove(p);
        }
    }

    public Person updatePerson(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Person must not be null");
        }

        return em.merge(person);
    }

    public List<Person> findAllPersons() {
        return em.createQuery("SELECT p FROM Person p", Person.class)
                .getResultList();
    }
    
    
    public Set<Role> getRole(String emailAddress)
    {
    
     List<Role> roles = em.createQuery(
            "SELECT r FROM Person p JOIN p.roles r WHERE p.emailAddress = :emailAddress",
            Role.class
        )
        .setParameter("emailAddress", emailAddress)
        .getResultList();

    return new HashSet<>(roles);

    
    }
}
