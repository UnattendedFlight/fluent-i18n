package io.github.unattendedflight.fluent.i18n.annotation;

import java.lang.annotation.*;

/**
 * Annotation for defining localized, parameterizable messages within the application.
 *
 * Designed to support natural language strings with optional disambiguation context
 * and parameterization for variable substitution. Useful for generating translatable
 * content dynamically while ensuring message consistency across the system.
 *
 * - `value`: The main message text to be localized. Keep it human-readable for translators.
 * - `context`: Provides disambiguation when the same text appears in different contexts.
 * - `args`: Supports variable substitution for dynamic content; ensure arguments
 *   match placeholders in the message format.
 *
 * Avoid misuse where static, non-localized text suffices. Always consider pluralization
 * and formatting where applicable.
 */
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Message {
    
    /**
     * Defines the localized, parameterizable message text to be used within the system.
     * Ensure the value is concise, contextually appropriate, and avoids concatenation
     * of dynamic parts to maintain translatability.
     *
     * @return The human-readable message text intended for localization.
     */
    String value();
    
    /**
     * Disambiguates translations by providing contextual information when the same
     * message text could have different meanings in different scenarios.
     *
     * @return Optional context to clarify message intent during localization.
     */
    String context() default "";
    
    /**
     * Optional placeholders for dynamic substitution within the localized message.
     * Ensure placeholders in the message align with the provided arguments to avoid runtime
     * inconsistencies or formatting issues. Can handle variable-length arguments, but
     * avoid overuse for readability and translation clarity.
     *
     * @return An array of variable names or descriptions expected to be injected into the message.
     */
    String[] args() default {};
}