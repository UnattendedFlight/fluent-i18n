package io.github.unattendedflight.fluent.i18n.extractor;

/**
 * Types of extracted messages
 */
public enum MessageType {
    SIMPLE,        // Regular translate() call
    PLURAL,        // Plural form (zero, one, other, etc.)
    CONTEXTUAL,    // Context-specific translation
    ANNOTATION     // From @Translatable annotation
}