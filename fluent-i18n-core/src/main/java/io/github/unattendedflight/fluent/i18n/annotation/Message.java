package io.github.unattendedflight.fluent.i18n.annotation;

import java.lang.annotation.*;

/**
 * Annotation for marking translatable messages
 */
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Message {
    
    /**
     * The natural text message
     */
    String value();
    
    /**
     * Context for disambiguation
     */
    String context() default "";
    
    /**
     * Arguments for parameterized messages
     */
    String[] args() default {};
}