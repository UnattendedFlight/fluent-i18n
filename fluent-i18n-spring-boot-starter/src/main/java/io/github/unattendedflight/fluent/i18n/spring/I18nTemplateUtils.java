package io.github.unattendedflight.fluent.i18n.spring;

import io.github.unattendedflight.fluent.i18n.I18n;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Utility class for internationalization (i18n) features.
 * Provides methods to translate messages, format dates based on locale,
 * retrieve the current locale, and handle pluralization.
 * This class is annotated as a Spring component with the name "i18n".
 */
@Component("i18n")
public class I18nTemplateUtils {
    
    /**
     * Translates the provided message based on the current locale and formatting rules.
     * The message can include placeholders for variable substitution, which will be
     * replaced by the provided arguments.
     *
     * @param message the message key or string to be translated
     * @param args optional arguments to be substituted into the message
     * @return the translated and formatted string based on the current locale
     */
    public String translate(String message, Object... args) {
        return I18n.translate(message, args);
    }
    
    /**
     * Translates a message string with optional arguments into the current locale.
     *
     * @param message the message to be translated
     * @param args optional arguments to format the message
     * @return the translated and formatted message
     */
    public String t(String message, Object... args) {
        return translate(message, args);
    }
    
    /**
     * Translates a message within a specific context using internationalization features.
     *
     * @param context the context for the translation, typically a scope or category
     * @param message the message to translate
     * @param args optional arguments to format the translated message
     * @return the translated message, formatted with the provided arguments if applicable
     */
    public String ctx(String context, String message, Object... args) {
        return I18n.context(context).translate(message, args);
    }
    
    /**
     * Formats the given date into a localized string representation based on the current locale and a medium display style.
     *
     * @param date the LocalDate to be formatted; must not be null
     * @return a string representation of the date in a medium style format localized to the current locale
     */
    public String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(I18n.getCurrentLocale()));
    }
    
    /**
     * Formats a given {@link LocalDateTime} object into a string representation
     * according to the medium format style localized to the current locale.
     *
     * @param dateTime the {@link LocalDateTime} object to format
     * @return a locale-aware, medium-style formatted string representation of the date and time
     */
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(I18n.getCurrentLocale()));
    }
    
    /**
     * Retrieves the current locale used for internationalization (i18n) purposes.
     *
     * @return the current {@link Locale} instance.
     */
    public Locale getCurrentLocale() {
        return I18n.getCurrentLocale();
    }
    
    /**
     * Checks if the given text has a translation available in the current locale.
     *
     * @param text the text to check for translation availability
     * @return true if a translation exists for the given text; false otherwise
     */
    public boolean hasTranslation(String text) {
        return I18n.describe(text).hasTranslation();
    }
    
    /**
     * Creates a helper instance to handle locale-aware pluralization templates.
     * The returned {@link PluralHelper} provides methods to set pluralization
     * messages for different plural forms such as zero, one, two, few, many, and other.
     *
     * @param count the number that determines the pluralization form
     * @return an instance of {@link PluralHelper} for configuring and formatting plural messages
     */
    public PluralHelper plural(Number count) {
        return new PluralHelper(count.intValue());
    }
    
    /**
     * Provides a utility class for handling pluralization of strings in a locale-aware manner.
     * This class supports setting messages for various plural forms including zero, one, two,
     * few, many, and other, based on the specified count.
     *
     * The {@code PluralHelper} allows fluent API chaining to configure pluralization messages.
     * Once configured, the {@code format} method determines the appropriate plural form based
     * on the count and returns the corresponding message.
     */
    public static class PluralHelper {
        /**
         * Represents the count used to determine the appropriate plural form for a message.
         * This value is utilized within the pluralization logic to select the correct
         * form of a message based on locale-specific rules.
         *
         * The count is immutable and is assigned at the time of object creation.
         */
        private final int count;
        /**
         * Represents the message associated with the "zero" plural form in a pluralization context.
         * The "zero" form is used to define the output message when the count is explicitly zero.
         *
         * This variable is part of the pluralization system and plays a key role in locale-aware
         * message formatting. It is typically set using the fluent API method {@code zero(String message)}.
         *
         * The "zero" form is particularly useful in languages or contexts where the message for
         * a count of "0" is distinct and does not fall under the "other" plural category.
         */
        private String zero, /**
         * Represents the message to be used for the "one" plural form in locale-aware pluralization.
         * This variable is used to store a string intended to be displayed when the count is exactly one.
         */
        one, /**
         * Represents a message configured for the "two" plural form.
         * This variable is intended to hold the message that corresponds to
         * situations where a count matches the "two" plural category.
         *
         * It is set through the {@link PluralHelper#two(String)} method and
         * used internally by the {@link PluralHelper#format()} method to determine
         * the appropriate string output for the given count.
         */
        two, /**
         * Represents the message to be used when the "few" plural form is applicable.
         *
         * This variable holds the text to be returned for the "few" plural category,
         * depending on the count and localized pluralization rules. It is part of the
         * fluent API design of the {@code PluralHelper} class for configuring plural
         * forms.
         */
        few, /**
         * Represents the plural message used for the "many" category in a locale-aware pluralization system.
         * The "many" category is generally applied to values that correspond to large quantities, as determined
         * by locale-specific rules.
         *
         * This field stores the message string that will be used when the plural form is evaluated as "many".
         * Its value can be set using the {@link PluralHelper#many(String)} method.
         */
        many, /**
         * Represents the fallback or default message to be used in cases where none of the other
         * pluralization forms (zero, one, two, few, many) is applicable for the current count.
         *
         * This field is typically used to define a catch-all message for pluralization scenarios.
         */
        other;
        
        /**
         * Constructs a new instance of the PluralHelper for managing plural messages
         * based on the provided count. This count is used to determine the appropriate
         * plural form for a message when formatting is performed.
         *
         * @param count the numerical value representing the quantity, which is used to
         *              select the correct plural form of a message
         */
        public PluralHelper(int count) {
            this.count = count;
        }
        
        /**
         * Sets the message corresponding to the "zero" plural form in the pluralization process.
         * This method allows you to provide a specific message when the count is zero.
         *
         * @param message the message to use for the "zero" plural form
         * @return the current instance of {@code PluralHelper} for method chaining
         */
        public PluralHelper zero(String message) { this.zero = message; return this; }
        /**
         * Sets the message for the singular (one) form of the pluralization.
         * This method allows defining the string that should be used for the singular
         * case in a pluralization context.
         *
         * @param message the message to represent the singular form
         * @return the current instance of {@code PluralHelper} for fluent chaining
         */
        public PluralHelper one(String message) { this.one = message; return this; }
        /**
         * Sets the message corresponding to the "two" plural form and returns the
         * current instance of {@code PluralHelper} for method chaining.
         *
         * @param message the message to be used for the "two" plural form
         * @return the current instance of {@code PluralHelper}, allowing for further chaining of methods
         */
        public PluralHelper two(String message) { this.two = message; return this; }
        /**
         * Sets the message corresponding to the "few" plural form for the current instance.
         * This method enables fluent API chaining by returning the instance itself.
         *
         * @param message the message to associate with the "few" plural form
         * @return the current instance of {@code PluralHelper}
         */
        public PluralHelper few(String message) { this.few = message; return this; }
        /**
         * Sets the message to be used for the "many" plural form and returns the current instance.
         * This allows chaining of plural form configurations.
         *
         * @param message the message to associate with the "many" plural form
         * @return the current instance of {@code PluralHelper} with the "many" message updated
         */
        public PluralHelper many(String message) { this.many = message; return this; }
        /**
         * Sets the message for the "other" plural form.
         * This is used in cases that do not match any of the specific plural categories
         * such as zero, one, two, few, or many.
         *
         * @param message the message to be used for the "other" plural form
         * @return the current instance of {@code PluralHelper} for method chaining
         */
        public PluralHelper other(String message) { this.other = message; return this; }
        
        /**
         * Generates a formatted string based on the specified count and plural forms.
         * This method evaluates the pre-configured messages for various plural categories
         * (zero, one, two, few, many, other) to determine the appropriate message to return.
         *
         * @return The formatted string corresponding to the most suitable plural form for the given count.
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
         * Converts the {@code PluralHelper} instance into a string representation.
         * This method returns the formatted message based on the configured pluralization
         * rules and the specified count.
         *
         * @return A formatted text string determined by the count and pluralization rules.
         */
        @Override
        public String toString() {
            return format();
        }
    }
}