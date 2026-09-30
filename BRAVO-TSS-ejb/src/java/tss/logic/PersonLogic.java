package tss.logic;

import jakarta.ejb.Remote;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import tss.dto.PersonDTO;
import tss.entity.Role;

/**
 * Remote interface managing the business logic for Persons (users).
 */
@Remote
public interface PersonLogic {

    /**
     * Creates a new person/user in the system.
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param emailAddress The email address (used as login).
     * @param consent Whether the user has provided consent.
     * @param password The user's password.
     * @param role The set of roles assigned to the user.
     * @return The newly created person.
     */
    PersonDTO createPerson(String firstName, String lastName, String emailAddress, boolean consent, String password, Set<Role> role);

    /**
     * Finds a person by their ID.
     * @param id The ID of the person.
     * @return The person details, or null if not found.
     */
    PersonDTO findPerson(Long id);

    /**
     * Deletes a person from the system.
     * @param person The person data to delete.
     */
    void deletePerson(PersonDTO person);

    /**
     * Updates the information of an existing person.
     * @param person The updated person data.
     * @return The updated person.
     */
    PersonDTO updatePerson(PersonDTO person);

    /**
     * Retrieves a list of all persons in the system.
     * @return A list of all persons.
     */
    List<PersonDTO> findAllPersons();

    /**
     * Updates the date of birth for a specific person.
     * @param personId The ID of the person.
     * @param dob The new date of birth.
     */
    void updateDateOfBirth(Long personId, LocalDate dob);

    /**
     * Flags the consent as accepted for a specific person.
     * @param personId The ID of the person.
     */
    void acceptConsent(Long personId);

    /**
     * Revokes the consent for a specific person.
     * @param personId The ID of the person.
     */
    void revokeConsent(Long personId);

    /**
     * Changes a person's password after verifying the current password.
     * @param personId The ID of the person.
     * @param currentPassword The user's current password.
     * @param newPassword The desired new password.
     */
    void changePassword(Long personId, String currentPassword, String newPassword);
}
