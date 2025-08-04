package io.github.unattendedflight.fluent.i18n.compiler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

/**
 * Configuration class for the translation file compilation process.
 * Provides options to configure the input files, output formats, and compilation settings.
 * Utilizes the builder pattern to allow for more flexible configurations.
 */
public class CompilerConfig {
    /**
     * Represents the default directory path where translation `.po` files are located.
     * This directory is used as the source for translation file compilation.
     */
    private Path poDirectory = Path.of("src/main/resources/i18n/po");
    /**
     * Specifies the directory where the compiled translation files will be written.
     * It serves as the target output location for storing generated files in supported formats.
     * The default value is set to "src/main/resources/i18n".
     */
    private Path outputDirectory = Path.of("src/main/resources/i18n");
    /**
     * Set of supported locale codes for the application or translation system.
     * Used to specify which locales will be included during the translation compilation process.
     * Default values include "en" (English), "nb" (Norwegian Bokmål), and "sv" (Swedish).
     */
    private Set<String> supportedLocales = Set.of("en", "nb", "sv");
    /**
     * A set of output formats to be used during the translation compilation process.
     * By default, it includes only the JSON format. The set is mutable, allowing
     * for additional formats to be added or removed as needed.
     */
    private Set<OutputFormat> outputFormats = EnumSet.of(OutputFormat.JSON);
    /**
     * Defines the character encoding used for reading and writing files during the compilation process.
     * This determines how text is interpreted and written, ensuring compatibility with input and output file formats.
     */
    private Charset encoding = StandardCharsets.UTF_8;
    /**
     * Determines whether to preserve existing output files during the compilation process.
     * If set to true, existing files will not be overwritten. Instead, new files will only be created
     * for translations or locales that do not yet exist in the output directory.
     * If set to false, all output files will be overwritten regardless of their previous state.
     */
    private boolean preserveExisting = true;
    /**
     * Flag that determines whether translations should be validated during the compilation process.
     * If set to true, the system will perform checks to ensure the translations meet the required standards or formats.
     */
    private boolean validateTranslations = true;
    /**
     * Indicates whether the output should be minified during the compilation process.
     * If set to true, the resulting output files will have unnecessary whitespace,
     * comments, and formatting removed to reduce file size.
     * Defaults to false, which preserves human-readable formatting in the output.
     */
    private boolean minifyOutput = false;
    /**
     * Determines whether metadata from the PO file header should be included
     * in the compiled output files. Metadata, such as project version and
     * language details, can provide additional context or information about
     * the translation files being compiled.
     *
     * When set to {@code true}, metadata will be extracted from the PO file
     * header (e.g., {@link PoMetadata}) and included in the output files.
     * If set to {@code false}, metadata will be omitted from the output.
     *
     * This option is particularly useful for tracking and debugging compiled
     * translations or when metadata is required to comply with specific
     * project requirements.
     *
     * Default value is {@code true}.
     */
    private boolean includeMetadata = true;
    
    /**
     * Provides a new instance of `CompilerConfig` for building a customized configuration using the builder pattern.
     *
     * @return a new instance of `CompilerConfig` for configuration customization.
     */
    // Builder pattern
    public static CompilerConfig builder() {
        return new CompilerConfig();
    }
    
    /**
     * Sets the directory path where the PO files are located.
     * This method allows configuring the source directory for translation files.
     *
     * @param directory the directory path containing the PO files
     * @return the current instance of {@code CompilerConfig} for method chaining
     */
    public CompilerConfig poDirectory(Path directory) {
        this.poDirectory = directory;
        return this;
    }
    
    /**
     * Sets the output directory where the compiled translation files will be written.
     *
     * @param directory the path of the directory to set as the output location
     * @return the current instance of {@code CompilerConfig} for method chaining
     */
    public CompilerConfig outputDirectory(Path directory) {
        this.outputDirectory = directory;
        return this;
    }
    
    /**
     * Configures the set of supported locales for the compilation process.
     * The specified set will replace any existing configured locales.
     *
     * @param locales a set of locale identifiers to be supported (e.g., "en", "nb")
     * @return the current instance of {@code CompilerConfig} for method chaining
     */
    public CompilerConfig supportedLocales(Set<String> locales) {
        this.supportedLocales = Set.copyOf(locales);
        return this;
    }
    
    /**
     * Configures the output formats that the compiler will generate for translations.
     * Updates the configuration with the provided formats.
     *
     * @param formats one or more {@code OutputFormat} values indicating the output formats to be used.
     * @return the updated {@code CompilerConfig} instance, to allow for method chaining in the builder pattern.
     */
    public CompilerConfig outputFormats(OutputFormat... formats) {
        this.outputFormats = EnumSet.of(formats[0], formats);
        return this;
    }
    
    /**
     * Sets the minification option for the output during the compilation process.
     * When enabled, the output will be minimized by removing unnecessary whitespace and formatting.
     *
     * @param minify a boolean value indicating whether the output should be minified.
     *               If true, the output will be minified; if false, it will retain the default formatting.
     * @return the updated CompilerConfig instance with the new minification setting applied.
     */
    public CompilerConfig minifyOutput(boolean minify) {
        this.minifyOutput = minify;
        return this;
    }
    
    /**
     * Configures whether translation validation should be performed during the compilation process.
     *
     * @param validate a boolean indicating whether to validate translations.
     *                 If true, validation will be enabled.
     * @return the current instance of {@code CompilerConfig}, allowing for method chaining.
     */
    public CompilerConfig validateTranslations(boolean validate) {
        this.validateTranslations = validate;
        return this;
    }
    
    /**
     * Retrieves the directory path where PO (Portable Object) files are stored.
     *
     * @return the path to the directory containing PO files
     */
    // Getters
    public Path getPoDirectory() { return poDirectory; }
    /**
     * Retrieves the directory where compiled translation files will be output.
     *
     * @return the output directory as a {@code Path} object
     */
    public Path getOutputDirectory() { return outputDirectory; }
    /**
     * Retrieves the set of supported locales for the translation compilation process.
     *
     * @return a set of strings representing the supported locale identifiers (e.g., language codes).
     */
    public Set<String> getSupportedLocales() { return supportedLocales; }
    /**
     * Retrieves the set of output formats currently configured for the compilation process.
     * These formats represent the file types to be generated from the translation data.
     *
     * @return a set of {@code OutputFormat} enumerations representing the supported output formats.
     */
    public Set<OutputFormat> getOutputFormats() { return outputFormats; }
    /**
     * Retrieves the character encoding used for file operations, such as reading
     * and writing translation files.
     *
     * @return the character encoding as a Charset object
     */
    public Charset getEncoding() { return encoding; }
    /**
     * Determines whether existing compiled translation files should be preserved
     * during the compilation process.
     *
     * @return true if existing files should be preserved, false otherwise
     */
    public boolean isPreserveExisting() { return preserveExisting; }
    /**
     * Indicates whether translation validation is enabled.
     *
     * @return true if translation validation is enabled; false otherwise.
     */
    public boolean isValidateTranslations() { return validateTranslations; }
    /**
     * Indicates whether the output of the compilation process should be minified.
     *
     * @return true if the output should be minified, false otherwise
     */
    public boolean isMinifyOutput() { return minifyOutput; }
    /**
     * Indicates whether metadata from the PO file headers should be included
     * in the output during the compilation process.
     *
     * @return true if metadata inclusion is enabled; false otherwise
     */
    public boolean isIncludeMetadata() { return includeMetadata; }
}