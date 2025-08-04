package io.github.unattendedflight.fluent.i18n.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Implementation of the {@link HashGenerator} interface that uses the SHA-256
 * hashing algorithm to generate a unique hash value for a given natural text input.
 *
 * This class provides a method to create a truncated hash string
 * based on the first 11 characters of a Base64-encoded SHA-256 hash.
 * It utilizes the {@link MessageDigest} and {@link Base64} classes
 * to compute and encode the hash value.
 *
 * In case of an error during hashing, a {@link RuntimeException} is thrown with
 * an appropriate error message.
 */
public class Sha256HashGenerator implements HashGenerator {
    private static final int HASH_LENGTH = 11;
    
    @Override
    public String generateHash(String naturalText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(naturalText.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(hash)
                .substring(0, HASH_LENGTH);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate hash for: " + naturalText, e);
        }
    }
}