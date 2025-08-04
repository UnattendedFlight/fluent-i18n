package io.github.unattendedflight.fluent.i18n.core;

/**
 * Represents the different plural forms that can be used in internationalization and localization.
 *
 * This enumeration provides a set of commonly used plural forms based on linguistic rules.
 * It is often utilized in conjunction with pluralization logic to determine
 * the appropriate form of a word or phrase based on a count or quantity.
 *
 * The available plural forms are:
 * - ZERO: Represents the plural form for zero quantities.
 * - ONE: Represents the singular form for a quantity of one.
 * - TWO: Represents the plural form for exactly two quantities.
 * - FEW: Represents a plural form used for small quantities, depending on linguistic rules.
 * - MANY: Represents a plural form used for large quantities, depending on linguistic rules.
 * - OTHER: Represents the default or catch-all plural form not covered by the other categories.
 */
public enum PluralForm {
    /**
     * Represents the plural form for zero quantities.
     *
     * This constant is used in pluralization rules to denote cases
     * where the count or quantity associated with a word is zero.
     * It is particularly useful in the context of internationalization
     * and localization, where different languages may have specific
     * grammatical rules for handling zero quantities.
     */
    ZERO,
    /**
     * Represents the singular form for a quantity of one.
     *
     * This plural category is used in languages where the singular form
     * of a word or phrase is applied when describing precisely one item or unit.
     * For example, in English, the noun "car" would use this plural form when
     * referring to "1 car".
     */
    ONE,
    /**
     * Represents the plural form used for exactly two quantities in pluralization logic.
     *
     * This constant is part of the enumeration defining different plural forms
     * that are applied based on linguistic rules in internationalization
     * and localization processes. It is typically used in contexts where the
     * precise number of two is significant to determine the appropriate
     * grammatical form of a word or phrase.
     */
    TWO,
    /**
     * Represents a plural form used for small quantities, depending on linguistic rules.
     *
     * This plural form is commonly used in languages where specific rules apply
     * to small numbers, typically greater than two but less than a specific threshold.
     * The exact quantities that map to this form may vary depending on the language
     * and its pluralization rules.
     */
    FEW,
    /**
     * Represents the plural form for large quantities in internationalization and localization.
     * This form is used depending on linguistic rules specific to the language or locale.
     * "MANY" is typically associated with higher counts that are distinct from singular,
     * dual, or smaller plural forms.
     */
    MANY,
    /**
     * Represents the default or catch-all plural form that is used
     * when none of the other specific plural categories apply.
     *
     * This plural form is commonly used as a fallback in cases where
     * linguistic rules do not provide a specific pluralization for
     * a given count or quantity.
     */
    OTHER
}