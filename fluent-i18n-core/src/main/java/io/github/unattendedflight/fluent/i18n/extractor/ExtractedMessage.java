package io.github.unattendedflight.fluent.i18n.extractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a message extracted from source code
 */
public class ExtractedMessage {
    private String hash;
    private String naturalText;
    private String context;
    private MessageType type;
    private List<SourceLocation> locations = new ArrayList<>();
    
    public ExtractedMessage(String naturalText) {
        this(naturalText, null, MessageType.SIMPLE);
    }
    
    public ExtractedMessage(String naturalText, String context, MessageType type) {
        this.naturalText = naturalText;
        this.context = context;
        this.type = type;
    }
    
    public void addLocation(SourceLocation location) {
        locations.add(location);
    }
    
    // Getters and setters
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    
    public String getNaturalText() { return naturalText; }
    public void setNaturalText(String naturalText) { this.naturalText = naturalText; }
    
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public List<SourceLocation> getLocations() { return locations; }
}