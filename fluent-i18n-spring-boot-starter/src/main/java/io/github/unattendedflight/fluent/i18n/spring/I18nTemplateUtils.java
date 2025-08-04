package io.github.unattendedflight.fluent.i18n.spring;

import io.github.unattendedflight.fluent.i18n.I18n;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Template utility bean for use in Thymeleaf and other template engines
 */
@Component("i18n")
public class I18nTemplateUtils {
    
    /**
     * Translate a message
     */
    public String translate(String message, Object... args) {
        return I18n.translate(message, args);
    }
    
    /**
     * Short alias for translate
     */
    public String t(String message, Object... args) {
        return translate(message, args);
    }
    
    /**
     * Context-aware translation
     */
    public String ctx(String context, String message, Object... args) {
        return I18n.context(context).translate(message, args);
    }
    
    /**
     * Format date for current locale
     */
    public String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(I18n.getCurrentLocale()));
    }
    
    /**
     * Format datetime for current locale
     */
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(I18n.getCurrentLocale()));
    }
    
    /**
     * Get current locale
     */
    public Locale getCurrentLocale() {
        return I18n.getCurrentLocale();
    }
    
    /**
     * Check if text has translation
     */
    public boolean hasTranslation(String text) {
        return I18n.describe(text).hasTranslation();
    }
    
    /**
     * Pluralization helper
     */
    public PluralHelper plural(Number count) {
        return new PluralHelper(count.intValue());
    }
    
    /**
     * Helper class for template-based pluralization
     */
    public static class PluralHelper {
        private final int count;
        private String zero, one, two, few, many, other;
        
        public PluralHelper(int count) {
            this.count = count;
        }
        
        public PluralHelper zero(String message) { this.zero = message; return this; }
        public PluralHelper one(String message) { this.one = message; return this; }
        public PluralHelper two(String message) { this.two = message; return this; }
        public PluralHelper few(String message) { this.few = message; return this; }
        public PluralHelper many(String message) { this.many = message; return this; }
        public PluralHelper other(String message) { this.other = message; return this; }
        
        public String format() {
            var builder = I18n.plural(count);
            if (zero != null) builder.zero(zero);
            if (one != null) builder.one(one);
            if (two != null) builder.two(two);
            if (few != null) builder.few(few);
            if (many != null) builder.many(many);
            if (other != null) builder.other(other);
            return builder.format();
        }
        
        @Override
        public String toString() {
            return format();
        }
    }
}