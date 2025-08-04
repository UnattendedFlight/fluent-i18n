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
 * Mojo for validating translations in PO files. This validation process ensures
 * the correctness and completeness of translations by checking for missing
 * translations and placeholder consistency.
 *
 * The following checks are performed:
 * - Missing translations: Ensures all `msgid` entries have a corresponding translated `msgstr`.
 * - Placeholder consistency: Verifies that the placeholders in original messages (`msgid`)
 *   match the number of placeholders in the translations (`msgstr`).
 *
 * Configuration:
 * - checkMissingTranslations: Enables or disables the check for missing translations.
 * - checkPlaceholders: Enables or disables the check for placeholder consistency.
 * - failOnErrors: Determines whether the build should fail if errors are found during validation.
 *
 * This goal processes supported locales, reads the respective PO files, performs validation,
 * and logs any validation errors. If validation errors occur and `failOnErrors` is set to
 * true, the Mojo will fail the build.
 *
 * This class extends AbstractFluentI18nMojo and requires correct configuration of supported
 * locales and PO file directory to perform the validation.
 */
@Mojo(name = "validate")
public class ValidateMojo extends AbstractFluentI18nMojo {
    
    /**
     * Indicates whether missing translations in the `.po` files should be checked during validation.
     *
     * When enabled (set to true), the validator ensures that all translations are populated
     * and proper warnings or errors are raised for any missing translations. If disabled (set to false),
     * missing translations will not trigger warnings or errors during the validation process.
     *
     * This property can be configured using the Maven plugin parameter
     * `fluent.i18n.validate.checkMissingTranslations` in the project configuration.
     *
     * Default value: true
     */
    @Parameter(property = "fluent.i18n.validate.checkMissingTranslations", defaultValue = "true")
    private boolean checkMissingTranslations;
    
    /**
     * Controls whether placeholder validation is performed during the validation process.
     *
     * This variable determines if the validation logic should check for consistency between
     * the placeholders in the source and translated messages (e.g., matching curly braces or
     * placeholder indices in translations). If enabled, any inconsistencies in placeholders
     * between the source and target languages will result in validation warnings or errors.
     *
     * The default value for this property is {@code true}. It can be configured via the Maven parameter
     * {@code fluent.i18n.validate.checkPlaceholders}.
     */
    @Parameter(property = "fluent.i18n.validate.checkPlaceholders", defaultValue = "true")
    private boolean checkPlaceholders;
    
    /**
     * Indicates whether the validation process should fail when errors are found
     * in the localization files.
     *
     * If set to {@code true}, the validation process terminates with an exception
     * when one or more validation errors (e.g., missing translations, invalid placeholders)
     * are encountered. This enforces strict compliance with the expected localization standards.
     *
     * If set to {@code false}, validation errors are logged as warnings, but the process
     * continues and does not interrupt the build.
     *
     * This parameter can be configured via the Maven property {@code fluent.i18n.validate.failOnErrors}.
     * The default value is {@code false}.
     */
    @Parameter(property = "fluent.i18n.validate.failOnErrors", defaultValue = "false")
    private boolean failOnErrors;
    
    /**
     * Executes the Maven goal to perform translation validation.
     *
     * This method performs the following steps:
     *
     * - Verifies if the execution should be skipped using the `checkSkip` method.
     * - Logs the start of the validation process.
     * - Iterates through the supported locales and validates each corresponding `.po` file.
     *     - If a `.po` file is missing for a locale, a validation error is recorded.
     *     - If a file is present, its contents are validated using the `validatePoFile` method.
     *     - Errors occurring during file reading or validation are captured and recorded.
     * - Logs the results of the validation process:
     *     - Reports whether validation completed successfully or with issues.
     *     - Provides detailed warnings for each validation issue.
     * - Throws a `MojoFailureException` if validation errors are present and the `failOnErrors` flag is set.
     *
     * @throws MojoExecutionException if the goal execution is flagged to be skipped or other execution issues occur
     * @throws MojoFailureException if validation fails and the `failOnErrors` flag is enabled
     */
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
    
    /**
     * Validates the content of a PO file line-by-line, ensuring that entries are properly
     * structured and meet translation validation criteria. Any validation errors are added
     * to the provided list of errors.
     *
     * @param poFile the path to the PO file to be validated
     * @param locale the locale corresponding to the translation in the PO file
     * @param errors a list to which any validation errors will be added
     * @throws IOException if an error occurs while reading the file
     */
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
    
    /**
     * Validates a single entry in a PO file for missing translations and placeholder consistency.
     *
     * @param locale the locale of the translation being validated
     * @param msgId the original message key or identifier in the source language
     * @param msgStr the translated message string in the target language
     * @param lineNumber the line number of the entry in the PO file
     * @param errors a list to which any validation errors encountered will be added
     */
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
    
    /**
     * Validates that the number of placeholders in the original message and its translation are identical.
     * If the counts do not match, a validation error is added to the provided errors list.
     *
     * @param locale the locale of the translation being validated
     * @param msgId the original message ID or content
     * @param msgStr the translated message string
     * @param lineNumber the line number of the message in the translation file
     * @param errors the list of validation errors to which any placeholder mismatch errors are added
     */
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
    
    /**
     * Counts the number of placeholders in the given text. Placeholders are identified
     * as either numbered placeholders (e.g., "{0}", "{1}") or empty curly braces ("{}").
     *
     * @param text the string in which placeholders are to be counted
     * @return the total number of placeholders found in the input text
     */
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
    
    /**
     * Extracts the content of a quoted string, removing the leading and trailing double quotes.
     * If the input string does not start and end with double quotes, it is returned unchanged.
     *
     * @param quotedString the input string to process, which may or may not be enclosed in double quotes
     * @return the content of the input string without the enclosing double quotes, or the original string if it is not quoted
     */
    private String extractQuotedString(String quotedString) {
        if (quotedString.startsWith("\"") && quotedString.endsWith("\"")) {
            return quotedString.substring(1, quotedString.length() - 1);
        }
        return quotedString;
    }
    
    /**
     * Represents a validation error encountered during the translation validation process.
     *
     * This class encapsulates details about a specific validation error, including
     * the locale associated with the error and an error message describing the issue.
     *
     * Instances of this class are created when a translation validation error is detected
     * and are typically used to log or report details of the error to the user.
     */
    private static class ValidationError {
        /**
         * The locale associated with the validation error.
         *
         * This variable represents the specific locale for which the validation error occurred.
         * It is used to identify the language or region context related to the error, often
         * corresponding to a locale code matching internationalization standards (e.g., "en", "fr", "en-US").
         *
         * The value of this variable is immutable and set during the object construction.
         */
        private final String locale;
        /**
         * The validation error message describing the specific issue encountered.
         *
         * This field holds the error message associated with a validation error.
         * It provides details about why a given validation failed, typically describing
         * the problem in a human-readable format. This message is expected to be used
         * for logging, debugging, or displaying meaningful feedback to the user.
         *
         * The value is immutable and is set during object initialization.
         */
        private final String message;
        
        /**
         * Constructs a new ValidationError with the specified locale and message.
         *
         * @param locale the locale associated with the validation error
         * @param message the error message providing details about the validation issue
         */
        ValidationError(String locale, String message) {
            this.locale = locale;
            this.message = message;
        }
        
        /**
         * Returns a string representation of this validation error.
         *
         * The returned string includes the locale associated with the error
         * and the corresponding error message, separated by a colon and a space.
         *
         * @return a string containing the locale and message of this validation error
         */
        @Override
        public String toString() {
            return locale + ": " + message;
        }
    }
}