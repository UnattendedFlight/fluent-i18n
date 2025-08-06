package io.github.unattendedflight.fluent.i18n.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * Configuration class for Fluent i18n library.
 * This class provides framework-agnostic configuration options for the library.
 */
public class FluentConfig {
    
    /**
     * The base path where translation files are located.
     * Default is "i18n" (relative to classpath).
     */
    private String basePath = "i18n";
    
    /**
     * Set of supported locales for the application.
     * Default includes English.
     */
    private Set<Locale> supportedLocales = Set.of(Locale.ENGLISH);
    
    /**
     * The default locale to use when no specific locale is set.
     * Default is English.
     */
    private Locale defaultLocale = Locale.ENGLISH;
    
    /**
     * Character encoding used for reading translation files.
     * Default is UTF-8.
     */
    private Charset encoding = StandardCharsets.UTF_8;
    
    /**
     * Message source type to use for loading translations.
     * Default is AUTO (automatically detect available formats).
     */
    private MessageSourceType messageSourceType = MessageSourceType.AUTO;
    
    /**
     * Whether to enable caching of translations.
     * Default is true.
     */
    private boolean enableCaching = true;
    
    /**
     * Cache timeout in seconds.
     * Default is 300 seconds (5 minutes).
     */
    private long cacheTimeoutSeconds = 300;
    
    /**
     * Whether to enable automatic reloading of translation files.
     * Default is false.
     */
    private boolean enableAutoReload = false;
    
    /**
     * Auto-reload check interval in seconds.
     * Default is 60 seconds.
     */
    private long autoReloadIntervalSeconds = 60;
    
    /**
     * Whether to enable fallback to original text when translation is not found.
     * Default is true.
     */
    private boolean enableFallback = true;
    
    /**
     * Whether to log missing translations.
     * Default is false.
     */
    private boolean logMissingTranslations = false;
    
    /**
     * Custom configuration properties.
     */
    private final Map<String, Object> customProperties = new HashMap<>();
    
    /**
     * Message source types supported by fluent-i18n.
     */
    public enum MessageSourceType {
        /**
         * Automatically detect the best available format (binary > JSON > properties).
         */
        AUTO,
        
        /**
         * Use binary format (most efficient).
         */
        BINARY,
        
        /**
         * Use JSON format.
         */
        JSON,
        
        /**
         * Use properties format.
         */
        PROPERTIES
    }
    
    /**
     * Creates a new FluentConfig with default settings.
     */
    public FluentConfig() {}
    
    /**
     * Creates a new FluentConfig with the specified base path.
     *
     * @param basePath the base path for translation files
     */
    public FluentConfig(String basePath) {
        this.basePath = basePath;
    }
    
    // Builder methods
    
    /**
     * Sets the base path for translation files.
     *
     * @param basePath the base path
     * @return this config for method chaining
     */
    public FluentConfig basePath(String basePath) {
        this.basePath = basePath;
        return this;
    }
    
    /**
     * Sets the supported locales.
     *
     * @param locales the supported locales
     * @return this config for method chaining
     */
    public FluentConfig supportedLocales(Set<Locale> locales) {
        this.supportedLocales = new HashSet<>(locales);
        return this;
    }
    
    /**
     * Sets the supported locales from locale strings.
     *
     * @param localeStrings the supported locale strings (e.g., "en", "fr", "de")
     * @return this config for method chaining
     */
    public FluentConfig supportedLocales(String... localeStrings) {
        Set<Locale> locales = new HashSet<>();
        for (String localeStr : localeStrings) {
            locales.add(Locale.forLanguageTag(localeStr));
        }
        this.supportedLocales = locales;
        return this;
    }
    
    /**
     * Sets the default locale.
     *
     * @param defaultLocale the default locale
     * @return this config for method chaining
     */
    public FluentConfig defaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        return this;
    }
    
    /**
     * Sets the default locale from a string.
     *
     * @param localeString the default locale string (e.g., "en")
     * @return this config for method chaining
     */
    public FluentConfig defaultLocale(String localeString) {
        this.defaultLocale = Locale.forLanguageTag(localeString);
        return this;
    }
    
    /**
     * Sets the character encoding.
     *
     * @param encoding the character encoding
     * @return this config for method chaining
     */
    public FluentConfig encoding(Charset encoding) {
        this.encoding = encoding;
        return this;
    }
    
    /**
     * Sets the message source type.
     *
     * @param messageSourceType the message source type
     * @return this config for method chaining
     */
    public FluentConfig messageSourceType(MessageSourceType messageSourceType) {
        this.messageSourceType = messageSourceType;
        return this;
    }
    
    /**
     * Sets the message source type from a string.
     *
     * @param messageSourceType the message source type string (e.g., "json", "binary", "properties", "auto")
     * @return this config for method chaining
     */
    public FluentConfig messageSourceType(String messageSourceType) {
        this.messageSourceType = MessageSourceType.valueOf(messageSourceType.toUpperCase());
        return this;
    }
    
    /**
     * Enables or disables caching.
     *
     * @param enableCaching whether to enable caching
     * @return this config for method chaining
     */
    public FluentConfig enableCaching(boolean enableCaching) {
        this.enableCaching = enableCaching;
        return this;
    }
    
    /**
     * Sets the cache timeout.
     *
     * @param cacheTimeoutSeconds the cache timeout in seconds
     * @return this config for method chaining
     */
    public FluentConfig cacheTimeoutSeconds(long cacheTimeoutSeconds) {
        this.cacheTimeoutSeconds = cacheTimeoutSeconds;
        return this;
    }
    
    /**
     * Enables or disables auto-reload.
     *
     * @param enableAutoReload whether to enable auto-reload
     * @return this config for method chaining
     */
    public FluentConfig enableAutoReload(boolean enableAutoReload) {
        this.enableAutoReload = enableAutoReload;
        return this;
    }
    
    /**
     * Sets the auto-reload interval.
     *
     * @param autoReloadIntervalSeconds the auto-reload interval in seconds
     * @return this config for method chaining
     */
    public FluentConfig autoReloadIntervalSeconds(long autoReloadIntervalSeconds) {
        this.autoReloadIntervalSeconds = autoReloadIntervalSeconds;
        return this;
    }
    
    /**
     * Enables or disables fallback to original text.
     *
     * @param enableFallback whether to enable fallback
     * @return this config for method chaining
     */
    public FluentConfig enableFallback(boolean enableFallback) {
        this.enableFallback = enableFallback;
        return this;
    }
    
    /**
     * Enables or disables logging of missing translations.
     *
     * @param logMissingTranslations whether to log missing translations
     * @return this config for method chaining
     */
    public FluentConfig logMissingTranslations(boolean logMissingTranslations) {
        this.logMissingTranslations = logMissingTranslations;
        return this;
    }
    
    /**
     * Sets a custom property.
     *
     * @param key the property key
     * @param value the property value
     * @return this config for method chaining
     */
    public FluentConfig customProperty(String key, Object value) {
        this.customProperties.put(key, value);
        return this;
    }
    
    // Getters
    
    public String getBasePath() { return basePath; }
    public Set<Locale> getSupportedLocales() { return new HashSet<>(supportedLocales); }
    public Locale getDefaultLocale() { return defaultLocale; }
    public Charset getEncoding() { return encoding; }
    public MessageSourceType getMessageSourceType() { return messageSourceType; }
    public boolean isEnableCaching() { return enableCaching; }
    public long getCacheTimeoutSeconds() { return cacheTimeoutSeconds; }
    public boolean isEnableAutoReload() { return enableAutoReload; }
    public long getAutoReloadIntervalSeconds() { return autoReloadIntervalSeconds; }
    public boolean isEnableFallback() { return enableFallback; }
    public boolean isLogMissingTranslations() { return logMissingTranslations; }
    public Map<String, Object> getCustomProperties() { return new HashMap<>(customProperties); }
    
    /**
     * Gets a custom property value.
     *
     * @param key the property key
     * @param defaultValue the default value if the property is not found
     * @param <T> the expected type
     * @return the property value or the default value
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomProperty(String key, T defaultValue) {
        Object value = customProperties.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * Creates a copy of this configuration.
     *
     * @return a new FluentConfig with the same settings
     */
    public FluentConfig copy() {
        FluentConfig copy = new FluentConfig(basePath);
        copy.supportedLocales = new HashSet<>(supportedLocales);
        copy.defaultLocale = defaultLocale;
        copy.encoding = encoding;
        copy.messageSourceType = messageSourceType;
        copy.enableCaching = enableCaching;
        copy.cacheTimeoutSeconds = cacheTimeoutSeconds;
        copy.enableAutoReload = enableAutoReload;
        copy.autoReloadIntervalSeconds = autoReloadIntervalSeconds;
        copy.enableFallback = enableFallback;
        copy.logMissingTranslations = logMissingTranslations;
        copy.customProperties.putAll(customProperties);
        return copy;
    }
} 