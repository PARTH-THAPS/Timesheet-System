package tss.web.Bean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Locale;

import tss.dto.PersonDTO;
import tss.dto.User;
import tss.entity.Language;
import tss.logic.PersonLogic;

@Named
@SessionScoped
public class LocaleBean implements Serializable {

    private static final long serialVersionUID = 516756595421760915L;

    private static final List<LanguageOption> LANGUAGES = List.of(
            new LanguageOption("en", "EN", "English"),
            new LanguageOption("de", "DE", "Deutsch"),
            new LanguageOption("fr", "FR", "Français"),
            new LanguageOption("pl", "PL", "Polski")
    );

    @EJB
    private PersonLogic personLogic;

    @Inject
    private loginBean loginBean;

    private Locale userLocale;

    private String initializedPrincipalName;

    public Locale getUserLocale() {

        synchronizeWithLoggedInUser();

        if (userLocale == null) {

            FacesContext context
                    = FacesContext.getCurrentInstance();

            if (context != null) {
                userLocale = context
                        .getExternalContext()
                        .getRequestLocale();
            }
        }

        if (userLocale == null) {

            FacesContext context
                    = FacesContext.getCurrentInstance();

            if (context != null) {
                userLocale = context
                        .getApplication()
                        .getDefaultLocale();
            }
        }

        if (userLocale == null) {
            userLocale = Locale.ENGLISH;
        }

        return userLocale;
    }

    public void setUserLocale(Locale userLocale) {
        this.userLocale = userLocale;
        updateCurrentViewLocale();
    }

    public List<LanguageOption> getLanguages() {
        return LANGUAGES;
    }

    public String getSelectedLanguage() {
        return getUserLocale().getLanguage();
    }

    public void setSelectedLanguage(String languageCode) {

        if (languageCode == null
                || languageCode.isBlank()) {
            return;
        }

        Language language
                = languageEnumForCode(languageCode);

        if (language == null) {
            return;
        }

        applyPreferredLanguage(language);

        persistPreferredLanguage(language);
    }

    public void applyPreferredLanguage(Language language) {

        if (language == null) {
            return;
        }

        userLocale = localeForLanguage(language);

        updateCurrentViewLocale();
    }

    private void synchronizeWithLoggedInUser() {

        FacesContext context = FacesContext.getCurrentInstance();

        if (context == null) {
            return;
        }

        Principal principal = context
                .getExternalContext()
                .getUserPrincipal();

        if (principal == null) {
            return;
        }

        String principalName = principal.getName();

        if (principalName.equals(initializedPrincipalName)) {
            return;
        }

        try {
            User user = loginBean.getUser();

            if (user == null || user.getId() == null) {
                return;
            }

            PersonDTO person
                    = personLogic.findPerson(user.getId());

            if (person != null
                    && person.getPreferredLanguage() != null) {

                userLocale
                        = localeForLanguage(
                                person.getPreferredLanguage()
                        );

                updateCurrentViewLocale();
            }

            initializedPrincipalName = principalName;

        } catch (Exception e) {
            System.err.println(
                    "Could not load preferred locale for "
                    + principalName
                    + ": "
                    + e.getMessage()
            );
        }
    }

    private void persistPreferredLanguage(
            Language language
    ) {

        if (language == null) {
            return;
        }

        User user = loginBean.getUser();

        if (user == null
                || user.getId() == null) {
            return;
        }

        PersonDTO person
                = personLogic.findPerson(
                        user.getId()
                );

        if (person == null) {
            return;
        }

        if (language.equals(
                person.getPreferredLanguage()
        )) {
            return;
        }

        person.setPreferredLanguage(language);

        personLogic.updatePerson(person);
    }

    private Locale localeForLanguage(
            Language language
    ) {

        return switch (language) {
            case EN ->
                Locale.ENGLISH;
            case DE ->
                Locale.GERMAN;
            case FR ->
                Locale.FRENCH;
            case PL ->
                Locale.of("pl", "PL");
        };
    }

    private Language languageEnumForCode(
            String languageCode
    ) {

        if (languageCode == null) {
            return null;
        }

        return switch (languageCode.toLowerCase(
                Locale.ROOT
        )) {
            case "en" ->
                Language.EN;
            case "de" ->
                Language.DE;
            case "fr" ->
                Language.FR;
            case "pl" ->
                Language.PL;
            default ->
                null;
        };
    }

    private void updateCurrentViewLocale() {

        FacesContext context
                = FacesContext.getCurrentInstance();

        if (context != null
                && context.getViewRoot() != null
                && userLocale != null) {

            context.getViewRoot()
                    .setLocale(userLocale);
        }
    }

    public String getCurrentLanguageShortLabel() {

        String languageCode
                = getSelectedLanguage();

        return LANGUAGES.stream()
                .filter(language
                        -> language.getCode()
                        .equalsIgnoreCase(
                                languageCode
                        ))
                .map(LanguageOption::getShortLabel)
                .findFirst()
                .orElse("EN");
    }

    public String getCurrentLanguageFullLabel() {

        String languageCode
                = getSelectedLanguage();

        return LANGUAGES.stream()
                .filter(language
                        -> language.getCode()
                        .equalsIgnoreCase(
                                languageCode
                        ))
                .map(LanguageOption::getFullLabel)
                .findFirst()
                .orElse("English");
    }

    public static class LanguageOption
            implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String code;
        private final String shortLabel;
        private final String fullLabel;

        public LanguageOption(
                String code,
                String shortLabel,
                String fullLabel
        ) {
            this.code = code;
            this.shortLabel = shortLabel;
            this.fullLabel = fullLabel;
        }

        public String getCode() {
            return code;
        }

        public String getShortLabel() {
            return shortLabel;
        }

        public String getFullLabel() {
            return fullLabel;
        }
    }
}
