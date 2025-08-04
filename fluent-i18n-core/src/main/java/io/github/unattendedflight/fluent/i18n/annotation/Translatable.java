package io.github.unattendedflight.fluent.i18n.annotation;

import java.lang.annotation.*;

/**
 * Marks a string constant as translatable.
 * The annotated value will be extracted during build-time message extraction.
 */
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Translatable {
    
    /**
     * The natural text to be translated
     */
    String value();
    
    /**
     * Context for the translation (optional)
     */
    String context() default "";
    
    /**
     * Description for translators (optional)
     */
    String description() default "";
    
    /**
     * Whether this message supports pluralization
     */
    boolean plural() default false;
}