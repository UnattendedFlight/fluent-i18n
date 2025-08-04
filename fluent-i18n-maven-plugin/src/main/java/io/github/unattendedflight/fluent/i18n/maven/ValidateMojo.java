package io.github.unattendedflight.fluent.i18n.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates translation files and reports issues
 */
@Mojo(name = "validate")
public class ValidateMojo extends AbstractFluentI18nMojo {
    
    @Parameter(property = "fluent.i18n.validate.checkMissingTranslations", defaultValue = "true")
    private boolean checkMissingTranslations;
    
    @Parameter(property = "fluent.i18n.validate.checkPlaceholders", defaultValue = "true")
    private boolean checkPlaceholders;
    
    @Parameter(property = "fluent.i18n.validate.failOnErrors", defaultValue = "false")
    private boolean failOnErrors;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        checkSkip();
        
        getLog().info("Starting translation validation...");
        
        List<ValidationError> errors = new ArrayList<>();
        
        for (String locale : getSupportedLocalesSet()) {
            Path poFile = poDirectory.toPath().resolve("messages_" + locale + ".po");
            
            if (!Files.exists(poFile)) {
                errors.add(new ValidationError(locale, "PO file not found: " + poFile));
                continue;
            }
            
            try {
                validatePoFile(poFile, locale, errors);
            } catch (IOException e) {
                errors.add(new ValidationError(locale, "Failed to read PO file: " + e.getMessage()));
            }
        }
        
        // Report results
        if (errors.isEmpty()) {
            getLog().info("Validation completed successfully - no issues found");
        } else {
            getLog().warn("Validation completed with " + errors.size() + " issues:");
            for (ValidationError error : errors) {
                getLog().warn("  " + error.toString());
            }
            
            if (failOnErrors) {
                throw new MojoFailureException("Translation validation failed");
            }
        }
    }
    
    private void validatePoFile(Path poFile, String locale, List<ValidationError> errors) 
            throws IOException {
        
        List<String> lines = Files.readAllLines(poFile);
        
        String currentMsgId = null;
        String currentMsgStr = null;
        int lineNumber = 0;
        
        for (String line : lines) {
            lineNumber++;
            line = line.trim();
            
            if (line.startsWith("msgid ")) {
                currentMsgId = extractQuotedString(line.substring(6));
            } else if (line.startsWith("msgstr ")) {
                currentMsgStr = extractQuotedString(line.substring(7));
                
                // Validate this entry
                if (currentMsgId != null && !currentMsgId.isEmpty()) {
                    validateEntry(locale, currentMsgId, currentMsgStr, lineNumber, errors);
                }
            }
        }
    }
    
    private void validateEntry(String locale, String msgId, String msgStr, 
                             int lineNumber, List<ValidationError> errors) {
        
        // Check for missing translations
        if (checkMissingTranslations && (msgStr == null || msgStr.isEmpty() || msgStr.equals(msgId))) {
            errors.add(new ValidationError(locale, 
                "Missing translation at line " + lineNumber + ": " + msgId));
        }
        
        // Check placeholder consistency
        if (checkPlaceholders && msgStr != null && !msgStr.isEmpty()) {
            validatePlaceholders(locale, msgId, msgStr, lineNumber, errors);
        }
    }
    
    private void validatePlaceholders(String locale, String msgId, String msgStr, 
                                    int lineNumber, List<ValidationError> errors) {
        
        // Count placeholders in original and translation
        int originalPlaceholders = countPlaceholders(msgId);
        int translationPlaceholders = countPlaceholders(msgStr);
        
        if (originalPlaceholders != translationPlaceholders) {
            errors.add(new ValidationError(locale,
                "Placeholder mismatch at line " + lineNumber + 
                ": original has " + originalPlaceholders + 
                ", translation has " + translationPlaceholders + 
                " (" + msgId + ")"));
        }
    }
    
    private int countPlaceholders(String text) {
        int count = 0;
        int index = 0;
        
        // Count {0}, {1}, etc.
        while ((index = text.indexOf("{", index)) != -1) {
            if (index + 1 < text.length() && Character.isDigit(text.charAt(index + 1))) {
                count++;
            }
            index++;
        }
        
        // Count {} placeholders
        count += text.split("\\{\\}", -1).length - 1;
        
        return count;
    }
    
    private String extractQuotedString(String quotedString) {
        if (quotedString.startsWith("\"") && quotedString.endsWith("\"")) {
            return quotedString.substring(1, quotedString.length() - 1);
        }
        return quotedString;
    }
    
    private static class ValidationError {
        private final String locale;
        private final String message;
        
        ValidationError(String locale, String message) {
            this.locale = locale;
            this.message = message;
        }
        
        @Override
        public String toString() {
            return locale + ": " + message;
        }
    }
}