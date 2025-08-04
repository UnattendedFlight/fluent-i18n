package io.github.unattendedflight.fluent.i18n.maven;

import io.github.unattendedflight.fluent.i18n.compiler.CompilationError;
import io.github.unattendedflight.fluent.i18n.compiler.CompilationResult;
import io.github.unattendedflight.fluent.i18n.compiler.CompilerConfig;
import io.github.unattendedflight.fluent.i18n.compiler.OutputFormat;
import io.github.unattendedflight.fluent.i18n.compiler.TranslationCompiler;
import io.github.unattendedflight.fluent.i18n.compiler.PoFileParser;
import io.github.unattendedflight.fluent.i18n.compiler.TranslationData;
import io.github.unattendedflight.fluent.i18n.compiler.TranslationEntry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;

/**
 * CompileMojo is a Maven plugin goal that compiles translation files for supported locales
 * into specified output formats. This goal is typically invoked during the PROCESS_RESOURCES
 * phase of the Maven lifecycle and aims to prepare translation resources for use in the
 * application, such as generating JSON or JavaScript files from PO (Portable Object) files.
 *
 * This class extends AbstractFluentI18nMojo to leverage the configuration and utility
 * functions provided by the Fluent I18n project, ensuring adherence to localization standards.
 *
 * Responsibilities:
 * - Validates configuration and skips execution if applicable.
 * - Analyzes translation completeness using robust PO file parsing.
 * - Compiles translations to specified formats according to plugin or application configuration.
 * - Displays detailed statistics about translation completeness and generated output.
 * - Reports any missing translation files or entries, providing warnings for incomplete translations.
 *
 * Features:
 * - Support for multiple locales based on Maven or application configuration.
 * - Configurable output formats, such as JSON or JavaScript.
 * - Minification and validation of output, controlled via configuration.
 * - Detailed logging of compilation results and statistics for debugging or review.
 *
 * Configuration:
 * Configuration for this plugin is derived from either the Maven plugin settings or the
 * application's `FluentI18nProperties`. This includes settings such as:
 * - Output directory paths.
 * - Supported locales.
 * - Output formats.
 * - Minification and validation preferences.
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class CompileMojo extends AbstractFluentI18nMojo {

    /**
     * Executes the Maven goal for compiling translations and generating output files.
     *
     * The method performs the following operations:
     * - Checks if the goal execution should be skipped.
     * - Configures the translation compiler by building the {@code CompilerConfig} object.
     * - Analyzes the completeness of translations using robust statistical tools.
     * - Compiles translations into the specified output format.
     * - Logs detailed statistics on processed locales and generated files.
     *
     * If errors occur during the compilation process:
     * - Logs the errors encountered during the process.
     * - Throws a {@code MojoFailureException} when compilation fails.
     *
     * If translation files are missing:
     * - Logs warnings for each missing PO file.
     *
     * Any {@code IOException} encountered during the configuration or compilation process
     * results in a {@code MojoExecutionException} being thrown to halt execution.
     *
     * @throws MojoExecutionException if an error occurs during configuration or compilation
     * @throws MojoFailureException if the compilation fails due to errors in translation files
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        checkSkip();

        getLog().info("Starting translation compilation...");

        try {
            CompilerConfig config = buildCompilerConfig();

            // Use robust parser for statistics
            CompilationStats stats = analyzeTranslationCompleteness(config);

            // Perform actual compilation
            getLog().info("Compiling translations to output format: " + config.getOutputFormats());
            TranslationCompiler compiler = new TranslationCompiler(config);
            CompilationResult result = compiler.compile();

            if (result.isSuccessful()) {
                getLog().info("Compilation completed successfully:");
                getLog().info("  Processed locales: " + result.getProcessedLocales().size());
                getLog().info("  Generated files: " + result.getTotalGeneratedFiles());

                // Display detailed translation statistics using our robust analysis
                displayTranslationStatistics(stats);

            } else {
                getLog().error("Compilation completed with errors:");
                for (CompilationError error : result.getErrors()) {
                    getLog().error("  " + error.toString());
                }
                throw new MojoFailureException("Translation compilation failed");
            }

            if (!result.getMissingPoFiles().isEmpty()) {
                getLog().warn("Missing PO files:");
                result.getMissingPoFiles().forEach(missing ->
                    getLog().warn("  " + missing)
                );
            }

        } catch (IOException e) {
            throw new MojoExecutionException("Failed to compile translations", e);
        }
    }

    /**
     * Analyzes the completeness of translations for each supported locale, based on
     * PO files stored in the specified directory. For each locale, it evaluates the translation
     * data and collects statistics. If no PO file exists for a locale, it records zero statistics.
     *
     * @param config the configuration of the compiler, including supported locales and PO file directory
     * @return a {@link CompilationStats} object containing statistics for each locale
     * @throws IOException if an I/O error occurs while reading the PO files
     */
    private CompilationStats analyzeTranslationCompleteness(CompilerConfig config) throws IOException {
        CompilationStats stats = new CompilationStats();
        PoFileParser parser = new PoFileParser();
        
        // Get default locale from configuration
        FluentI18nProperties appConfig = getConfiguration();
        String defaultLocale = appConfig.getDefaultLocale().toLanguageTag();

        for (String locale : config.getSupportedLocales()) {
            Path poFile = config.getPoDirectory().resolve("messages_" + locale + ".po");

            if (Files.exists(poFile)) {
                TranslationData data = parser.parse(poFile);
                LocaleStats localeStats = analyzeLocaleStats(data, locale, defaultLocale);
                stats.addLocaleStats(locale, localeStats);
            } else {
                // No PO file exists
                stats.addLocaleStats(locale, new LocaleStats(0, 0));
            }
        }

        return stats;
    }

    /**
     * Analyzes the translation data for a specific locale and provides statistical information about
     * the total number of messages and the number of translated messages for the given locale.
     *
     * If the locale matches the default locale, all entries are considered translated.
     * For other locales, each entry is checked to determine if a translation is provided.
     * Logs a warning for any missing translations.
     *
     * @param data the translation data containing entries and associated metadata
     * @param locale the target locale for which the translation statistics are calculated
     * @param defaultLocale the default locale used as a reference, where all entries are considered translated
     * @return a LocaleStats object containing the total number of messages and the number of translated messages
     */
    private LocaleStats analyzeLocaleStats(TranslationData data, String locale, String defaultLocale) {
        int totalMessages = data.getEntryCount();
        int translatedMessages = 0;

        // For default locale, all entries are considered translated
        if (locale.equals(defaultLocale)) {
            translatedMessages = totalMessages;
        } else {
            // For other locales, check if each entry has a translation
            for (TranslationEntry entry : data.getEntries().values()) {
                if (entry.hasTranslation()) {
                    translatedMessages++;
                } else {
                    getLog().warn(String.format("Missing translation for '%s' in locale '%s' (source: %s)",
                        entry.getOriginalText(), locale, entry.getSourceLocation()));
                }
            }
        }

        return new LocaleStats(totalMessages, translatedMessages);
    }

    /**
     * Displays detailed statistics about the translation completeness for each locale
     * and overall summary, including the percentage of translations completed and missing translations.
     *
     * @param stats a CompilationStats object containing statistics for each locale,
     *              including total messages, translated messages, and completeness data.
     */
    private void displayTranslationStatistics(CompilationStats stats) {
        for (Map.Entry<String, LocaleStats> entry : stats.getLocaleStats().entrySet()) {
            String locale = entry.getKey();
            LocaleStats localeStats = entry.getValue();

            int total = localeStats.getTotalMessages();
            int translated = localeStats.getTranslatedMessages();
            int missing = total - translated;
            double percentage = total > 0 ? (translated * 100.0 / total) : 0.0;

            getLog().info(String.format("%s: %d/%d translations (%.1f%% complete, %d missing)",
                locale, translated, total, percentage, missing));
        }

        // Overall statistics
        int totalLocales = stats.getLocaleStats().size();
        int completeLocales = (int) stats.getLocaleStats().values().stream()
            .filter(LocaleStats::isComplete)
            .count();

        double averageCompletion = stats.getLocaleStats().values().stream()
            .mapToDouble(LocaleStats::getCompletionPercentage)
            .average()
            .orElse(0.0);

        getLog().info(String.format("Overall: %d/%d locales complete (%.1f%% average completion)",
            completeLocales, totalLocales, averageCompletion));
    }

    /**
     * Builds and configures a CompilerConfig instance using application settings as the primary source
     * and Maven plugin configuration as a fallback. The configuration process includes setting supported
     * locales, output formats, validation settings, and minification options.
     *
     * @return a configured CompilerConfig instance.
     * @throws MojoExecutionException if there is an issue with configuration values, such as invalid output formats.
     * @throws IOException if there is an issue accessing configuration properties or file system paths.
     */
    private CompilerConfig buildCompilerConfig() throws MojoExecutionException, IOException {
        // Get configuration from application properties (primary source)
        FluentI18nProperties appConfig = getConfiguration();

        // Initialize with application configuration
        CompilerConfig config = CompilerConfig.builder()
            .poDirectory(poDirectory.toPath())
            .outputDirectory(outputDirectory.toPath());

        // Set supported locales from application config
        Set<Locale> appLocales = appConfig.getSupportedLocales();
        if (!appLocales.isEmpty()) {
            config.supportedLocales(appLocales.stream()
                .map(Locale::toLanguageTag)
                .collect(Collectors.toSet()));
        } else {
            // Fallback to Maven plugin configuration
            config.supportedLocales(getSupportedLocalesSet());
        }

        // Set output format from application config
        FluentI18nProperties.Compilation appCompilation = appConfig.getCompilation();
        String appOutputFormat = appCompilation.getOutputFormat();
        getLog().info("appOutputFormat: " + appOutputFormat);
        if (appOutputFormat != null && !appOutputFormat.isEmpty()) {
            try {
                String[] formats = appOutputFormat.split(",");
                OutputFormat[] outputFormats = new OutputFormat[formats.length];
                for (int i = 0; i < formats.length; i++) {
                    outputFormats[i] = OutputFormat.valueOf(formats[i].trim().toUpperCase());
                }
                config.outputFormats(outputFormats);
            } catch (IllegalArgumentException e) {
                throw new MojoExecutionException(
                    "Invalid output format in application config: " + appOutputFormat);
            }
        } else {
            // Fallback to Maven plugin configuration
            try {
                OutputFormat format = OutputFormat.valueOf(outputFormat.toUpperCase());
                config.outputFormats(format);
            } catch (IllegalArgumentException e) {
                throw new MojoExecutionException("Invalid output format: " + outputFormat);
            }
        }

        // Set validation from application config
        Boolean appValidation = appCompilation.isValidation();
        if (appValidation != null) {
            config.validateTranslations(appValidation);
        } else {
            // Fallback to Maven plugin configuration
            config.validateTranslations(validateTranslations);
        }

        // Set minify output from application config
        Boolean appMinifyOutput = appCompilation.isMinifyOutput();
        if (appMinifyOutput != null) {
            config.minifyOutput(appMinifyOutput);
        } else {
            // Fallback to Maven plugin configuration
            config.minifyOutput(minifyOutput);
        }

        return config;
    }

    /**
     * Represents compilation statistics for translation files across multiple locales.
     *
     * This class provides the following functionalities:
     * - Maintains a mapping of locale identifiers to their respective translation statistics.
     * - Allows addition of new locale-specific statistics.
     * - Provides access to the collected statistics for further analysis or reporting.
     */
    private static class CompilationStats {
        /**
         * A mapping of locale identifiers to their respective translation statistics.
         *
         * Each entry in the map associates a locale (as a String key) with its corresponding
         * {@link LocaleStats} object, which provides information about the translation status
         * for that locale. This includes details such as the total number of messages and the
         * number of messages that have been translated.
         *
         * This field is used to store and manage statistics for multiple locales, enabling
         * efficient retrieval and analysis of translation data.
         *
         * Immutable as it is defined as a `final` field but can have its content modified
         * through operations provided by the {@link CompilationStats} class.
         */
        private final Map<String, LocaleStats> localeStats = new HashMap<>();

        /**
         * Adds locale-specific translation statistics to the internal mapping.
         *
         * This method associates a locale identifier with its corresponding
         * {@code LocaleStats} object, which contains information about the total
         * and translated messages for that locale.
         *
         * @param locale the identifier for the locale (e.g., "en", "fr", "es")
         * @param stats  the {@code LocaleStats} object containing translation statistics
         *               for the specified locale
         */
        public void addLocaleStats(String locale, LocaleStats stats) {
            localeStats.put(locale, stats);
        }

        /**
         * Retrieves the mapping of locale identifiers to their respective translation statistics.
         *
         * This method provides access to the collected statistics, where the key represents
         * a locale identifier (e.g., "en", "fr") and the value is a {@link LocaleStats} object
         * containing details about the total and translated messages for that locale.
         *
         * @return a map of locale identifiers to their corresponding {@link LocaleStats} objects
         */
        public Map<String, LocaleStats> getLocaleStats() {
            return localeStats;
        }
    }

    /**
     * Represents statistical data related to translations for a specific locale.
     *
     * This class provides information about the total number of messages and the
     * number of translated messages for a particular locale. It also includes
     * methods to determine whether all messages are translated or to calculate the
     * completion percentage.
     */
    private static class LocaleStats {
        /**
         * The total number of messages in a specific locale.
         *
         * This variable represents the total count of all messages that may need translation
         * for a given locale. It is used in conjunction with {@code translatedMessages} to
         * compute the translation completion status and percentage.
         *
         * This value is immutable and assigned at the creation of the containing object.
         */
        private final int totalMessages;
        /**
         * The number of messages that have been translated for a specific locale.
         *
         * This variable represents a subset of messages from the total messages available
         * and is used to calculate translation completeness and the percentage of
         * translated messages.
         */
        private final int translatedMessages;

        /**
         * Constructs a new {@code LocaleStats} instance with the specified total number of
         * messages and the number of translated messages.
         *
         * @param totalMessages the total number of messages available for a specific locale
         * @param translatedMessages the number of messages that have been translated for the locale
         */
        public LocaleStats(int totalMessages, int translatedMessages) {
            this.totalMessages = totalMessages;
            this.translatedMessages = translatedMessages;
        }

        /**
         * Returns the total number of messages.
         *
         * @return the total number of messages as an integer
         */
        public int getTotalMessages() { return totalMessages; }
        /**
         * Retrieves the number of translated messages.
         *
         * @return the total count of translated messages
         */
        public int getTranslatedMessages() { return translatedMessages; }

        /**
         * Determines whether all messages have been translated.
         *
         * The method checks if the total number of messages is greater than zero
         * and if the number of translated messages matches the total number of messages.
         *
         * @return true if all messages are translated and the total number of messages is greater than zero;
         *         false otherwise
         */
        public boolean isComplete() {
            return totalMessages > 0 && translatedMessages == totalMessages;
        }

        /**
         * Calculates the percentage of messages that have been translated.
         *
         * This method determines the completion percentage based on the total number
         * of messages and the number of translated messages. If there are no messages
         * to translate (i.e., totalMessages is 0), the method returns 0.0.
         *
         * @return the completion percentage as a double value ranging from 0.0 to 100.0,
         *         or 0.0 if there are no messages to translate.
         */
        public double getCompletionPercentage() {
            return totalMessages > 0 ? (translatedMessages * 100.0 / totalMessages) : 0.0;
        }
    }
}