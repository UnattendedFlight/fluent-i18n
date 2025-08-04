package io.github.unattendedflight.fluent.i18n.compiler;

import io.github.unattendedflight.fluent.i18n.extractor.ExtractionResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * The TranslationCompiler class is responsible for compiling translation PO files
 * into runtime formats suitable for application deployment. It provides methods
 * to process the translations in the supported locales and generate output files
 * in specified formats.
 *
 * It supports multiple output formats such as JSON, properties files, and binary formats.
 * During the compilation process, it collects statistics about the translations and
 * the generated files for each locale.
 */
public class TranslationCompiler {
    /**
     * Holds the configuration settings for the {@link TranslationCompiler}.
     * This includes details such as the supported locales, the directory
     * containing PO files to be compiled, the output directory for generated
     * files, and the formats in which the translation data should be compiled.
     *
     * The configuration object is immutable once initialized and is used
     * throughout the compilation process to ensure consistency in input and
     * output settings.
     */
    private final CompilerConfig config;
    /**
     * A parser for processing PO (Portable Object) files, which are used in translation systems
     * to store text translations for different locales. The `poParser` is responsible for
     * parsing the input PO files and extracting the translation data for further processing.
     *
     * This variable is an instance of the `PoFileParser` class and is used within the
     * `TranslationCompiler` context to convert PO file content into a structured format
     * that can be utilized by output writers to generate corresponding translation files
     * suitable for various runtime environments.
     *
     * The `poParser` handles operations such as reading PO files, validating their format,
     * and returning parsed translation data in an intermediate representation
     * that encapsulates keys, translated values, and metadata.
     */
    private final PoFileParser poParser;
    /**
     * A mapping between output format types and their corresponding {@link OutputWriter} implementations.
     * This map is used to determine the appropriate writer instance for a given {@link OutputFormat}
     * when generating translation output files.
     */
    private final Map<OutputFormat, OutputWriter> writers;
    
    /**
     * Constructs a new instance of the TranslationCompiler with the specified configuration.
     * This compiler is responsible for processing translation PO files and generating output
     * files in the desired formats.
     *
     * @param config the configuration settings used for the compilation process, including
     *               supported locales, PO file directory, output formats, and output directory.
     */
    public TranslationCompiler(CompilerConfig config) {
        this.config = config;
        this.poParser = new PoFileParser();
        this.writers = createWriters();
    }
    
    /**
     * Compiles the available PO files into specified runtime formats for the supported locales.
     * It processes each locale specified in the configuration, parses the respective PO files,
     * and generates output files in the configured formats. Translation statistics and details about
     * missing files or processed locales are collected during the compilation.
     *
     * @return a {@link CompilationResult} containing details about the processed locales, generated files,
     *         translation statistics, and any missing PO files. This result provides a comprehensive
     *         summary of the compilation process.
     * @throws IOException if an I/O error occurs while reading PO files or writing output files.
     */
    public CompilationResult compile() throws IOException {
        CompilationResult.Builder resultBuilder = CompilationResult.builder();
        
        for (String locale : config.getSupportedLocales()) {
            Path poFile = config.getPoDirectory().resolve("messages_" + locale + ".po");
            
            if (Files.exists(poFile)) {
                TranslationData data = poParser.parse(poFile);
                
                // Add translation statistics
                resultBuilder.addTranslationStats(locale, data);
                
                for (OutputFormat format : config.getOutputFormats()) {
                    OutputWriter writer = writers.get(format);
                    Path outputFile = writer.write(data, locale, config.getOutputDirectory());
                    resultBuilder.addGeneratedFile(locale, format, outputFile);
                }
                
                resultBuilder.addProcessedLocale(locale, data.getEntryCount());
            } else {
                resultBuilder.addMissingPoFile(locale, poFile);
            }
        }
        
        return resultBuilder.build();
    }
    
    /**
     * Compiles the translations based on the provided extraction result.
     * This method utilizes the existing compilation mechanism, reading existing
     * PO files in the configured directory to generate the outputs in the desired formats.
     *
     * @param extractionResult the result of the extraction process that contains metadata
     *                         and details about extracted translation entries. It is
     *                         utilized to synchronize the compilation process with
     *                         extracted data.
     * @return a result object containing statistics, generated files, and processing details
     *         related to the compilation of translation files.
     * @throws IOException if an I/O error occurs while accessing PO files or writing outputs.
     */
    public CompilationResult compileFromExtraction(ExtractionResult extractionResult) throws IOException {
        // The compiler only reads existing PO files, generation is handled by the Maven plugin
        return compile();
    }
    
    /**
     * Creates a mapping of {@link OutputFormat} to their corresponding {@link OutputWriter} implementations.
     * Each output format is associated with an instance of a specific writer implementation
     * responsible for handling the output in that format.
     *
     * @return a {@code Map} where the keys are {@link OutputFormat} constants (e.g., JSON, PROPERTIES, BINARY),
     *         and the values are {@link OutputWriter} instances configured to handle the respective output formats.
     */
    private Map<OutputFormat, OutputWriter> createWriters() {
        return Map.of(
            OutputFormat.JSON, new JsonOutputWriter(config),
            OutputFormat.PROPERTIES, new PropertiesOutputWriter(config),
            OutputFormat.BINARY, new BinaryOutputWriter(config)
        );
    }
}