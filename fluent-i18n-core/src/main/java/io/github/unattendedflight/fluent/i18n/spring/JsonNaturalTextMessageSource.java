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
 * JSON-based implementation of NaturalTextMessageSource
 */
public class JsonNaturalTextMessageSource implements NaturalTextMessageSource {
    private static final Logger logger = LoggerFactory.getLogger(JsonNaturalTextMessageSource.class);
    
    private final String basename;
    private final Set<Locale> supportedLocales;
    private final Locale defaultLocale; // Default locale if not specified
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    
    private final Map<Locale, Map<String, String>> translationCache = new ConcurrentHashMap<>();
    private final Map<Locale, Instant> cacheTimestamps = new ConcurrentHashMap<>();
    
    public JsonNaturalTextMessageSource(String basename, Set<Locale> supportedLocales, Locale defaultLocale) {
        this.basename = basename;
        this.supportedLocales = supportedLocales;
        this.defaultLocale = defaultLocale != null ? defaultLocale : Locale.ENGLISH; // Fallback to English if no default provided
    }
    
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
    
    @Override
    public boolean exists(String hash, Locale locale) {
        Map<String, String> translations = getTranslations(locale);
        return translations.containsKey(hash);
    }
    
    @Override
    public Iterable<Locale> getSupportedLocales() {
        return supportedLocales;
    }
    
    @Override
    public void reload() {
        translationCache.clear();
        cacheTimestamps.clear();
        logger.info("Translation cache cleared");
    }
    
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