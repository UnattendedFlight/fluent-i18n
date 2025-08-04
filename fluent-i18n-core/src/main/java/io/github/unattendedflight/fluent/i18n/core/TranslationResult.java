package io.github.unattendedflight.fluent.i18n.core;

/**
 * Result of a translation lookup
 */
public class TranslationResult {
    private final String translation;
    private final boolean found;
    private final String fallback;
    
    private TranslationResult(String translation, boolean found, String fallback) {
        this.translation = translation;
        this.found = found;
        this.fallback = fallback;
    }
    
    public static TranslationResult found(String translation) {
        return new TranslationResult(translation, true, null);
    }
    
    public static TranslationResult notFound(String fallback) {
        return new TranslationResult(null, false, fallback);
    }
    
    public String getTranslation() {
        return found ? translation : fallback;
    }
    
    public boolean isFound() {
        return found;
    }
    
    public String getFallback() {
        return fallback;
    }
}