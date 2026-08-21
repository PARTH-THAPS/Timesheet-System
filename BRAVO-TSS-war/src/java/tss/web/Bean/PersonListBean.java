package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import tss.dto.PersonDTO;
import tss.entity.Person;
import tss.logic.PersonLogic;

@Named("PersonListBean")
@RequestScoped
public class PersonListBean implements Serializable {

    @EJB
    private PersonLogic personLogic;
//    private List<Person> persons;
    
    private List <PersonDTO> persons;

    @PostConstruct
    public void init() {
        persons = personLogic.findAllPersons();
        System.out.println("Persons found: " + (persons == null ? "null" : persons.size()));
    }

    public List<PersonDTO> getPersons() {
        return persons;
    }
}
