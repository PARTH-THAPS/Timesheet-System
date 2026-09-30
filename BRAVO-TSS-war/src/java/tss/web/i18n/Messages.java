package tss.web.i18n;

import jakarta.faces.context.FacesContext;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class Messages {

    private Messages() {
    }

    public static String get(String key, Object... arguments) {

        FacesContext context = FacesContext.getCurrentInstance();

        ResourceBundle bundle = context
                .getApplication()
                .getResourceBundle(context, "msg");

        String pattern = bundle.getString(key);

        if (arguments == null || arguments.length == 0) {
            return pattern;
        }

        return MessageFormat.format(pattern, arguments);
    }
}