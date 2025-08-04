package io.github.unattendedflight.fluent.i18n.springboot.fluenti18n;

import java.util.Locale;
import java.util.Set;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.unattendedflight.fluent.i18n.I18n;
import io.github.unattendedflight.fluent.i18n.core.NaturalTextMessageSource;
import io.github.unattendedflight.fluent.i18n.spring.JsonNaturalTextMessageSource;
import io.github.unattendedflight.fluent.i18n.spring.PropertiesNaturalTextMessageSource;
import io.github.unattendedflight.fluent.i18n.spring.BinaryNaturalTextMessageSource;
// Web interceptor is now in this package
import io.github.unattendedflight.fluent.i18n.spring.I18nTemplateUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration for Fluent i18n
 */
@AutoConfiguration
@ConditionalOnClass(I18n.class)
@EnableConfigurationProperties(FluentI18nProperties.class)
@ConditionalOnProperty(prefix = "fluent.i18n", name = "enabled", matchIfMissing = true)
public class FluentI18nAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "fluent.i18n.message-source", name = "type", havingValue = "json", matchIfMissing = true)
    static class JsonMessageSourceConfiguration {
        
        @Bean
        @ConditionalOnMissingBean
        public NaturalTextMessageSource naturalTextMessageSource(FluentI18nProperties properties) {
            NaturalTextMessageSource src = new JsonNaturalTextMessageSource(
                properties.getMessageSource().getBasename(),
                properties.getSupportedLocales(),
                properties.getDefaultLocale()
            );
            if (properties.getWarmUp().isEnabled()) {
                Set<Locale> locales = properties.getWarmUp().getLocales();
                if (locales == null || locales.isEmpty()) {
                    locales = properties.getSupportedLocales();
                }
                src.warmUp(locales);
            }
            return src;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "fluent.i18n.message-source", name = "type", havingValue = "properties")
    static class PropertiesMessageSourceConfiguration {
        
        @Bean
        @ConditionalOnMissingBean
        public NaturalTextMessageSource naturalTextMessageSource(FluentI18nProperties properties) {
            NaturalTextMessageSource src = new PropertiesNaturalTextMessageSource(
                properties.getMessageSource().getBasename(),
                properties.getSupportedLocales(),
                properties.getDefaultLocale()
            );
            if (properties.getWarmUp().isEnabled()) {
                Set<Locale> locales = properties.getWarmUp().getLocales();
                if (locales == null || locales.isEmpty()) {
                    locales = properties.getSupportedLocales();
                }
                src.warmUp(locales);
            }
            return src;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "fluent.i18n.message-source", name = "type", havingValue = "binary")
    static class BinaryMessageSourceConfiguration {
        
        @Bean
        @ConditionalOnMissingBean
        public NaturalTextMessageSource naturalTextMessageSource(FluentI18nProperties properties, 
                                                              org.springframework.core.io.ResourceLoader resourceLoader) {
            NaturalTextMessageSource src = new BinaryNaturalTextMessageSource(
                resourceLoader,
                properties.getMessageSource().getBasename(),
                properties.getSupportedLocales(),
                properties.getDefaultLocale()
            );
            if (properties.getWarmUp().isEnabled()) {
                Set<Locale> locales = properties.getWarmUp().getLocales();
                if (locales == null || locales.isEmpty()) {
                    locales = properties.getSupportedLocales();
                }
                src.warmUp(locales);
            }
            return src;
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public FluentI18nInitializer fluentI18nInitializer(NaturalTextMessageSource messageSource) {
        return new FluentI18nInitializer(messageSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public I18nTemplateUtils i18nTemplateUtils() {
        return new I18nTemplateUtils();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
    @ConditionalOnProperty(prefix = "fluent.i18n.web", name = "enabled", matchIfMissing = true)
    static class WebMvcConfiguration implements WebMvcConfigurer {
        
        private final FluentI18nProperties properties;
        
        WebMvcConfiguration(FluentI18nProperties properties) {
            this.properties = properties;
        }
        
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new FluentI18nWebInterceptor(properties));
        }
    }
}