package io.github.unattendedflight.fluent.i18n.compiler;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * An implementation of the {@link OutputWriter} interface for writing translation data
 * in the properties file format. This class generates `.properties` files with key-value
 * pairs that store translations for the specified locale. It also supports metadata
 * inclusion such as source information and entry counts, based on configuration.
 */
public class PropertiesOutputWriter implements OutputWriter {
    /**
     * Configuration object for the compiler used to determine specific behaviors
     * and settings for output generation, such as encoding options or metadata
     * inclusion rules. This variable is immutable and set during the creation of
     * the {@link PropertiesOutputWriter}.
     */
    private final CompilerConfig config;
    
    /**
     * Constructs a new instance of {@code PropertiesOutputWriter} for writing
     * translation data in the properties file format.
     *
     * @param config the configuration object that specifies settings such as
     *               file encoding, metadata inclusion, and other output preferences
     */
    public PropertiesOutputWriter(CompilerConfig config) {
        this.config = config;
    }
    
    /**
     * Writes the provided translation data to a `.properties` file for the specified locale
     * in the given output directory. The file will be written using the encoding and metadata
     * settings defined in the associated configuration.
     *
     * @param data the translation data containing entries to write to the properties file
     * @param locale the locale identifier (e.g., "en", "fr") to include in the file name and metadata
     * @param outputDirectory the directory where the properties file should be created
     * @return the {@code Path} to the written properties file
     * @throws IOException if an I/O error occurs while creating directories or writing the file
     */
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
    
    /**
     * Escapes special characters in a properties file value to ensure compatibility with the properties file format.
     * This includes escaping backslashes, newlines, carriage returns, tabs, and special characters like `=` and `:`.
     *
     * @param value the input string to be escaped, representing a value in a properties file
     * @return the escaped string, ready to be safely written to a properties file
     */
    private String escapePropertiesValue(String value) {
        return value.replace("\\", "\\\\")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t")
                   .replace("=", "\\=")
                   .replace(":", "\\:");
    }
    
    /**
     * Retrieves the output format used by this instance of the {@link OutputWriter}.
     * The output format defines the file type for the generated translations.
     *
     * @return the output format, which is {@link OutputFormat#PROPERTIES} for this implementation.
     */
    @Override
    public OutputFormat getOutputFormat() {
        return OutputFormat.PROPERTIES;
    }
}