package tss.web.Bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

@SessionScoped
@Named
public class LocaleBean implements Serializable {
     private static final long serialVersionUID = 516756595421760915L;

     private Locale userLocale;
     
     
     public Locale getUserLocale() {
     if (userLocale == null) {
            userLocale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
        }
        if (userLocale == null) {
            userLocale = FacesContext.getCurrentInstance().getApplication().getDefaultLocale();
        }
        if (userLocale == null) {
            userLocale = Locale.ENGLISH;
        }
        return userLocale;
     }
     
     
     public void setUserLocale(Locale userLocale) {
        this.userLocale = userLocale;
    }
     public void selectGerman() {
        userLocale = Locale.GERMAN;
    }
    public void selectEnglish() {
        userLocale = Locale.ENGLISH;
    }
     
}
