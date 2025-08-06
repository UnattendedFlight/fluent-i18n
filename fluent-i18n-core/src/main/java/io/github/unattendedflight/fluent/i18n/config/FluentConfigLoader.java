package io.github.unattendedflight.fluent.i18n.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

/**
 * Loads Fluent i18n configuration from various sources.
 * Supports loading from classpath, file system, or programmatic configuration.
 */
public class FluentConfigLoader {

    private static final Logger logger = Logger.getLogger(FluentConfigLoader.class.getName());
    private static final String DEFAULT_CONFIG_FILE = "fluent.yml";
    private static final String CLASSPATH_CONFIG_FILE = "classpath:" + DEFAULT_CONFIG_FILE;
    
    private final ObjectMapper yamlMapper;
    
    public FluentConfigLoader() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }
    
    /**
     * Loads configuration from the default location (classpath:fluent.yml).
     *
     * @return the loaded configuration
     */
    public FluentConfig load() {
        return load(CLASSPATH_CONFIG_FILE);
    }
    
    /**
     * Loads configuration from the specified location.
     * Supports classpath: prefix for classpath resources or file paths.
     *
     * @param location the configuration location
     * @return the loaded configuration
     */
    public FluentConfig load(String location) {
        try {
            if (location.startsWith("classpath:")) {
                return loadFromClasspath(location.substring("classpath:".length()));
            } else {
                return loadFromFile(Path.of(location));
            }
        } catch (Exception e) {
            logger.warning(String.format("Failed to load configuration from %s: %s", location, e.getMessage()));
            return new FluentConfig();
        }
    }
    
    /**
     * Loads configuration from a file path.
     *
     * @param filePath the file path
     * @return the loaded configuration
     */
    public FluentConfig load(Path filePath) {
        return loadFromFile(filePath);
    }
    
    /**
     * Loads configuration from classpath resource.
     *
     * @param resourcePath the classpath resource path
     * @return the loaded configuration
     */
    public FluentConfig loadFromClasspath(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                logger.fine(String.format("Configuration file not found in classpath: %s", resourcePath));
                return new FluentConfig();
            }
            
            JsonNode root = yamlMapper.readTree(is);
            return parseConfiguration(root);
        } catch (IOException e) {
            logger.warning(String.format("Failed to load configuration from classpath %s: %s", resourcePath, e.getMessage()));
            return new FluentConfig();
        }
    }
    
    /**
     * Loads configuration from a file.
     *
     * @param filePath the file path
     * @return the loaded configuration
     */
    public FluentConfig loadFromFile(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                logger.fine(String.format("Configuration file not found: %s", filePath));
                return new FluentConfig();
            }
            
            JsonNode root = yamlMapper.readTree(filePath.toFile());
            return parseConfiguration(root);
        } catch (IOException e) {
            logger.warning(String.format("Failed to load configuration from file %s: %s", filePath, e.getMessage()));
            return new FluentConfig();
        }
    }
    
    /**
     * Loads configuration from a URL.
     *
     * @param url the URL
     * @return the loaded configuration
     */
    public FluentConfig loadFromUrl(URL url) {
        try (InputStream is = url.openStream()) {
            JsonNode root = yamlMapper.readTree(is);
            return parseConfiguration(root);
        } catch (IOException e) {
            logger.warning(String.format("Failed to load configuration from URL %s: %s", url, e.getMessage()));
            return new FluentConfig();
        }
    }
    
    /**
     * Parses the YAML configuration into a FluentConfig object.
     *
     * @param root the root JSON node
     * @return the parsed configuration
     */
    private FluentConfig parseConfiguration(JsonNode root) {
        FluentConfig config = new FluentConfig();
        
        if (root.has("basePath")) {
            config.basePath(root.get("basePath").asText());
        }
        
        if (root.has("supportedLocales")) {
            JsonNode localesNode = root.get("supportedLocales");
            if (localesNode.isArray()) {
                List<String> localeStrings = new ArrayList<>();
                for (JsonNode localeNode : localesNode) {
                    localeStrings.add(localeNode.asText());
                }
                config.supportedLocales(localeStrings.toArray(new String[0]));
            }
        }
        
        if (root.has("defaultLocale")) {
            config.defaultLocale(root.get("defaultLocale").asText());
        }
        
        if (root.has("encoding")) {
            config.encoding(Charset.forName(root.get("encoding").asText()));
        }
        
        if (root.has("messageSourceType")) {
            config.messageSourceType(root.get("messageSourceType").asText());
        }
        
        if (root.has("caching")) {
            JsonNode cachingNode = root.get("caching");
            if (cachingNode.has("enabled")) {
                config.enableCaching(cachingNode.get("enabled").asBoolean());
            }
            if (cachingNode.has("timeoutSeconds")) {
                config.cacheTimeoutSeconds(cachingNode.get("timeoutSeconds").asLong());
            }
        }
        
        if (root.has("autoReload")) {
            JsonNode autoReloadNode = root.get("autoReload");
            if (autoReloadNode.has("enabled")) {
                config.enableAutoReload(autoReloadNode.get("enabled").asBoolean());
            }
            if (autoReloadNode.has("intervalSeconds")) {
                config.autoReloadIntervalSeconds(autoReloadNode.get("intervalSeconds").asLong());
            }
        }
        
        if (root.has("fallback")) {
            config.enableFallback(root.get("fallback").asBoolean());
        }
        
        if (root.has("logMissingTranslations")) {
            config.logMissingTranslations(root.get("logMissingTranslations").asBoolean());
        }
        
        return config;
    }
    
    /**
     * Saves configuration to a file.
     *
     * @param config the configuration to save
     * @param filePath the file path
     * @throws IOException if an I/O error occurs
     */
    public void save(FluentConfig config, Path filePath) throws IOException {
        Map<String, Object> configMap = new HashMap<>();
        
        configMap.put("basePath", config.getBasePath());
        configMap.put("supportedLocales", config.getSupportedLocales().stream()
            .map(Locale::toLanguageTag)
            .toList());
        configMap.put("defaultLocale", config.getDefaultLocale().toLanguageTag());
        configMap.put("encoding", config.getEncoding().name());
        
        Map<String, Object> cachingMap = new HashMap<>();
        cachingMap.put("enabled", config.isEnableCaching());
        cachingMap.put("timeoutSeconds", config.getCacheTimeoutSeconds());
        configMap.put("caching", cachingMap);
        
        Map<String, Object> autoReloadMap = new HashMap<>();
        autoReloadMap.put("enabled", config.isEnableAutoReload());
        autoReloadMap.put("intervalSeconds", config.getAutoReloadIntervalSeconds());
        configMap.put("autoReload", autoReloadMap);
        
        configMap.put("fallback", config.isEnableFallback());
        configMap.put("logMissingTranslations", config.isLogMissingTranslations());
        
        yamlMapper.writeValue(filePath.toFile(), configMap);
    }
    
    /**
     * Creates a default configuration file if it doesn't exist.
     *
     * @param filePath the file path
     * @throws IOException if an I/O error occurs
     */
    public void createDefaultConfig(Path filePath) throws IOException {
        if (Files.exists(filePath)) {
            return;
        }
        
        FluentConfig defaultConfig = new FluentConfig()
            .supportedLocales("en")
            .defaultLocale("en")
            .enableCaching(true)
            .cacheTimeoutSeconds(300)
            .enableAutoReload(false)
            .autoReloadIntervalSeconds(60)
            .enableFallback(true)
            .logMissingTranslations(true);
        
        save(defaultConfig, filePath);
    }
} 