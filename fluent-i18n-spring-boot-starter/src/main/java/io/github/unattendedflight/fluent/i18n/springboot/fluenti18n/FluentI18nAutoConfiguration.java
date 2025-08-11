package io.github.unattendedflight.fluent.i18n.springboot.fluenti18n;

import io.github.unattendedflight.fluent.i18n.config.FluentConfig;
import io.github.unattendedflight.fluent.i18n.config.FluentConfigLoader;
import io.github.unattendedflight.fluent.i18n.spring.FluentI18nSpringConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Auto-configuration for Fluent i18n Spring Boot integration.
 * This configuration automatically sets up fluent-i18n when Spring Boot is detected.
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.web.servlet.LocaleResolver")
@Import(FluentI18nSpringConfig.class)
public class FluentI18nAutoConfiguration {
    
    /**
     * Provides the FluentConfig bean loaded from fluent.yml or application configuration.
     */
    @Bean
    @ConditionalOnMissingBean(FluentConfig.class)
    public FluentConfig fluentConfig() {
        FluentConfigLoader loader = new FluentConfigLoader();
        return loader.load();
    }
    
    /**
     * Web configuration that adds the fluent-i18n interceptor.
     */
    @Configuration
    public static class FluentI18nWebConfiguration implements WebMvcConfigurer {
        
        private final FluentConfig config;
        
        public FluentI18nWebConfiguration(FluentConfig config) {
            this.config = config;
        }
        
        @Bean
        @ConditionalOnMissingBean(FluentI18nWebInterceptor.class)
        public FluentI18nWebInterceptor fluentI18nWebInterceptor() {
            return new FluentI18nWebInterceptor(config);
        }
        
        @Override
        public void addInterceptors(@NonNull InterceptorRegistry registry) {
            // Add our interceptor that handles locale resolution in the proper order
            registry.addInterceptor(fluentI18nWebInterceptor());
        }
    }
}