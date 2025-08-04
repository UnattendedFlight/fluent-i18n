package io.github.unattendedflight.fluent.i18n.extractor;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for extracting messages from different source file types
 */
public interface SourceExtractor {
    
    /**
     * Check if this extractor can process the given file
     */
    boolean canProcess(Path file);
    
    /**
     * Extract messages from file content
     */
    List<ExtractedMessage> extract(String content, String filePath);
}