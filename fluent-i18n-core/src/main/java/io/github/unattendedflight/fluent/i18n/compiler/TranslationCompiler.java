package io.github.unattendedflight.fluent.i18n.compiler;

import io.github.unattendedflight.fluent.i18n.extractor.ExtractionResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Main compiler for converting PO files to runtime translation formats
 */
public class TranslationCompiler {
    private final CompilerConfig config;
    private final PoFileParser poParser;
    private final Map<OutputFormat, OutputWriter> writers;
    
    public TranslationCompiler(CompilerConfig config) {
        this.config = config;
        this.poParser = new PoFileParser();
        this.writers = createWriters();
    }
    
    /**
     * Compile all PO files to runtime format
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
     * Compile from extraction result (direct workflow)
     * Note: PO file generation is handled by the Maven plugin, not the compiler
     */
    public CompilationResult compileFromExtraction(ExtractionResult extractionResult) throws IOException {
        // The compiler only reads existing PO files, generation is handled by the Maven plugin
        return compile();
    }
    
    private Map<OutputFormat, OutputWriter> createWriters() {
        return Map.of(
            OutputFormat.JSON, new JsonOutputWriter(config),
            OutputFormat.PROPERTIES, new PropertiesOutputWriter(config),
            OutputFormat.BINARY, new BinaryOutputWriter(config)
        );
    }
}