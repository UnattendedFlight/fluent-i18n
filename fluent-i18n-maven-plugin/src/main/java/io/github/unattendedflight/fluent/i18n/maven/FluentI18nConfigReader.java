package io.github.unattendedflight.fluent.i18n.maven;

import io.github.unattendedflight.fluent.i18n.config.FluentConfig;
import io.github.unattendedflight.fluent.i18n.config.FluentConfigLoader;
import org.apache.maven.plugin.logging.Log;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

/**
 * Unified configuration reader for Maven plugin.
 * Uses the core FluentConfig system consistently across all modules.
 */
public class FluentI18nConfigReader {

    private final Log log;
    private final FluentConfigLoader configLoader;
    
    // Configuration files to check in priority order
    private static final String[] CONFIG_FILES = {
        "fluent.yml",           // New unified format (preferred)
        "application.yml",      // Spring Boot format (supported)
        "application.yaml",     // Alternative YAML extension
        "application.properties" // Properties format
    };

    public FluentI18nConfigReader(Log log) {
        this.log = log;
        this.configLoader = new FluentConfigLoader();
    }

    /**
     * Loads configuration from the project, checking multiple locations.
     */
    public FluentConfig loadConfiguration(Path projectRoot) {
        Path resourcesDir = projectRoot.resolve("src/main/resources");
        
        // Try each configuration file in priority order
        for (String configFile : CONFIG_FILES) {
            Path configPath = resourcesDir.resolve(configFile);
            if (Files.exists(configPath)) {
                log.info("Loading configuration from: " + configPath);
                return loadFromFile(configPath);
            }
        }
        
        log.info("No configuration file found, using defaults");
        return new FluentConfig();
    }

    /**
     * Loads configuration from a specific file.
     */
    public FluentConfig loadFromFile(Path configPath) {
        return configLoader.loadFromFile(configPath);
    }

    /**
     * Loads configuration from classpath.
     */
    public FluentConfig loadFromClasspath(String resourcePath) {
        return configLoader.loadFromClasspath(resourcePath);
    }

    /**
     * Gets the configuration file that was actually loaded.
     */
    public Path getLoadedConfigFile(Path projectRoot) {
        Path resourcesDir = projectRoot.resolve("src/main/resources");
        
        for (String configFile : CONFIG_FILES) {
            Path configPath = resourcesDir.resolve(configFile);
            if (Files.exists(configPath)) {
                return configPath;
            }
        }
        
        return null;
    }

    /**
     * Checks if fluent.yml exists (preferred format).
     */
    public boolean hasFluentYaml(Path projectRoot) {
        Path resourcesDir = projectRoot.resolve("src/main/resources");
        return Files.exists(resourcesDir.resolve("fluent.yml"));
    }
}