package io.github.jmecn.font.editor;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    static final Locale[] SUPPORTED = {
            Locale.ENGLISH,
            Locale.SIMPLIFIED_CHINESE,
    };

    private final Locale defaultLanguage;
    private Locale currentLocale;
    private ResourceBundle resourceBundle;

    public I18n() {
        defaultLanguage = Locale.ENGLISH;
        currentLocale = Locale.getDefault();
        resourceBundle = ResourceBundle.getBundle("lang", currentLocale);
    }

    public void setLocale(Locale locale) {
        if (locale == null) {
            locale = defaultLanguage;
        }
        if (locale.equals(currentLocale)) {
            return;
        }
        currentLocale = locale;
        resourceBundle = ResourceBundle.getBundle("lang", locale);
    }

    public String getString(String key) {
        return resourceBundle.getString(key);
    }
}
