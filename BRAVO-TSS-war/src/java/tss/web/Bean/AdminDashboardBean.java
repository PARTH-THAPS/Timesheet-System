package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tss.dto.PersonDTO;
import tss.entity.Role;
import tss.logic.PersonLogic;

@Named
@RequestScoped
public class AdminDashboardBean implements Serializable {

   private static final long serialVersionUID = 1L;

   @EJB
   private PersonLogic personLogic;

   private List<PersonDTO> allPersons;
   private Map<String, Long> roleCounts;

   public int getTotalUsers() {
       loadPersonsIfNeeded();
       return allPersons.size();
   }

   public Map<String, Long> getRoleCounts() {
       loadPersonsIfNeeded();
       return roleCounts;
   }

   public long getConsentedUserCount() {
       loadPersonsIfNeeded();

       return allPersons.stream()
               .filter(PersonDTO::isConsent)
               .count();
   }

   public int getActiveContractCount() {
       return 0;
   }

   public int getTotalContractCount() {
       return 0;
   }

   private void loadPersonsIfNeeded() {

       if (allPersons == null) {

           allPersons = personLogic.findAllPersons();

           roleCounts = new HashMap<>();

           for (PersonDTO p : allPersons) {

               if (p.getRole() != null) {

                   for (Role r : p.getRole()) {

                       roleCounts.merge(
                               r.name(),
                               1L,
                               Long::sum
                       );
                   }
               }
           }
       }
   }
}