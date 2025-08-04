package io.github.unattendedflight.fluent.i18n.spring;

import io.github.unattendedflight.fluent.i18n.I18n;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Utility class for internationalization (i18n) used to provide locale-specific messages,
 * translations, date formatting, and pluralization.
 * This class is annotated as a Spring component and can be autowired.
 */
@Component("i18n")
public class I18nTemplateUtils {
    
    /**
     * Translates the given message into the current locale, optionally formatting
     * the message with the provided arguments.
     *
     * @param message the message key to be translated
     * @param args optional arguments to format the translated message
     * @return the translated and optionally formatted message
     */
    public String translate(String message, Object... args) {
        return I18n.translate(message, args);
    }
    
    /**
     * Translates the given message using the current locale and optionally formats it with the provided arguments.
     *
     * @param message the message key or literal to be translated
     * @param args optional arguments to format the message
     * @return the translated and formatted message based on the current locale
     */
    public String t(String message, Object... args) {
        return translate(message, args);
    }
    
    /**
     * Translates a message within a specified context using the provided arguments and internationalization configuration.
     *
     * @param context the specific context or category for the message translation
     * @param message the message to be translated
     * @param args optional arguments to format the message
     * @return the translated and formatted message, localized based on the current locale and context
     */
    public String ctx(String context, String message, Object... args) {
        return I18n.context(context).translate(message, args);
    }
    
    /**
     * Formats the provided LocalDate object into a locale-specific string
     * representation based on the current locale settings.
     *
     * @param date the LocalDate to be formatted
     * @return the formatted date string in medium style for the current locale
     */
    public String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(I18n.getCurrentLocale()));
    }
    
    /**
     * Formats a given LocalDateTime object into a localized string representation based on the
     * medium date and time format style of the current locale.
     *
     * @param dateTime the LocalDateTime object to be formatted
     * @return a string representation of the given LocalDateTime in the localized medium format
     */
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(I18n.getCurrentLocale()));
    }
    
    /**
     * Retrieves the current locale used for internationalization (i18n) operations.
     *
     * @return the current {@link Locale} used for translations, formatting, and other locale-specific operations
     */
    public Locale getCurrentLocale() {
        return I18n.getCurrentLocale();
    }
    
    /**
     * Determines if a given text has a translation available in the current locale.
     *
     * @param text the text to check for translation
     * @return true if a translation exists for the given text, false otherwise
     */
    public boolean hasTranslation(String text) {
        return I18n.describe(text).hasTranslation();
    }
    
    /**
     * Returns a PluralHelper instance initialized with the given count.
     * This helper class is used for defining locale-based pluralization
     * rules based on the provided count.
     *
     * @param count the numeric value used to determine the applicable plural form
     * @return a PluralHelper instance to configure and format pluralized messages
     */
    public PluralHelper plural(Number count) {
        return new PluralHelper(count.intValue());
    }
    
    /**
     * A helper class to manage pluralization rules and format messages
     * based on a count value and locale. It provides customizable plural forms
     * for numbers such as zero, one, two, few, many, and other, allowing
     * messages to properly align with linguistic pluralization rules.
     */
    public static class PluralHelper {
        /**
         * The numeric value representing the count used to determine the appropriate
         * plural form for messages. This value is used internally to match against
         * pluralization rules based on the specified language or locale.
         */
        private final int count;
        /**
         * Represents the pluralization message for the "zero" count in a pluralization rule.
         * This variable is used to store the message associated with a count of zero,
         * allowing for proper localization and formatting based on linguistic plural rules.
         */
        private String zero, /**
         * Represents the message or format specifically associated with the singular form ("one")
         * in the context of pluralization. This is typically used when the count value is equal to 1,
         * aligning with grammatical rules for singular entities in most languages.
         */
        one, /**
         * Represents the message template to be used for the "two" pluralization rule
         * in a pluralization formatting process. This value is typically utilized
         * for languages where specific grammar rules apply when the count equals two.
         */
        two, /**
         * Specifies the message template to use when a plural form corresponds to the
         * "few" category. The "few" category typically applies to certain counts
         * according to specific pluralization rules of a language.
         */
        few, /**
         * Represents the message or format to be used for the "many" pluralization
         * category. This is utilized when the count falls under the "many" plural form
         * as determined by linguistic pluralization rules for a particular locale.
         */
        many, /**
         * Represents the message or text associated with the "other" plural form.
         * This is used when none of the other plural forms (zero, one, two, few, many) apply.
         * Commonly utilized in pluralization logic to provide a fallback or default message
         * that aligns with linguistic pluralization rules.
         */
        other;
        
        /**
         * Constructs a PluralHelper instance using the specified count value.
         * This numeric value determines the applicable pluralization rules
         * for formatting localized messages.
         *
         * @param count the numeric value used to identify the appropriate plural form
         */
        public PluralHelper(int count) {
            this.count = count;
        }
        
        /**
         * Sets the message to be used for the zero plural form.
         *
         * @param message the message to represent the zero plural form
         * @return the current instance of PluralHelper for method chaining
         */
        public PluralHelper zero(String message) { this.zero = message; return this; }
        /**
         * Sets the message for the "one" pluralization rule.
         *
         * @param message the message to use when the pluralization rule matches "one"
         * @return the current instance of PluralHelper for method chaining
         */
        public PluralHelper one(String message) { this.one = message; return this; }
        /**
         * Sets the message for the "two" pluralization form and returns the current
         * instance of the PluralHelper for method chaining.
         *
         * @param message the message to associate with the "two" pluralization form
         * @return the current instance of PluralHelper to allow method chaining
         */
        public PluralHelper two(String message) { this.two = message; return this; }
        /**
         * Sets the message associated with the 'few' plural form.
         * This is typically used for counts that correspond to the 'few' category in pluralization rules.
         *
         * @param message the message to use for the 'few' plural form
         * @return the current instance of PluralHelper for method chaining
         */
        public PluralHelper few(String message) { this.few = message; return this; }
        /**
         * Sets the message for the "many" pluralization rule and returns the current instance.
         *
         * @param message the message to use for the "many" pluralization rule
         * @return the current instance of {@code PluralHelper} for method chaining
         */
        public PluralHelper many(String message) { this.many = message; return this; }
        /**
         * Sets the message for the "other" plural form and returns the updated PluralHelper instance.
         *
         * @param message the message associated with the "other" plural category
         * @return the updated PluralHelper instance allowing method chaining
         */
        public PluralHelper other(String message) { this.other = message; return this; }
        
        /**
         * Formats a localized message based on the defined pluralization rules
         * for the count value. It utilizes the configured messages for zero, one,
         * two, few, many, or other plural forms if they are provided.
         *
         * @return the formatted message according to the pluralization rules
         *         and the count value.
         */
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
        
        /**
         * Returns a string representation of the object by formatting its message
         * based on the count value and specified pluralization rules.
         *
         * @return the formatted string representation of the object.
         */
        @Override
        public String toString() {
            return format();
        }
    }
}