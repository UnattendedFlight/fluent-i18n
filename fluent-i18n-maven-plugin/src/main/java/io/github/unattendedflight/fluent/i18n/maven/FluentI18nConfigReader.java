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
 * A utility class that reads and processes Fluent I18n configurations from various file formats and
 * maps them into structured internal representations.
 *
 * This class prioritizes configuration file formats as follows:
 * 1. `application.yml`
 * 2. `application.yaml`
 * 3. `application.properties`
 *
 * The configuration data is parsed, processed, and converted into an instance of {@code FluentI18nProperties}.
 */
public class FluentI18nConfigReader {

    /**
     * Logger instance used to log messages, warnings, and errors within the FluentI18nConfigReader class.
     * This logger is utilized across various methods of the class to facilitate debugging and provide
     * insight into the configuration reading and mapping processes, including YAML and properties file handling.
     * It is final to ensure immutability and consistent logging behavior throughout the class lifecycle.
     */
    private final Log log;
    /**
     * An instance of {@code ObjectMapper} configured for processing YAML files.
     * This object is used to read, parse, and map YAML-based configuration files
     * into the internal representation of the application.
     */
    private final ObjectMapper yamlMapper;
    /**
     * An {@link ObjectMapper} instance used for reading and processing JSON data.
     * This variable provides functionality for serialization and deserialization
     * of JSON structures to and from Java objects, as part of the configuration
     * reading and mapping process within the FluentI18nConfigReader.
     *
     * It is designed to handle tasks such as parsing JSON data extracted from
     * configuration files or other sources into the appropriate properties
     * structure for further processing.
     */
    private final ObjectMapper jsonMapper;

    /**
     * Constructs a new instance of FluentI18nConfigReader.
     *
     * @param log the logger used for logging messages during configuration reading and processing
     */
    public FluentI18nConfigReader(Log log) {
        this.log = log;
        this.yamlMapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.jsonMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Reads the configuration file for the FluentI18n application. This method looks for a
     * YAML or properties file inside the "src/main/resources" directory of the specified project root.
     * If no configuration file is found, it returns default configuration values.
     *
     * @param projectRoot the root directory of the project where the configuration files are expected
     *                    to be located
     * @return a FluentI18nProperties object containing the parsed configuration data or defaults
     * @throws IOException if an error occurs while reading the configuration files
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
     * Reads configuration data from a YAML file and converts it into a {@link FluentI18nProperties} object.
     * The method specifically looks for and processes the "fluent.i18n" section in the YAML structure.
     * If the section is not found or is empty, the method logs a warning and returns an empty
     * {@link FluentI18nProperties} instance.
     *
     * @param yamlFile the {@link Path} representing the YAML file to read the configuration from
     * @return an instance of {@link FluentI18nProperties} populated with data from the "fluent.i18n"
     *         section of the YAML file, or an empty instance if the section is missing or empty
     * @throws IOException if an I/O error occurs while reading the YAML file
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
     * Reads and processes a properties file, extracting properties that start with the prefix "fluent.i18n.".
     * These properties are mapped to a FluentI18nProperties object, which encapsulates the configuration data.
     * If no matching properties are found, a default FluentI18nProperties object is returned.
     *
     * @param propsFile the path to the properties file to be read
     * @return a FluentI18nProperties object containing the extracted configuration data
     * @throws IOException if an I/O error occurs while reading the properties file
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
     * Extracts a nested property within a map based on a dot-separated path.
     *
     * The method traverses the given map using the provided path, which indicates
     * the hierarchy of keys to follow to reach the desired nested property. If the
     * path resolves to a nested map, it is returned. Otherwise, null is returned.
     *
     * @param data the root map to traverse for extracting the nested property
     * @param path the dot-separated string specifying the keys to navigate in the map
     * @return the nested map at the specified path if it exists and is a map; otherwise, null
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
     * Maps a given data map to a FluentI18nProperties object. This method uses a JSON mapper to
     * convert the input map into the desired properties format, handles any special processing
     * required, and ensures compatibility with specific configurations.
     *
     * @param data a map containing configuration data to be mapped to FluentI18nProperties
     * @return an instance of FluentI18nProperties containing the mapped configuration data
     * @throws IOException if an error occurs during the mapping process, such as JSON serialization or deserialization issues
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
     * Maps the given properties from a {@code Map<String, String>} to a {@code FluentI18nProperties} object.
     * The mapping involves setting the values of various configuration sections such as main properties,
     * message source properties, web properties, extraction properties, and compilation properties.
     *
     * @param props a map of properties where keys represent property names and values represent their corresponding values
     * @return a configured instance of {@code FluentI18nProperties} based on the provided properties
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

    /**
     * Maps the properties related to the message source from a given map to
     * the provided FluentI18nProperties.MessageSource object.
     *
     * @param messageSource the target FluentI18nProperties.MessageSource object where properties will be mapped
     * @param props the map containing key-value pairs of property settings
     */
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

    /**
     * Maps web configuration properties from a provided map of key-value pairs to the {@link FluentI18nProperties.Web} object.
     * This method updates the Web instance with properties related to web configuration, such as enabling web functionality,
     * handling locale parameters, session usage, and content-language header settings.
     *
     * @param web   the {@link FluentI18nProperties.Web} instance to which the properties are mapped
     * @param props the map containing configuration key-value pairs, where keys represent property names and values represent their corresponding settings
     */
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

    /**
     * Maps and configures extraction-related properties for FluentI18n based on the provided properties map.
     *
     * @param extraction the FluentI18nProperties.Extraction object to which the properties will be mapped
     * @param props a map containing string key-value pairs of configuration properties
     */
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

    /**
     * Maps the provided compilation properties from the given map to the specified {@link FluentI18nProperties.Compilation} object.
     *
     * @param compilation the {@link FluentI18nProperties.Compilation} instance where properties will be set
     * @param props the map containing property keys and their corresponding values
     */
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

    /**
     * Sets a string property by finding the first matching key from the provided keys in the given map
     * and applying its value using the specified setter function.
     *
     * @param setter a consumer function to apply the property value
     * @param props a map containing property keys and their corresponding string values
     * @param keys an array of keys to look up in the map; the first matching key with a non-null value will be used
     */
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

    /**
     * Sets a boolean property by attempting to find the value associated with one of the given keys
     * in the provided properties map. If a value is found, it is parsed as a boolean and applied
     * using the provided setter function. Iterates through the keys in order and stops at the first
     * match.
     *
     * @param setter a consumer function to apply the parsed boolean value
     * @param props a map containing property keys and their corresponding string values
     * @param keys a variable-length array of keys to look for in the properties map
     */
    private void setBooleanProperty(java.util.function.Consumer<Boolean> setter, Map<String, String> props, String... keys) {
        for (String key : keys) {
            String value = props.get(key);
            if (value != null) {
                setter.accept(Boolean.parseBoolean(value));
                break;
            }
        }
    }

    /**
     * Sets a list property using the provided setter function. It processes a map of properties and
     * tries to retrieve the first non-null value associated with the specified keys. The retrieved
     * value is split into a list using a comma as a delimiter, and each element is trimmed before being
     * passed to the setter function.
     *
     * @param setter a Consumer that accepts the resulting list of strings
     * @param props a map containing property key-value pairs
     * @param keys a varargs array of strings representing the property keys to search
     */
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

    /**
     * Parses a string representation of a locale and converts it into a {@code Locale} object.
     * If the input string is {@code null} or empty, the default locale is set to English.
     * Otherwise, the input is transformed to conform to a language tag format and converted to a {@code Locale}.
     *
     * @param localeStr the string representation of the locale, which may include underscores for separators
     * @return the parsed {@code Locale} object; defaults to {@code Locale.ENGLISH} if the input string is {@code null} or empty
     */
    private Locale parseLocale(String localeStr) {
        if (localeStr == null || localeStr.trim().isEmpty()) {
            return Locale.ENGLISH.stripExtensions();
        }
        return Locale.forLanguageTag(localeStr.replace('_', '-'));
    }

    /**
     * Parses a comma-separated string of locale identifiers into a set of {@link Locale} objects.
     * If the input is null or empty, returns a set containing only {@link Locale#ENGLISH}.
     *
     * @param localesStr a comma-separated string of locale identifiers (e.g., "en,fr,es").
     *                   Each identifier should follow ISO 639 and optionally ISO 3166 standards.
     * @return a set of {@link Locale} objects parsed from the input string. Never null.
     *         If the input is null or empty, the returned set will contain only {@link Locale#ENGLISH}.
     */
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
     * Processes and updates the properties of the provided FluentI18nProperties object
     * based on the given data map. This method handles various property configurations,
     * including locale settings and compilation configurations, ensuring proper
     * value parsing and format compatibility.
     *
     * @param properties the FluentI18nProperties object to be populated or updated with the processed data
     * @param data a map containing configuration data, which may include locale settings,
     *        compilation options, and other configuration elements in either camelCase
     *        or kebab-case formats
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