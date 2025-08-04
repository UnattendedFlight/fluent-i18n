package io.github.unattendedflight.fluent.i18n.extractor;

import java.util.Map;
import java.util.Set;

/**
 * Result of message extraction process
 */
public class ExtractionResult {
    private final Map<String, ExtractedMessage> extractedMessages;
    private final Set<String> supportedLocales;
    
    public ExtractionResult(Map<String, ExtractedMessage> extractedMessages, 
                          Set<String> supportedLocales) {
        this.extractedMessages = extractedMessages;
        this.supportedLocales = supportedLocales;
    }
    
    public Map<String, ExtractedMessage> getExtractedMessages() {
        return extractedMessages;
    }
    
    public Set<String> getSupportedLocales() {
        return supportedLocales;
    }
    
    public int getMessageCount() {
        return extractedMessages.size();
    }
    
    public int getTotalOccurrences() {
        return extractedMessages.values().stream()
            .mapToInt(msg -> msg.getLocations().size())
            .sum();
    }
}