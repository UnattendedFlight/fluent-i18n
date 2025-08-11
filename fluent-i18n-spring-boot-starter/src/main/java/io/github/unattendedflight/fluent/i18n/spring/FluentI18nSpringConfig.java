package io.github.unattendedflight.fluent.i18n.spring;

import io.github.unattendedflight.fluent.i18n.I18n;
import io.github.unattendedflight.fluent.i18n.config.FluentConfig;
import io.github.unattendedflight.fluent.i18n.core.MessageSourceFactory;
import io.github.unattendedflight.fluent.i18n.core.NaturalTextMessageSource;
import io.github.unattendedflight.fluent.i18n.springboot.fluenti18n.FluentI18nInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.LocaleResolver;



/**
 * Spring configuration for Fluent i18n integration.
 * This configuration automatically sets up fluent-i18n with Spring Boot.
 */
@Configuration
public class FluentI18nSpringConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FluentI18nSpringConfig.class);

    @Bean
    @Primary
    @ConditionalOnMissingBean(I18nTemplateUtils.class)
    public I18nTemplateUtils i18nTemplateUtils() {
        return new I18nTemplateUtils();
    }

    /**
     * Creates a NaturalTextMessageSource bean that integrates with Spring's locale resolution.
     * This message source will be used by fluent-i18n for translation lookups using classpath-based loading.
     *
     * @param fluentConfig the fluent configuration
     * @return the NaturalTextMessageSource bean
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(NaturalTextMessageSource.class)
    public NaturalTextMessageSource naturalTextMessageSource(FluentConfig fluentConfig) {
        // Create a Spring-aware message source that uses classpath-based loading
      NaturalTextMessageSource messageSource = MessageSourceFactory.createMessageSource(fluentConfig);
        
        // Initialize the I18n system with this message source
        I18n.initialize(messageSource);
        
        logger.info("Initialized Fluent i18n with Spring-aware message source (classpath-based)");
        return messageSource;
    }
    
    /**
     * Creates a LocaleResolver that integrates with fluent-i18n.
     * This resolver will set the current locale for fluent-i18n based on Spring's locale resolution.
     * It supports session and request-based locale detection.
     *
     * @return the LocaleResolver bean
     */
    @Bean(name = "localeResolver")
    @ConditionalOnMissingBean(name = "localeResolver")
    public LocaleResolver localeResolver() {
        return new FluentLocaleResolver();
    }
    
    /**
     * Provides a Spring-managed bean to initialize the Fluent I18n system with the given message source.
     * Ensures i18n translations are resolved consistently within the application context.
     *
     * @param messageSource the message source providing translations for the i18n system; must not be null.
     *                      If misconfigured, translation lookups may fail or fallback to default behavior.
     * @return a fully initialized {@link FluentI18nInitializer} for integration with the i18n system.
     */
    @Bean
    public FluentI18nInitializer fluentI18nInitializer(NaturalTextMessageSource messageSource) {
        return new FluentI18nInitializer(messageSource);
    }
} 