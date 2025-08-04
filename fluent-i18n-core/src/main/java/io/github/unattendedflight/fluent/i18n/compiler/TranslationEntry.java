package io.github.unattendedflight.fluent.i18n.compiler;

import java.util.Map;

/**
 * Single translation entry
 */
public class TranslationEntry {
    /**
     * Represents the original text associated with a translation entry.
     * This field stores the source text that is to be translated or used as a key
     * in the translation process.
     */
    private final String originalText;
    /**
     * Represents the translated text for a specific original string in a localization context.
     * This variable holds the translated version of the original text and is used to provide
     * the corresponding localized content.
     *
     * For singular entries, this contains the translated string. For plural entries, this value is null.
     */
    private final String translation;
    /**
     * Represents the location or context from which this translation entry was sourced.
     * Typically used to provide additional metadata about the origin, such as a file name,
     * a resource identifier, or other contextual information useful for maintaining
     * translation mappings or debugging issues in localization workflows.
     */
    private final String sourceLocation;
    /**
     * Represents the plural form of the original text in a translation entry.
     * This variable is used to store the pluralized version of the original text
     * when the entry corresponds to a pluralizable string.
     *
     * It is primarily relevant in localization contexts where the language
     * requires handling of singular and plural forms (e.g., "apple" vs. "apples").
     * For non-plural entries, this variable will typically be null.
     */
    private final String pluralForm;
    /**
     * Stores the plural forms of a translation entry, mapping plural form indices
     * to their respective translations.
     *
     * This map is primarily used for pluralized strings in localization, where
     * multiple forms of a word or sentence are needed based on grammatical number
     * rules. Each key represents the index of the plural form, while the associated
     * value contains the corresponding translation text.
     *
     * For example, in English, there may be two forms (singular and plural),
     * while other languages may require more forms based on their pluralization rules.
     */
    private final Map<Integer, String> pluralForms;
    /**
     * Indicates whether the translation entry represents a plural form.
     * When true, the entry contains translations for multiple plural forms.
     * Otherwise, it represents a single non-plural translation.
     */
    private final boolean isPlural;
    
    /**
     * Constructs a TranslationEntry with the provided original text, translation, and source location.
     * This constructor is used for non-plural translations.
     *
     * @param originalText the original text to be translated
     * @param translation the translated text
     * @param sourceLocation the location or context where this translation is used
     */
    public TranslationEntry(String originalText, String translation, String sourceLocation) {
        this.originalText = originalText;
        this.translation = translation;
        this.sourceLocation = sourceLocation;
        this.pluralForm = null;
        this.pluralForms = null;
        this.isPlural = false;
    }
    
    /**
     * Constructs a translation entry for a plural form with its associated translations.
     * This constructor is used for creating entries that include multiple plural forms
     * and their corresponding translations.
     *
     * @param originalText the original text of the translation
     * @param pluralForm the singular form (or key) for the plural-related translations
     * @param pluralForms a map where keys represent plural form indices and values are the corresponding translations
     * @param sourceLocation a string representing the source location of the translation entry, such as a file or contextual reference
     */
    public TranslationEntry(String originalText, String pluralForm, Map<Integer, String> pluralForms, String sourceLocation) {
        this.originalText = originalText;
        this.translation = null;
        this.sourceLocation = sourceLocation;
        this.pluralForm = pluralForm;
        this.pluralForms = pluralForms;
        this.isPlural = true;
    }
    
    /**
     * Retrieves the original text associated with this translation entry.
     *
     * @return the original text as a string
     */
    public String getOriginalText() {
        return originalText;
    }
    
    /**
     * Retrieves the translation text associated with this entry.
     *
     * @return the translation text as a string, or null if no translation is available.
     */
    public String getTranslation() {
        return translation;
    }
    
    /**
     * Retrieves the source location associated with this translation entry.
     *
     * @return the source location as a string, typically representing the original source file
     *         or location where the translation entry originates.
     */
    public String getSourceLocation() {
        return sourceLocation;
    }
    
    /**
     * Indicates whether this translation entry represents a pluralized form
     * with multiple variations to handle different quantities.
     *
     * @return true if the entry is a plural form, false otherwise
     */
    public boolean isPlural() {
        return isPlural;
    }
    
    /**
     * Retrieves the plural form text associated with this translation entry.
     * For pluralizable entries, this typically represents the pluralized version
     * of the original text.
     *
     * @return the plural form as a string, or null if the entry is not plural.
     */
    public String getPluralForm() {
        return pluralForm;
    }
    
    /**
     * Retrieves the map of plural forms associated with the translation entry.
     * The map associates a plural form index with its corresponding translation.
     *
     * @return a map where keys are integers representing plural form indices and values are strings
     *         representing the corresponding translations. Returns null if no plural forms are associated.
     */
    public Map<Integer, String> getPluralForms() {
        return pluralForms;
    }
    
    /**
     * Determines whether this translation entry has a valid translation.
     * For singular entries, it checks if the translation text is non-null and non-empty.
     * For plural entries, it verifies if at least one plural form is non-null and non-empty.
     *
     * @return true if the translation entry has a valid translation (singular or at least one plural form); false otherwise
     */
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