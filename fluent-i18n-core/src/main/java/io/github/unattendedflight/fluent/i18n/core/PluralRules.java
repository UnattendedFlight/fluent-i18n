package io.github.unattendedflight.fluent.i18n.core;

import java.util.Locale;

/**
 * Provides utilities for determining the appropriate plural form
 * based on a given count and locale.
 *
 * This class supports the selection of plural forms using simple linguistic rules
 * that can be extended to accommodate specific languages or locales. The rules help
 * facilitate internationalization by identifying how quantities are expressed
 * grammatically in different languages.
 */
public class PluralRules {
    
    /**
     * Determines the appropriate plural form for a given count and locale.
     *
     * This method uses linguistic rules to evaluate the provided count
     * and locale information and returns the corresponding plural form.
     * The rules account for basic pluralization in some languages and can
     * be extended to support additional locale-specific variations.
     *
     * @param count the numeric value for which the plural form is determined
     * @param locale the locale information representing language and region
     * @return the plural form (e.g., ZERO, ONE, TWO, OTHER) that corresponds to the count and locale
     */
    public static PluralForm determine(Number count, Locale locale) {
        int n = count.intValue();
        
        // Simple English-like rules for now
        if (n == 0) return PluralForm.ZERO;
        if (n == 1) return PluralForm.ONE;
        if (n == 2) return PluralForm.TWO;
        
        // Add locale-specific rules here later, for now just return OTHER
        String language = locale.getLanguage();

        return PluralForm.OTHER; // Default to OTHER for unsupported languages
    }
}