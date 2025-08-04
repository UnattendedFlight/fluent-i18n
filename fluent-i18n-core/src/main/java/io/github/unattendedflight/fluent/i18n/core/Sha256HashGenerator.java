package io.github.unattendedflight.fluent.i18n.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * SHA-256 based hash generator
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