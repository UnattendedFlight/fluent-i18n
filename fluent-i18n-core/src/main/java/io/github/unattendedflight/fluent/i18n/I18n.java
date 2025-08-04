package io.github.unattendedflight.fluent.i18n;

import io.github.unattendedflight.fluent.i18n.core.ContextBuilder;
import io.github.unattendedflight.fluent.i18n.core.HashGenerator;
import io.github.unattendedflight.fluent.i18n.core.MessageDescriptor;
import io.github.unattendedflight.fluent.i18n.core.MessageFormatter;
import io.github.unattendedflight.fluent.i18n.core.NaturalTextMessageSource;
import io.github.unattendedflight.fluent.i18n.core.PluralBuilder;
import io.github.unattendedflight.fluent.i18n.core.Sha256HashGenerator;
import io.github.unattendedflight.fluent.i18n.core.TranslationResult;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main entry point for natural text-based internationalization.
 * Allows developers to use actual human-readable text instead of artificial keys.
 */
public final class I18n {
    private static volatile NaturalTextMessageSource messageSource;
    private static final ThreadLocal<Locale> currentLocale = new ThreadLocal<>();
    private static final Map<String, String> hashCache = new ConcurrentHashMap<>();
    private static HashGenerator hashGenerator = new Sha256HashGenerator();
    
    private I18n() {} // Utility class
    
    /**
     * Initialize I18n with a message source
     */
    public static void initialize(NaturalTextMessageSource source) {
        messageSource = source;
    }
    
    /**
     * Set custom hash generator
     */
    public static void setHashGenerator(HashGenerator generator) {
        hashGenerator = generator;
    }
    
    /**
     * Translate natural text to the current locale
     * 
     * @param naturalText The original text in your development language
     * @param args Optional parameters for message formatting
     * @return Translated text or original text if translation not found
     */
    public static String translate(String naturalText, Object... args) {
        if (naturalText == null) return null;
        
        Locale locale = getCurrentLocale();
        String hash = getOrGenerateHash(naturalText);
        
        if (messageSource != null) {
            TranslationResult result = messageSource.resolve(hash, naturalText, locale);
            if (result.isFound()) {
                return formatMessage(result.getTranslation(), args, locale);
            }
        }
        
        // Fallback to original natural text
        return formatMessage(naturalText, args, locale);
    }
    
    /**
     * Short alias for translate()
     */
    public static String t(String naturalText, Object... args) {
        return translate(naturalText, args);
    }

    public static String resolve(MessageDescriptor descriptor) {
        if (descriptor == null) return null;

        Locale locale = getCurrentLocale();
        return resolve(descriptor, locale);
    }

    public static String resolve(MessageDescriptor descriptor, Locale locale, Object... args) {
        if (descriptor == null) return null;

        String hash = descriptor.getHash();

        if (messageSource != null) {
            TranslationResult result = messageSource.resolve(hash, descriptor.getNaturalText(), locale);
            if (result.isFound()) {
                return formatMessage(result.getTranslation(), args, locale);
            }
        }

        // Fallback to original natural text
        return formatMessage(descriptor.getNaturalText(), args, locale);
    }
    
    /**
     * Create a message descriptor for lazy evaluation
     */
    public static MessageDescriptor describe(String naturalText, Object... args) {
        String hash = getOrGenerateHash(naturalText);
        return new MessageDescriptor(hash, naturalText, args);
    }
    
    /**
     * Create a plural builder for handling different count forms
     */
    public static PluralBuilder plural(Number count) {
        return new PluralBuilder(count, getCurrentLocale(), messageSource, hashGenerator);
    }
    
    /**
     * Create a context-aware translation builder
     */
    public static ContextBuilder context(String context) {
        return new ContextBuilder(context, messageSource, hashGenerator);
    }
    
    /**
     * Set the current locale for this thread
     */
    public static void setCurrentLocale(Locale locale) {
        currentLocale.set(locale);
    }
    
    /**
     * Get the current locale for this thread
     */
    public static Locale getCurrentLocale() {
        Locale locale = currentLocale.get();
        if (locale != null) return locale;
        
        // Fallback to Spring's LocaleContextHolder if available
        try {
            return LocaleContextHolder.getLocale();
        } catch (Exception e) {
            return Locale.getDefault();
        }
    }
    
    /**
     * Clear the current locale for this thread
     */
    public static void clearCurrentLocale() {
        currentLocale.remove();
    }
    
    /**
     * Get or generate hash for natural text (with caching)
     */
    private static String getOrGenerateHash(String naturalText) {
        return hashCache.computeIfAbsent(naturalText, hashGenerator::generateHash);
    }
    
    /**
     * Format message with parameters using MessageFormat
     */
    private static String formatMessage(String template, Object[] args, Locale locale) {
        if (args == null || args.length == 0) {
            return template;
        }
        
        try {
            return MessageFormatter.format(template, args, locale);
        } catch (Exception e) {
            // Log warning and return template with simple substitution
            return simpleFormat(template, args);
        }
    }
    
    /**
     * Simple string formatting fallback 
     */
    private static String simpleFormat(String template, Object[] args) {
        String result = template;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(args[i]));
            result = result.replace("{}", String.valueOf(args[i])); // First {} replacement
        }
        return result;
    }
    
    /**
     * Get all cached message hashes (for extraction tools)
     */
    public static Map<String, String> getMessageHashes() {
        return new ConcurrentHashMap<>(hashCache);
    }

    public static NaturalTextMessageSource getMessageSource() {
        return messageSource;
    }
    
    /**
     * Pre-register a natural text message (for build-time optimization)
     */
    public static void registerMessage(String naturalText) {
        getOrGenerateHash(naturalText);
    }
}