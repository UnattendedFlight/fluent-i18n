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
 * Web interceptor for handling locale resolution and setting the current locale in I18n
 */
public class FluentI18nWebInterceptor implements HandlerInterceptor {
    
    private final FluentI18nProperties properties;
    
    public FluentI18nWebInterceptor(FluentI18nProperties properties) {
        this.properties = properties;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Locale locale = determineLocale(request);
        if (locale != null) {
            I18n.setCurrentLocale(locale);
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, @Nullable Exception ex) {
        // Clear locale from thread local after request
        I18n.clearCurrentLocale();
    }
    
    /**
     * Determine the locale for this request
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
     * Parse locale string
     */
    private Locale parseLocale(String localeString) {
        try {
            return Locale.forLanguageTag(localeString.replace("_", "-"));
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if locale is supported
     */
    private boolean isSupportedLocale(Locale locale) {
        return properties.getSupportedLocales().contains(locale) ||
               properties.getSupportedLocales().stream()
                   .anyMatch(supported -> supported.getLanguage().equals(locale.getLanguage()));
    }
    
    /**
     * Resolve locale from Accept-Language header
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