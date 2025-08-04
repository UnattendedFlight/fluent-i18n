package io.github.unattendedflight.fluent.i18n.extractor;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents a contract for extracting messages from source files or strings based on certain criteria.
 * Implementations of this interface define how to determine whether a file can be processed
 * and how to extract messages from the content of the file.
 */
public interface SourceExtractor {
    
    /**
     * Determines whether the specified file can be processed based on certain criteria.
     *
     * @param file the path to the file to be checked for processing eligibility
     * @return true if the file meets the criteria for processing, false otherwise
     */
    boolean canProcess(Path file);
    
    /**
     * Extracts a list of messages from the given content and associates them with the specified file path.
     * The extraction process identifies and processes messages eligible for internationalization or localization.
     *
     * @param content the textual content from which messages are to be extracted
     * @param filePath the file path of the source file corresponding to the content, used for identifying
     *                 the origin of the extracted messages
     * @return a list of {@link ExtractedMessage} objects representing the messages found in the given content
     */
    List<ExtractedMessage> extract(String content, String filePath);
}