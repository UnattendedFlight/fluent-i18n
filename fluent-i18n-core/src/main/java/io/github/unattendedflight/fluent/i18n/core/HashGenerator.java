package io.github.unattendedflight.fluent.i18n.core;

/**
 * Interface for generating hash values from natural text input.
 * Provides the ability to create unique string representations
 * for texts, optionally incorporating additional context.
 */
public interface HashGenerator {
    
    /**
     * Generates a hash value based on the provided natural text input.
     * This method is used to create a unique identifier for the given text.
     *
     * @param naturalText the natural language text input for which the hash is to be generated
     * @return the hash value as a string
     */
    String generateHash(String naturalText);
    
    /**
     * Generates a hash value for the given natural text and context.
     * This method combines the provided context and natural text
     * to create a unique string representation and generates a hash.
     *
     * @param naturalText the natural language input for which the hash is generated
     * @param context an additional context to include in the hash generation
     * @return the generated hash value as a string
     */
    default String generateHash(String naturalText, String context) {
        return generateHash(context + ":" + naturalText);
    }
}