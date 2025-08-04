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
 * Extracts translatable messages from source code
 */
@Mojo(name = "extract", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ExtractMojo extends AbstractFluentI18nMojo {

    @Parameter(property = "fluent.i18n.extract.scanTestSources", defaultValue = "false")
    private boolean scanTestSources;

    @Parameter(property = "fluent.i18n.extract.overwriteExisting", defaultValue = "false")
    private boolean overwriteExisting;

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

    private Map<String, Integer> getTranslationCounts(ExtractionResult result) {
        Map<String, Integer> counts = new HashMap<>();
        for (ExtractedMessage message : result.getExtractedMessages().values()) {
            for (SourceLocation location : message.getLocations()) {
                counts.compute(location.getFilePath(), (k, v) -> v == null ? 1 : v + 1);
            }
        }
        return counts;
    }

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