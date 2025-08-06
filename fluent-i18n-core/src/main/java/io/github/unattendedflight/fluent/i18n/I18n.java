package io.github.unattendedflight.fluent.i18n;

import io.github.unattendedflight.fluent.i18n.config.FluentConfig;
import io.github.unattendedflight.fluent.i18n.config.FluentConfigLoader;
import io.github.unattendedflight.fluent.i18n.core.ContextBuilder;
import io.github.unattendedflight.fluent.i18n.core.HashGenerator;
import io.github.unattendedflight.fluent.i18n.core.MessageDescriptor;
import io.github.unattendedflight.fluent.i18n.core.MessageFormatter;
import io.github.unattendedflight.fluent.i18n.core.MessageSourceFactory;
import io.github.unattendedflight.fluent.i18n.core.NaturalTextMessageSource;
import io.github.unattendedflight.fluent.i18n.core.PluralBuilder;
import io.github.unattendedflight.fluent.i18n.core.Sha256HashGenerator;
import io.github.unattendedflight.fluent.i18n.core.TranslationResult;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides internationalization (i18n) capabilities such as translation, locale management,
 * text formatting, and message registration. Central to enabling applications to support
 * multiple languages and cultures efficiently.
 *
 * Designed to work with natural text keys for clear and maintainable translation processes,
 * falling back to default text or system configurations when translations or settings are missing.
 *
 * Includes mechanisms for caching, formatting, and hash-based message retrieval to optimize
 * performance and accommodate dynamic or variable content.
 */
public final class I18n {
    private static volatile NaturalTextMessageSource messageSource;
    private static final ThreadLocal<Locale> currentLocale = new ThreadLocal<>();
    private static final Map<String, String> hashCache = new ConcurrentHashMap<>();
    private static HashGenerator hashGenerator = new Sha256HashGenerator();
    private static FluentConfig config;
    private static boolean initialized = false;
    
    private I18n() {} // Utility class
    
    /**
     * Initializes the internationalization system with a specified message source.
     *
     * @param source The primary source of natural language messages. Must not be null.
     *               Setting this determines the basis for all subsequent translations.
     *               Ensure the provided source is configured correctly, as any errors
     *               here may lead to missing or incorrect translations.
     */
    public static void initialize(NaturalTextMessageSource source) {
        messageSource = source;
        initialized = true;
    }
    
    /**
     * Configures the internationalization system using a fluent-style configuration.
     * This method ensures that subsequent operations rely on the provided configuration
     * and initializes the message source accordingly.
     *
     * @param fluentConfig The configuration object defining localization settings and behavior.
     *                     Must be pre-validated and non-null. A misconfigured object could
     *                     result in improper localization or translation errors.
     */
    public static void initialize(FluentConfig fluentConfig) {
        config = fluentConfig;
        messageSource = MessageSourceFactory.createMessageSource(fluentConfig);
        initialized = true;
    }
    
    /**
     * Convenience method to initialize the internationalization system using default FluentConfig.
     * Abstracts away the need to manually configure and pass the FluentConfig instance.
     *
     * Useful for setups where a preconfigured FluentConfig instance is expected to suffice.
     *
     * Relies on `FluentConfigLoader` to load configuration, so any misconfiguration or failure
     * in `FluentConfigLoader` impacts initialization. Ensure the loader is functioning correctly
     * and the underlying configuration is valid.
     *
     * If the system was already initialized, this may override specific settings depending on
     * the loaded configuration, which*/
    public static void initialize() {
        FluentConfigLoader loader = new FluentConfigLoader();
        FluentConfig loadedConfig = loader.load();
        initialize(loadedConfig);
    }
    
    /**
     * Configures and initializes the system using a specified configuration file path.
     *
     * @param configLocation The path to the configuration file. This must point to a valid,
     *                       readable file structured correctly for FluentConfig. An invalid path
     *                       or malformed configuration can cause initialization to fail,
     *                       potentially leaving the system in an unusable state.
     *
     * Business logic assumes this is used when a file-based configuration source is required
     * rather than a programmatically defined FluentConfig object. It's particularly useful in
     * environments where configuration is externalized or managed separately.
     * Ensure*/
    public static void initialize(String configLocation) {
        FluentConfigLoader loader = new FluentConfigLoader();
        FluentConfig loadedConfig = loader.load(configLocation);
        initialize(loadedConfig);
    }
    
    /**
     * Sets the hash generator implementation to be used for generating unique hashes
     * corresponding to natural text inputs within the internationalization system.
     *
     * @param generator the {@link HashGenerator} implementation, which must be capable
     *                  of producing consistent and collision-resistant hashes. Passing
     *                  a null value or a poorly implemented generator could result in
     *                  invalid message lookups or undefined behavior when resolving
     *                  translations, as hashes serve as keys for internal caching mechanisms.
     *
     * Business Logic:
     * Switching the hash generator should only happen during initialization or controlled
     * contexts where the application logic ensures no*/
    public static void setHashGenerator(HashGenerator generator) {
        hashGenerator = generator;
    }
    
    /**
     * Translates the given natural language text into the current locale's language, applying placeholders
     * where specified. Falls back to the original input if no translation is found.
     *
     * @param naturalText the input text to be translated; must be non-null to ensure meaningful processing.
     *                    Passing null returns null directly.
     * @param args optional arguments used to replace placeholders in the translated text. These should
     *             be compatible with the current locale's format.
     * @return the translated text in the target locale, formatted with the provided arguments if placeholders
     *         exist. If no translation is*/
    public static String translate(String naturalText, Object... args) {
        if (naturalText == null) return null;
        
        ensureInitialized();
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
     * Retrieves a localized version of the given natural language text, formatted with arguments.
     * Uses the current locale and the underlying message source to resolve translations.
     * If no translation is found, the input text is returned as a fallback.
     *
     * @param naturalText The human-readable natural language text to translate. Must be non-null.
     *                    This text serves as a key for translation and is returned if no
     *                    localization is available.
     * @param args Optional arguments to replace placeholders in the translated text. These allow
     *             for dynamic content insertion into the resolved message.
     * @return*/
    public static String t(String naturalText, Object... args) {
        return translate(naturalText, args);
    }

    /**
     * Resolves a localized translation for the given message descriptor based on the current locale.
     * If the descriptor is `null`, returns `null`. Ensures the internationalization system
     * is initialized before attempting resolution.
     *
     * The resolution uses the current application locale and falls back to the descriptor's
     * natural text if no translation exists. Edge cases include:
     * - Null descriptors returning `null` to signal absence of a message.
     * - Uninitialized systems triggering a deferred automatic initialization.
     *
     * @param descriptor The message descriptor representing the text to be translated.
     *                   Must not be `*/
    public static String resolve(MessageDescriptor descriptor) {
        if (descriptor == null) return null;
        
        ensureInitialized();
        return resolve(descriptor, getCurrentLocale());
    }

    /**
     * Resolves the natural language text associated with the provided message descriptor, formatted for the given locale
     * and arguments. This method prioritizes localized translations from a message source but falls back to the descriptor's
     * natural text if none are available.
     *
     * @param descriptor The message descriptor encapsulating the natural text and its unique hash. Must not be null;
     *                   if null, the method returns null.
     * @param locale     The desired locale for resolving translations. If no translations exist for this locale or
     *                   if the locale is null, fallback behavior applies.
     * @param args       Optional arguments to*/
    public static String resolve(MessageDescriptor descriptor, Locale locale, Object... args) {
        if (descriptor == null) return null;
        
        ensureInitialized();
        Locale currentLocale = getCurrentLocale();
        String hash = descriptor.getHash();

        if (messageSource != null && messageSource.exists(hash, locale)) {
          TranslationResult result = messageSource.resolve(hash, descriptor.getNaturalText(), locale);
           if (result.isFound()) {
               return formatMessage(result.getTranslation(), args, locale);
           }
        }
        return formatMessage(descriptor.getNaturalText(), args, locale);
    }

    /**
     * Constructs a `MessageDescriptor` using a variable template and optional arguments.
     * Ensures the internationalization system is properly initialized before proceeding.
     *
     * @param textVariable The template for the message, serving as an identifier for localization.
     *                     This should be a meaningful and unique natural language string.
     * @param args Optional arguments to format the message dynamically. Use these to inject
     *             variable parts into the message, such as user-specific data or placeholders.
     * @return A `MessageDescriptor` containing the template's unique hash, text, and provided arguments.
     *         Useful for deferred resolution and efficient localization handling*/
    public static MessageDescriptor variable(String textVariable, Object... args) {
        ensureInitialized();
        String hash = getOrGenerateHash(textVariable);
        return new MessageDescriptor(hash, textVariable, args);
    }

    /**
     * Generates a `MessageDescriptor` for a defined text variable, ensuring its hash is
     * uniquely calculated or retrieved. Used for handling reusable, localized text fragments.
     *
     * @param textVariable The human-readable text of the variable, which serves as an input for hash generation.
     *                     Must be consistently provided to ensure deterministic hash generation.
     *                     If null or empty, edge cases in downstream hash generation logic might occur.
     *
     * @return A `MessageDescriptor` object containing the hash, natural text, and default arguments (empty array).
     *         Ensures the text is encapsulated for future*/
    public static MessageDescriptor variable(String textVariable) {
        ensureInitialized();
        String hash = getOrGenerateHash(textVariable);
        return new MessageDescriptor(hash, textVariable, new Object[0]);
    }

    /**
     * Describes a message by associating it with a unique hash for localization and
     * metadata purposes. Ensures the internationalization system is initialized before use.
     * The hash is either fetched from a cache or generated to uniquely represent the natural text.
     * Arguments can be attached to support runtime message formatting.
     *
     * @param naturalText The natural language text of the message. Treated as a key for translation
     *                    and hash generation. Avoid null or dynamically constructed strings
     *                    for predictable behavior.
     * @param args        Optional runtime arguments used for message formatting. May be empty*/
    public static MessageDescriptor describe(String naturalText, Object... args) {
        ensureInitialized();
        String hash = getOrGenerateHash(naturalText);
        return new MessageDescriptor(hash, naturalText, args);
    }

    /**
     * Creates a PluralBuilder to handle pluralization logic based on the given count and current locale.
     * Ensures the internationalization system is initialized before proceeding.
     *
     * @param count The numeric value used to determine the pluralization form.
     *              Expected to be non-null. Edge cases like negative numbers or non-integral values will
     *              follow locale-specific rules, which may vary significantly.
     * @return A PluralBuilder preconfigured with the current locale and message source.
     *         Facilitates generation of grammatically correct plural phrases, even in
     *         complex linguistic contexts.
     */
    public static PluralBuilder plural(Number count) {
        ensureInitialized();
        return new PluralBuilder(count, getCurrentLocale(), messageSource, hashGenerator);
    }

    /**
     * Resolves a translation key (hash) to its localized message, formatting it with arguments if necessary.
     *
     * Translates the given hash for the current or default locale if available, formatting the result using the specified arguments.
     * Falls back to using the hash itself as the message template if no translation can be resolved.
     *
     * Handles scenarios where a translation might exist only for the default locale but not the current one.
     * Ensures graceful degradation in the absence of translations by using the hash as a fallback message.
     *
     * @param hash The key representing the text to translate. Expected to be a unique*/
    public static String resolveKey(String hash, Object... args) {
        ensureInitialized();
        Locale locale = getCurrentLocale();
        
        if (messageSource != null && messageSource.exists(hash, locale)) {
            TranslationResult result = messageSource.resolve(hash, hash, locale);
            if (result.isFound()) {
                return formatMessage(result.getTranslation(), args, locale);
            }
        }
      // We should not immediately fall back to formatMessage as we only have the hash.
      // We need to use the default locale to get the message.
      if (messageSource.exists(hash, config.getDefaultLocale())) {
        TranslationResult result = messageSource.resolve(hash, hash, config.getDefaultLocale());
        if (result.isFound()) {
          return formatMessage(result.getTranslation(), args, locale);
        }
      }

      return formatMessage(hash, args, locale);

    }

    /**
     * Resolves a unique message key (hash) to its localized translation using the current locale.
     * If the key is not found for the current locale, it falls back to the default locale.
     * If no translation is found, the hash itself is returned formatted with arguments.
     *
     * Business logic: Ensures messages are localized and formatted consistently across languages,
     * while handling scenarios where translations might be missing for specific locales.
     * Minimizes disruptions by using fallback mechanisms and formatting placeholders
     * directly into the hash when all else fails.
     *
     * Edge cases:
     * - If the*/
    public static String resolveKey(String key) {
        return resolveKey(key, new Object[0]);
    }

    /**
     * Creates a {@link ContextBuilder} for managing translations tied to a specific context.
     * Ensures the internationalization system is initialized before proceeding. The context
     * serves as a logical namespace for organizing and resolving translations, preventing
     * message collisions in systems with overlapping natural text.
     *
     * The returned builder allows defining and resolving translations scoped to the provided context,
     * utilizing the configured message source and hash generator.
     *
     * @param contextKey The logical grouping or namespace for translations. Used to scope and
     *                disambiguate natural text with identical wording but different meanings.
     */
    public static ContextBuilder context(String contextKey) {
        ensureInitialized();
        return new ContextBuilder(contextKey, contextKey, messageSource, hashGenerator);
    }

    /**
     * Updates the current thread's locale, influencing features like text formatting and resource bundles.
     * Setting null forces default locale fallback, but should be avoided.
     *
     * @param locale the Locale to set for this thread's context; ensure it's valid and supported in your application.
     */
    public static void setCurrentLocale(Locale locale) {
        currentLocale.set(locale);
        if (locale == null) {
          System.err.println("Warning: I18n.setCurrentLocale was set to null. To avoid potential issues, provide a Locale object.");
        }
    }
    
    /**
     * Resolves the current thread-specific locale, falling back to the system default if none is set.
     * Ensures threads without a specified locale still align with default system behavior.
     * Handle edge cases where thread-local storage might be uninitialized.
     *
     * @return the thread-specific locale if set; otherwise, the system default locale.
     */
    public static Locale getCurrentLocale() {
        Locale locale = currentLocale.get();
        if (locale != null) return locale;
        
        // Fallback to system default locale
        return Locale.getDefault();
    }
    
    /**
     * Clears the thread-local locale context to prevent it from unintentionally
     * leaking across requests or operations. Crucial for maintaining localization
     * correctness in multi-threaded environments. Ensure this is called when the
     * locale is no longer needed in the current thread.
     */
    public static void clearCurrentLocale() {
        currentLocale.remove();
    }
    
    /**
     * Ensures the system is initialized before proceeding.
     * Avoids redundant initialization by checking a flag to maintain efficiency.
     * Critical for preventing state inconsistencies in dependent operations.
     */
    private static void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
    
    /**
     * Retrieves the cached hash for the given natural text, or generates and caches a new one
     * if it does not already exist. Hashes are used as unique identifiers for natural text
     * to improve lookup efficiency and ensure consistency in translation operations.
     *
     * @param naturalText the natural language text requiring a hash; must not be null.
     *                    Caller should ensure text uniqueness if relying on hashes for resolution.
     * @return the hash string, either retrieved from the cache or newly generated.
     *         This value is guaranteed to be consistent for the same input text.
     *
     * Edge cases:
     */
    private static String getOrGenerateHash(String naturalText) {
        return hashCache.computeIfAbsent(naturalText, hashGenerator::generateHash);
    }
    
    /**
     * Formats a localized message template with arguments, handling both complex
     * locale-aware formatting and simple placeholder substitution for edge cases.
     * If locale-aware formatting fails, falls back to basic substitution to ensure
     * functionality, avoiding complete failure when arguments or format parsing
     * are problematic. Prioritizes robustness for varied input scenarios.
     *
     * @param template the message template containing placeholders (e.g., {0}, {1}, etc.)
     * @param args an array of objects to replace placeholders; may be null or empty
     *             to bypass formatting
     * @param locale the locale to apply during*/
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
     * Formats the provided template by replacing placeholders with argument values.
     * Supports indexed placeholders ({0}, {1}, etc.) and a single unindexed placeholder ({}).
     * Useful for lightweight template-based string generation without external libraries.
     *
     * Edge cases: Multiple `{}` placeholders are replaced by the same argument sequentially.
     * Does not handle cases where `{}` and indexed placeholders overlap in meaning within the template.
     *
     * @param template The string containing placeholders to replace.
     * @param args The values to substitute into the placeholders.
     * @return The formatted string with placeholders replaced by corresponding arguments.
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
     * Retrieves a thread-safe copy of all cached message hashes.
     *
     * Hashes serve as unique identifiers for natural language messages,
     * enabling efficient translation lookups or resolutions in a localized system.
     * Providing a concurrent map ensures safe access for high-concurrency environments
     * like web applications. Changes to the returned map do not affect the underlying cache.
     *
     * @return a copy of the current message hash cache mapping natural text to their unique hashes.
     */
    public static Map<String, String> getMessageHashes() {
        return new ConcurrentHashMap<>(hashCache);
    }
    
    /**
     * Provides access to the current message source used for resolving translations.
     * This is the core component responsible for localization in the application.
     *
     * @return the active {@code NaturalTextMessageSource} instance, allowing lookup and resolution
     *         of translations based on natural text keys. Ensures consistency across the
     *         internationalization system and must be initialized prior to usage to avoid
     *         unexpected behavior or null references.
     */
    public static NaturalTextMessageSource getMessageSource() {
        return messageSource;
    }
    
    /**
     * Registers a natural language message by ensuring it is assigned a unique hash.
     * This prepares the message for translation or lookup in the message source.
     *
     * @param naturalText The natural language message to register. If null, the method has no effect.
     *                    Ensure this text is meaningful and necessary for the translation context,
     *                    as excessive or redundant registrations may degrade performance over time.
     *
     * Use caution when introducing dynamic or user-generated text, as it could lead
     * to an overwhelming number of unique hash entries, complicating localization efforts
     * and consuming system resources unnecessarily.
     */
    public static void registerMessage(String naturalText) {
        if (naturalText != null) {
            getOrGenerateHash(naturalText);
        }
    }
    
    /**
     * Provides access to the current FluentConfig instance, allowing inspection or modification of runtime
     * configuration related to internationalization.
     *
     * @return The shared FluentConfig instance representing the global state. If the system has not been
     *         initialized, returns the default configuration or potentially null, depending on initialization status.
     *         Callers should ensure the system is properly initialized before accessing this, as behavior may be
     *         undefined otherwise.
     */
    public static FluentConfig getConfig() {
        return config;
    }
    
    /**
     * Indicates whether the internationalization system has been properly initialized.
     *
     * @return true if initialization has been successfully completed using one of the
     *         available `initialize` methods; false if the system is uninitialized, which
     *         may result in translation failures or improper behavior for localization tasks.
     *         Always ensure initialization is performed before utilizing translation functionality.
     */
    public static boolean isInitialized() {
        return initialized;
    }
}