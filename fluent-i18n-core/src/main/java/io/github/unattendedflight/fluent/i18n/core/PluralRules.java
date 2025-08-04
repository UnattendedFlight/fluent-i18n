package io.github.unattendedflight.fluent.i18n.core;

import java.util.Locale;

/**
 * Simple plural rules implementation
 * TODO: Replace with proper CLDR implementation
 */
public class PluralRules {
    
    public static PluralForm determine(Number count, Locale locale) {
        int n = count.intValue();
        
        // Simple English-like rules for now
        if (n == 0) return PluralForm.ZERO;
        if (n == 1) return PluralForm.ONE;
        if (n == 2) return PluralForm.TWO;
        
        // Add locale-specific rules here
        String language = locale.getLanguage();
        
        switch (language) {
            case "nb", "nn", "no": // Norwegian
                return n == 1 ? PluralForm.ONE : PluralForm.OTHER;
            
            case "en": // English
                return n == 1 ? PluralForm.ONE : PluralForm.OTHER;
            
            default:
                return PluralForm.OTHER;
        }
    }
}