package io.github.unattendedflight.fluent.i18n.core;

/**
 * Interface for generating consistent hashes from natural text
 */
public interface HashGenerator {
    
    /**
     * Generate a hash for the given natural text
     */
    String generateHash(String naturalText);
    
    /**
     * Generate a hash with context
     */
    default String generateHash(String naturalText, String context) {
        return generateHash(context + ":" + naturalText);
    }
}