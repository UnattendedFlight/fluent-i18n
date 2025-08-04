package io.github.unattendedflight.fluent.i18n.compiler;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Writes translation data to Java Properties format
 */
public class PropertiesOutputWriter implements OutputWriter {
    private final CompilerConfig config;
    
    public PropertiesOutputWriter(CompilerConfig config) {
        this.config = config;
    }
    
    @Override
    public Path write(TranslationData data, String locale, Path outputDirectory) throws IOException {
        Files.createDirectories(outputDirectory);
        Path outputFile = outputDirectory.resolve(OutputFormat.PROPERTIES.getFileName(locale));
        
        try (Writer writer = Files.newBufferedWriter(outputFile, config.getEncoding())) {
            if (config.isIncludeMetadata()) {
                writer.write("# Translation file for locale: " + locale + "\n");
                writer.write("# Generated from PO file\n");
                writer.write("# Entry count: " + data.getEntryCount() + "\n\n");
            }
            
            for (Map.Entry<String, TranslationEntry> entry : data.getEntries().entrySet()) {
                String hash = entry.getKey();
                TranslationEntry translationEntry = entry.getValue();
                
                if (config.isIncludeMetadata() && translationEntry.getSourceLocation() != null) {
                    writer.write("# Source: " + translationEntry.getSourceLocation() + "\n");
                }
                
                String escapedTranslation = escapePropertiesValue(translationEntry.getTranslation());
                writer.write(hash + "=" + escapedTranslation + "\n");
                
                if (config.isIncludeMetadata()) {
                    writer.write("\n");
                }
            }
        }
        
        return outputFile;
    }
    
    private String escapePropertiesValue(String value) {
        return value.replace("\\", "\\\\")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t")
                   .replace("=", "\\=")
                   .replace(":", "\\:");
    }
    
    @Override
    public OutputFormat getOutputFormat() {
        return OutputFormat.PROPERTIES;
    }
}