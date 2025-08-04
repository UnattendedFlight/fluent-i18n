package io.github.unattendedflight.fluent.i18n.extractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts method calls and plural forms from Java source files based on predefined patterns.
 *
 * This class processes Java source files, identifies specific method calls that conform to
 * the provided patterns, and extracts messages or texts for further processing. It also
 * identifies and processes methods related to plural forms, constructing ICU message format
 * text and storing contextual information where applicable.
 */
public class JavaMethodCallExtractor implements SourceExtractor {
    /**
     * A list of compiled regular expression patterns used to match specific syntax
     * or constructs within source code during message extraction. These patterns
     * are applied to the source code content to identify and process relevant segments.
     * This field is immutable and initialized during the construction of the
     * containing object.
     */
    private final List<Pattern> patterns;
    
    /**
     * Constructs a new instance of JavaMethodCallExtractor, which compiles a list of
     * regex patterns from the provided pattern strings. These patterns are used to extract
     * specific method calls from source code.
     *
     * @param patternStrings a list of strings representing regular expression patterns to be compiled
     */
    public JavaMethodCallExtractor(List<String> patternStrings) {
        this.patterns = patternStrings.stream()
            .map(Pattern::compile)
            .toList();
    }
    
    /**
     * Determines whether the given file can be processed by this extractor.
     *
     * @param file the file path to check for processability
     * @return true if the file ends with a ".java" extension, false otherwise
     */
    @Override
    public boolean canProcess(Path file) {
        return file.toString().endsWith(".java");
    }
    
    /**
     * Extracts a list of messages from the given content and file path.
     * The method identifies both plural forms and regular method calls matching
     * predefined patterns while accounting for duplicate or overlapping positions.
     *
     * @param content the content to parse and extract messages from
     * @param filePath the path of the file being processed, used for source location tracking
     * @return a list of {@code ExtractedMessage} instances, each representing a
     *         message extracted from the content
     */
    @Override
    public List<ExtractedMessage> extract(String content, String filePath) {
        List<ExtractedMessage> messages = new ArrayList<>();
        
        // First, check for plural forms and extract them
        List<ExtractedMessage> pluralMessages = extractPluralForms(content, filePath);
        messages.addAll(pluralMessages);
        
        // Then extract regular method calls (but skip those that are part of plural blocks)
        Set<Integer> pluralPositions = new HashSet<>();
        for (ExtractedMessage pluralMsg : pluralMessages) {
            for (SourceLocation loc : pluralMsg.getLocations()) {
                // Mark positions that are part of plural blocks
                pluralPositions.add(loc.getLineNumber());
            }
        }
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                int lineNumber = findLineNumber(content, matcher.start());
                
                // Skip if this position is part of a plural block
                if (pluralPositions.contains(lineNumber)) {
                    continue;
                }
                
                String naturalText = matcher.group(1);
                if (naturalText != null && !naturalText.trim().isEmpty()) {
                    ExtractedMessage message = new ExtractedMessage(naturalText);
                    
                    message.addLocation(new SourceLocation(filePath, lineNumber));
                    
                    // Check if this is a contextual call
                    if (matcher.group().contains("context(")) {
                        message.setType(MessageType.CONTEXTUAL);
                        // Extract context if available
                        String contextPattern = "I18n\\.context\\(\"([^\"]+)\"\\)";
                        Pattern ctx = Pattern.compile(contextPattern);
                        Matcher ctxMatcher = ctx.matcher(matcher.group());
                        if (ctxMatcher.find()) {
                            message.setContext(ctxMatcher.group(1));
                        }
                    }
                    
                    messages.add(message);
                }
            }
        }
        
        return messages;
    }
    
    /**
     * Extracts plural form messages from the given source content. This method scans the provided
     * content for occurrences of `I18n.plural()` calls, parses their blocks, and converts them
     * into a list of `ExtractedMessage` instances. It is used to identify pluralization strings
     * intended for internationalization.
     *
     * @param content the source content to be scanned for plural form messages
     * @param filePath the file path of the source content, used for location tracking in the extraction
     * @return a list of {@code ExtractedMessage} instances representing the parsed plural form messages
     */
    private List<ExtractedMessage> extractPluralForms(String content, String filePath) {
        List<ExtractedMessage> pluralMessages = new ArrayList<>();
        
        // Pattern to find I18n.plural() calls
        Pattern pluralStartPattern = Pattern.compile("I18n\\.plural\\s*\\([^)]*\\)");
        Matcher pluralStartMatcher = pluralStartPattern.matcher(content);
        
        int matchCount = 0;
        while (pluralStartMatcher.find()) {
            matchCount++;
            int startPos = pluralStartMatcher.start();
            int endPos = findPluralBlockEnd(content, startPos);
            
            if (endPos > startPos) {
                String pluralBlock = content.substring(startPos, endPos);
                ExtractedMessage pluralMessage = extractPluralFormsFromBlock(pluralBlock, filePath, startPos, content);
                if (pluralMessage != null) {
                    pluralMessages.add(pluralMessage);
                }
            }
        }
        
        return pluralMessages;
    }
    
    /**
     * Finds the end index of a plural block, starting from the given position in the content string.
     * This method first locates the end of the I18n.plural() method call and then determines the
     * end of the entire method chain that follows.
     *
     * @param content the full string content being processed
     * @param startPos the starting position to search for the end of the plural block
     * @return the index of the end of the plural block, or -1 if the end cannot be determined
     */
    private int findPluralBlockEnd(String content, int startPos) {
        // First find the end of the I18n.plural() call
        int braceCount = 0;
        boolean inString = false;
        char stringDelimiter = 0;
        int pluralCallEnd = -1;
        
        for (int i = startPos; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (!inString) {
                if (c == '"' || c == '\'') {
                    inString = true;
                    stringDelimiter = c;
                } else if (c == '(') {
                    braceCount++;
                } else if (c == ')') {
                    braceCount--;
                    if (braceCount == 0) {
                        // Found the end of the plural() call
                        pluralCallEnd = i;
                        break;
                    }
                }
            } else {
                if (c == stringDelimiter && content.charAt(i - 1) != '\\') {
                    inString = false;
                }
            }
        }
        
        if (pluralCallEnd == -1) {
            return -1;
        }
        
        // Now find the end of the entire method chain
        int chainEnd = findChainEnd(content, pluralCallEnd);
        
        return chainEnd;
    }
    
    /**
     * Identifies the end of a statement chain in the provided content starting
     * from a given position. The end of the chain is determined by locating the
     * first semicolon (`;`) after the starting position. If no semicolon is found,
     * it returns the length of the content.
     *
     * @param content the content to search through for the end of the chain
     * @param startPos the starting position within the content to begin the search
     * @return the index immediately following the end of the statement chain
     *         (the position after the semicolon) or the content length if no
     *         semicolon is found
     */
    private int findChainEnd(String content, int startPos) {
        // Look for the end of the statement (semicolon)
        for (int i = startPos; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == ';') {
                return i + 1; // include the semicolon
            }
        }
        return content.length();
    }
    
    /**
     * Extracts plural forms from a given block of text representing a plural message definition.
     * Constructs an {@code ExtractedMessage} object containing the parsed data, including the
     * plural forms and their corresponding values, formatted as an ICU MessageFormat string.
     *
     * @param pluralBlock the block of text containing plural form definitions
     * @param filePath the path to the source file from which the message is extracted
     * @param startPos the starting position of the plural block in the file
     * @param content the entire content of the source file for context and line number determination
     * @return an {@code ExtractedMessage} representing the parsed plural forms, or {@code null}
     *         if no plural forms are found in the provided block
     */
    private ExtractedMessage extractPluralFormsFromBlock(String pluralBlock, String filePath, int startPos, String content) {
        Map<String, String> pluralForms = new HashMap<>();
        
        // Pattern to extract plural forms (including {} placeholders)
        Pattern formPattern = Pattern.compile("\\.(zero|one|two|few|many|other)\\s*\\(\\s*\"([^\"]*)\"\\)");
        
        // Also try a more permissive pattern for the other form
        Pattern otherFormPattern = Pattern.compile("\\.other\\s*\\(\\s*\"([^\"]*)\"\\)");
        Matcher formMatcher = formPattern.matcher(pluralBlock);
        
        // Try to find the other form specifically
        Matcher otherMatcher = otherFormPattern.matcher(pluralBlock);
        if (otherMatcher.find()) {
            String otherText = otherMatcher.group(1);
            pluralForms.put("other", otherText);
        }
        
        while (formMatcher.find()) {
            String form = formMatcher.group(1);
            String text = formMatcher.group(2);
            pluralForms.put(form, text);
        }
        
        if (pluralForms.isEmpty()) {
            return null;
        }
        
        // Create a special plural message
        ExtractedMessage message = new ExtractedMessage("", null, MessageType.PLURAL);
        
        // Use the "one" form as the main text (for hash generation)
        String oneForm = pluralForms.get("one");
        String otherForm = pluralForms.get("other");
        
        if (oneForm != null) {
            message.setNaturalText(oneForm);
        } else if (otherForm != null) {
            message.setNaturalText(otherForm);
        } else {
            // Fallback to first available form
            String firstForm = pluralForms.values().iterator().next();
            message.setNaturalText(firstForm);
        }
        
        // Generate the complete ICU MessageFormat string with all forms in order
        StringBuilder icuBuilder = new StringBuilder("{0, plural, ");
        boolean first = true;
        
        // Define the order of forms as they appear in the source code
        String[] formOrder = {"zero", "one", "two", "few", "many", "other"};
        
        for (String formName : formOrder) {
            String formText = pluralForms.get(formName);
            if (formText != null) {
                if (!first) {
                    icuBuilder.append(" ");
                }
                icuBuilder.append(formName).append(" {").append(formText).append("}");
                first = false;
            }
        }
        
        icuBuilder.append("}");
        String icuPluralFormat = icuBuilder.toString();
        message.setNaturalText(icuPluralFormat);
        
        // Add location
        int lineNumber = findLineNumber(content, startPos);
        message.addLocation(new SourceLocation(filePath, lineNumber));
        
        // Store plural forms as context
        StringBuilder contextBuilder = new StringBuilder("plural:");
        for (Map.Entry<String, String> entry : pluralForms.entrySet()) {
            if (contextBuilder.length() > 7) { // "plural:" is 7 chars
                contextBuilder.append(",");
            }
            contextBuilder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        message.setContext(contextBuilder.toString());
        
        return message;
    }
    
    /**
     * Determines the line number in the given content for a specified position.
     *
     * This method counts the newline characters ('\n') in the substring of the content
     * from the beginning up to the given position. The resulting count is incremented by 1
     * to determine the 1-based line number.
     *
     * @param content the full text content from which the line number is to be determined
     * @param position the 0-based position within the content to calculate the line number for
     * @return the 1-based line number corresponding to the specified position in the content
     */
    private int findLineNumber(String content, int position) {
        return (int) content.substring(0, position).chars()
            .filter(ch -> ch == '\n')
            .count() + 1;
    }
}