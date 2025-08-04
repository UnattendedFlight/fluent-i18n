package io.github.unattendedflight.fluent.i18n.maven;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

/**
 * Configuration reader for Maven Mojo context
 */
public class FluentI18nConfigReader {

    private final Log log;
    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public FluentI18nConfigReader(Log log) {
        this.log = log;
        this.yamlMapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.jsonMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Read configuration from various sources in order of precedence:
     * 1. application.yml
     * 2. application.yaml
     * 3. application.properties
     */
    public FluentI18nProperties readConfiguration(Path projectRoot) throws IOException {
        FluentI18nProperties properties = new FluentI18nProperties();

        // Try to find configuration files
        Path configDir = projectRoot.resolve("src/main/resources");

        // Check for YAML files first
        Path yamlFile = configDir.resolve("application.yml");
        if (!Files.exists(yamlFile)) {
            yamlFile = configDir.resolve("application.yaml");
        }

        if (Files.exists(yamlFile)) {
            log.info("Reading configuration from: " + yamlFile);
            properties = readFromYaml(yamlFile);
        } else {
            // Fallback to properties file
            Path propsFile = configDir.resolve("application.properties");
            if (Files.exists(propsFile)) {
                log.info("Reading configuration from: " + propsFile);
                properties = readFromProperties(propsFile);
            } else {
                log.warn("No configuration file found, using defaults");
            }
        }

        return properties;
    }

    /**
     * Read from YAML file
     */
    private FluentI18nProperties readFromYaml(Path yamlFile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(yamlFile)) {
            // Read the full YAML structure
            Map<String, Object> yamlData = yamlMapper.readValue(inputStream, Map.class);
            // Extract fluent.i18n section
            Map<String, Object> fluentSection = extractNestedProperty(yamlData, "fluent.i18n");

            if (fluentSection == null || fluentSection.isEmpty()) {
                log.warn("No 'fluent.i18n' configuration found in YAML file");
                return new FluentI18nProperties();
            }

            // Convert to FluentI18nProperties
            return mapToProperties(fluentSection);
        }
    }

    /**
     * Read from properties file
     */
    private FluentI18nProperties readFromProperties(Path propsFile) throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(propsFile)) {
            properties.load(inputStream);
        }

        // Filter properties that start with "fluent.i18n."
        Map<String, String> fluentProps = new HashMap<>();
        String prefix = "fluent.i18n.";

        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                String subKey = key.substring(prefix.length());
                fluentProps.put(subKey, properties.getProperty(key));
            }
        }

        if (fluentProps.isEmpty()) {
            log.warn("No 'fluent.i18n.*' properties found in properties file");
            return new FluentI18nProperties();
        }

        return mapPropertiesToObject(fluentProps);
    }

    /**
     * Extract nested property from YAML data using dot notation
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractNestedProperty(Map<String, Object> data, String path) {
        String[] parts = path.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }

        return current instanceof Map ? (Map<String, Object>) current : null;
    }

    /**
     * Map YAML/JSON structure to FluentI18nProperties
     */
    private FluentI18nProperties mapToProperties(Map<String, Object> data) throws IOException {
        // Use Jackson to convert the map to our properties object
        String json = jsonMapper.writeValueAsString(data);
        // Json mapper, support for "en" -> Locale instance using Locale.forLanguageTag
        jsonMapper.findAndRegisterModules();
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        jsonMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        jsonMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        jsonMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        FluentI18nProperties properties = jsonMapper.readValue(json, FluentI18nProperties.class);

        // Handle special cases that Jackson might not handle well
        postProcessProperties(properties, data);

        return properties;
    }

    /**
     * Map flat properties to nested object structure
     */
    private FluentI18nProperties mapPropertiesToObject(Map<String, String> props) {
        FluentI18nProperties config = new FluentI18nProperties();

        // Main properties
        if (props.containsKey("enabled")) {
            config.setEnabled(Boolean.parseBoolean(props.get("enabled")));
        }

        if (props.containsKey("default-locale") || props.containsKey("defaultLocale")) {
            String locale = props.getOrDefault("default-locale", props.get("defaultLocale"));
            config.setDefaultLocale(parseLocale(locale));
        }

        if (props.containsKey("supported-locales") || props.containsKey("supportedLocales")) {
            String locales = props.getOrDefault("supported-locales", props.get("supportedLocales"));
            config.setSupportedLocales(parseLocaleSet(locales));
        }

        // Message source properties
        FluentI18nProperties.MessageSource messageSource = config.getMessageSource();
        mapMessageSourceProperties(messageSource, props);

        // Web properties
        FluentI18nProperties.Web web = config.getWeb();
        mapWebProperties(web, props);

        // Extraction properties
        FluentI18nProperties.Extraction extraction = config.getExtraction();
        mapExtractionProperties(extraction, props);

        // Compilation properties
        FluentI18nProperties.Compilation compilation = config.getCompilation();
        mapCompilationProperties(compilation, props);

        return config;
    }

    private void mapMessageSourceProperties(FluentI18nProperties.MessageSource messageSource, Map<String, String> props) {
        String prefix = "message-source.";
        String altPrefix = "messageSource.";

        setStringProperty(messageSource::setType, props, prefix + "type", altPrefix + "type");
        setStringProperty(messageSource::setBasename, props, prefix + "basename", altPrefix + "basename");
        setStringProperty(messageSource::setEncoding, props, prefix + "encoding", altPrefix + "encoding");
        setBooleanProperty(messageSource::setUseOriginalAsFallback, props,
            prefix + "use-original-as-fallback", altPrefix + "useOriginalAsFallback");
        setBooleanProperty(messageSource::setLogMissingTranslations, props,
            prefix + "log-missing-translations", altPrefix + "logMissingTranslations");

        // Handle duration
        String duration = props.getOrDefault(prefix + "cache-duration", props.get(altPrefix + "cacheDuration"));
        if (duration != null) {
            messageSource.setCacheDuration(Duration.parse(duration));
        }
    }

    private void mapWebProperties(FluentI18nProperties.Web web, Map<String, String> props) {
        String prefix = "web.";

        setBooleanProperty(web::setEnabled, props, prefix + "enabled");
        setStringProperty(web::setLocaleParameter, props, prefix + "locale-parameter", prefix + "localeParameter");
        setBooleanProperty(web::setUseAcceptLanguageHeader, props,
            prefix + "use-accept-language-header", prefix + "useAcceptLanguageHeader");
        setBooleanProperty(web::setUseSession, props, prefix + "use-session", prefix + "useSession");
        setBooleanProperty(web::setSetContentLanguageHeader, props,
            prefix + "set-content-language-header", prefix + "setContentLanguageHeader");
    }

    private void mapExtractionProperties(FluentI18nProperties.Extraction extraction, Map<String, String> props) {
        String prefix = "extraction.";

        setBooleanProperty(extraction::setEnabled, props, prefix + "enabled");
        setStringProperty(extraction::setSourceEncoding, props,
            prefix + "source-encoding", prefix + "sourceEncoding");

        // Handle list properties
        setListProperty(extraction::setMethodCallPatterns, props,
            prefix + "method-call-patterns", prefix + "methodCallPatterns");
        setListProperty(extraction::setAnnotationPatterns, props,
            prefix + "annotation-patterns", prefix + "annotationPatterns");
        setListProperty(extraction::setTemplatePatterns, props,
            prefix + "template-patterns", prefix + "templatePatterns");
    }

    private void mapCompilationProperties(FluentI18nProperties.Compilation compilation, Map<String, String> props) {
        String prefix = "compilation.";

        setStringProperty(compilation::setOutputFormat, props,
            prefix + "output-format", prefix + "outputFormat");
        setBooleanProperty(compilation::setValidation, props, prefix + "validation");
        setBooleanProperty(compilation::setPreserveExisting, props,
            prefix + "preserve-existing", prefix + "preserveExisting");
        setBooleanProperty(compilation::setMinifyOutput, props,
            prefix + "minify-output", prefix + "minifyOutput");
        setBooleanProperty(compilation::setIncludeMetadata, props,
            prefix + "include-metadata", prefix + "includeMetadata");
    }

    // Helper methods
    private void setStringProperty(java.util.function.Consumer<String> setter, Map<String, String> props, String... keys) {
        for (String key : keys) {
            String value = props.get(key);
            if (value != null) {
                setter.accept(value);
                break;
            }
        }
    }

    private void setBooleanProperty(java.util.function.Consumer<Boolean> setter, Map<String, String> props, String... keys) {
        for (String key : keys) {
            String value = props.get(key);
            if (value != null) {
                setter.accept(Boolean.parseBoolean(value));
                break;
            }
        }
    }

    private void setListProperty(java.util.function.Consumer<List<String>> setter, Map<String, String> props, String... keys) {
        for (String key : keys) {
            String value = props.get(key);
            if (value != null) {
                List<String> list = Arrays.asList(value.split(","));
                list = list.stream().map(String::trim).toList();
                setter.accept(list);
                break;
            }
        }
    }

    private Locale parseLocale(String localeStr) {
        if (localeStr == null || localeStr.trim().isEmpty()) {
            return Locale.ENGLISH.stripExtensions();
        }
        return Locale.forLanguageTag(localeStr.replace('_', '-'));
    }

    private Set<Locale> parseLocaleSet(String localesStr) {
        if (localesStr == null || localesStr.trim().isEmpty()) {
            return Set.of(Locale.ENGLISH.stripExtensions());
        }

        Set<Locale> locales = new HashSet<>();
        for (String locale : localesStr.split(",")) {
            locales.add(parseLocale(locale.trim()));
        }
        return locales;
    }

    /**
     * Post-process properties for special cases
     */
    private void postProcessProperties(FluentI18nProperties properties, Map<String, Object> data) {
        // Handle locale conversion if Jackson didn't handle it properly
        Object defaultLocale = data.get("defaultLocale");
        if (defaultLocale instanceof String) {
            properties.setDefaultLocale(parseLocale((String) defaultLocale));
        }

        Object supportedLocales = data.get("supportedLocales");
        if (supportedLocales instanceof List) {
            Set<Locale> locales = new HashSet<>();
            for (Object locale : (List<?>) supportedLocales) {
                if (locale instanceof String) {
                    locales.add(parseLocale((String) locale));
                }
            }
            properties.setSupportedLocales(locales);
        }
        
        // Also check for kebab-case property names
        Object defaultLocaleKebab = data.get("default-locale");
        if (defaultLocaleKebab instanceof String) {
            properties.setDefaultLocale(parseLocale((String) defaultLocaleKebab));
        }

        Object supportedLocalesKebab = data.get("supported-locales");
        if (supportedLocalesKebab instanceof List) {
            Set<Locale> locales = new HashSet<>();
            for (Object locale : (List<?>) supportedLocalesKebab) {
                if (locale instanceof String) {
                    locales.add(parseLocale((String) locale));
                }
            }
            properties.setSupportedLocales(locales);
        }
        
        // Handle compilation properties
        Object compilation = data.get("compilation");
        if (compilation instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> compilationMap = (Map<String, Object>) compilation;
            
            Object outputFormat = compilationMap.get("outputFormat");
            if (outputFormat instanceof String) {
                properties.getCompilation().setOutputFormat((String) outputFormat);
            }
            
            Object outputFormatKebab = compilationMap.get("output-format");
            if (outputFormatKebab instanceof String) {
                properties.getCompilation().setOutputFormat((String) outputFormatKebab);
            }
            
            Object validation = compilationMap.get("validation");
            if (validation instanceof Boolean) {
                properties.getCompilation().setValidation((Boolean) validation);
            }
            
            Object preserveExisting = compilationMap.get("preserveExisting");
            if (preserveExisting instanceof Boolean) {
                properties.getCompilation().setPreserveExisting((Boolean) preserveExisting);
            }
            
            Object preserveExistingKebab = compilationMap.get("preserve-existing");
            if (preserveExistingKebab instanceof Boolean) {
                properties.getCompilation().setPreserveExisting((Boolean) preserveExistingKebab);
            }
            
            Object minifyOutput = compilationMap.get("minifyOutput");
            if (minifyOutput instanceof Boolean) {
                properties.getCompilation().setMinifyOutput((Boolean) minifyOutput);
            }
            
            Object minifyOutputKebab = compilationMap.get("minify-output");
            if (minifyOutputKebab instanceof Boolean) {
                properties.getCompilation().setMinifyOutput((Boolean) minifyOutputKebab);
            }
            
            Object includeMetadata = compilationMap.get("includeMetadata");
            if (includeMetadata instanceof Boolean) {
                properties.getCompilation().setIncludeMetadata((Boolean) includeMetadata);
            }
            
            Object includeMetadataKebab = compilationMap.get("include-metadata");
            if (includeMetadataKebab instanceof Boolean) {
                properties.getCompilation().setIncludeMetadata((Boolean) includeMetadataKebab);
            }
        }
    }
}