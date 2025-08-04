package io.github.unattendedflight.fluent.i18n.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// FluentI18nProperties will be injected from autoconfigure module
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import io.github.unattendedflight.fluent.i18n.core.NaturalTextMessageSource;
import io.github.unattendedflight.fluent.i18n.core.TranslationResult;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of the {@link NaturalTextMessageSource} interface that manages translations
 * using properties files for different locales. Translations are retrieved based on a hash
 * key and a specified locale, with support for caching and fallback mechanisms.
 *
 * The class supports loading translations from properties files stored in the specified
 * location, clearing and reloading cached translations, and warming up translation data
 * for specified locales.
 */
public class PropertiesNaturalTextMessageSource implements NaturalTextMessageSource {
    /**
     * A static final logger instance used to log messages and events
     * related to the behavior and operations of the {@code PropertiesNaturalTextMessageSource} class.
     * Utilizes the SLF4J framework for logging.
     */
    private static final Logger logger = LoggerFactory.getLogger(PropertiesNaturalTextMessageSource.class);
    
    /**
     * The base name of the properties files containing translations.
     * This is used as the root name for finding language-specific translation files.
     */
    private final String basename;
    /**
     * A set of locales that the message source supports for translation.
     * This field determines the languages that can be resolved when fetching translations.
     */
    private final Set<Locale> supportedLocales;
    /**
     * The default locale used when no specific locale is provided.
     * It determines the fallback locale for resolving messages and translations.
     * Specified during the initialization of the {@code PropertiesNaturalTextMessageSource} instance.
     */
    private final Locale defaultLocale; // Default locale if not specified
    /**
     * Resource pattern resolver for loading and matching resources.
     *
     * Used to locate and load resource files based on defined patterns.
     * This resolver is particularly useful for retrieving localized message
     * files, translations, or other externalized configuration resources.
     * Plays a key role in supporting resource-based translation loading and
     * maintaining flexibility in defining resource paths or patterns.
     */
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    /**
     * The character encoding used to read properties files containing translations.
     * This determines how the text content within the property files is interpreted.
     * Proper configuration of this encoding is essential to ensure correct handling
     * of special characters and localization.
     */
    private final Charset encoding;
    
    /**
     * A thread-safe cache for storing translations corresponding to different locales.
     * The outer map uses {@link Locale} as keys to group translations by locale,
     * while the inner map stores translations where the key is the translation key,
     * and the value is the actual translated string.
     *
     * This cache is utilized to optimize the retrieval of translations,
     * avoiding redundant loading of translation resources for frequently accessed locales.
     */
    private final Map<Locale, Map<String, String>> translationCache = new ConcurrentHashMap<>();
    /**
     * Stores timestamps indicating the last time translation data was loaded or refreshed
     * for a specific locale in the translation cache.
     *
     * The keys of the map represent the locales, and the values represent the corresponding
     * timestamps of the most recent load or refresh as {@link Instant} values.
     *
     * This map is thread-safe, using a {@link ConcurrentHashMap}, to support concurrent
     * access and updates across multiple threads.
     */
    private final Map<Locale, Instant> cacheTimestamps = new ConcurrentHashMap<>();
    
    /**
     * Constructs a new instance of the PropertiesNaturalTextMessageSource class.
     *
     * @param basename the base name of the properties file or resource bundle to be used for loading translations
     * @param supportedLocales the set of locales that are supported by this message source
     * @param defaultLocale the default locale to be used if no specific locale is provided; if null, defaults to English
     */
    public PropertiesNaturalTextMessageSource(String basename, Set<Locale> supportedLocales, Locale defaultLocale) {
        this.basename = basename;
        this.supportedLocales = supportedLocales;
        this.encoding = StandardCharsets.UTF_8;
        this.defaultLocale = defaultLocale != null ? defaultLocale : Locale.ENGLISH; // Fallback to English if no default provided
    }
    
    /**
     * Resolves a translation for the given hash and locale. If a translation is found,
     * returns the translated text encapsulated in a {@code TranslationResult}.
     * If no translation is found, it logs the missing translation for non-default locales
     * and returns a {@code TranslationResult} indicating the text was not found.
     *
     * @param hash the unique identifier for the translation key
     * @param naturalText the natural text fallback to use if no translation is found
     * @param locale the locale to search translations for
     * @return a {@code TranslationResult} containing the found translation or the fallback text if not found
     */
    @Override
    public TranslationResult resolve(String hash, String naturalText, Locale locale) {
        Map<String, String> translations = getTranslations(locale);
        String translation = translations.get(hash);
        
        if (translation != null) {
            return TranslationResult.found(translation);
        }
        
        if (!locale.equals(defaultLocale)) { // Always log missing translations for non-default locales
            logger.debug("No translation found for hash '{}' (text: '{}') in locale '{}'", 
                        hash, naturalText, locale);
        }
        
        return TranslationResult.notFound(naturalText);
    }
    
    /**
     * Checks whether a translation exists for a specified hash in the given locale.
     *
     * @param hash the unique identifier for the translation (e.g., message key)
     * @param locale the locale in which the translation is being checked
     * @return true if a translation exists for the given hash in the specified locale, false otherwise
     */
    @Override
    public boolean exists(String hash, Locale locale) {
        Map<String, String> translations = getTranslations(locale);
        return translations.containsKey(hash);
    }
    
    /**
     * Retrieves the collection of locales supported by this message source.
     *
     * @return an Iterable of Locale objects representing the set of supported locales
     */
    @Override
    public Iterable<Locale> getSupportedLocales() {
        return supportedLocales;
    }
    
    /**
     * Reloads the translation cache by clearing the current contents
     * and resetting the associated cache timestamps. This method is
     * used to ensure that any changes to the translation data can be
     * reloaded and take effect immediately.
     *
     * It clears the mapping of cached translations and timestamps,
     * and logs a message indicating that the translation cache has
     * been successfully cleared.
     */
    @Override
    public void reload() {
        translationCache.clear();
        cacheTimestamps.clear();
        logger.info("Translation cache cleared");
    }
    
    /**
     * Retrieves a map of translation key-value pairs for the specified locale.
     * The method checks if the translations for the given locale are available in the cache
     * and valid based on the cache duration. If the cache is invalid or does not exist,
     * the translations are loaded, stored in the cache, and returned.
     *
     * @param locale the locale for which to retrieve translations
     * @return a map containing translation key-value pairs for the specified locale
     */
    private Map<String, String> getTranslations(Locale locale) {
        // Check cache validity
        Instant timestamp = cacheTimestamps.get(locale);
        Duration cacheDuration = Duration.ofMinutes(30); // Default cache duration
        
        if (timestamp != null && Duration.between(timestamp, Instant.now()).compareTo(cacheDuration) < 0) {
            return translationCache.getOrDefault(locale, Map.of());
        }
        
        // Load translations
        Map<String, String> translations = loadTranslations(locale);
        translationCache.put(locale, translations);
        cacheTimestamps.put(locale, Instant.now());
        
        return translations;
    }
    
    /**
     * Loads translations for the given {@link Locale} by attempting to find
     * translation files corresponding to the full locale and, if none are found,
     * for the language-only variant of the locale. The translations are loaded
     * into a map where the key represents the translation key and the value
     * represents the associated translated text.
     *
     * @param locale the locale for which translations should be loaded;
     *               must not be null and should represent a combination of
     *               language and optional country (e.g., en_US).
     * @return a map containing translation key-value pairs for the given locale.
     *         If no translations are found, an empty map is returned.
     */
    private Map<String, String> loadTranslations(Locale locale) {
        Map<String, String> translations = new ConcurrentHashMap<>();
        
        // Try with full locale first (e.g., en_US)
        loadTranslationsForLocale(locale.toString(), translations);
        
        // If nothing found and locale has country, try with language only (e.g., en)
        if (translations.isEmpty() && !locale.getCountry().isEmpty()) {
            loadTranslationsForLocale(locale.getLanguage(), translations);
        }
        
        logger.debug("Loaded {} translations for locale '{}'", translations.size(), locale);
        return translations;
    }
    
    /**
     * Loads translations for the given locale and populates the provided translations map.
     * It searches for property files matching the specified locale and reads their contents
     * into the translations map.
     *
     * @param localeString the locale string for which translations need to be loaded
     * @param translations a map that will be populated with key-value pairs of translations
     */
    private void loadTranslationsForLocale(String localeString, Map<String, String> translations) {
        String resourcePattern = "classpath*:" + basename + 
                               "_" + localeString + ".properties";
        
        try {
            Resource[] resources = resolver.getResources(resourcePattern);
            
            for (Resource resource : resources) {
                if (resource.exists()) {
                    loadTranslationsFromResource(resource, translations);
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to load translations for locale '{}': {}", localeString, e.getMessage());
        }
    }
    
    /**
     * Loads translations from the provided resource and populates the given translations map.
     * The method reads the resource using the specified encoding, parses its contents as
     * properties, and adds non-empty translation entries to the map.
     *
     * @param resource the resource containing the translations to be loaded
     * @param translations the map to populate with translation key-value pairs
     */
    private void loadTranslationsFromResource(Resource resource, Map<String, String> translations) {
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), encoding)) {
            Properties props = new Properties();
            props.load(reader);
            
            props.forEach((key, value) -> {
                if (key instanceof String && value instanceof String valueStr) {
                    if (!valueStr.trim().isEmpty()) {
                        translations.put((String) key, valueStr.trim());
                    }
                }
            });
            
            logger.debug("Loaded translations from resource: {}", resource.getURI());
            
        } catch (IOException e) {
            logger.warn("Failed to read translations from resource '{}': {}", resource, e.getMessage());
        }
    }

    /**
     * Pre-loads translation data for the specified locales into the cache for quicker access.
     * This method is intended to enhance performance by ensuring translations for the given
     * locales are available in memory prior to actual usage.
     *
     * @param locales an iterable collection of Locale objects for which translations should
     *                be loaded into the cache. If no locales are provided, the operation will
     *                be skipped, and a warning will be logged.
     */
    @Override
    public void warmUp(Iterable<Locale> locales) {
        logger.info("Warming up translations for {} locales", locales.spliterator().getExactSizeIfKnown());
        if (!locales.iterator().hasNext()) {
            logger.warn("No locales provided for warm-up, skipping");
            return;
        }
        for (Locale locale : locales) {
            try {
                logger.debug("Warming up translations for locale '{}'", locale);
                getTranslations(locale);
            } catch (Exception e) {
                logger.warn("Failed to warm up translations for locale '{}': {}", locale, e.getMessage());
            }
        }
        logger.info("Translation cache warmed up for {} locales", locales.spliterator().getExactSizeIfKnown());
    }
}