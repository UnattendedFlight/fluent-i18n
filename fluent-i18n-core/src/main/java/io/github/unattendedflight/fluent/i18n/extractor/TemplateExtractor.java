package io.github.unattendedflight.fluent.i18n.extractor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts messages from template files (HTML, JSP, etc.)
 */
public class TemplateExtractor implements SourceExtractor {
    private final List<Pattern> patterns;
    
    public TemplateExtractor(List<String> patternStrings) {
        this.patterns = patternStrings.stream()
            .map(Pattern::compile)
            .toList();
    }
    
    @Override
    public boolean canProcess(Path file) {
        String fileName = file.toString().toLowerCase();
        return fileName.endsWith(".html") || 
               fileName.endsWith(".jsp") || 
               fileName.endsWith(".jspx");
    }
    
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
    
    private int findLineNumber(String content, int position) {
        return (int) content.substring(0, position).chars()
            .filter(ch -> ch == '\n')
            .count() + 1;
    }
}