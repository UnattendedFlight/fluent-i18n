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
 * Extracts messages from Java method calls like I18n.translate("text")
 */
public class JavaMethodCallExtractor implements SourceExtractor {
    private final List<Pattern> patterns;
    
    public JavaMethodCallExtractor(List<String> patternStrings) {
        this.patterns = patternStrings.stream()
            .map(Pattern::compile)
            .toList();
    }
    
    @Override
    public boolean canProcess(Path file) {
        return file.toString().endsWith(".java");
    }
    
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
    
    private int findLineNumber(String content, int position) {
        return (int) content.substring(0, position).chars()
            .filter(ch -> ch == '\n')
            .count() + 1;
    }
}