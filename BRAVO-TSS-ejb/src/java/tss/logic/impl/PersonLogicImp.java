package tss.logic.impl;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import tss.dao.PersonDao;
import tss.dto.PersonDTO;
import tss.entity.Person;
import tss.entity.Role;
import tss.logic.PersonLogic;
import tss.util.PasswordHash;

/**
 * Implementation of the {@link PersonLogic} interface.
 * Provides services for creating, finding, updating, and deleting users (Persons) in the system.
 */
@Stateless
public class PersonLogicImp implements PersonLogic {

    @EJB
    private PersonDao personDao;

    /**
     * Creates a new Person (user) in the system.
     *
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param emailAddress The user's email address (login).
     * @param consent Whether the user has provided data consent.
     * @param password The plaintext password to be hashed.
     * @param role The roles assigned to the user.
     * @return A PersonDTO representing the created user.
     */
    @Override
    public PersonDTO createPerson(String firstName, String lastName, String emailAddress, boolean consent, String password, Set<Role> role) {
        if (role == null || role.isEmpty()) {
            throw new IllegalArgumentException("At least one role must be provided");
        }
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmailAddress(emailAddress);
        person.setPassword(PasswordHash.hashPassword(password));
        person.setConsent(consent);
        person.setRoles(role);
        personDao.createPerson(person);
        return personDto(person);
    }

    /**
     * Finds a specific Person by their ID.
     *
     * @param id The ID of the Person.
     * @return The corresponding PersonDTO.
     */
    @Override
    public PersonDTO findPerson(Long id) {

        Person person = personDao.findPersonById(id);

        if (person == null) {
            throw new IllegalArgumentException(
                    "No Person found with id: " + id
            );
        }

        return personDto(person);
    }

    /**
     * Updates an existing Person's details.
     *
     * @param dto The PersonDTO containing the updated information.
     * @return The updated PersonDTO.
     */
    @Override
    public PersonDTO updatePerson(PersonDTO dto) {
        Person person = personDao.findPersonById(dto.getId());

        if (dto.getRole() == null || dto.getRole().isEmpty()) {
            throw new IllegalArgumentException("At least one role must be provided");
        }

        if (person == null) {
            throw new IllegalArgumentException("No Person found with id: " + dto.getId());
        }

        validateDateOfBirth(dto.getDateOfBirth());

        person.setFirstName(dto.getFirstName());
        person.setLastName(dto.getLastName());
        person.setEmailAddress(dto.getEmailAddress());
        person.setDateOfBirth(dto.getDateOfBirth());
        person.setRoles(dto.getRole());
        person.setPreferredLanguage(dto.getPreferredLanguage());
        return personDto(personDao.updatePerson(person));
    }

    /**
     * Deletes a Person from the system.
     *
     * @param dto The PersonDTO identifying the user to delete.
     */
    @Override
    public void deletePerson(PersonDTO dto) {
        Person person = personDao.findPersonById(dto.getId());
        if (person == null) {
            throw new IllegalArgumentException("No Person found with id: " + dto.getId());
        }
        personDao.deletePerson(person);
    }

    /**
     * Retrieves all registered Persons in the system.
     *
     * @return A list of PersonDTOs.
     */
    @Override
    public List<PersonDTO> findAllPersons() {
        List<Person> persons = personDao.findAllPersons();
        return toDtoList(persons);
    }

    private List<PersonDTO> toDtoList(List<Person> persons) {
        List<PersonDTO> dtoList = new ArrayList<>();

        for (Person p : persons) {
            dtoList.add(personDto(p));
        }

        return dtoList;
    }

    /**
     * Updates the date of birth for a given Person.
     *
     * @param personId The ID of the Person.
     * @param dob The new date of birth.
     */
    @Override
    public void updateDateOfBirth(Long personId, LocalDate dob) {
        Person person = personDao.findPersonById(personId);
        if (person == null) {
            throw new IllegalArgumentException("No Person found with id: " + personId);
        }

        validateDateOfBirth(dob);

        person.setDateOfBirth(dob);
        personDao.updatePerson(person);
    }

    /**
     * Flags the consent status as true for a given Person.
     *
     * @param personId The ID of the Person.
     */
    @Override
    public void acceptConsent(Long personId) {
        Person person = personDao.findPersonById(personId);

        if (person == null) {
            throw new IllegalArgumentException(
                    "No Person found with id: " + personId
            );
        }

        person.setConsent(true);
        personDao.updatePerson(person);
    }

    /**
     * Flags the consent status as false for a given Person.
     *
     * @param personId The ID of the Person.
     */
    @Override
    public void revokeConsent(Long personId) {
        Person person = personDao.findPersonById(personId);

        if (person == null) {
            throw new IllegalArgumentException(
                    "No Person found with id: " + personId
            );
        }

        person.setConsent(false);
        personDao.updatePerson(person);
    }

    /**
     * Changes the user's password after verifying the current password.
     *
     * @param personId The ID of the Person.
     * @param currentPassword The user's current plaintext password.
     * @param newPassword The new plaintext password.
     */
    @Override
    public void changePassword(
            Long personId,
            String currentPassword,
            String newPassword
    ) {
        Person person = personDao.findPersonById(personId);

        if (person == null) {
            throw new IllegalArgumentException(
                    "No Person found with id: " + personId
            );
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Current password is required."
            );
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "New password is required."
            );
        }

        String currentPasswordHash
                = PasswordHash.hashPassword(currentPassword);

        if (!currentPasswordHash.equals(person.getPassword())) {
            throw new IllegalArgumentException(
                    "Current password is incorrect."
            );
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "New password must contain at least 8 characters."
            );
        }

        person.setPassword(
                PasswordHash.hashPassword(newPassword)
        );

        personDao.updatePerson(person);
    }

    private PersonDTO personDto(Person p) {
        PersonDTO dto = new PersonDTO();
        dto.setId(p.getId());
        dto.setUuid(p.getUuid());
        dto.setFirstName(p.getFirstName());
        dto.setLastName(p.getLastName());
        dto.setEmailAddress(p.getEmailAddress());
        dto.setConsent(p.isConsent());
        dto.setRole(p.getRole());
        dto.setPreferredLanguage(p.getPreferredLanguage());
        dto.setDateOfBirth(p.getDateOfBirth());
        return dto;
    }

    private void validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth != null
                && dateOfBirth.isAfter(
                        LocalDate.now()
                )) {

            throw new IllegalArgumentException(
                    "Date of birth cannot be in the future."
            );
        }
    }
}
