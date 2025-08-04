package io.github.unattendedflight.fluent.i18n.springboot.fluenti18n;

import org.springframework.beans.factory.InitializingBean;
import io.github.unattendedflight.fluent.i18n.I18n;
import io.github.unattendedflight.fluent.i18n.core.NaturalTextMessageSource;

/**
 * A class responsible for initializing the I18n system with a specified {@link NaturalTextMessageSource}.
 * This ensures that the internationalization (I18n) framework is properly configured
 * with a message source for resolving translations during runtime.
 *
 * Implements the {@link InitializingBean} interface to perform initialization logic
 * after all properties are set by the Spring container.
 */
public class FluentI18nInitializer implements InitializingBean {
    
    /**
     * Represents the source of natural text-based translations used by the internationalization (I18n) system.
     * This variable is an instance of {@link NaturalTextMessageSource}, which provides the functionality
     * to resolve translations, check the existence of translations, retrieve supported locales, and manage
     * translation resources.
     *
     * It is primarily used to integrate and configure the underlying message source required for runtime
     * translation lookups and management.
     */
    private final NaturalTextMessageSource messageSource;
    
    /**
     * Constructs a new instance of {@code FluentI18nInitializer} with the specified {@link NaturalTextMessageSource}.
     * This initializer sets up the message source for the internationalization (I18n) framework,
     * enabling proper resolution of translations based on natural text inputs.
     *
     * @param messageSource the {@link NaturalTextMessageSource} to be used for resolving translations
     */
    public FluentI18nInitializer(NaturalTextMessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    /**
     * Invoked after all properties for this bean have been set by the Spring container.
     *
     * This method initializes the I18n system by passing the configured {@link NaturalTextMessageSource}
     * to the {@link I18n#initialize} method. This ensures that the message source is set up
     * for resolving internationalized messages at runtime.
     */
    @Override
    public void afterPropertiesSet() {
        I18n.initialize(messageSource);
    }
}