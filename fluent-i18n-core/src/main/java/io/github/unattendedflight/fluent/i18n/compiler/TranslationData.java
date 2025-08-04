package io.github.unattendedflight.fluent.i18n.compiler;

import java.util.Map;

/**
 * Parsed translation data from a PO file
 */
public class TranslationData {
    private final Map<String, TranslationEntry> entries;
    private final PoMetadata metadata;
    
    public TranslationData(Map<String, TranslationEntry> entries, PoMetadata metadata) {
        this.entries = entries;
        this.metadata = metadata;
    }
    
    public Map<String, TranslationEntry> getEntries() {
        return entries;
    }
    
    public PoMetadata getMetadata() {
        return metadata;
    }
    
    public int getEntryCount() {
        return entries.size();
    }
    
    public boolean hasTranslation(String hash) {
        TranslationEntry entry = entries.get(hash);
        return entry != null && entry.hasTranslation();
    }
}