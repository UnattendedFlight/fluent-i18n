package io.github.unattendedflight.fluent.i18n.spring;

import io.github.unattendedflight.fluent.i18n.I18n;
import io.github.unattendedflight.fluent.i18n.config.FluentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Initializer for fluent-i18n that runs after Spring Boot has started.
 * This ensures that fluent-i18n is properly configured with Spring Boot.
 */
public class FluentI18nInitializer implements InitializingBean {
    
    private static final Logger logger = LoggerFactory.getLogger(FluentI18nInitializer.class);
    
    private final FluentConfig config;
    
    public FluentI18nInitializer(FluentConfig config) {
        this.config = config;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Initializing Fluent i18n with Spring Boot");
        
        // Set the default locale in fluent-i18n
        I18n.setCurrentLocale(config.getDefaultLocale());
        
        // Warm up the message source if auto-reload is enabled
        if (config.isEnableAutoReload()) {
            I18n.getMessageSource().warmUp(config.getSupportedLocales());
            logger.info("Warmed up translations for {} locales", config.getSupportedLocales().size());
        }
        
        logger.info("Fluent i18n initialized successfully with Spring Boot");
    }
} 