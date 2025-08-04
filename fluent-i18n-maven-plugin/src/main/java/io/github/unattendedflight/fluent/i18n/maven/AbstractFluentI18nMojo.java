package io.github.unattendedflight.fluent.i18n.maven;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract base class for handling Maven Fluent Internationalization plugin operations.
 * Provides common configurations and utilities for managing translation files,
 * maintaining supported locales, and integrating Maven project settings.
 * This class serves as the foundation for specific Mojo implementations for
 * compiling, extracting, cleaning, validating, or other i18n-related operations.
 */
public abstract class AbstractFluentI18nMojo extends AbstractMojo {
    
    /**
     * Represents the Maven project associated with the Mojo execution.
     * This variable provides access to the current Maven project and
     * its configuration during the build process.
     *
     * It is used internally by the plugin to retrieve project details,
     * such as properties, dependencies, and file paths, ensuring that
     * the plugin operates within the context of the current build lifecycle.
     *
     * This field is injected by the Maven framework and is read-only,
     * meaning it cannot be modified by the plugin.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    
    /**
     * A Maven parameter representing the locales supported by the Fluent i18n framework.
     * This parameter specifies the locales that will be used when generating or validating translations.
     *
     * The value should be a comma-separated list of locale identifiers (e.g., "en,fr,es").
     * By default, if no value is specified, the supported locale is set to "en".
     *
     * Example usage includes specifying the locales to process for translation files or
     * controlling the scope of generated output files particular to i18n workflows.
     *
     * Configurable via the "fluent.i18n.supportedLocales" property.
     */
    @Parameter(property = "fluent.i18n.supportedLocales", defaultValue = "en")
    protected String supportedLocales;
    
    /**
     * A list of directories containing source files that will be scanned for extracting translation keys.
     * If not explicitly specified, default directories will be used.
     * This parameter can be configured in the Maven build script by using the property `fluent.i18n.sourceDirectories`.
     */
    @Parameter(property = "fluent.i18n.sourceDirectories")
    protected List<String> sourceDirectories;
    
    /**
     * Specifies the directory where PO (Portable Object) translation files are located.
     * This directory contains gettext-style `.po` and `.pot` files which are used for managing
     * translations in internationalization workflows.
     *
     * The value can be configured using the Maven property `fluent.i18n.poDirectory`. By default,
     * it is set to `${project.basedir}/src/main/resources/i18n/po`, which points to the `po`
     * directory within the standard `resources` path of the project.
     *
     * This variable is critical for translation management and is used in various translation-related
     * operations such as cleaning, extracting, or processing translation files.
     */
    @Parameter(property = "fluent.i18n.poDirectory", defaultValue = "${project.basedir}/src/main/resources/i18n/po")
    protected File poDirectory;
    
    /**
     * The directory where generated translation files should be output.
     * This parameter specifies the location of the i18n resource files that will
     * be generated as part of the Maven build.
     *
     * Controlled by the "fluent.i18n.outputDirectory" property. Defaults to
     * "${project.basedir}/src/main/resources/i18n".
     *
     * This directory typically contains the localized resource files (e.g., JSON,
     * properties, or binary formats) produced after processing source localization
     * files and applicable configurations.
     */
    @Parameter(property = "fluent.i18n.outputDirectory", defaultValue = "${project.basedir}/src/main/resources/i18n")
    protected File outputDirectory;
    
    /**
     * Specifies the character encoding used for reading and writing files during the localization process.
     *
     * This property allows you to define the character set to ensure correct encoding and decoding of text content
     * in files such as translations, properties files, or JSON files. By default, it is set to "UTF-8", which is a
     * widely used encoding supporting a broad range of characters.
     *
     * You can override the default value by setting the value of the Maven property "fluent.i18n.encoding"
     * in your project's configuration or command-line execution.
     *
     * Example valid values for this property include common encodings like "UTF-8", "ISO-8859-1", "US-ASCII", etc.
     *
     * It is recommended to ensure that all input and output translation files are aligned with the specified encoding
     * to avoid corruption or misinterpretation of character data.
     */
    @Parameter(property = "fluent.i18n.encoding", defaultValue = "UTF-8")
    protected String encoding;
    
    /**
     * Represents a list of file patterns used to extract translation keys and values from source files.
     * These patterns specify which files should be scanned during the translation extraction process.
     * This field is configurable via the 'fluent.i18n.extractionPatterns' Maven property.
     */
    @Parameter(property = "fluent.i18n.extractionPatterns")
    protected List<String> extractionPatterns;
    
    /**
     * Specifies the format for the generated output files during the localization process.
     * Available formats may include "json", "properties", or other supported output types.
     * This parameter determines how the translation files are structured and written to the output directory.
     *
     * By default, this parameter is set to "json".
     *
     * Property: fluent.i18n.outputFormat
     * Default Value: "json"
     */
    @Parameter(property = "fluent.i18n.outputFormat", defaultValue = "json")
    protected String outputFormat;
    
    /**
     * Indicates whether translation files should be validated during the Maven plugin execution.
     *
     * This property controls whether the plugin performs validation checks on the translations
     * encountered during the internationalization process. Validation includes verifying the format,
     * structure, or consistency of translation files. This ensures that localization data adheres
     * to certain expected standards before further processing or inclusion in the build.
     *
     * Default value is {@code true}.
     *
     * Maven property: fluent.i18n.validateTranslations
     */
    @Parameter(property = "fluent.i18n.validateTranslations", defaultValue = "true")
    protected boolean validateTranslations;
    
    /**
     * Indicates whether existing translations should be preserved during generation.
     * When set to {@code true}, any existing translations in the output files will not be overwritten during the
     * processing of translation files. This is helpful to ensure that manual translations or edits remain intact.
     *
     * Property: {@code fluent.i18n.preserveExisting}
     * Default: {@code true}
     */
    @Parameter(property = "fluent.i18n.preserveExisting", defaultValue = "true")
    protected boolean preserveExisting;
    
    /**
     * Indicates whether the generated output files should be minified.
     *
     * When set to {@code true}, the output files (e.g., JSON, properties, or other supported formats)
     * will be minified to reduce file size, removing unnecessary whitespace or formatting.
     * By default, this value is set to {@code false}, meaning the output files will retain their
     * standard formatting.
     *
     * This option can be configured using the Maven property {@code fluent.i18n.minifyOutput}.
     * Example configuration can be specified in the Maven project's {@code pom.xml}.
     */
    @Parameter(property = "fluent.i18n.minifyOutput", defaultValue = "false")
    protected boolean minifyOutput;
    
    /**
     * Indicates whether metadata should be included in the generated translation files.
     * Metadata can consist of additional information about the translations, such as
     * source references or comments.
     *
     * This parameter is primarily used during the generation process of translation files.
     * If set to {@code true}, metadata will be included in the output files. If set to
     * {@code false}, this information will be excluded, resulting in smaller, more
     * concise output files but less context for translators or maintainers.
     *
     * The default value is {@code true}.
     */
    @Parameter(property = "fluent.i18n.includeMetadata", defaultValue = "true")
    protected boolean includeMetadata;
    
    /**
     * Indicates whether the goal should be skipped during execution.
     * If set to {@code true}, the plugin will not perform any actions and exit early.
     * This can be useful for conditional builds or disabling the plugin's behavior in certain environments.
     *
     * Configurable through the Maven property {@code fluent.i18n.skip}.
     * The default value is {@code false}.
     */
    @Parameter(property = "fluent.i18n.skip", defaultValue = "false")
    protected boolean skip;
    
    /**
     * Retrieves a set of supported locale codes from the `supportedLocales` field.
     * The supported locale codes are extracted by splitting the `supportedLocales`
     * string using a comma (",") as the delimiter, trimming any whitespace, and
     * filtering out empty strings.
     *
     * @return a {@code Set} of strings representing the supported locale codes
     */
    protected Set<String> getSupportedLocalesSet() {
        return Arrays.stream(supportedLocales.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }
    
    /**
     * Retrieves a list of paths representing the source directories for the project.
     * If no source directories are explicitly defined, default source directories
     * for `src/main/java`, `src/main/resources`, and `src/test/java` will be provided.
     *
     * @return a list of {@code Path} objects representing the source directories.
     */
    protected List<Path> getSourceDirectoriesPaths() {
        if (sourceDirectories == null || sourceDirectories.isEmpty()) {
            return Arrays.asList(
                project.getBasedir().toPath().resolve("src/main/java"),
                project.getBasedir().toPath().resolve("src/main/resources"),
                project.getBasedir().toPath().resolve("src/test/java")
            );
        }
        
        return sourceDirectories.stream()
            .map(dir -> project.getBasedir().toPath().resolve(dir))
            .collect(Collectors.toList());
    }
    
    /**
     * Retrieves the list of extraction patterns used to locate translatable strings within source files.
     * If no patterns are explicitly defined, a default list of common patterns is provided.
     *
     * @return a list of string patterns for extracting translatable content
     */
    protected List<String> getExtractionPatternsList() {
        if (extractionPatterns == null || extractionPatterns.isEmpty()) {
            return Arrays.asList(
                "I18n\\.translate\\s*\\(\\s*\"([^\"]+)\"",
                "I18n\\.t\\s*\\(\\s*\"([^\"]+)\"",
                "I18n\\.context\\([^)]+\\)\\.translate\\s*\\(\\s*\"([^\"]+)\"",
                "@Translatable\\s*\\(\\s*\"([^\"]+)\"",
                "\\$\\{@i18n\\.translate\\('([^']+)'\\)\\}",
                "\\$\\{@i18n\\.t\\('([^']+)'\\)\\}"
            );
        }
        return extractionPatterns;
    }
    
    /**
     * Checks whether the Maven goal execution should be skipped.
     * <p>
     * If the skip flag is set to true, this method logs a message indicating
     * that the goal execution is being skipped and throws a {@code MojoExecutionException}.
     * This ensures that further execution of the goal is aborted when skipping is required.
     *
     * @throws MojoExecutionException if the goal execution is flagged to be skipped
     */
    protected void checkSkip() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skipping fluent i18n goal execution");
            throw new MojoExecutionException("Goal execution skipped");
        }
    }
    
    /**
     * Logs the configuration details of the Fluent i18n plugin.
     * This method outputs key configuration parameters to the log, including supported locales,
     * source directories, translation-related paths, and various settings related to the build process.
     *
     * The logged details include:
     * - Supported locales configured for the project.
     * - Paths to source directories used for extracting messages.
     * - The directory for storing PO files.
     * - The directory for generated output files.
     * - The format used for output files (e.g., JSON, properties).
     * - Whether translation validation is enabled.
     * - The encoding used for source files.
     *
     * This method is intended to aid in debugging and verifying the configuration
     * of the Fluent i18n plugin during the build lifecycle.
     */
    protected void logConfiguration() {
        getLog().info("Fluent i18n Configuration:");
        getLog().info("  Supported Locales: " + getSupportedLocalesSet());
        getLog().info("  Source Directories: " + getSourceDirectoriesPaths());
        getLog().info("  PO Directory: " + poDirectory);
        getLog().info("  Output Directory: " + outputDirectory);
        getLog().info("  Output Format: " + outputFormat);
        getLog().info("  Validate Translations: " + validateTranslations);
        getLog().info("  Source Encoding: " + encoding);
    }
    
    /**
     * Checks if the project contains any Spring configuration files in the
     * resources directory. The method looks for "application.yml", "application.yaml",
     * and "application.properties" files within "src/main/resources".
     *
     * @return true if at least one of the files "application.yml", "application.yaml",
     *         or "application.properties" exists in the project's resources directory;
     *         false otherwise.
     */
    protected boolean hasSpringConfigurationFiles() {
        Path resourcesDir = project.getBasedir().toPath().resolve("src/main/resources");
        Path ymlFile = resourcesDir.resolve("application.yml");
        Path yamlFile = resourcesDir.resolve("application.yaml");
        Path propertiesFile = resourcesDir.resolve("application.properties");
        
        return java.nio.file.Files.exists(ymlFile) || java.nio.file.Files.exists(propertiesFile) || java.nio.file.Files.exists(yamlFile);
    }

    /**
     * Reads the configuration properties required for Fluent I18n processing.
     * This includes configurations such as project base directory and other
     * settings loaded using the FluentI18nConfigReader.
     *
     * @return an instance of {@link FluentI18nProperties} containing the loaded configuration properties
     * @throws IOException if an I/O error occurs while reading the configuration
     */
    protected FluentI18nProperties readConfigProperties() throws IOException {
        FluentI18nConfigReader configReader = new FluentI18nConfigReader(getLog());
        return configReader.readConfiguration(Paths.get(project.getBasedir().getAbsolutePath()));
    }
    
    /**
     * Retrieves the configuration for FluentI18n, either from application properties
     * or by creating a default configuration if none is found or if the configuration
     * is not enabled.
     *
     * @return the FluentI18nProperties instance, containing relevant configuration data
     * @throws IOException if an error occurs while reading the configuration properties
     */
    protected FluentI18nProperties getConfiguration() throws IOException {
        // Always try to read from application properties first
        FluentI18nProperties config = readConfigProperties();
        
        // If no application config found, create default config
        if (config == null || !config.isEnabled()) {
            config = new FluentI18nProperties();
        }
        
        return config;
    }
}