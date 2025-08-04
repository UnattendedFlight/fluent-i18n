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
 * Utility class for internationalization (i18n) providing text translation,
 * localization, and context-aware message handling.
 * This class operates using natural text as input and supports thread-local
 * locale settings, caching of message hashes, and integration with custom message
 * sources and hash generators.
 */
public final class I18n {
    private static volatile NaturalTextMessageSource messageSource;
    private static final ThreadLocal<Locale> currentLocale = new ThreadLocal<>();
    private static final Map<String, String> hashCache = new ConcurrentHashMap<>();
    private static HashGenerator hashGenerator = new Sha256HashGenerator();
    
    private I18n() {} // Utility class
    
    /**
     * Initializes the internationalization system with the specified message source.
     * This method sets the global message source that will be used for resolving translations.
     *
     * @param source the message source to be used for translation lookups and management
     */
    public static void initialize(NaturalTextMessageSource source) {
        messageSource = source;
    }
    
    /**
     * Sets the hash generator to be used for generating consistent hashes from natural text.
     * The specified hash generator will replace the current hash generator, if any,
     * and will be utilized by the system for text hash generation processes.
     *
     * @param generator The HashGenerator implementation to set, which will compute
     *                  hashes for natural text.
     */
    public static void setHashGenerator(HashGenerator generator) {
        hashGenerator = generator;
    }
    
    /**
     * Translates the given natural language text to the current locale, with optional arguments for formatting.
     * If no translation is found, the original natural text is returned as a fallback.
     *
     * @param naturalText The natural language text to be translated. If null, the method will return null.
     * @param args Optional arguments used to format the translated text.
     * @return The translated and optionally formatted text if a translation exists; otherwise, the original natural text formatted with the provided arguments.
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
     * Translates the provided natural text into the current locale, using optional arguments for formatting.
     * If a translation is not found for the specified text, the original natural text is returned as a fallback.
     *
     * @param naturalText The natural text to be translated. This is the default fallback text if no translation is found.
     * @param args Optional arguments for formatting the translated text or natural text.
     * @return The translated and formatted text if a translation is found; otherwise, the original natural text formatted with the provided arguments.
     */
    public static String t(String naturalText, Object... args) {
        return translate(naturalText, args);
    }

    /**
     * Resolves a message descriptor to a translated string using the current locale.
     *
     * @param descriptor the {@code MessageDescriptor} containing the information to resolve the message.
     *                   If null, the method will return null.
     * @return the translated string for the message descriptor based on the current locale, or null if the descriptor is null.
     */
    public static String resolve(MessageDescriptor descriptor) {
        if (descriptor == null) return null;

        Locale locale = getCurrentLocale();
        return resolve(descriptor, locale);
    }

    /**
     * Resolves a translation for the given message descriptor, locale, and arguments.
     *
     * The method attempts to fetch the translation associated with the descriptor's hash and natural text
     * from the `messageSource`. If a matching translation is found, it formats the result using the provided
     * arguments and locale. If no translation is found, it falls back to formatting the natural text of the descriptor.
     *
     * @param descriptor The message descriptor containing the hash, natural text, and optional arguments. Must not be null.
     * @param locale The target locale used for the translation and formatting.
     * @param args Optional arguments used for placeholder substitution in the resolved or fallback message.
     * @return The translated and formatted message if the hash resolves to a translation; otherwise, the formatted fallback natural text.
     */
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
     * Creates a {@code MessageDescriptor} for the given text variable and optional arguments.
     *
     * @param textVariable The natural text variable that forms the basis of the message descriptor.
     *                     Cannot be null or empty.
     * @param args         Additional arguments to include in the message descriptor.
     * @return A {@code MessageDescriptor} constructed with the provided text variable and arguments.
     * @throws IllegalArgumentException If {@code textVariable} is null or empty.
     */
    public static MessageDescriptor variable(String textVariable, Object... args) {
        if (textVariable == null || textVariable.isEmpty()) {
            throw new IllegalArgumentException("Text variable cannot be null or empty");
        }

        // Generate a hash for the variable text
        return new MessageDescriptor(getOrGenerateHash(textVariable), textVariable, args);
    }

    /**
     * Constructs a MessageDescriptor for the provided variable text.
     *
     * @param textVariable The natural text representing the variable.
     *                     Must not be null or empty.
     * @return A MessageDescriptor object for the given variable text.
     * @throws IllegalArgumentException If the textVariable is null or empty.
     */
    public static MessageDescriptor variable(String textVariable) {
        return variable(textVariable, new Object[0]);
    }
    
    /**
     * Creates a message descriptor for a given natural language text and arguments.
     * Constructs a {@link MessageDescriptor} object that encapsulates the hash of
     * the text, the text itself, and any additional arguments provided.
     *
     * @param naturalText The natural language text to describe. This is the main content
     *                    used for message resolution.
     * @param args        Optional arguments that can be used for formatting the text or
     *                    for message placeholders.
     * @return A {@link MessageDescriptor} object containing the generated hash, the
     *         provided natural text, and the associated arguments.
     */
    public static MessageDescriptor describe(String naturalText, Object... args) {
        String hash = getOrGenerateHash(naturalText);
        return new MessageDescriptor(hash, naturalText, args);
    }
    
    /**
     * Creates a {@link PluralBuilder} instance for handling pluralization of natural text
     * based on the provided count and the current locale.
     *
     * @param count the numerical value to determine the appropriate plural form
     * @return a {@link PluralBuilder} instance to configure and format pluralized text
     */
    public static PluralBuilder plural(Number count) {
        return new PluralBuilder(count, getCurrentLocale(), messageSource, hashGenerator);
    }

    /**
     * Resolves and retrieves a localized message for the given key using the current locale and arguments.
     * If a translation is found in the message source, it is formatted with the provided arguments and returned.
     * Otherwise, the original key is formatted and returned as a fallback.
     *
     * @param hash the key to look up in the message source; must not be null or empty
     * @param args the arguments to format the resolved message with; may be empty
     * @return the resolved and formatted message if found, otherwise the formatted original key; returns null if the key is null or empty
     */
    public static String resolveKey(String hash, Object... args) {
        if (hash == null || hash.isEmpty()) return null;

        Locale locale = getCurrentLocale();

        if (messageSource != null) {
            TranslationResult result = messageSource.resolve(hash, hash, locale);
            if (result.isFound()) {
                return formatMessage(result.getTranslation(), args, locale);
            }
        }

        // Fallback to original key
        return formatMessage(hash, args, locale);
    }

    /**
     * Resolves the given key and returns the corresponding value as a string.
     *
     * @param key the key to be resolved
     * @return the resolved value corresponding to the given key
     */
    public static String resolveKey(String key) {
        return resolveKey(key, new Object[0]);
    }
    
    /**
     * Creates a {@code ContextBuilder} for the specified translation context.
     *
     * The created {@code ContextBuilder} allows translations to be performed
     * within the specified context, providing additional semantics for resolving
     * translations of texts that can have different meanings based on context.
     *
     * @param context the context for the translation, which serves as a
     *                discriminator for translations of natural text
     * @return a {@code ContextBuilder} instance initialized with the specified
     *         context
     */
    public static ContextBuilder context(String context) {
        return new ContextBuilder(context, messageSource, hashGenerator);
    }
    
    /**
     * Sets the current locale for the application.
     * The specified locale will be used to determine the language and region-specific
     * behavior during internationalization processes.
     *
     * @param locale the {@code Locale} to be set as the current locale
     */
    public static void setCurrentLocale(Locale locale) {
        currentLocale.set(locale);
    }
    
    /**
     * Retrieves the current locale used for translations and formatting.
     * If a locale is not explicitly set, it falls back to Spring's LocaleContextHolder
     * or the system default locale as a last resort.
     *
     * @return the current locale in use; if not explicitly set, returns the fallback locale
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
     * Clears the current locale associated with the current thread.
     *
     * This method removes the locale setting stored in the thread-local variable.
     * It is typically used to clean up the locale information after processing
     * is complete, such as at the end of a request lifecycle in a web application.
     */
    public static void clearCurrentLocale() {
        currentLocale.remove();
    }
    
    /**
     * Retrieves the hash for the given natural text from the cache, or generates a new hash
     * if one does not already exist in the cache.
     *
     * @param naturalText The natural text for which the hash is to be retrieved or generated.
     * @return The hash associated with the given natural text.
     */
    private static String getOrGenerateHash(String naturalText) {
        return hashCache.computeIfAbsent(naturalText, hashGenerator::generateHash);
    }
    
    /**
     * Formats a message template by substituting placeholders with the provided arguments
     * based on the specified locale. If an error occurs during formatting, it falls back
     * to a simple substitution mechanism.
     *
     * @param template The message template containing placeholders (e.g., "{0}" or "{}")
     *                 to be replaced with arguments. Must not be null.
     * @param args     An array of objects to be used for substituting placeholders in
     *                 the template. Can be null or empty if no substitution is needed.
     * @param locale   The locale to be used for formatting. Must not be null.
     *
     * @return A string with placeholders in the template replaced with the respective
     *         arguments. If there are no arguments or a formatting error occurs,
     *         returns the original template with minimal substitution applied.
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
     * Formats the given template string by replacing placeholders with the specified arguments.
     * Placeholders in the template are denoted with `{n}` or `{}`, where `n` is the index of the argument
     * in the provided array.
     *
     * @param template the string containing placeholders to be replaced
     * @param args the arguments to replace the placeholders in the template
     * @return the formatted string with placeholders replaced by the corresponding arguments
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
     * Retrieves a map of message hashes, where keys represent natural texts and values
     * represent their corresponding generated hashes.
     *
     * @return a thread-safe map containing message hashes
     */
    public static Map<String, String> getMessageHashes() {
        return new ConcurrentHashMap<>(hashCache);
    }

    /**
     * Retrieves the current instance of the NaturalTextMessageSource used for resolving translations.
     *
     * @return the NaturalTextMessageSource instance.
     */
    public static NaturalTextMessageSource getMessageSource() {
        return messageSource;
    }
    
    /**
     * Registers a message by ensuring its hash is generated and cached.
     *
     * @param naturalText The natural text of the message to be registered.
     */
    public static void registerMessage(String naturalText) {
        getOrGenerateHash(naturalText);
    }
}