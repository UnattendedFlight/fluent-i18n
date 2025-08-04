package io.github.unattendedflight.fluent.i18n.extractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The TemplateExtractor class is responsible for extracting translatable messages
 * from template-based source files such as HTML, JSP, or JSPX. It uses a list of
 * regular expression patterns to identify and extract the desired content for
 * localization or internationalization purposes.
 *
 * This class implements the SourceExtractor interface, ensuring that it provides
 * the required methods for determining file type compatibility and extracting messages
 * from the content of those files.
 */
public class TemplateExtractor implements SourceExtractor {
    /**
     * A list of compiled regular expression patterns used for extracting specific
     * portions of text from template-based source files.
     *
     * Each pattern in this list represents a unique criterion for identifying
     * translatable messages within the content of HTML, JSP, or JSPX files.
     * These patterns are applied sequentially to the content during the
     * extraction process.
     *
     * This collection facilitates the flexibility and configurability of
     * the extraction logic, enabling it to adapt to varied message formats
     * or structures present in the source files.
     */
    private final List<Pattern> patterns;
    
    /**
     * Constructs a TemplateExtractor using a list of regular expression pattern strings.
     * These patterns are compiled and used to match and extract messages from content
     * for localization or internationalization purposes.
     *
     * @param patternStrings the list of regular expression strings to identify the desired
     *                       text in template files
     */
    public TemplateExtractor(List<String> patternStrings) {
        this.patterns = patternStrings.stream()
            .map(Pattern::compile)
            .toList();
    }
    
    /**
     * Determines whether the specified file can be processed by checking its file extension
     * against a predefined list of supported extensions (.html, .jsp, .jspx).
     *
     * @param file the path of the file to be evaluated
     * @return true if the file has a supported extension; false otherwise
     */
    @Override
    public boolean canProcess(Path file) {
        String fileName = file.toString().toLowerCase();
        return fileName.endsWith(".html") || 
               fileName.endsWith(".jsp") || 
               fileName.endsWith(".jspx");
    }
    
    /**
     * Extracts a list of translatable messages from the given content based on specified patterns.
     * Each message contains the extracted text and the location in the file where it was found.
     *
     * @param content the text content of the file from which messages are to be extracted
     * @param filePath the path of the file being processed, used for tracking the source location of messages
     * @return a list of {@link ExtractedMessage} objects, each representing an extracted message with associated location information
     */
    @Override
    public List<ExtractedMessage> extract(String content, String filePath) {
        List<ExtractedMessage> messages = new ArrayList<>();
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String naturalText = matcher.group(1);
                if (naturalText != null && !naturalText.trim().isEmpty()) {
                    ExtractedMessage message = new ExtractedMessage(naturalText);
                    
                    int lineNumber = findLineNumber(content, matcher.start());
                    message.addLocation(new SourceLocation(filePath, lineNumber));
                    
                    messages.add(message);
                }
            }
        }
        
        return messages;
    }
    
    /**
     * Computes the line number corresponding to a specific character position
     * within a given string content. The line number is determined by counting
     * the newline characters ('\n') in the substring from the beginning of the
     * content to the specified position.
     *
     * @param content the string content to search within
     * @param position the character position in the content for which the line
     *                 number needs to be determined
     * @return the line number corresponding to the specified position within the
     *         content, with line numbers starting from 1
     */
    private int findLineNumber(String content, int position) {
        return (int) content.substring(0, position).chars()
            .filter(ch -> ch == '\n')
            .count() + 1;
    }
}