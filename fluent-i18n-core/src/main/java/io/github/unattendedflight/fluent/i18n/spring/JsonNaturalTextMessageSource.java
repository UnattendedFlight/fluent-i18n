package io.github.unattendedflight.fluent.i18n.spring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// FluentI18nProperties will be injected from autoconfigure module
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import io.github.unattendedflight.fluent.i18n.core.NaturalTextMessageSource;
import io.github.unattendedflight.fluent.i18n.core.TranslationResult;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of the {@link NaturalTextMessageSource} interface that provides
 * translations based on JSON resources. This class is designed to handle multiple
 * locales and uses caching mechanisms to optimize translation retrieval.
 *
 * Translations are expected to be stored in JSON files named with the pattern
 * `[basename]_[locale].json`. For example, `messages_en_US.json` or `messages_fr.json`.
 * JSON files should contain mappings between hash keys and translation strings.
 *
 * This class supports reloading of translation files and warming up the cache for
 * specific locales.
 */
public class JsonNaturalTextMessageSource implements NaturalTextMessageSource {
    /**
     * A logger instance used for logging messages and diagnostics within the
     * JsonNaturalTextMessageSource class. It leverages the LoggerFactory to provide
     * structured and efficient logging mechanisms.
     */
    private static final Logger logger = LoggerFactory.getLogger(JsonNaturalTextMessageSource.class);
    
    /**
     * Represents the base name of the configuration or resource files used
     * for translating natural text messages.
     * This value is fixed for the lifetime of the instance and typically
     * identifies a group of related translation files (e.g., a file prefix).
     */
    private final String basename;
    /**
     * A set of supported locales for translation purposes.
     *
     * This variable defines the collection of `Locale` objects
     * that are supported by the instance for resolving translations.
     * It is used to determine whether a given locale is available
     * for processing and translation within the context of the application.
     *
     * The set is immutable, ensuring thread-safe access and consistency
     * throughout the lifecycle of the application.
     */
    private final Set<Locale> supportedLocales;
    /**
     * The default locale used when no specific locale is provided.
     * This locale serves as the fallback for resolving translations
     * or natural text when a more specific locale is not available.
     * It is immutable and must be defined at the time of object construction.
     */
    private final Locale defaultLocale; // Default locale if not specified
    /**
     * A final instance of the {@link ObjectMapper} used for handling JSON serialization
     * and deserialization operations in the context of the JsonNaturalTextMessageSource class.
     * This instance is configured internally as needed to manage JSON-related tasks effectively.
     * It is likely used for loading and parsing translation data from JSON resources.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * A PathMatchingResourcePatternResolver is used to resolve resources
     * matching a specified path pattern. This resolver enables locating
     * all resources in the classpath or file system that satisfy the pattern,
     * which is especially useful for managing resource-based translations.
     *
     * The resolver is utilized to load translation resources and supports
     * compatible resource-loading mechanisms to facilitate internationalization.
     */
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    
    /**
     * A caching mechanism for storing translations mapped to their respective locales.
     * This map stores a nested structure where the outer map uses {@link Locale}
     * as keys to identify the specific locale, and the inner map contains key-value pairs
     * of translation entries for that locale.
     *
     * This cache is designed to optimize performance by reducing the need to repeatedly
     * load or compute translations for a given locale.
     */
    private final Map<Locale, Map<String, String>> translationCache = new ConcurrentHashMap<>();
    /**
     * A thread-safe map that stores the timestamps of the last cache updates for each locale.
     * This is used to track when translations for a specific locale were last loaded or refreshed.
     */
    private final Map<Locale, Instant> cacheTimestamps = new ConcurrentHashMap<>();
    
    /**
     * Constructs a new JsonNaturalTextMessageSource instance.
     *
     * @param basename the base name of the resource bundle to load translations from.
     * @param supportedLocales the set of locales that this message source supports for translation.
     * @param defaultLocale the default locale to use if a specific locale is not provided or translations
     *                      for a specific locale are unavailable. Defaults to English if null.
     */
    public JsonNaturalTextMessageSource(String basename, Set<Locale> supportedLocales, Locale defaultLocale) {
        this.basename = basename;
        this.supportedLocales = supportedLocales;
        this.defaultLocale = defaultLocale != null ? defaultLocale : Locale.ENGLISH; // Fallback to English if no default provided
    }
    
    /**
     * Resolves the translation for the specified hash and locale. If a translation is found,
     * it returns the corresponding translated value. If no translation is found, it logs
     * a debug message for non-default locales and returns a "not found" result with the
     * provided natural text as a fallback.
     *
     * @param hash the unique identifier for the text fragment to be translated
     * @param naturalText the fallback natural text to use if no translation is found
     * @param locale the locale to use for translation lookup
     * @return a {@code TranslationResult} containing either the found translation
     *         or a "not found" result with the fallback natural text
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
     * Checks if a translation exists for the given hash and locale.
     *
     * @param hash the hash key representing the natural text to be translated.
     * @param locale the locale for which the translation is being checked.
     * @return true if the translation exists for the given hash and locale, false otherwise.
     */
    @Override
    public boolean exists(String hash, Locale locale) {
        Map<String, String> translations = getTranslations(locale);
        return translations.containsKey(hash);
    }
    
    /**
     * Returns the set of locales supported by this message source for translation.
     *
     * @return an iterable collection of supported Locale objects
     */
    @Override
    public Iterable<Locale> getSupportedLocales() {
        return supportedLocales;
    }
    
    /**
     * Reloads the translation cache and clears all cached timestamps.
     * This ensures that any updates to the underlying translation resources
     * are retrieved and used. After the reload process, the relevant caches
     * are emptied, and a log entry is created to indicate the operation.
     */
    @Override
    public void reload() {
        translationCache.clear();
        cacheTimestamps.clear();
        logger.info("Translation cache cleared");
    }
    
    /**
     * Retrieves the translations for the specified locale. Translations are
     * cached to improve performance, and the cache is refreshed if it has
     * expired.
     *
     * @param locale the {@code Locale} for which translations should be retrieved.
     *               It represents the language and region for the translations.
     * @return a {@code Map<String, String>} containing the translation key-value
     *         pairs for the specified locale. If no translations exist for the
     *         specified locale, an empty map is returned.
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
     * Loads translations for the specified locale by searching appropriate resources.
     * It first tries to load translations for the full locale (e.g., en_US) and, if
     * no translations are found and the locale includes a country code, it attempts
     * to load translations for just the language (e.g., en).
     *
     * @param locale the locale for which translations are to be loaded.
     * @return a map containing the loaded translations, where the keys are
     *         message identifiers and the values are the corresponding translated
     *         text.
     */
    protected Map<String, String> loadTranslations(Locale locale) {
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
     * Loads translations for a specific locale into the provided translations map.
     *
     * This method searches for translation resources that match the specified locale string
     * and populates the provided map with translation keys and corresponding translations
     * found in the resources.
     *
     * @param localeString the locale identifier as a string (e.g., "en", "fr").
     * @param translations the map to populate with translation key-value pairs.
     */
    private void loadTranslationsForLocale(String localeString, Map<String, String> translations) {
        String resourcePattern = "classpath*:" + basename + 
                               "_" + localeString + ".json";
        
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
     * Loads translations from the provided resource and populates them into the provided translations map.
     * Each translation entry is identified by a hash key and its corresponding translation value.
     *
     * The method skips entries that are marked as metadata (keys starting with "_metadata") and ensures
     * that only valid, non-empty translation values are added to the translations map.
     *
     * @param resource the resource from which translations are to be loaded
     * @param translations the map where the loaded translations will be stored, using the hash as the key
     */
    private void loadTranslationsFromResource(Resource resource, Map<String, String> translations) {
        try (InputStream is = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            
            root.fields().forEachRemaining(entry -> {
                String hash = entry.getKey();
                JsonNode translationNode = entry.getValue();
                
                // Skip metadata
                if (hash.startsWith("_metadata")) {
                    return;
                }
                
                String translation = null;
                if (translationNode.isObject() && translationNode.has("translation")) {
                    translation = translationNode.get("translation").asText();
                } else if (translationNode.isTextual()) {
                    translation = translationNode.asText();
                }
                
                if (translation != null && !translation.isEmpty()) {
                    translations.put(hash, translation);
                }
            });
            
            logger.debug("Loaded translations from resource: {}", resource.getURI());
            
        } catch (IOException e) {
            logger.warn("Failed to read translations from resource '{}': {}", resource, e.getMessage());
        }
    }

    /**
     * Warms up the translation cache for the provided locales by preloading their translations.
     * This helps to ensure that the translations are available and cached before actual usage.
     *
     * @param locales an iterable collection of Locale objects for which the translations should be preloaded.
     *                If no locales are provided, the method logs a warning and skips the warming process.
     */
    @Override
    public void warmUp(Iterable<Locale> locales) {
        logger.info("Warming up translation cache for {} locales", locales.spliterator().getExactSizeIfKnown());
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