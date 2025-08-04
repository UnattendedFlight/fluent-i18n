package io.github.unattendedflight.fluent.i18n.compiler;

import java.util.Map;

/**
 * Represents translation data consisting of translation entries and metadata.
 * This class provides access to a collection of translation entries, metadata information,
 * and utility methods to query and inspect the translations.
 */
public class TranslationData {
    /**
     * Stores a mapping of hashes to their corresponding translation entries.
     * Each key in the map represents a unique hash for a translation entry,
     * and the associated value is a {@link TranslationEntry} object containing
     * the translation details.
     */
    private final Map<String, TranslationEntry> entries;
    /**
     * Holds metadata information for a Portable Object (PO) file associated with translation data.
     * This metadata provides details such as project version, target language, creation date,
     * revision date, and content type for a PO file.
     *
     * Serves to describe contextual and technical information about the translations contained
     * within the associated {@link TranslationData}.
     */
    private final PoMetadata metadata;
    
    /**
     * Constructs a new instance of TranslationData with the specified translation entries and metadata.
     *
     * @param entries a map where the key is a unique identifier (usually a hash) and the value is a TranslationEntry
     *                object representing translation data, including original text, translations, and other details
     * @param metadata an instance of PoMetadata containing metadata information about the Portable Object (PO) file,
     *                 such as project version, language, and timestamps
     */
    public TranslationData(Map<String, TranslationEntry> entries, PoMetadata metadata) {
        this.entries = entries;
        this.metadata = metadata;
    }
    
    /**
     * Retrieves the map of translation entries.
     *
     * @return a map where the keys are strings representing unique identifiers (e.g., hashes)
     *         and the values are {@link TranslationEntry} objects containing translation data.
     */
    public Map<String, TranslationEntry> getEntries() {
        return entries;
    }
    
    /**
     * Retrieves the metadata associated with the translation data.
     *
     * @return an instance of {@link PoMetadata} containing metadata information
     *         such as project version, language, creation date, revision date,
     *         and content type of the Portable Object (PO) file.
     */
    public PoMetadata getMetadata() {
        return metadata;
    }
    
    /**
     * Retrieves the total number of translation entries stored in the current instance.
     *
     * @return the number of translation entries as an integer
     */
    public int getEntryCount() {
        return entries.size();
    }
    
    /**
     * Checks whether a translation exists for the specified hash.
     *
     * This method retrieves the translation entry associated with the provided hash
     * and determines if it contains a valid translation. A valid translation is considered
     * either a non-empty singular translation or at least one non-empty plural form in case
     * of plural entries.
     *
     * @param hash the unique identifier associated with the translation entry
     * @return true if the translation entry exists and contains a valid translation,
     *         false otherwise
     */
    public boolean hasTranslation(String hash) {
        TranslationEntry entry = entries.get(hash);
        return entry != null && entry.hasTranslation();
    }
}