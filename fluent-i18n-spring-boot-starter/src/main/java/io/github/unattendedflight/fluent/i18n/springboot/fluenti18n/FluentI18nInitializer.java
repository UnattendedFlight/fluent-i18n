package io.github.unattendedflight.fluent.i18n.springboot.fluenti18n;

import org.springframework.beans.factory.InitializingBean;
import io.github.unattendedflight.fluent.i18n.I18n;
import io.github.unattendedflight.fluent.i18n.core.NaturalTextMessageSource;

/**
 * Initializes the I18n static instance with the configured message source
 */
public class FluentI18nInitializer implements InitializingBean {
    
    private final NaturalTextMessageSource messageSource;
    
    public FluentI18nInitializer(NaturalTextMessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    @Override
    public void afterPropertiesSet() {
        I18n.initialize(messageSource);
    }
}