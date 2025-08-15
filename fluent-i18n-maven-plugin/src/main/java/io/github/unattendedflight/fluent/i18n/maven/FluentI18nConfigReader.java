package io.github.unattendedflight.fluent.i18n.maven;

import io.github.unattendedflight.fluent.i18n.config.FluentConfig;
import io.github.unattendedflight.fluent.i18n.config.FluentConfigLoader;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.maven.plugin.logging.Log;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.apache.maven.project.MavenProject;

/**
 * Unified configuration reader for Maven plugin.
 * Uses the core FluentConfig system consistently across all modules.
 */
public class FluentI18nConfigReader {

    private final Log log;
    private final FluentConfigLoader configLoader;
    private final MavenProject project;
    
    // Configuration files to check in priority order
    private static final String[] CONFIG_FILES = {
        "fluent.yml",           // New unified format (preferred)
        "application.yml",      // Spring Boot format (supported)
        "application.yaml",     // Alternative YAML extension
        "application.properties" // Properties format
    };

    public FluentI18nConfigReader(Log log, MavenProject project) {
        this.log = log;
        this.configLoader = new FluentConfigLoader();
        this.project = project;
    }

    /**
     * Loads configuration from the project, checking multiple locations.
     */
    public FluentConfig loadConfiguration(Path projectRoot) {
        return loadConfiguration(projectRoot, null, null, null, true);
    }

    /**
     * Loads configuration from the project, checking multiple locations including dependency-based configurations.
     * 
     * @param projectRoot the project root directory
     * @param dependencyGroupId optional group ID of dependency containing configuration
     * @param dependencyArtifactId optional artifact ID of dependency containing configuration
     * @param dependencyResourcePath optional resource path within dependency
     * @param enableAutodiscovery whether to enable autodiscovery of configuration files in classpath
     */
    public FluentConfig loadConfiguration(Path projectRoot, String dependencyGroupId, 
                                        String dependencyArtifactId, String dependencyResourcePath, 
                                        boolean enableAutodiscovery) {
        
        // First, try to load from specific dependency if specified
        if (dependencyGroupId != null && dependencyArtifactId != null) {
            log.info("Attempting to load configuration from dependency: " + dependencyGroupId + ":" + dependencyArtifactId);
            FluentConfig dependencyConfig = loadFromDependency(dependencyResourcePath != null ? dependencyResourcePath : "fluent.yml");
            if (dependencyConfig != null && !isDefaultConfig(dependencyConfig)) {
                log.info("Loaded configuration from dependency classpath resource: " + dependencyResourcePath);
                return dependencyConfig;
            }
            log.info("No configuration found in dependency, falling back to autodiscovery");
        }
        
        // Second, try autodiscovery of fluent.yml in classpath (if enabled)
        if (enableAutodiscovery) {
            log.debug("Attempting autodiscovery of configuration files in classpath");
            FluentConfig autodiscoveredConfig = autodiscoverFromClasspath();
            if (autodiscoveredConfig != null && !isDefaultConfig(autodiscoveredConfig)) {
                log.info("Autodiscovered configuration from classpath");
                return autodiscoveredConfig;
            }
        } else {
            log.debug("Autodiscovery is disabled");
        }
        
        // Third, fallback to local project configuration
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

    /**
     * Loads configuration from a dependency's classpath resources.
     * This uses the current thread's context classloader which includes
     * all Maven dependencies during plugin execution.
     */
    private FluentConfig loadFromDependency(String resourcePath) {
        try {
            return configLoader.loadFromClasspath(resourcePath);
        } catch (Exception e) {
            log.debug("Failed to load configuration from dependency classpath: " + resourcePath + " - " + e.getMessage());
            return new FluentConfig();
        }
    }

    /**
     * Attempts to autodiscover fluent.yml configuration files in the classpath.
     * This method looks for fluent.yml in the classpath without requiring
     * specific dependency coordinates to be specified.
     */
    private FluentConfig autodiscoverFromClasspath() {
        try {
            List<FluentConfig> discoveredConfigs = new ArrayList<>();

            // 1. Try to get project classloader (if available through Maven context)
            ClassLoader projectClassLoader = getClassLoader();

            if (projectClassLoader != null) {
                log.debug("Using project classloader for resource discovery");

                // Search for fluent.yml using project classloader
                Enumeration<URL> resources = projectClassLoader.getResources("fluent.yml");
                int totalFound = 0;
                while (resources.hasMoreElements()) {
                    totalFound++;
                    URL resource = resources.nextElement();
                    try {
                        FluentConfig config = configLoader.loadFromUrl(resource);
                        if (config != null && !isDefaultConfig(config)) {
                            discoveredConfigs.add(config);
                            log.debug(String.format("Discovered fluent.yml at: %s", resource.toString()));
                        }
                    } catch (Exception e) {
                        log.debug(String.format("Failed to load fluent.yml from %s: %s", resource, e.getMessage()));
                    }
                }
                log.debug(String.format("Found %d fluent.yml files using project classloader", totalFound));
            }

            // 2. Fallback to plugin classloader if project classloader not available
            if (discoveredConfigs.isEmpty()) {
                log.debug("Falling back to plugin classloader");
                Enumeration<URL> resources = getClass().getClassLoader().getResources("fluent.yml");
                int totalFound = 0;
                while (resources.hasMoreElements()) {
                    totalFound++;
                    URL resource = resources.nextElement();
                    try {
                        FluentConfig config = configLoader.loadFromUrl(resource);
                        if (config != null && !isDefaultConfig(config)) {
                            discoveredConfigs.add(config);
                            log.debug(String.format("Discovered fluent.yml at: %s", resource.toString()));
                        }
                    } catch (Exception e) {
                        log.debug(String.format("Failed to load fluent.yml from %s: %s", resource, e.getMessage()));
                    }
                }
                log.debug(String.format("Found %d fluent.yml files using plugin classloader", totalFound));
            }

            // 3. If still nothing found, try Thread context classloader
            if (discoveredConfigs.isEmpty()) {
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                if (contextClassLoader != null && contextClassLoader != getClass().getClassLoader()) {
                    log.debug("Trying thread context classloader");
                    Enumeration<URL> resources = contextClassLoader.getResources("fluent.yml");
                    int totalFound = 0;
                    while (resources.hasMoreElements()) {
                        totalFound++;
                        URL resource = resources.nextElement();
                        try {
                            FluentConfig config = configLoader.loadFromUrl(resource);
                            if (config != null && !isDefaultConfig(config)) {
                                discoveredConfigs.add(config);
                                log.debug(String.format("Discovered fluent.yml at: %s", resource.toString()));
                            }
                        } catch (Exception e) {
                            log.debug(String.format("Failed to load fluent.yml from %s: %s", resource, e.getMessage()));
                        }
                    }
                    log.debug(String.format("Found %d fluent.yml files using context classloader", totalFound));
                }
            }

            // Return result
            if (!discoveredConfigs.isEmpty()) {
                if (discoveredConfigs.size() == 1) {
                    log.info("Autodiscovered fluent.yml configuration from classpath");
                    return discoveredConfigs.get(0);
                } else {
                    log.info(String.format("Autodiscovered %d fluent.yml configurations, merging...", discoveredConfigs.size()));
                    return mergeConfigurations(discoveredConfigs);
                }
            }

            log.debug("No configuration files autodiscovered in classpath");
            return new FluentConfig();

        } catch (Exception e) {
            log.debug("Failed to autodiscover configuration from classpath: " + e.getMessage());
            return new FluentConfig();
        }
    }

    /**
     * Gets the project classloader if available through Maven context.
     * This method should be implemented based on how you pass Maven context to this class.
     */
    private ClassLoader getClassLoader()
    {
        try
        {
            List<String> classpathElements = project.getCompileClasspathElements();
            classpathElements.add( project.getBuild().getOutputDirectory() );
            classpathElements.add( project.getBuild().getTestOutputDirectory() );
            URL[] urls = new URL[classpathElements.size()];
            for ( int i = 0; i < classpathElements.size(); ++i )
            {
                urls[i] = new File( (String) classpathElements.get( i ) ).toURI().toURL();
            }
            return new URLClassLoader( urls, this.getClass().getClassLoader() );
        }
        catch ( Exception e )
        {
            log.debug( "Couldn't get the classloader." );
            return this.getClass().getClassLoader();
        }
    }

    private Set<URL> findAllFluentConfigsInClasspath() {
        Set<URL> fluentConfigs = new HashSet<>();

        try {
            // Get all classpath URLs
            ClassLoader classLoader = getClass().getClassLoader();

            // Handle different classloader types
            if (classLoader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) classLoader).getURLs();
                searchInUrls(urls, fluentConfigs);
            } else {
                // For newer Java versions with system classloader
                String classpath = System.getProperty("java.class.path");
                if (classpath != null) {
                    String[] paths = classpath.split(System.getProperty("path.separator"));
                    URL[] urls = Arrays.stream(paths)
                        .map(path -> {
                            try {
                                return Paths.get(path).toUri().toURL();
                            } catch (Exception e) {
                                log.debug(String.format("Invalid classpath entry: %s", path));
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toArray(URL[]::new);
                    searchInUrls(urls, fluentConfigs);
                }
            }
        } catch (Exception e) {
            log.debug(String.format("Error during recursive classpath search: %s", e.getMessage()));
        }

        return fluentConfigs;
    }

    private void searchInUrls(URL[] urls, Set<URL> fluentConfigs) {
        for (URL url : urls) {
            if (url.getProtocol().equals("file")) {
                try {
                    Path path = Paths.get(url.toURI());
                    if (Files.isDirectory(path)) {
                        // Search in directory
                        findFluentConfigsInDirectory(path, fluentConfigs);
                    } else if (path.toString().endsWith(".jar")) {
                        // Search in JAR file
                        findFluentConfigsInJar(path, fluentConfigs);
                    }
                } catch (Exception e) {
                    log.debug(String.format("Error processing URL %s: %s", url, e.getMessage()));
                }
            }
        }
    }

    private void findFluentConfigsInDirectory(Path directory, Set<URL> fluentConfigs) {
        try {
            Files.walk(directory)
                .filter(path -> path.getFileName().toString().equals("fluent.yml"))
                .forEach(path -> {
                    try {
                        // Convert to classpath-relative URL
                        Path relativePath = directory.relativize(path);
                        URL resource = getClass().getClassLoader().getResource(relativePath.toString().replace('\\', '/'));
                        if (resource != null) {
                            fluentConfigs.add(resource);
                        }
                    } catch (Exception e) {
                        log.debug(String.format("Error processing fluent.yml at %s: %s", path, e.getMessage()));
                    }
                });
        } catch (Exception e) {
            log.debug(String.format("Error searching directory %s: %s", directory, e.getMessage()));
        }
    }

    private void findFluentConfigsInJar(Path jarPath, Set<URL> fluentConfigs) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith("fluent.yml")) {
                    try {
                        URL resource = new URI("jar:file:" + jarPath.toString() + "!/" + entry.getName()).toURL();
                        fluentConfigs.add(resource);
                    } catch (Exception e) {
                        log.debug(String.format("Error creating URL for JAR entry %s: %s", entry.getName(), e.getMessage()));
                    }
                }
            }
        } catch (Exception e) {
            log.debug(String.format("Error searching JAR %s: %s", jarPath, e.getMessage()));
        }
    }

    private FluentConfig mergeConfigurations(List<FluentConfig> configs) {
        if (configs.isEmpty()) {
            return new FluentConfig();
        }

        if (configs.size() == 1) {
            return configs.get(0);
        }

        // Start with the first config as base
        FluentConfig merged = configs.get(0).copy();

        // Merge remaining configs (later configs override earlier ones for conflicts)
        for (int i = 1; i < configs.size(); i++) {
            merged = mergeConfig(merged, configs.get(i));
        }

        return merged;
    }

    private FluentConfig mergeConfig(FluentConfig base, FluentConfig override) {
        FluentConfig result = base.copy();

        // Override non-default values from override config
        if (!isDefaultBasePath(override.getBasePath()) && !override.getBasePath().equals("i18n")) {
            result.basePath(override.getBasePath());
        }

        if (!isDefaultSupportedLocales(override.getSupportedLocales())) {
            // Merge supported locales (union of both sets)
            Set<Locale> mergedLocales = new HashSet<>(result.getSupportedLocales());
            mergedLocales.addAll(override.getSupportedLocales());
            result.supportedLocales(mergedLocales);
        }

        if (!isDefaultLocale(override.getDefaultLocale())) {
            result.defaultLocale(override.getDefaultLocale());
        }

        if (!isDefaultEncoding(override.getEncoding())) {
            result.encoding(override.getEncoding());
        }

        if (!isDefaultMessageSourceType(override.getMessageSourceType())) {
            result.messageSourceType(override.getMessageSourceType());
        }

        // Boolean and numeric values - override takes precedence
        if (hasNonDefaultBooleanValue(override, "enableCaching", true)) {
            result.enableCaching(override.isEnableCaching());
        }

        if (hasNonDefaultNumericValue(override.getCacheTimeoutSeconds(), 300L)) {
            result.cacheTimeoutSeconds(override.getCacheTimeoutSeconds());
        }

        if (hasNonDefaultBooleanValue(override, "enableAutoReload", false)) {
            result.enableAutoReload(override.isEnableAutoReload());
        }

        if (hasNonDefaultNumericValue(override.getAutoReloadIntervalSeconds(), 60L)) {
            result.autoReloadIntervalSeconds(override.getAutoReloadIntervalSeconds());
        }

        if (hasNonDefaultBooleanValue(override, "enableFallback", true)) {
            result.enableFallback(override.isEnableFallback());
        }

        if (hasNonDefaultBooleanValue(override, "logMissingTranslations", false)) {
            result.logMissingTranslations(override.isLogMissingTranslations());
        }

        // Merge custom properties (override takes precedence for conflicts)
        Map<String, Object> mergedCustomProps = new HashMap<>(result.getCustomProperties());
        mergedCustomProps.putAll(override.getCustomProperties());

        for (Map.Entry<String, Object> entry : mergedCustomProps.entrySet()) {
            result.customProperty(entry.getKey(), entry.getValue());
        }

        return result;
    }

    private boolean configsAreEquivalent(FluentConfig config1, FluentConfig config2) {
        return Objects.equals(config1.getBasePath(), config2.getBasePath()) &&
            Objects.equals(config1.getSupportedLocales(), config2.getSupportedLocales()) &&
            Objects.equals(config1.getDefaultLocale(), config2.getDefaultLocale()) &&
            Objects.equals(config1.getEncoding(), config2.getEncoding()) &&
            Objects.equals(config1.getMessageSourceType(), config2.getMessageSourceType()) &&
            config1.isEnableCaching() == config2.isEnableCaching() &&
            config1.getCacheTimeoutSeconds() == config2.getCacheTimeoutSeconds() &&
            config1.isEnableAutoReload() == config2.isEnableAutoReload() &&
            config1.getAutoReloadIntervalSeconds() == config2.getAutoReloadIntervalSeconds() &&
            config1.isEnableFallback() == config2.isEnableFallback() &&
            config1.isLogMissingTranslations() == config2.isLogMissingTranslations() &&
            Objects.equals(config1.getCustomProperties(), config2.getCustomProperties());
    }

    private boolean isDefaultConfig(FluentConfig config) {
        FluentConfig defaultConfig = new FluentConfig();
        return configsAreEquivalent(config, defaultConfig);
    }

    // Helper methods for checking default values
    private boolean isDefaultBasePath(String basePath) {
        return "i18n".equals(basePath);
    }

    private boolean isDefaultSupportedLocales(Set<Locale> locales) {
        return locales.size() == 1 && locales.contains(Locale.ENGLISH);
    }

    private boolean isDefaultLocale(Locale locale) {
        return Locale.ENGLISH.equals(locale);
    }

    private boolean isDefaultEncoding(Charset encoding) {
        return StandardCharsets.UTF_8.equals(encoding);
    }

    private boolean isDefaultMessageSourceType(FluentConfig.MessageSourceType type) {
        return FluentConfig.MessageSourceType.AUTO.equals(type);
    }

    private boolean hasNonDefaultBooleanValue(FluentConfig config, String property, boolean defaultValue) {
        switch (property) {
            case "enableCaching":
                return config.isEnableCaching() != defaultValue;
            case "enableAutoReload":
                return config.isEnableAutoReload() != defaultValue;
            case "enableFallback":
                return config.isEnableFallback() != defaultValue;
            case "logMissingTranslations":
                return config.isLogMissingTranslations() != defaultValue;
            default:
                return false;
        }
    }

    private boolean hasNonDefaultNumericValue(long value, long defaultValue) {
        return value != defaultValue;
    }
}