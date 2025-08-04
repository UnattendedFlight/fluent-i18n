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
 * Enhanced compile mojo with robust PO parsing and accurate statistics
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class CompileMojo extends AbstractFluentI18nMojo {

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
     * Analyze translation completeness using robust PO parsing
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
     * Analyze statistics for a single locale
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
     * Display translation statistics in the same format as the original Maven output
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
     * Statistics container classes
     */
    private static class CompilationStats {
        private final Map<String, LocaleStats> localeStats = new HashMap<>();

        public void addLocaleStats(String locale, LocaleStats stats) {
            localeStats.put(locale, stats);
        }

        public Map<String, LocaleStats> getLocaleStats() {
            return localeStats;
        }
    }

    private static class LocaleStats {
        private final int totalMessages;
        private final int translatedMessages;

        public LocaleStats(int totalMessages, int translatedMessages) {
            this.totalMessages = totalMessages;
            this.translatedMessages = translatedMessages;
        }

        public int getTotalMessages() { return totalMessages; }
        public int getTranslatedMessages() { return translatedMessages; }

        public boolean isComplete() {
            return totalMessages > 0 && translatedMessages == totalMessages;
        }

        public double getCompletionPercentage() {
            return totalMessages > 0 ? (translatedMessages * 100.0 / totalMessages) : 0.0;
        }
    }
}