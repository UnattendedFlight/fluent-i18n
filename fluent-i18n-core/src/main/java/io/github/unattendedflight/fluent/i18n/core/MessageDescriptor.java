package io.github.unattendedflight.fluent.i18n.core;

import io.github.unattendedflight.fluent.i18n.I18n;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * The MessageDescriptor class represents a message template and its associated metadata,
 * including its hash, natural language text, and optional arguments.
 * It is used for internationalization and localization purposes.
 */
public class MessageDescriptor {
    /**
     * A unique identifier representing the hash value of a message template.
     * The hash is used to reference and retrieve the localized or translated
     * version of the message in the system. It is typically generated based
     * on the natural text of the message and serves as a key within
     * the internationalization framework.
     */
    private final String hash;
    /**
     * Represents the underlying natural language text of a message or phrase.
     * This text is typically used as a human-readable representation of the message
     * within the context of internationalization and localization.
     *
     * It serves as the primary raw text input for generating hashes or resolving translations.
     * The `naturalText` is immutable and directly associated with the message descriptor.
     */
    private final String naturalText;
    /**
     * Represents an array of optional arguments associated with the message.
     * These arguments are intended to be used for formatting the message template or providing
     * dynamic content when resolving the message.
     */
    private final Object[] args;
    
    /**
     * Constructs a new MessageDescriptor instance with the specified hash, natural text,
     * and optional arguments.
     *
     * @param hash the unique identifier for the message, typically generated from the natural text
     * @param naturalText the natural language text representing the message template
     * @param args an array of objects representing optional arguments that can be applied to the message
     */
    public MessageDescriptor(String hash, String naturalText, Object[] args) {
        this.hash = hash;
        this.naturalText = naturalText;
        this.args = args != null ? Arrays.copyOf(args, args.length) : new Object[0];
    }
    
    /**
     * Retrieves the current locale in use by the application.
     * If an error occurs while fetching the locale from the application's localization system,
     * the default system locale is returned as a fallback.
     *
     * @return the application's current locale, or the system default locale in case of an error
     */
    private java.util.Locale getCurrentLocaleInternal() {
        try {
            return I18n.getCurrentLocale();
        } catch (Exception e) {
            return java.util.Locale.getDefault();
        }
    }
    
    /**
     * Resolves the message descriptor to a localized string based on the current locale.
     *
     * @return The localized string for the message descriptor, or the natural text
     *         if no translation is found.
     */
    public String resolve() {
       return I18n.resolve(this);
    }

    /**
     * Resolves the localized message for the given locale.
     *
     * @param locale the locale for which the message should be resolved
     * @return the resolved localized message as a string
     */
    public String resolve(Locale locale) {
        return I18n.resolve(this, locale);
    }
    
    /**
     * Determines if the message has a translation for the current locale.
     *
     * @return true if a translation exists for the message in the current locale, otherwise false
     */
    public boolean hasTranslation() {
        return hasTranslation(getCurrentLocaleInternal());
    }
    
    /**
     * Checks whether a translation exists for the message identified by this descriptor's hash
     * in the specified locale.
     *
     * @param locale the locale for which to check the existence of the translation
     * @return true if a translation exists for the specified locale, false otherwise
     */
    public boolean hasTranslation(Locale locale) {
        return I18n.getMessageSource().exists(hash, locale);
    }
    
    /**
     * Creates a new MessageDescriptor instance with additional arguments
     * combined with the existing ones. The additional arguments are appended
     * to the original arguments of the current MessageDescriptor.
     *
     * @param additionalArgs the additional arguments to be added to the current arguments
     * @return a new MessageDescriptor with the combined arguments
     */
    public MessageDescriptor withArgs(Object... additionalArgs) {
        Object[] combinedArgs = new Object[args.length + additionalArgs.length];
        System.arraycopy(args, 0, combinedArgs, 0, args.length);
        System.arraycopy(additionalArgs, 0, combinedArgs, args.length, additionalArgs.length);
        return new MessageDescriptor(hash, naturalText, combinedArgs);
    }
    
    /**
     * Retrieves the hash value associated with this instance.
     * The hash typically represents a unique identifier generated
     * from the natural language text of the message.
     *
     * @return the hash value as a string
     */
    // Getters
    public String getHash() { return hash; }
    /**
     * Retrieves the natural language text associated with this instance.
     *
     * @return the natural text as a string
     */
    public String getNaturalText() { return naturalText; }
    /**
     * Retrieves a copy of the arguments associated with the message descriptor.
     *
     * @return an array of objects representing the arguments, ensuring the original array remains unmodified
     */
    public Object[] getArgs() { return Arrays.copyOf(args, args.length); }
    
    /**
     * Returns the natural text representation of this object.
     *
     * @return the natural text as a string
     */
    @Override
    public String toString() {
        return naturalText;
    }
    
    /**
     * Compares this object to the specified object to determine equality.
     *
     * @param o the object to be compared for equality with this instance
     * @return true if the specified object is equal to this instance; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageDescriptor)) return false;
        MessageDescriptor that = (MessageDescriptor) o;
        return Objects.equals(hash, that.hash) && 
               Objects.equals(naturalText, that.naturalText) &&
               Arrays.equals(args, that.args);
    }
    
    /**
     * Computes the hash code for the MessageDescriptor object.
     * The hash code is calculated based on the `hash`, `naturalText`,
     * and `args` fields.
     *
     * @return the computed hash code as an integer
     */
    @Override
    public int hashCode() {
        return Objects.hash(hash, naturalText, Arrays.hashCode(args));
    }
}