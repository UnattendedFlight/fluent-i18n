package io.github.unattendedflight.fluent.i18n.core;

/**
 * Represents the result of a translation lookup operation, encapsulating
 * the translated value if found and a fallback value if not.
 *
 * This class provides methods to create instances representing
 * either found or not found translations, and allows retrieval
 * of the relevant values. It is immutable and ensures consistent
 * behavior for translation handling.
 */
public class TranslationResult {
    /**
     * The translated value resulting from a translation lookup operation.
     *
     * This variable holds the specific translation that maps to the input key
     * when the translation exists. If no translation is found, this field
     * may be null, and a fallback value may be used instead.
     *
     * Use this field to retrieve the localized or translated string
     * associated with a lookup operation.
     */
    private final String translation;
    /**
     * Indicates whether a translation result was found during a lookup operation.
     *
     * This variable is used to determine if the translation process successfully
     * identified a matching value. When set to {@code true}, the associated
     * translation exists and is available for use; when {@code false}, the
     * translation could not be found, and a fallback value may be provided
     * instead.
     */
    private final boolean found;
    /**
     * The fallback value to be used when no translation is found.
     *
     * This field stores an alternative string that is returned in cases
     * where a translation is unavailable. It ensures that the application
     * can gracefully handle missing translations by providing a default or
     * placeholder value. This is especially useful in internationalization
     * when certain text may not have a corresponding translation in the
     * desired language.
     *
     * The value of this field is immutable after being initialized.
     */
    private final String fallback;
    
    /**
     * Constructs a TranslationResult instance.
     *
     * @param translation the translated value, which may be null if no translation was found
     * @param found a boolean indicating whether the translation was successfully found
     * @param fallback the fallback value to use if the translation was not found, which may be null
     */
    private TranslationResult(String translation, boolean found, String fallback) {
        this.translation = translation;
        this.found = found;
        this.fallback = fallback;
    }
    
    /**
     * Creates a new instance of the TranslationResult to represent a successful translation lookup.
     * The translation is considered found, with the provided translation string as the result.
     *
     * @param translation the translation string that was found
     * @return a TranslationResult object representing a successful translation lookup with the given translation
     */
    public static TranslationResult found(String translation) {
        return new TranslationResult(translation, true, null);
    }
    
    /**
     * Creates a TranslationResult instance representing a "not found" translation scenario,
     * where the translation is not available and a fallback string is provided instead.
     *
     * @param fallback the fallback string to use when the translation is not found
     * @return a TranslationResult indicating that the translation was not found,
     *         with the provided fallback string
     */
    public static TranslationResult notFound(String fallback) {
        return new TranslationResult(null, false, fallback);
    }
    
    /**
     * Retrieves the appropriate translation based on the lookup result.
     * If a translation was found, it returns the translated value. Otherwise,
     * it returns the fallback value provided during creation of the instance.
     *
     * @return the translated value if found; otherwise, the fallback value
     */
    public String getTranslation() {
        return found ? translation : fallback;
    }
    
    /**
     * Indicates whether the translation was found during the lookup operation.
     *
     * This method returns a boolean value reflecting the state of the translation
     * lookup. A value of true indicates that a translation was successfully found,
     * while a value of false indicates that no translation was located, and a
     * fallback value may be used instead.
     *
     * @return true if the translation is found, false otherwise
     */
    public boolean isFound() {
        return found;
    }
    
    /**
     * Retrieves the fallback value associated with this instance.
     *
     * The fallback value is used when a translation is not found
     * and provides an alternative textual representation.
     *
     * @return the fallback value, or null if no fallback is defined
     */
    public String getFallback() {
        return fallback;
    }
}