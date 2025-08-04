package io.github.unattendedflight.fluent.i18n.maven;

import io.github.unattendedflight.fluent.i18n.extractor.ExtractedMessage;
import io.github.unattendedflight.fluent.i18n.extractor.SourceLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import io.github.unattendedflight.fluent.i18n.extractor.ExtractionConfig;
import io.github.unattendedflight.fluent.i18n.extractor.ExtractionResult;
import io.github.unattendedflight.fluent.i18n.extractor.MessageExtractor;

import java.io.IOException;

/**
 * Maven plugin goal to extract translatable messages from source code and synchronize them with PO files.
 * This class is responsible for managing the extraction process, including reading configuration,
 * initiating message extraction, and handling PO file synchronization.
 *
 * The goal "extract" can be executed during the Maven build, typically in the "generate-resources" phase.
 * It supports configuration for scanning test sources and handling pre-existing translations.
 *
 * The process includes:
 * - Extracting messages based on specified patterns and source directories.
 * - Synchronizing the extracted messages with corresponding PO files.
 * - Managing external editor changes to PO files while preserving translations.
 *
 * Configuration options:
 * - scanTestSources: Determines if test source directories should be scanned for translatable messages.
 * - overwriteExisting: Defines whether pre-existing translations in PO files should be overwritten.
 *
 * Exceptions:
 * - MojoExecutionException: Thrown when there is an error during the extraction or synchronization process.
 * - MojoFailureException: Thrown when the plugin execution fails due to a configuration or runtime issue.
 */
@Mojo(name = "extract", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ExtractMojo extends AbstractFluentI18nMojo {

    /**
     * Specifies whether to include test source directories when scanning for translatable strings.
     *
     * If set to true, the plugin will scan both main and test source directories for localizable content.
     * If set to false, only the main source directories will be scanned.
     *
     * Configurable via the Maven property "fluent.i18n.extract.scanTestSources", with a default value of false.
     */
    @Parameter(property = "fluent.i18n.extract.scanTestSources", defaultValue = "false")
    private boolean scanTestSources;

    /**
     * Determines whether existing translation files should be overwritten during the extraction process.
     *
     * When set to {@code true}, any pre-existing files in the output directory for extracted translations
     * (e.g., `.po` or `.pot` files) will be replaced with newly generated files. If set to {@code false},
     * the extraction process will preserve these files and avoid overwriting them.
     *
     * Default value is {@code false}.
     */
    @Parameter(property = "fluent.i18n.extract.overwriteExisting", defaultValue = "false")
    private boolean overwriteExisting;

    /**
     * Executes the Maven goal to extract internationalized messages and synchronize
     * PO files containing translations. This method performs the following tasks:
     *
     * 1. Verifies if the execution should be skipped by calling {@code checkSkip}.
     *    If skipping is required, the execution is aborted.
     * 2. Logs the start of the message extraction process.
     * 3. Builds the configuration for message extraction using {@code buildExtractionConfig}.
     * 4. Executes the extraction of messages through the {@link MessageExtractor}.
     * 5. Logs details of the extraction results including the number of messages, occurrences,
     *    supported locales, and translation counts by location.
     * 6. Synchronizes PO files based on extracted messages and existing translations.
     *    The synchronization ensures compatibility with external editor workflows
     *    by preserving existing translations.
     * 7. Logs the location of the synchronized PO files.
     *
     * If an error occurs during this process (e.g., IO exceptions), the method throws
     * corresponding exceptions to signal failure.
     *
     * @throws MojoExecutionException if the execution encounters an unexpected error
     *                                or is explicitly flagged for skipping
     * @throws MojoFailureException   if the execution fails due to a configuration
     *                                or process-related issue
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        checkSkip();

        getLog().info("Starting message extraction and PO synchronization...");

        try {
            ExtractionConfig config = buildExtractionConfig();
            MessageExtractor extractor = new MessageExtractor(config);
            ExtractionResult result = extractor.extract();
            Map<String, Integer> translationCounts = getTranslationCounts(result);

            getLog().info("Extraction completed successfully:");
            getLog().info("  Messages found: " + result.getMessageCount());
            getLog().info("  Total occurrences: " + result.getTotalOccurrences());
            getLog().info("  Supported locales: " + result.getSupportedLocales());
            getLog().info("  Translation counts by location:");
            for (Map.Entry<String, Integer> entry : translationCounts.entrySet()) {
                getLog().info("    " + entry.getKey() + ": " + entry.getValue() + " occurrences");
            }

            getLog().info("Synchronizing PO files with external editor changes...");

            // Generate synchronized PO files (always preserve existing when working with external editors)
            PoFileGenerator generator = new PoFileGenerator(
                poDirectory.toPath(),
                config.getSupportedLocales(),
                true  // Always preserve existing translations for external editor workflow
            );
            generator.generatePoFiles(result);

            getLog().info("PO files synchronized in: " + poDirectory);

        } catch (IOException e) {
            throw new MojoExecutionException("Failed to extract and synchronize messages", e);
        }
    }

    /**
     * Computes and returns a mapping of file paths to the count of extracted
     * message occurrences in each respective file.
     *
     * @param result the extraction result containing extracted messages with their
     *               source locations
     * @return a map where keys are file paths as strings, and values are integers
     *         representing the number of occurrences of extracted messages in each file
     */
    private Map<String, Integer> getTranslationCounts(ExtractionResult result) {
        Map<String, Integer> counts = new HashMap<>();
        for (ExtractedMessage message : result.getExtractedMessages().values()) {
            for (SourceLocation location : message.getLocations()) {
                counts.compute(location.getFilePath(), (k, v) -> v == null ? 1 : v + 1);
            }
        }
        return counts;
    }

    /**
     * Builds and returns an ExtractionConfig object by fetching and integrating configurations from
     * the application properties, Maven plugin configuration, and project-specific settings.
     * The method configures parameters such as supported locales, source encoding, extraction patterns,
     * and source directories, and optionally includes test sources if enabled.
     *
     * @return The configured ExtractionConfig instance.
     * @throws IOException If an I/O error occurs during the configuration process.
     */
    private ExtractionConfig buildExtractionConfig() throws IOException {
        // Get configuration from application properties (primary source)
        FluentI18nProperties appConfig = getConfiguration();

        // Initialize with application configuration
        ExtractionConfig config = ExtractionConfig.builder()
            .projectRoot(project.getBasedir().toPath())
            .sourceDirectories(getSourceDirectoriesPaths());

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

        // Set source encoding from application config
        FluentI18nProperties.Extraction appExtraction = appConfig.getExtraction();
        String appSourceEncoding = appExtraction.getSourceEncoding();
        if (appSourceEncoding != null && !appSourceEncoding.isEmpty()) {
            config.sourceEncoding(Charset.forName(appSourceEncoding));
        } else {
            // Fallback to Maven plugin configuration
            config.sourceEncoding(Charset.forName(encoding));
        }

        // Set extraction patterns from application config
        List<String> appMethodCallPatterns = appExtraction.getMethodCallPatterns();
        if (!appMethodCallPatterns.isEmpty()) {
            for (String pattern : appMethodCallPatterns) {
                config.addMethodCallPattern(pattern);
            }
        } else {
            // Fallback to Maven plugin configuration
            for (String pattern : getExtractionPatternsList()) {
                config.addMethodCallPattern(pattern);
            }
        }

        // Set annotation patterns from application config
        List<String> appAnnotationPatterns = appExtraction.getAnnotationPatterns();
        if (!appAnnotationPatterns.isEmpty()) {
            for (String pattern : appAnnotationPatterns) {
                config.addAnnotationPattern(pattern);
            }
        }

        // Set template patterns from application config
        List<String> appTemplatePatterns = appExtraction.getTemplatePatterns();
        if (!appTemplatePatterns.isEmpty()) {
            for (String pattern : appTemplatePatterns) {
                config.addTemplatePattern(pattern);
            }
        }

        // Add test sources if enabled (always use Maven plugin config for this)
        if (scanTestSources) {
            List<Path> allSources = new ArrayList<>(config.getSourceDirectories());
            Path testJavaPath = project.getBasedir().toPath().resolve("src/test/java");
            Path testResourcesPath = project.getBasedir().toPath().resolve("src/test/resources");
            allSources.add(testJavaPath);
            allSources.add(testResourcesPath);
            config.sourceDirectories(allSources);
        }

        return config;
    }
}