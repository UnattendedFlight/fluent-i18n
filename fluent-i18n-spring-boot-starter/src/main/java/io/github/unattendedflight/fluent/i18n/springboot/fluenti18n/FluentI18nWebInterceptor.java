package io.github.unattendedflight.fluent.i18n.springboot.fluenti18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import io.github.unattendedflight.fluent.i18n.I18n;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

/**
 * FluentI18nWebInterceptor is an implementation of the HandlerInterceptor interface
 * used to manage locale resolution in a web application. It determines the user's
 * locale based on multiple factors such as URL parameters, session attributes,
 * Accept-Language headers, and default locale configuration.
 *
 * This interceptor ensures that the determined locale is set and properly handled
 * for each web request, and cleans up the thread-local context after request
 * processing is complete.
 */
public class FluentI18nWebInterceptor implements HandlerInterceptor {
    
    /**
     * Holds the configuration properties for FluentI18n within the interceptor.
     * Acts as a centralized access point to internationalization configurations
     * utilized for locale determination and request handling.
     */
    private final FluentI18nProperties properties;
    
    /**
     * Constructs a new instance of FluentI18nWebInterceptor with the specified FluentI18nProperties.
     *
     * @param properties the FluentI18nProperties instance containing the configuration for the interceptor
     */
    public FluentI18nWebInterceptor(FluentI18nProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Intercepts the incoming HTTP request to determine and set the current locale for the application.
     * This method checks various sources such as request parameters, session attributes, and headers
     * to resolve the locale and applies it globally for the current request lifecycle.
     *
     * @param request  the incoming HttpServletRequest object
     * @param response the outgoing HttpServletResponse object
     * @param handler  the selected handler to execute, for type and/or instance evaluation
     * @return {@code true} always to continue the request processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Locale locale = determineLocale(request);
        if (locale != null) {
            I18n.setCurrentLocale(locale);
        }
        return true;
    }
    
    /**
     * Callback method to be executed after the request is completed.
     * This method is typically used for cleanup purposes. In this implementation,
     * it clears the thread-local storage for the current locale to prevent
     * memory leaks or incorrect locale references in subsequent requests.
     *
     * @param request the HttpServletRequest object that contains the request made by the client
     * @param response the HttpServletResponse object that contains the response sent to the client
     * @param handler the handler or controller that processed the request
     * @param ex an exception thrown during request processing, if any; otherwise null
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, @Nullable Exception ex) {
        // Clear locale from thread local after request
        I18n.clearCurrentLocale();
    }
    
    /**
     * Determines the appropriate locale for the current request.
     *
     * The method checks multiple sources in the following order for resolving the locale:
     * 1. Locale specified as a URL parameter.
     * 2. Locale stored in the user's session.
     * 3. Locale specified in the `Accept-Language` header of the HTTP request.
     * 4. Application's configured default locale, used as a fallback if no other sources provide a valid locale.
     *
     * If supported, the resolved locale is stored in the user's session for future requests.
     *
     * @param request the current {@link HttpServletRequest} object, which provides information regarding the user's request
     * @return the resolved {@link Locale}, taking into consideration user-specified preferences and application configuration
     */
    private Locale determineLocale(HttpServletRequest request) {
        // 1. Check URL parameter
        String localeParam = request.getParameter(properties.getWeb().getLocaleParameter());
        if (StringUtils.hasText(localeParam)) {
            Locale locale = parseLocale(localeParam);
            if (locale != null && isSupportedLocale(locale)) {
                // Store in session for future requests
                HttpSession session = request.getSession(true);
                session.setAttribute("locale", locale);
                return locale;
            }
        }
        
        // 2. Check session
        HttpSession session = request.getSession(false);
        if (session != null) {
            Locale sessionLocale = (Locale) session.getAttribute("locale");
            if (sessionLocale != null && isSupportedLocale(sessionLocale)) {
                return sessionLocale;
            }
        }
        
        // 3. Check Accept-Language header
        if (properties.getWeb().isUseAcceptLanguageHeader()) {
            Locale headerLocale = resolveFromAcceptLanguage(request);
            if (headerLocale != null && isSupportedLocale(headerLocale)) {
                return headerLocale;
            }
        }
        
        // 4. Use default locale
        return properties.getDefaultLocale();
    }
    
    /**
     * Parses a locale string into a {@link Locale} object.
     *
     * @param localeString the locale string to be parsed, expected in a format that can be recognized
     *                     by {@link Locale#forLanguageTag(String)} after replacing underscores with hyphens
     * @return the corresponding {@link Locale} object if parsing is successful, or null if an exception occurs
     */
    private Locale parseLocale(String localeString) {
        try {
            return Locale.forLanguageTag(localeString.replace("_", "-"));
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Determines if the provided locale is supported by checking against the set of supported locales.
     * A locale is considered supported if it is either present in the supported locales set or
     * if its language matches the language of any locale in the supported set.
     *
     * @param locale the locale to check for support
     * @return true if the locale or its language is supported, otherwise false
     */
    private boolean isSupportedLocale(Locale locale) {
        return properties.getSupportedLocales().contains(locale) ||
               properties.getSupportedLocales().stream()
                   .anyMatch(supported -> supported.getLanguage().equals(locale.getLanguage()));
    }
    
    /**
     * Resolves a {@link Locale} from the "Accept-Language" header present in the provided HTTP request.
     * This method first attempts to use the {@link LocaleResolver} if available in the request context.
     * If no resolver is available or an error occurs, it falls back to parsing the "Accept-Language" header manually.
     *
     * @param request the {@link HttpServletRequest} containing the "Accept-Language" header and other request-related information
     * @return the resolved {@link Locale}, or null if no valid locale can be determined
     */
    private Locale resolveFromAcceptLanguage(HttpServletRequest request) {
        try {
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            if (localeResolver != null) {
                return localeResolver.resolveLocale(request);
            }
        } catch (Exception e) {
            // Fallback to simple header parsing
        }
        
        String acceptLanguage = request.getHeader("Accept-Language");
        if (StringUtils.hasText(acceptLanguage)) {
            String[] languages = acceptLanguage.split(",");
            for (String lang : languages) {
                String langCode = lang.trim().split(";")[0].trim();
                Locale locale = parseLocale(langCode.replace("-", "_"));
                if (locale != null) {
                    return locale;
                }
            }
        }
        return null;
    }
}