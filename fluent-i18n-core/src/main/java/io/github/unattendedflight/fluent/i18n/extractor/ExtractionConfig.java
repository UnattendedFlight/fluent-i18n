package io.github.unattendedflight.fluent.i18n.extractor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * Configuration class for extracting internationalization-related strings from source files.
 *
 * This class allows configuring various aspects of the extraction process such as project
 * settings, source directories, supported encodings, file patterns to process, and custom
 * patterns for recognizing translatable content within code, annotations, templates, and
 * other sources.
 */
public class ExtractionConfig {
    /**
     * Represents the root directory of the project.
     * This path serves as the base location for resolving relative paths within the project's configuration and operations.
     */
    private Path projectRoot = Path.of(".");
    /**
     * A list of directories where source files are located. These directories
     * are used as the primary locations for extracting messages during the
     * internationalization process. Each directory is represented as a
     * {@code Path} object.
     *
     * The default values typically point to standard Java project structures,
     * such as `src/main/java` for Java source code and `src/main/resources`
     * for resource files.
     */
    private List<Path> sourceDirectories = Arrays.asList(
        Path.of("src/main/java"),
        Path.of("src/main/resources")
    );
    /**
     * A set of supported locales for internationalization purposes.
     * Each locale is represented as a string, typically following the format of a
     * language code (e.g., "en", "fr", "de") or a combination of language and region
     * (e.g., "en-US", "fr-CA").
     *
     * This variable defines the locales that are recognized and processed during
     * source extraction or localization tasks. It is designed to ensure only the
     * specified locales are considered, avoiding unnecessary processing of unsupported locales.
     *
     * The default value is an empty set, indicating no supported locales by default.
     */
    private Set<String> supportedLocales = Set.of();
    /**
     * Defines the character encoding used for reading source files in the extraction process.
     * This encoding determines how the content of source files is interpreted, ensuring
     * compatibility with the intended character set of the files being processed.
     * The default value is set to UTF-8, a widely used encoding standard that supports
     * a broad range of characters and languages.
     */
    private Charset sourceEncoding = StandardCharsets.UTF_8;
    
    /**
     * Defines a list of regular expression patterns to match file types for processing.
     * These patterns are used as part of the configuration to specify which source code
     * files should be included for extraction or other operations, based on their file
     * extensions or formats.
     *
     * The default patterns include:
     * - `.*\\.java$`: Matches all Java source files.
     * - `.*\\.html$`: Matches all HTML files.
     * - `.*\\.jsp$`: Matches all JSP (JavaServer Pages) files.
     * - `.*\\.jspx$`: Matches all JSPX (XML-based JSP) files.
     *
     * The list of patterns can be used to filter files for use with extractors or other
     * components that operate on specific file types.
     */
    // File patterns to process
    private List<String> filePatterns = Arrays.asList(
        ".*\\.java$",
        ".*\\.html$", 
        ".*\\.jsp$",
        ".*\\.jspx$"
    );
    
    /**
     * A list of regular expressions representing patterns for method calls related
     * to internationalization message extraction. These patterns are used to
     * identify occurrences of localized strings in the source code.
     *
     * Each string in the list is a regular expression, and they specifically match
     * method calls for extracting translatable text. The recognized method call
     * patterns include:
     *
     * 1. Pattern for `I18n.translate("key")` calls.
     * 2. Pattern for `I18n.describe("key")` calls.
     * 3. Pattern for `I18n.t("key")` calls.
     * 4. Pattern for context-specific calls such as
     *    `I18n.context(...).translate("key")`.
     *
     * The extracted strings from these patterns can be used for further processing
     * in translation workflows, localization file generation, or analytical tools.
     *
     * This field is initialized with default patterns that are commonly used in
     * applications relying on a specific i18n library.
     */
    // Extraction patterns
    private List<String> methodCallPatterns = new ArrayList<>(Arrays.asList(
        "I18n\\.translate\\s*\\(\\s*\"([^\"]+)\"",
        "I18n\\.describe\\s*\\(\\s*\"([^\"]+)\"",
        "I18n\\.t\\s*\\(\\s*\"([^\"]+)\"",
        "I18n\\.context\\([^)]+\\)\\.translate\\s*\\(\\s*\"([^\"]+)\""
    ));
    
    /**
     * A list of regular expression patterns used to identify annotations in source code
     * for extracting translatable messages. The patterns are predefined to match specific
     * annotations such as `@Translatable` and `@Message`.
     *
     * Each pattern describes a regular expression that captures the relevant annotation
     * syntax and extracts the translatable string within quotation marks.
     */
    private List<String> annotationPatterns = new ArrayList<>(Arrays.asList(
        "@Translatable\\s*\\(\\s*\"([^\"]+)\"",
        "@Message\\s*\\(\\s*\"([^\"]+)\""
    ));
    
    /**
     * A list of regular expression patterns used to identify translatable template strings
     * within source files. These patterns are designed to match various i18n-related templates
     * and expressions commonly used in application code, such as `@i18n.translate()` and
     * `@i18nTemplateUtils.translate()`. Supported patterns also include message extraction
     * from frameworks like Thymeleaf and `<spring:message>` tags.
     *
     * This list forms part of the configuration for extracting translatable text from templates
     * during the internationalization process.
     *
     * Patterns include:
     * - i18n translate calls, e.g., `${@i18n.translate('key')}`
     * - i18nTemplateUtils translate and shorthand t methods
     * - Thymeleaf tags with `th:text` attributes referencing i18n translate calls
     * - `<spring:message>` tags with the `code` attribute
     */
    private List<String> templatePatterns = new ArrayList<>(Arrays.asList(
        "\\$\\{@i18n\\.translate\\('([^']+)'\\)\\}",
        "\\$\\{@i18nTemplateUtils\\.translate\\('([^']+)'\\)\\}",
        "\\$\\{@i18nTemplateUtils\\.t\\('([^']+)'\\)\\}",
        "th:text=\"\\$\\{@i18n\\.translate\\('([^']+)'\\)\\}\"",
        "th:text=\"\\$\\{@i18nTemplateUtils\\.translate\\('([^']+)'\\)\\}\"",
        "th:text=\"\\$\\{@i18nTemplateUtils\\.t\\('([^']+)'\\)\\}\"",
        "<spring:message\\s+code=\"([^\"]+)\""
    ));
    
    /**
     * A list of regular expression patterns used to extract plural forms
     * from source code. These patterns target specific plural categories
     * such as zero, one, two, few, many, and other. Each regular expression
     * is designed to identify method calls with string arguments corresponding
     * to these plural forms.
     *
     * The plural categories are:
     * - zero: Matches expressions like `.zero("...")`.
     * - one: Matches expressions like `.one("...")`.
     * - two: Matches expressions like `.two("...")`.
     * - few: Matches expressions like `.few("...")`.
     * - many: Matches expressions like `.many("...")`.
     * - other: Matches expressions like `.other("...")`.
     *
     * This variable is used as part of the configuration for extracting
     * pluralized messages during internationalization routines.
     */
    // Plural patterns
    private List<String> pluralPatterns = new ArrayList<>(Arrays.asList(
        "\\.zero\\s*\\(\\s*\"([^\"]+)\"\\)",
        "\\.one\\s*\\(\\s*\"([^\"]+)\"\\)",
        "\\.two\\s*\\(\\s*\"([^\"]+)\"\\)",
        "\\.few\\s*\\(\\s*\"([^\"]+)\"\\)",
        "\\.many\\s*\\(\\s*\"([^\"]+)\"\\)",
        "\\.other\\s*\\(\\s*\"([^\"]+)\"\\)"
    ));
    
    /**
     * A list of custom {@link SourceExtractor} instances used to extract messages from
     * source files. Custom extractors can be added to handle specific file types or
     * formats beyond the default extraction mechanisms.
     */
    private List<SourceExtractor> customExtractors = new ArrayList<>();
    
    /**
     * Initializes a new instance of the {@code ExtractionConfig} class using the builder pattern.
     * This method creates a new configuration object that can be customized using other
     * builder methods to specify details such as project root, source directories,
     * supported locales, encoding, patterns, and custom extractors.
     *
     * @return a new {@code ExtractionConfig} instance for further customization
     */
    // Builder methods
    public static ExtractionConfig builder() {
        return new ExtractionConfig();
    }
    
    /**
     * Sets the root directory of the project to be used for extraction configuration.
     *
     * @param root the path of the project root directory
     * @return the current instance of {@code ExtractionConfig} for method chaining
     */
    public ExtractionConfig projectRoot(Path root) {
        this.projectRoot = root;
        return this;
    }
    
    /**
     * Sets the source directories for the extraction configuration.
     *
     * @param dirs a list of {@code Path} objects representing the source directories to be included
     * @return the updated {@code ExtractionConfig} instance
     */
    public ExtractionConfig sourceDirectories(List<Path> dirs) {
        this.sourceDirectories = new ArrayList<>(dirs);
        return this;
    }
    
    /**
     * Sets the supported locales for the extraction configuration.
     * This method initializes or updates the set of locales supported by the configuration.
     *
     * @param locales a set of locale identifiers to specify supported locales
     * @return the current ExtractionConfig instance for method chaining
     */
    public ExtractionConfig supportedLocales(Set<String> locales) {
        this.supportedLocales = new HashSet<>(locales);
        return this;
    }
    
    /**
     * Sets the character encoding used for reading source files and returns the updated configuration.
     *
     * @param encoding the {@code Charset} to be used for reading source files
     * @return the updated instance of {@code ExtractionConfig}
     */
    public ExtractionConfig sourceEncoding(Charset encoding) {
        this.sourceEncoding = encoding;
        return this;
    }
    
    /**
     * Adds a method call pattern to the extraction configuration.
     * The specified pattern is appended to the existing list of method call patterns.
     * Method call patterns are used to identify and extract specific method calls during source analysis.
     *
     * @param pattern the method call pattern to be added; it is expected to be a string format
     *                representing a method call filter or matching criteria
     * @return the updated {@code ExtractionConfig} instance, enabling method chaining
     */
    public ExtractionConfig addMethodCallPattern(String pattern) {
        this.methodCallPatterns.add(pattern);
        return this;
    }
    
    /**
     * Sets a list of method call patterns to be used for matching during extraction.
     * The provided patterns define the method calls to search for within the source code.
     *
     * @param patterns a list of strings, where each string represents a method call pattern
     * @return the current instance of {@code ExtractionConfig} to allow for method chaining
     */
    public ExtractionConfig setMethodCallPatterns(List<String> patterns) {
        this.methodCallPatterns = new ArrayList<>(patterns);
        return this;
    }
    
    /**
     * Adds a new annotation pattern to the list of patterns in this configuration.
     * Annotation patterns are used to identify specific annotations in the source code
     * to extract relevant messages for internationalization.
     *
     * @param pattern the annotation pattern to be added; this is typically a string
     *                specifying a regex or simple format of annotation names to match.
     * @return the updated {@code ExtractionConfig} instance to allow for method chaining.
     */
    public ExtractionConfig addAnnotationPattern(String pattern) {
        this.annotationPatterns.add(pattern);
        return this;
    }
    
    /**
     * Sets the patterns for identifying annotations in the source code. These patterns
     * are used to match and extract messages or information from annotated elements.
     *
     * @param patterns a list of strings representing the annotation patterns to be set
     * @return the updated instance of {@code ExtractionConfig} for method chaining
     */
    public ExtractionConfig setAnnotationPatterns(List<String> patterns) {
        this.annotationPatterns = new ArrayList<>(patterns);
        return this;
    }
    
    /**
     * Adds a template pattern to the configuration. Template patterns are used
     * to identify specific patterns in source files during the extraction process.
     *
     * @param pattern the template pattern to be added to the configuration
     * @return the current instance of {@code ExtractionConfig} to allow method chaining
     */
    public ExtractionConfig addTemplatePattern(String pattern) {
        this.templatePatterns.add(pattern);
        return this;
    }
    
    /**
     * Sets the list of template patterns to be used in the extraction configuration.
     * This method replaces any previously set template patterns with the provided list.
     *
     * @param patterns the list of template patterns to be set
     * @return the current instance of {@code ExtractionConfig} for method chaining
     */
    public ExtractionConfig setTemplatePatterns(List<String> patterns) {
        this.templatePatterns = new ArrayList<>(patterns);
        return this;
    }
    
    /**
     * Adds a custom SourceExtractor to the extraction configuration.
     *
     * @param extractor the custom SourceExtractor to be added; it allows defining
     *                  custom logic for extracting messages from source files.
     * @return the current instance of ExtractionConfig for method chaining
     */
    public ExtractionConfig addCustomExtractor(SourceExtractor extractor) {
        this.customExtractors.add(extractor);
        return this;
    }
    
    /**
     * Retrieves the root directory of the project.
     *
     * @return the path representing the project's root directory
     */
    // Getters
    public Path getProjectRoot() { return projectRoot; }
    /**
     * Retrieves the list of source directories specified in the configuration.
     *
     * @return a list of {@code Path} objects representing the source directories
     *         from which files are intended to be analyzed or processed.
     */
    public List<Path> getSourceDirectories() { return sourceDirectories; }
    /**
     * Retrieves the set of supported locales configured for the extraction process.
     * These locales represent the intended target languages for translation.
     *
     * @return a {@code Set} of {@code String} values, each representing a locale identifier (e.g., "en", "fr", "de") that is supported.
     */
    public Set<String> getSupportedLocales() { return supportedLocales; }
    /**
     * Retrieves the character encoding used for processing source files.
     *
     * @return the character set representing the source file encoding
     */
    public Charset getSourceEncoding() { return sourceEncoding; }
    /**
     * Retrieves the list of file patterns used for matching source files to be processed.
     *
     * @return a list of strings representing file patterns.
     */
    public List<String> getFilePatterns() { return filePatterns; }
    /**
     * Retrieves the list of method call patterns that are configured for extraction.
     * These patterns define specific method calls in the source code that should be
     * considered during the extraction process.
     *
     * @return a list of strings representing the method call patterns to be used for extraction
     */
    public List<String> getMethodCallPatterns() { return methodCallPatterns; }
    /**
     * Retrieves the list of annotation patterns used for message extraction.
     *
     * @return a list of strings representing the annotation patterns.
     */
    public List<String> getAnnotationPatterns() { return annotationPatterns; }
    /**
     * Retrieves the list of template patterns.
     *
     * @return a list of strings representing the template patterns
     */
    public List<String> getTemplatePatterns() { return templatePatterns; }
    /**
     * Retrieves the list of patterns used to identify plural forms
     * in the source code during the message extraction process.
     *
     * @return a list of strings representing the plural patterns.
     */
    public List<String> getPluralPatterns() { return pluralPatterns; }
    /**
     * Returns a list of custom source extractors that are configured for extracting
     * messages from source files. These extractors extend the capabilities of the
     * system by enabling the processing of additional file types or custom formats.
     *
     * @return a list of {@code SourceExtractor} instances representing the custom
     *         extractors used for message extraction
     */
    public List<SourceExtractor> getCustomExtractors() { return customExtractors; }

    /**
     * Returns a string representation of the ExtractionConfig object, including its
     * fields and their corresponding values.
     *
     * @return a string representation of the ExtractionConfig object
     */
    @Override
    public String toString() {
      return "ExtractionConfig{\n" + "  projectRoot=" + projectRoot + ",\n" +
          "  sourceDirectories=" + sourceDirectories + ",\n" +
          "  supportedLocales=" + supportedLocales + ",\n" +
          "  sourceEncoding=" + sourceEncoding + ",\n" +
          "  filePatterns=" + filePatterns + ",\n" +
          "  methodCallPatterns=" + methodCallPatterns + ",\n" +
          "  annotationPatterns=" + annotationPatterns + ",\n" +
          "  templatePatterns=" + templatePatterns + ",\n" +
          "  pluralPatterns=" + pluralPatterns + ",\n" +
          "  customExtractors=" + customExtractors + "\n" +
          '}';
    }
}