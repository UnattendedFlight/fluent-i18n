package io.github.unattendedflight.fluent.i18n.extractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a message extracted from source code for internationalization purposes.
 * It includes the textual content of the message, its context, type, and the locations
 * in the source code where it was found.
 */
public class ExtractedMessage {
    /**
     * A unique hash representing the content and context of the extracted message.
     * This hash is used to identify the message uniquely and can be utilized
     * to detect duplicates or for caching purposes.
     */
    private String hash;
    /**
     * The natural language text content of the message as it appears in the source code.
     * This field represents the primary, human-readable textual message intended for
     * internationalization or localization purposes.
     */
    private String naturalText;
    /**
     * The immutable context key used for hash generation and unique identification.
     * This key ensures that messages with the same text but different contexts
     * have different hashes and can be translated separately.
     */
    private String contextKey;
    
    /**
     * The mutable context description visible to translators in PO files.
     * This provides human-readable context information to help translators
     * understand the usage scenario without affecting the hash.
     */
    private String context;
    /**
     * Specifies the type of the extracted message. The type determines the nature
     * of the message, such as whether it is a simple translation string, a plural
     * form, a context-specific translation, or an annotation-derived message.
     */
    private MessageType type;
    /**
     * A list of locations in the source code where the message was found. Each
     * location is represented by an instance of {@link SourceLocation}, containing
     * information such as file path, line number, and optional column number.
     */
    private List<SourceLocation> locations = new ArrayList<>();
    
    /**
     * Constructs an ExtractedMessage with the specified natural text.
     *
     * @param naturalText the natural text content of the extracted message
     */
    public ExtractedMessage(String naturalText) {
        this(naturalText, null, MessageType.SIMPLE);
    }
    
    /**
     * Constructs a new ExtractedMessage instance with the specified natural text, context, and type.
     *
     * @param naturalText the natural language text content of the message
     * @param context the context associated with the message, which can be used to disambiguate translations
     * @param type the type of the message, represented as a {@link MessageType} enumeration value
     */
    public ExtractedMessage(String naturalText, String context, MessageType type) {
        this.naturalText = naturalText;
        this.context = context;
        this.type = type;
    }
    
    /**
     * Adds a source location instance to the list of locations where the message was found.
     *
     * @param location the source location to be added, representing where the message was identified in the source code
     */
    public void addLocation(SourceLocation location) {
        locations.add(location);
    }
    
    /**
     * Retrieves the hash value associated with the message.
     *
     * @return the hash of the message as a String
     */
    // Getters and setters
    public String getHash() { return hash; }
    /**
     * Sets the hash value associated with the message.
     *
     * @param hash the hash value to set. Typically this is a unique identifier for the message.
     */
    public void setHash(String hash) { this.hash = hash; }
    
    /**
     * Retrieves the natural text of the message.
     *
     * @return the natural text content of the message
     */
    public String getNaturalText() { return naturalText; }
    /**
     * Sets the natural text content of the message.
     *
     * @param naturalText the main textual content of the message to be set
     */
    public void setNaturalText(String naturalText) { this.naturalText = naturalText; }
    
    /**
     * Retrieves the context associated with this message.
     *
     * @return the context string, which provides additional information to distinguish
     *         between similar messages in different settings; may be null if no context is provided.
     */
    public String getContext() { return context; }
    /**
     * Sets the context associated with this message. The context can be used to
     * specify additional metadata or differentiate between messages with identical
     * textual content.
     *
     * @param context the context string to associate with this message
     */
    public void setContext(String context) { this.context = context; }
    
    /**
     * Gets the context key used for hash generation.
     *
     * @return the context key for this extracted message
     */
    public String getContextKey() { return contextKey; }
    
    /**
     * Sets the context key used for hash generation.
     *
     * @param contextKey the context key to set for the message
     */
    public void setContextKey(String contextKey) { this.contextKey = contextKey; }
    
    /**
     * Retrieves the type of the message.
     *
     * @return the type of the message as an instance of {@code MessageType}.
     */
    public MessageType getType() { return type; }
    /**
     * Sets the type of the extracted message.
     *
     * @param type the type of the message, representing the kind of extraction
     *             (e.g., SIMPLE, PLURAL, CONTEXTUAL, or ANNOTATION)
     */
    public void setType(MessageType type) { this.type = type; }
    
    /**
     * Retrieves the list of source locations where the message was found.
     *
     * @return a list of {@code SourceLocation} objects representing the locations
     *         in the source code where the message appears.
     */
    public List<SourceLocation> getLocations() { return locations; }
}