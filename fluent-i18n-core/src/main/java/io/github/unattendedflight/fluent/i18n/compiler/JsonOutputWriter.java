package io.github.unattendedflight.fluent.i18n.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Writes translation data to JSON format
 */
public class JsonOutputWriter implements OutputWriter {
    private final CompilerConfig config;
    private final ObjectMapper objectMapper;
    
    public JsonOutputWriter(CompilerConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        if (config.isMinifyOutput()) {
            // Configure for minified output
        }
    }
    
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
    
    @Override
    public OutputFormat getOutputFormat() {
        return OutputFormat.JSON;
    }
}