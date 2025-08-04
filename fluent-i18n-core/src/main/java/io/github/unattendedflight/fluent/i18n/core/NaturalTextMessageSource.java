package io.github.unattendedflight.fluent.i18n.core;

import java.util.Locale;

/**
 * An interface for managing translations based on natural text inputs.
 * Provides methods to resolve translations, check for their existence,
 * retrieve supported locales, and manage translation resources.
 */
public interface NaturalTextMessageSource {
    
    /**
     * Resolves a translation based on the given hash, natural text, and locale.
     * If a translation is found for the specified hash and locale, it is returned.
     * Otherwise, a fallback mechanism might be applied, or a not-found result is returned.
     *
     * @param hash the unique hash identifier for the natural text; used as a key to search for translations
     * @param naturalText the natural language input that serves as the basis for translation
     * @param locale the locale specifying the desired language and region for the translation
     * @return a {@code TranslationResult} containing the resolved translation, a flag indicating
     *         if the translation was found, and a possible fallback text if the translation is not found
     */
    TranslationResult resolve(String hash, String naturalText, Locale locale);
    
    /**
     * Checks if a translation exists for the given hash and locale.
     *
     * @param hash the hash representing the natural text to be checked
     * @param locale the locale for which the existence of the translation is being verified
     * @return true if a translation exists for the given hash and locale, false otherwise
     */
    boolean exists(String hash, Locale locale);
    
    /**
     * Retrieves the locales supported by this message source for translations.
     *
     * @return an iterable collection of {@link Locale} objects that represent
     *         the supported locales available for translations.
     */
    Iterable<Locale> getSupportedLocales();
    
    /**
     * Reloads the underlying message source or resources to ensure the most
     * up-to-date data is being used. This allows any external changes
     * (such as updates to translation files or configurations) to be
     * reflected without requiring an application restart.
     *
     * This method has a default implementation that performs no action.
     * Implementations may override this method to provide reloading
     * logic specific to their use case.
     */
    default void reload() {
        // Default implementation does nothing
    }

    /**
     * Initializes or preloads data for the specified locales to optimize performance
     * for subsequent operations, such as translation lookups.
     * This is an optional operation and may have no effect if not overridden by an implementation.
     *
     * @param locales an iterable collection of {@link Locale} objects representing the
     *                locales to be prepared or warmed up
     */
    default void warmUp(Iterable<Locale> locales) {
        // Default implementation does nothing
    }
}