package io.github.unattendedflight.fluent.i18n.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * This class is responsible for writing translation data to a JSON file format.
 * It implements the {@link OutputWriter} interface to handle the specifics
 * of serializing translation data in JSON structure. The generated JSON can include
 * both the translation entries and additional meta-information based on the
 * configuration provided.
 *
 * The output structure may include:
 * - Translation entries with their original text and translations.
 * - Optional metadata such as source location and file-level details like locale
 *   and revision date, depending on the configuration settings.
 */
public class JsonOutputWriter implements OutputWriter {
    /**
     * Holds the compiler configuration settings that influence the behavior of the
     * JSON output generation. The configuration may include options such as whether
     * to include metadata in the output, whether to produce minified JSON, and other
     * controls relevant to the formatting and content of the generated file.
     *
     * This variable is used to determine various aspects of how the translation data
     * is serialized during the file writing process.
     */
    private final CompilerConfig config;
    /**
     * An {@link ObjectMapper} instance used for JSON serialization and deserialization.
     * It is responsible for configuring and converting data to and from JSON format
     * as required by the {@link JsonOutputWriter} class. This includes creating JSON
     * nodes, writing JSON output files, and applying specific configurations such as
     * enabling or disabling minified output.
     *
     * The {@code objectMapper} is initialized in the constructor of the containing class and
     * may be further configured based on the provided {@link CompilerConfig}.
     */
    private final ObjectMapper objectMapper;
    
    /**
     * Constructs a {@code JsonOutputWriter} instance with the specified configuration.
     * The configuration determines various aspects of the JSON output, such as whether
     * the output should be minified or include metadata.
     *
     * @param config the {@link CompilerConfig} instance that defines settings
     *               for how the JSON output should be written. This includes options
     *               like minification, metadata inclusion, and other JSON format
     *               customization settings.
     */
    public JsonOutputWriter(CompilerConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        if (config.isMinifyOutput()) {
            // Configure for minified output
        }
    }
    
    /**
     * Writes the given translation data to a file in JSON format. The generated JSON file
     * includes translation entries and optional metadata such as file locale and entry count,
     * depending on configuration settings.
     *
     * @param data the {@code TranslationData} to be written to the output file, containing
     *             translation entries and metadata
     * @param locale the target locale for the translations, used in the output file's name
     * @param outputDirectory the directory where the JSON file will be created
     * @return the {@code Path} to the created JSON file
     * @throws IOException if an I/O error occurs while creating the directory or writing the file
     */
    @Override
    public Path write(TranslationData data, String locale, Path outputDirectory) throws IOException {
        Files.createDirectories(outputDirectory);
        Path outputFile = outputDirectory.resolve(OutputFormat.JSON.getFileName(locale));
        
        ObjectNode root = objectMapper.createObjectNode();
        
        for (Map.Entry<String, TranslationEntry> entry : data.getEntries().entrySet()) {
            String hash = entry.getKey();
            TranslationEntry translationEntry = entry.getValue();
            
            ObjectNode entryNode = objectMapper.createObjectNode();
            entryNode.put("original", translationEntry.getOriginalText());
            entryNode.put("translation", translationEntry.getTranslation());
            
            if (config.isIncludeMetadata() && translationEntry.getSourceLocation() != null) {
                entryNode.put("source", translationEntry.getSourceLocation());
            }
            
            root.set(hash, entryNode);
        }
        
        if (config.isIncludeMetadata()) {
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("locale", locale);
            metadata.put("entryCount", data.getEntryCount());
            if (data.getMetadata().getRevisionDate() != null) {
                metadata.put("lastModified", data.getMetadata().getRevisionDate().toString());
            }
            root.set("_metadata", metadata);
        }
        
        objectMapper.writeValue(outputFile.toFile(), root);
        return outputFile;
    }
    
    /**
     * Returns the output format handled by this writer.
     *
     * @return the output format as an {@code OutputFormat} enumeration, which in this case is {@code OutputFormat.JSON}.
     */
    @Override
    public OutputFormat getOutputFormat() {
        return OutputFormat.JSON;
    }
}