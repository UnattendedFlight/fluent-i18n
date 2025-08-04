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
 * Auto-configuration class for integrating Fluent I18n into a Spring Boot application.
 *
 * This configuration sets up the necessary beans and configurations to enable
 * internationalization support based on the application's properties. It supports
 * multiple message source types such as JSON, properties files, and binary formats.
 *
 * The configuration is activated if the application includes the `I18n` class in its classpath,
 * and the `fluent.i18n.enabled` property is enabled (default is true).
 *
 * Nested static classes define individual configurations for different types of message sources,
 * and each provides a bean of type {@code NaturalTextMessageSource}. These configurations can be
 * customized using the application properties with the prefix `fluent.i18n.message-source`.
 *
 * Additional beans provided by this auto-configuration:
 * 1. {@code FluentI18nInitializer} - Initializes Fluent I18n with the provided message source.
 * 2. {@code I18nTemplateUtils} - A utility for working with templates and message internationalization.
 *
 * Additionally, if Spring WebMvc is on the classpath and `fluent.i18n.web.enabled` is enabled
 * (default is true), an interceptor is registered via the {@code WebMvcConfigurer} interface to
 * handle I18n-specific web configurations.
 */
@AutoConfiguration
@ConditionalOnClass(I18n.class)
@EnableConfigurationProperties(FluentI18nProperties.class)
@ConditionalOnProperty(prefix = "fluent.i18n", name = "enabled", matchIfMissing = true)
public class FluentI18nAutoConfiguration {

    /**
     * Configuration class for setting up a JSON-based implementation of the {@link NaturalTextMessageSource}.
     * This configuration is activated if the property `fluent.i18n.message-source.type` is set to `json`
     * or if the property is missing.
     *
     * The JSON-based message source allows translations to be managed using JSON files,
     * configured with the base name, supported locales, and default locale provided in the application properties.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "fluent.i18n.message-source", name = "type", havingValue = "json", matchIfMissing = true)
    static class JsonMessageSourceConfiguration {
        
        /**
         * Creates a JSON-based implementation of the {@link NaturalTextMessageSource} for managing translations.
         * This method is a Spring bean definition that is applied if no other {@link NaturalTextMessageSource}
         * bean is defined in the application context.
         *
         * The created message source uses configuration properties for its setup, including the base name,
         * default locale, and supported locales. Optionally, it can also perform a warm-up operation
         * for preloading data for specified locales.
         *
         * @param properties the configuration properties providing details for initializing the
         *                   {@link NaturalTextMessageSource}, such as message source base name,
         *                   supported locales, default locale, and warm-up configuration
         * @return an instance of {@link NaturalTextMessageSource} that is configured
         *         and optionally warmed up for specified locales
         */
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

    /**
     * Configuration class for setting up a {@link PropertiesNaturalTextMessageSource} as
     * the {@link NaturalTextMessageSource} when the application is configured to use
     * a properties-backed message source.
     *
     * This configuration is activated when the `fluent.i18n.message-source.type` property
     * has the value "properties".
     *
     * It primarily defines a {@link NaturalTextMessageSource} bean using the properties
     * available under the {@link FluentI18nProperties} configuration, supporting
     * localization features.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "fluent.i18n.message-source", name = "type", havingValue = "properties")
    static class PropertiesMessageSourceConfiguration {
        
        /**
         * Creates and configures a {@link NaturalTextMessageSource} bean.
         * If a message source bean is not already defined, this method initializes
         * a {@link PropertiesNaturalTextMessageSource} using the provided configuration properties.
         * It also optionally performs a "warm-up" operation for specified locales if enabled.
         *
         * @param properties an instance of {@link FluentI18nProperties} containing configuration
         *                   details such as the basename of the message source, supported locales,
         *                   default locale, and warm-up settings
         * @return a configured instance of {@link NaturalTextMessageSource}, ready for use
         */
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

    /**
     * Configuration class for setting up a binary-based message source for managing internationalization.
     * This class is activated when the property `fluent.i18n.message-source.type` is set to `binary`.
     *
     * It defines a Spring bean of type {@link NaturalTextMessageSource} that uses a binary resource-based
     * implementation for handling internationalized messages.
     *
     * The configuration can preload or "warm up" the message source with translations for specific locales if the
     * warm-up feature is enabled in the application properties.
     *
     * Dependencies for this configuration include:
     * - {@link FluentI18nProperties} to retrieve configuration properties such as basename, supported locales, and default locale.
     * - {@link org.springframework.core.io.ResourceLoader} to load binary resources associated with translations.
     *
     * A condition is applied to ensure that if another {@link NaturalTextMessageSource} bean is already present in the
     * Spring context, this configuration will not create its own.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "fluent.i18n.message-source", name = "type", havingValue = "binary")
    static class BinaryMessageSourceConfiguration {
        
        /**
         * Creates and configures a {@link NaturalTextMessageSource} bean. This method sets up a binary-based
         * message source for internationalization, using configurations provided by `FluentI18nProperties`
         * and resources loaded through a `ResourceLoader`. Additionally, it supports warming up the message
         * source for specified or all supported locales if the warm-up feature is enabled.
         *
         * @param properties the {@code FluentI18nProperties} containing configuration details such as
         *                   message source basename, supported locales, and default locale
         * @param resourceLoader the {@code ResourceLoader} used to load translation resources in binary format
         * @return a {@code NaturalTextMessageSource} implementation that manages translations and supports
         *         binary resource-based message localization
         */
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

    /**
     * Creates and provides a {@link FluentI18nInitializer} bean, which initializes the
     * I18n static instance with the supplied {@link NaturalTextMessageSource}.
     * The method ensures the creation of an instance only if one does not already exist.
     *
     * @param messageSource a {@link NaturalTextMessageSource} used to manage translations,
     *                      resolve messages, and support internationalization.
     * @return a {@link FluentI18nInitializer} instance configured with the given message source.
     */
    @Bean
    @ConditionalOnMissingBean
    public FluentI18nInitializer fluentI18nInitializer(NaturalTextMessageSource messageSource) {
        return new FluentI18nInitializer(messageSource);
    }

    /**
     * Creates and provides an instance of {@link I18nTemplateUtils},
     * which is used for internationalization template utilities.
     * This bean is only created if no other bean of the same type is already defined.
     *
     * @return an instance of {@link I18nTemplateUtils}
     */
    @Bean
    @ConditionalOnMissingBean
    public I18nTemplateUtils i18nTemplateUtils() {
        return new I18nTemplateUtils();
    }

    /**
     * WebMvcConfiguration is a configuration class that integrates with Spring Web MVC
     * and enables the addition of an interceptor for internationalization.
     * This class is activated conditionally based on the presence of the Spring Web MVC framework
     * and a specific property configuration.
     *
     * It implements the {@link WebMvcConfigurer} interface, allowing customizations of the Spring Web MVC setup.
     * The class registers a {@link FluentI18nWebInterceptor} to the InterceptorRegistry to provide functionality
     * for resolving internationalized messages as configured through {@link FluentI18nProperties}.
     *
     * This configuration is enabled by default and can be controlled using the property prefix
     * `fluent.i18n.web.enabled`.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
    @ConditionalOnProperty(prefix = "fluent.i18n.web", name = "enabled", matchIfMissing = true)
    static class WebMvcConfiguration implements WebMvcConfigurer {
        
        /**
         * Holds the configuration properties for internationalization.
         * These properties are used to customize the behavior of the internationalization
         * interceptor, such as specifying default locales, message resolution strategies,
         * and enabling or disabling certain features.
         */
        private final FluentI18nProperties properties;
        
        /**
         * Constructor for WebMvcConfiguration.
         * Initializes a new instance with the specified FluentI18nProperties.
         *
         * @param properties the configuration properties for internationalization.
         *                   These properties define how the internationalization
         *                   features should be implemented, including default locale settings,
         *                   message resolution strategies, and feature toggles.
         */
        WebMvcConfiguration(FluentI18nProperties properties) {
            this.properties = properties;
        }
        
        /**
         * Adds custom interceptors to the Spring MVC application. This method is used to register
         * the {@link FluentI18nWebInterceptor}, which handles locale resolution and manages
         * internationalization settings for incoming requests.
         *
         * @param registry the {@link InterceptorRegistry} used to register interceptors. This
         *                 registry enables the configuration of interceptors that intercept
         *                 client requests and perform custom logic before or after they are handled
         *                 by controllers.
         */
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new FluentI18nWebInterceptor(properties));
        }
    }
}