package io.github.unattendedflight.fluent.i18n.extractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JavaAnnotationExtractor is responsible for extracting translatable messages
 * annotated within Java source code files. For each match, it identifies its
 * natural text and associates it with the location in the source file.
 *
 * This extractor works by applying a set of regular expression patterns,
 * provided during initialization, to the content of Java files.
 * Matching messages, along with their location details, are encapsulated
 * as {@link ExtractedMessage} instances.
 */
public class JavaAnnotationExtractor implements SourceExtractor {
    /**
     * A list of precompiled regular expression patterns used to identify
     * and extract specific text elements (such as translatable annotations)
     * from the contents of Java source files.
     *
     * Each pattern in this list is constructed from user-supplied
     * string representations at the time of initializing the class.
     * The patterns are used during the message extraction process to
     * locate matching text within a file's content.
     */
    private final List<Pattern> patterns;
    
    /**
     * Initializes a new instance of the JavaAnnotationExtractor class.
     * The extractor uses the provided list of regular expressions to identify
     * messages in Java source code files.
     *
     * @param patternStrings a list of regular expression strings to be compiled
     *                       and used for extracting annotated messages from source code
     */
    public JavaAnnotationExtractor(List<String> patternStrings) {
        this.patterns = patternStrings.stream()
            .map(Pattern::compile)
            .toList();
    }
    
    /**
     * Determines whether the given file can be processed by this extractor.
     *
     * @param file the path to the file to be checked
     * @return true if the file is a Java source file (ends with ".java"), false otherwise
     */
    @Override
    public boolean canProcess(Path file) {
        return file.toString().endsWith(".java");
    }
    
    /**
     * Extracts translatable messages from the provided content using predefined patterns.
     * Each matched message is encapsulated as an {@link ExtractedMessage} and includes
     * the natural text and its location within the source file.
     *
     * @param content the content of the file to be processed, represented as a string
     * @param filePath the file path of the source file being processed
     * @return a list of {@link ExtractedMessage} instances containing the extracted natural text
     *         and its respective source locations
     */
    @Override
    public List<ExtractedMessage> extract(String content, String filePath) {
        List<ExtractedMessage> messages = new ArrayList<>();
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String naturalText = matcher.group(1);
                if (naturalText != null && !naturalText.trim().isEmpty()) {
                    ExtractedMessage message = new ExtractedMessage(
                        naturalText, null, MessageType.ANNOTATION
                    );
                    
                    int lineNumber = findLineNumber(content, matcher.start());
                    message.addLocation(new SourceLocation(filePath, lineNumber));
                    
                    messages.add(message);
                }
            }
        }
        
        return messages;
    }
    
    /**
     * Determines the line number in the given content based on a specified character position.
     *
     * @param content the string content to analyze, typically representing the content of a file
     * @param position the character index within the content for which the line number is to be determined
     * @return the 1-based line number corresponding to the specified position
     */
    private int findLineNumber(String content, int position) {
        return (int) content.substring(0, position).chars()
            .filter(ch -> ch == '\n')
            .count() + 1;
    }
}