package io.github.unattendedflight.fluent.i18n.springboot.fluenti18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.github.unattendedflight.fluent.i18n.I18n;
import io.github.unattendedflight.fluent.i18n.config.FluentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

/**
 * FluentI18nWebInterceptor is a simple interceptor that ensures fluent-i18n's current locale
 * is synchronized with Spring's locale resolution and cleans up thread-local storage after request completion.
 * 
 * This interceptor works together with Spring's LocaleChangeInterceptor and our FluentLocaleResolver
 * to provide comprehensive locale support for web applications.
 */
public class FluentI18nWebInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(FluentI18nWebInterceptor.class);
    
    private final FluentConfig config;
    
    /**
     * Constructs a new instance of FluentI18nWebInterceptor with the specified FluentConfig.
     *
     * @param config the FluentConfig instance containing the configuration for the interceptor
     */
    public FluentI18nWebInterceptor(FluentConfig config) {
        this.config = config;
    }
    
    /**
     * Resolves and sets the locale for fluent-i18n using the proper resolution order:
     * 1. Query parameter (?locale=en)
     * 2. Session (set from previous query parameter) 
     * 3. Accept-Language header as final fallback
     *
     * @param request  the incoming HttpServletRequest object
     * @param response the outgoing HttpServletResponse object
     * @param handler  the selected handler to execute, for type and/or instance evaluation
     * @return {@code true} always to continue the request processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Locale locale = resolveLocale(request);
        if (locale != null) {
            I18n.setCurrentLocale(locale);
            logger.trace("Set fluent-i18n locale to: {}", locale);
        }
        
        return true;
    }
    
    /**
     * Resolves the locale in the proper order:
     * 1. Query parameter (?locale=en) - highest priority
     * 2. Session storage (from previous query parameter)
     * 3. Accept-Language header - lowest priority fallback
     */
    private Locale resolveLocale(HttpServletRequest request) {
        // 1. Check for locale query parameter first (highest priority)
        String localeParam = request.getParameter("lang");
        if (localeParam != null && !localeParam.trim().isEmpty()) {
            try {
                Locale locale = parseLocale(localeParam);
                if (locale != null && isSupportedLocale(locale)) {
                    // Store in session for future requests
                    request.getSession(true).setAttribute("fluent-i18n-locale", locale);
                    logger.debug("Found locale parameter '{}': {}, stored in session", localeParam, locale);
                    return locale;
                }
            } catch (Exception e) {
                logger.warn("Invalid locale parameter '{}': {}", localeParam, e.getMessage());
            }
        }
        
        // 2. Check session for stored locale (medium priority)
        try {
            Object sessionLocale = request.getSession(false) != null ? 
                request.getSession(false).getAttribute("fluent-i18n-locale") : null;
            if (sessionLocale instanceof Locale) {
                Locale locale = (Locale) sessionLocale;
                if (isSupportedLocale(locale)) {
                    logger.debug("Found locale in session: {}", locale);
                    return locale;
                }
            }
        } catch (Exception e) {
            logger.debug("Error reading locale from session: {}", e.getMessage());
        }
        
        // 3. Fall back to Accept-Language header (lowest priority)
        Locale headerLocale = request.getLocale();
        if (headerLocale != null && isSupportedLocale(headerLocale)) {
            logger.debug("Using Accept-Language header locale: {}", headerLocale);
            return headerLocale;
        }
        
        // 4. Final fallback to default locale
        Locale defaultLocale = config.getDefaultLocale();
        logger.debug("Using default locale: {}", defaultLocale);
        return defaultLocale;
    }
    
    /**
     * Parses a locale string into a Locale object.
     */
    private Locale parseLocale(String localeString) {
        if (localeString == null || localeString.trim().isEmpty()) {
            return null;
        }
        
        // Handle common formats: "en", "en_US", "en-US"
        String normalized = localeString.trim().replace('_', '-');
        return Locale.forLanguageTag(normalized);
    }
    
    /**
     * Checks if the locale is supported based on the configuration.
     */
    private boolean isSupportedLocale(Locale locale) {
        if (locale == null) {
            return false;
        }
        
        // Check exact match first
        if (config.getSupportedLocales().contains(locale)) {
            return true;
        }
        
        // Check if language matches any supported locale
        return config.getSupportedLocales().stream()
            .anyMatch(supported -> supported.getLanguage().equals(locale.getLanguage()));
    }
    
    /**
     * Clears the thread-local locale storage to prevent memory leaks and ensure clean state for subsequent requests.
     *
     * @param request the HttpServletRequest object that contains the request made by the client
     * @param response the HttpServletResponse object that contains the response sent to the client
     * @param handler the handler or controller that processed the request
     * @param ex an exception thrown during request processing, if any; otherwise null
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, @Nullable Exception ex) {
        // Clear locale from thread local after request to prevent memory leaks
        I18n.clearCurrentLocale();
        logger.trace("Cleared fluent-i18n thread-local locale");
    }
}