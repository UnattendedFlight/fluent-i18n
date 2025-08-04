package io.github.unattendedflight.fluent.i18n.extractor;

/**
 * Defines the different types of messages that can be extracted for
 * internationalization and localization purposes. Each type indicates
 * the nature of the message and how it should be processed.
 *
 * SIMPLE:
 * Represents a regular translation string. A direct call to translate the message.
 *
 * PLURAL:
 * Specifies a message that requires pluralization. This type includes forms for
 * quantities such as zero, one, other, etc.
 *
 * CONTEXTUAL:
 * Represents a context-specific translation that depends on additional
 * contextual information to disambiguate translations.
 *
 * ANNOTATION:
 * Indicates a message derived from a @Translatable annotation in the source code.
 */
public enum MessageType {
    /**
     * Represents a regular translation string. This type of message is a simple,
     * straightforward translation that does not require context or pluralization.
     */
    SIMPLE,
    /**
     * Represents a message that requires pluralization. This type is used for messages
     * where the output depends on numerical values and needs to account for language-specific
     * rules regarding singular, plural, and other quantity-related forms.
     * Typical forms include "zero," "one," "few," "many," and "other."
     */
    PLURAL,
    /**
     * Represents a context-specific translation that depends on additional
     * contextual information to disambiguate translations. This type of message
     * is used when the meaning of the message can vary based on its use or
     * surrounding context in the source code.
     */
    CONTEXTUAL,
    /**
     * Indicates a message that is extracted from a @Translatable annotation within the source code.
     * This type represents annotation-derived messages that are included in the internationalization
     * process based on their presence in explicitly marked locations in the source code.
     */
    ANNOTATION
}