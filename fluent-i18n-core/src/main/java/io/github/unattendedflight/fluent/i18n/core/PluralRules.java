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
     * Determines the appropriate plural form for a given count and locale
     * based on predefined linguistic pluralization rules.
     *
     * Pluralization logic varies by language, especially for non-singular forms like "few" or "many."
     * Handles edge cases like zero or locale-specific nuances (e.g., Arabic dual forms). Defaults to
     * English-like rules when the locale is not explicitly covered.
     *
     * @param count  The numerical count to evaluate, from which the plural form will be derived.
     *               Non-integer values are truncated to their integer component.
     * @param locale The locale specifying the language context for the pluralization rules.
     *               Determines language-specific behavior and rule application.
     * @return The calculated plural form (e.g., ONE, FEW, MANY) matching the count and locale.
     */
    public static PluralForm determine(Number count, Locale locale) {
        int n = count.intValue();
        String language = locale.getLanguage();
        
        // Zero form
        if (n == 0) return PluralForm.ZERO;
        
        // Language-specific rules
        switch (language) {
            case "en": // English
                return n == 1 ? PluralForm.ONE : PluralForm.OTHER;
                
            case "fr": // French
                return n <= 1 ? PluralForm.ONE : PluralForm.OTHER;
                
            case "ru": // Russian
                if (n % 10 == 1 && n % 100 != 11) return PluralForm.ONE;
                if (n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 10 || n % 100 >= 20)) return PluralForm.FEW;
                return PluralForm.MANY;
                
            case "pl": // Polish
                if (n == 1) return PluralForm.ONE;
                if (n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 10 || n % 100 >= 20)) return PluralForm.FEW;
                return PluralForm.MANY;
                
            case "ar": // Arabic
                if (n == 1) return PluralForm.ONE;
                if (n == 2) return PluralForm.TWO;
                if (n % 100 >= 3 && n % 100 <= 10) return PluralForm.FEW;
                if (n % 100 >= 11) return PluralForm.MANY;
                return PluralForm.OTHER;
                
            case "cs": // Czech
            case "sk": // Slovak
                if (n == 1) return PluralForm.ONE;
                if (n >= 2 && n <= 4) return PluralForm.FEW;
                return PluralForm.MANY;
                
            default: // Default Germanic/English-like rules
                return n == 1 ? PluralForm.ONE : PluralForm.OTHER;
        }
    }
}