package io.github.unattendedflight.fluent.i18n.compiler;

import java.util.Map;

/**
 * Single translation entry
 */
public class TranslationEntry {
    private final String originalText;
    private final String translation;
    private final String sourceLocation;
    private final String pluralForm;
    private final Map<Integer, String> pluralForms;
    private final boolean isPlural;
    
    public TranslationEntry(String originalText, String translation, String sourceLocation) {
        this.originalText = originalText;
        this.translation = translation;
        this.sourceLocation = sourceLocation;
        this.pluralForm = null;
        this.pluralForms = null;
        this.isPlural = false;
    }
    
    public TranslationEntry(String originalText, String pluralForm, Map<Integer, String> pluralForms, String sourceLocation) {
        this.originalText = originalText;
        this.translation = null;
        this.sourceLocation = sourceLocation;
        this.pluralForm = pluralForm;
        this.pluralForms = pluralForms;
        this.isPlural = true;
    }
    
    public String getOriginalText() {
        return originalText;
    }
    
    public String getTranslation() {
        return translation;
    }
    
    public String getSourceLocation() {
        return sourceLocation;
    }
    
    public boolean isPlural() {
        return isPlural;
    }
    
    public String getPluralForm() {
        return pluralForm;
    }
    
    public Map<Integer, String> getPluralForms() {
        return pluralForms;
    }
    
    public boolean hasTranslation() {
        if (isPlural) {
            // For plural entries, check if we have at least one non-empty plural form
            if (pluralForms == null || pluralForms.isEmpty()) {
                return false;
            }
            return pluralForms.values().stream()
                .anyMatch(form -> form != null && !form.trim().isEmpty());
        } else {
            return translation != null && !translation.trim().isEmpty();
        }
    }
}