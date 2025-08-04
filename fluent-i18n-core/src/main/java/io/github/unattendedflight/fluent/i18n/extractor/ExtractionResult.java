package io.github.unattendedflight.fluent.i18n.extractor;

import java.util.Map;
import java.util.Set;

/**
 * Represents the outcome of an extraction process for internationalization messages
 * from source files. It includes the extracted messages and the set of locales
 * that are supported.
 */
public class ExtractionResult {
    /**
     * A map that stores extracted internationalization messages keyed by their unique identifiers.
     * The key is a {@code String} that typically represents a unique hash or identifier for the message,
     * and the value is an {@link ExtractedMessage} object containing detailed information about the message.
     *
     * This map allows efficient access to individual messages by their identifiers and serves as
     * the main collection of extracted messages resulting from the extraction process.
     */
    private final Map<String, ExtractedMessage> extractedMessages;
    /**
     * A set of strings representing the locales supported by the extraction result.
     * Each locale is typically represented by a language code (e.g., "en", "fr")
     * or a language-region code combination (e.g., "en-US", "fr-CA").
     * This set contains all the locales for which messages have been extracted
     * and provides insight into the internationalization scope of the source files.
     */
    private final Set<String> supportedLocales;
    
    /**
     * Constructs an instance of {@code ExtractionResult}, which holds the result
     * of an extraction process including the extracted messages and supported locales.
     *
     * @param extractedMessages a map where the keys are unique identifiers (such as hashes)
     *                          and the values are {@code ExtractedMessage} objects representing the messages
     *                          found during the extraction process
     * @param supportedLocales  a set of locale identifiers (e.g., language or region codes) indicating
     *                          the supported translations for these messages
     */
    public ExtractionResult(Map<String, ExtractedMessage> extractedMessages,
                          Set<String> supportedLocales) {
        this.extractedMessages = extractedMessages;
        this.supportedLocales = supportedLocales;
    }
    
    /**
     * Retrieves the map of extracted messages that were gathered during the extraction process.
     * Each entry in the map consists of a unique string key that identifies the message
     * and the corresponding {@link ExtractedMessage} object, which contains details about the message
     * such as its text, context, type, and source locations.
     *
     * @return a map containing strings as keys and corresponding {@link ExtractedMessage} objects as values.
     */
    public Map<String, ExtractedMessage> getExtractedMessages() {
        return extractedMessages;
    }
    
    /**
     * Retrieves the set of locales that are supported by the extraction process.
     * The returned set contains locale identifiers as strings.
     *
     * @return a set of strings representing the supported locales
     */
    public Set<String> getSupportedLocales() {
        return supportedLocales;
    }
    
    /**
     * Returns the total count of messages extracted from the source files.
     *
     * @return the number of messages extracted, represented as an integer
     */
    public int getMessageCount() {
        return extractedMessages.size();
    }
    
    /**
     * Calculates the total number of occurrences of all extracted messages
     * based on the size of their associated location lists.
     *
     * @return the total count of locations from all extracted messages
     */
    public int getTotalOccurrences() {
        return extractedMessages.values().stream()
            .mapToInt(msg -> msg.getLocations().size())
            .sum();
    }
}