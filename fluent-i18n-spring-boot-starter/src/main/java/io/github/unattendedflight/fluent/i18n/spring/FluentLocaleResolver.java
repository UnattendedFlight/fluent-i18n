package io.github.unattendedflight.fluent.i18n.spring;

import io.github.unattendedflight.fluent.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;

/**
 * Locale resolver that integrates fluent-i18n with Spring's locale resolution.
 * This resolver supports session-based locale storage and request parameter detection.
 * 
 * Features:
 * - Session-based locale storage (persistent across requests)
 * - Request parameter detection (e.g., ?lang=en, ?locale=fr)
 * - Accept-Language header fallback
 * - Integration with fluent-i18n's locale management
 */
public class FluentLocaleResolver implements LocaleResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(FluentLocaleResolver.class);
    
    /**
     * Name of the session attribute that holds the Locale.
     * Must match the attribute name used by FluentI18nWebInterceptor.
     */
    public static final String LOCALE_SESSION_ATTRIBUTE_NAME = "fluent-i18n-locale";
    
    /**
     * Default name of the request parameter to check for locale changes.
     */
    public static final String DEFAULT_PARAM_NAME = "locale";
    
    private String paramName = DEFAULT_PARAM_NAME;
    private Locale defaultLocale;
    
    /**
     * Sets the name of the parameter that contains a locale specification
     * in a request. Default is "locale".
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
    
    /**
     * Returns the name of the parameter that contains a locale specification.
     */
    public String getParamName() {
        return this.paramName;
    }
    
    /**
     * Sets a default Locale that this resolver will return if no other locale found.
     */
    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
    
    /**
     * Returns the default Locale that this resolver will return if no other locale found.
     */
    protected Locale getDefaultLocale() {
        return this.defaultLocale;
    }
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Locale locale = null;
        
        // 1. Check for locale parameter in request (e.g., ?locale=en)
        String localeParam = request.getParameter(getParamName());
        if (localeParam != null && !localeParam.isEmpty()) {
            try {
                locale = parseLocaleValue(localeParam);
                logger.debug("Found locale parameter '{}': {}", getParamName(), locale);
                
                // Store the new locale in session for future requests
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.setAttribute(LOCALE_SESSION_ATTRIBUTE_NAME, locale);
                    logger.debug("Stored locale {} in session", locale);
                }
            } catch (IllegalArgumentException ex) {
                logger.warn("Invalid locale parameter '{}': {}", localeParam, ex.getMessage());
            }
        }
        
        // 2. Check session for stored locale
        if (locale == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                locale = (Locale) session.getAttribute(LOCALE_SESSION_ATTRIBUTE_NAME);
                if (locale != null) {
                    logger.debug("Found locale in session: {}", locale);
                }
            }
        }
        
        // 3. Fall back to Accept-Language header
        if (locale == null) {
            locale = request.getLocale();
            if (locale != null) {
                logger.debug("Using Accept-Language header locale: {}", locale);
            }
        }
        
        // 4. Fall back to default locale
        if (locale == null) {
            locale = getDefaultLocale();
            if (locale != null) {
                logger.debug("Using default locale: {}", locale);
            }
        }
        
        // 5. Final fallback to system default
        if (locale == null) {
            locale = Locale.getDefault();
            logger.debug("Using system default locale: {}", locale);
        }
        
        // Set the locale in fluent-i18n for this request
        I18n.setCurrentLocale(locale);
        logger.trace("Set current locale for fluent-i18n: {}", locale);
        
        return locale;
    }
    
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        if (locale != null) {
            // Store locale in session for persistence
            HttpSession session = request.getSession(true);
            session.setAttribute(LOCALE_SESSION_ATTRIBUTE_NAME, locale);
            logger.debug("Explicitly set locale {} in session", locale);
        } else {
            // Clear locale from session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(LOCALE_SESSION_ATTRIBUTE_NAME);
                logger.debug("Cleared locale from session");
            }
        }
        
        // Set the locale in fluent-i18n
        I18n.setCurrentLocale(locale);
        logger.trace("Set current locale for fluent-i18n: {}", locale);
    }
    
    /**
     * Parse the given locale value coming from a request parameter.
     * <p>The default implementation calls {@link Locale#forLanguageTag(String)}.
     * Can be overridden in subclasses.
     * 
     * @param localeValue the locale value to parse
     * @return the corresponding Locale instance
     * @throws IllegalArgumentException if the locale value is invalid
     */
    protected Locale parseLocaleValue(String localeValue) {
        if (localeValue == null || localeValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Locale value cannot be null or empty");
        }
        
        // Handle common formats: "en", "en_US", "en-US"
        String normalizedValue = localeValue.trim().replace('_', '-');
        
        try {
            Locale locale = Locale.forLanguageTag(normalizedValue);
            
            // Validate that the locale was properly parsed
            if (locale.getLanguage().isEmpty()) {
                throw new IllegalArgumentException("Invalid locale format: " + localeValue);
            }
            
            return locale;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to parse locale: " + localeValue, ex);
        }
    }
}