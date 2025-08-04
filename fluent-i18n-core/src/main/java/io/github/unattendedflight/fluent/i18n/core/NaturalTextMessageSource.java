package io.github.unattendedflight.fluent.i18n.core;

import java.util.Locale;

/**
 * Message source that resolves translations using natural text and generated hashes
 */
public interface NaturalTextMessageSource {
    
    /**
     * Resolve a translation for the given hash and natural text
     * 
     * @param hash Generated hash for the natural text
     * @param naturalText The original natural text (fallback)
     * @param locale Target locale
     * @return Translation result
     */
    TranslationResult resolve(String hash, String naturalText, Locale locale);
    
    /**
     * Check if a translation exists
     */
    boolean exists(String hash, Locale locale);
    
    /**
     * Get supported locales
     */
    Iterable<Locale> getSupportedLocales();
    
    /**
     * Reload translations from source
     */
    default void reload() {
        // Default implementation does nothing
    }

    /**
     * Warm up the message source by preloading translations
     * This can be used to optimize performance
     * @param locales Locales to preload or null for all supported locales
     */
    default void warmUp(Iterable<Locale> locales) {
        // Default implementation does nothing
    }
}