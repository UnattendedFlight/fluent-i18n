package io.github.unattendedflight.fluent.i18n.maven;

import io.github.unattendedflight.fluent.i18n.extractor.ExtractionResult;
import io.github.unattendedflight.fluent.i18n.extractor.ExtractedMessage;
import io.github.unattendedflight.fluent.i18n.extractor.MessageType;
import io.github.unattendedflight.fluent.i18n.extractor.SourceLocation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The PoFileGenerator class is responsible for generating .po files for localization purposes.
 * It supports creating and updating .po files with translations based on extracted messages and
 * existing translations.
 *
 * This class provides functionality to:
 * - Generate .po files for specified locales.
 * - Preserve existing translations where needed.
 * - Write necessary metadata and translation entries into .po files.
 * - Validate that all .po files are consistent in terms of the number of entries.
 *
 * Constructor Details:
 * - The constructor accepts the output directory for .po files, a set of supported locales,
 *   and a flag to indicate whether to preserve existing translations.
*
 * Key Methods:
 * - generatePoFiles(ExtractionResult): Generates .po files for all supported locales based on
 *   extracted messages and existing translations (if enabled).
 * - readExistingTranslations(Path): Reads translations from an existing .po file using robust parsing logic.
 * - writePoHeader(BufferedWriter, String): Writes the standard header for a .po file, including metadata
 *   like project version, creation date, and locale-related details.
 * - {@code writePoEntry(BufferedWriter, ExtractedMessage, Map<String, String>)}: Writes a single translation entry
 *   into the .po file, including comments for source locations, translation hash, and context, if applicable.
 * - validateConsistency(int): Ensures consistency of the generated .po files by validating the number of entries.
 *
 * The class uses regular expressions to parse existing .po files, and handles multiline msgid and msgstr entries
 * robustly, ensuring proper handling of continuation lines.
 *
 * This is intended to be used in applications requiring localization support where .po files are used
 * to manage translations.
 */
public class PoFileGenerator {

    /**
     * A compiled regular expression pattern for matching and capturing the content of
     * `msgid` entries in `.po` files.
     *
     * The pattern is designed to identify lines starting with `msgid`, followed by one
     * or more whitespace characters, and then a quoted string. The content of the quoted
     * string is captured as a group.
     *
     * Example matched line structure:
     * - `msgid "example string"`
     *
     * This pattern is utilized to parse `msgid` entries in the `.po` file during the
     * generation or reading of localization files.
     */
    // Patterns for parsing existing PO files (based on PoCompiler.java)
    private static final Pattern MSGID_PATTERN = Pattern.compile("^msgid\\s+\"(.*)\"$");
    /**
     * A regular expression pattern used to match and capture the content of
     * `msgstr` entries in `.po` files.
     *
     * The pattern looks for lines starting with the keyword `msgstr` followed
     * by one or more spaces and a quoted string. It captures the content within
     * the quotes as a single group.
     *
     * This pattern is case-sensitive and assumes the syntax follows the GNU gettext
     * `.po` file format.
     */
    private static final Pattern MSGSTR_PATTERN = Pattern.compile("^msgstr\\s+\"(.*)\"$");
    /**
     * A regular expression pattern used to match lines in a `.po` file that represent
     * hash comments. The pattern specifically identifies lines that start with a hash symbol (`#`),
     * followed by a period (`.`), a space, the keyword "hash:", and a space-separated hash value.
     * The captured group in the pattern extracts the hash value from the line.
     *
     * This pattern is utilized during `.po` file parsing to identify and process hash-related comments
     * embedded within the file, often used for uniquely identifying translations or for change tracking purposes.
     */
    private static final Pattern HASH_PATTERN = Pattern.compile("^#\\.\\s+hash:\\s+(.+)$");
    /**
     * A {@code Pattern} instance used to identify and capture the content
     * of strings that are enclosed in double quotes. The regular expression
     * used for this pattern ensures that the string begins and ends with
     * double quotes, while capturing the content between them.
     *
     * This pattern is primarily utilized within the {@code PoFileGenerator}
     * class to handle cases where strings require robust parsing, especially
     * when processing fields or entries from PO files.
     */
    private static final Pattern CONTINUATION_PATTERN = Pattern.compile("^\"(.*)\"$");

    /**
     * Represents the directory where `.po` (Portable Object) files will be generated or stored.
     * This directory is used as the central location for managing locale-specific translation files.
     *
     * Key Characteristics:
     * - This path is typically specified during the initialization of the class.
     * - It serves as the target folder for `.po` file generation in the workflow.
     * - Used by the application to read or write `.po` files as part of the translation process.
     *
     * This variable is immutable and initialized as a final field, ensuring that the directory
     * reference cannot be changed after the object has been created.
     */
    private final Path poDirectory;
    /**
     * Represents the set of locales supported by the tool for generating `.po` files.
     *
     * This variable defines the specific locales that the `PoFileGenerator` class
     * will consider when creating or managing `.po` files. Each locale is represented
     * as a string, typically following standard locale naming conventions (e.g.,
     * "en_US" for U.S. English or "fr_FR" for French).
     *
     * The set of supported locales is immutable and initialized upon the
     * construction of a `PoFileGenerator` instance.
     */
    private final Set<String> supportedLocales;
    /**
     * Indicates whether existing `.po` files should be preserved when generating new ones.
     *
     * If set to {@code true}, the system avoids overwriting existing `.po` files and only adds new entries
     * while retaining existing translations. This allows users to maintain previously translated strings.
     *
     * If set to {@code false}, `.po` files are regenerated entirely, potentially overwriting any existing translations.
     */
    private final boolean preserveExisting;

    /**
     * Constructs an instance of the PoFileGenerator class.
     *
     * @param poDirectory the directory where .po files will be generated or read from
     * @param supportedLocales a set of strings representing the locales that are supported
     * @param preserveExisting a boolean flag indicating whether to preserve existing translations in .po files
     */
    public PoFileGenerator(Path poDirectory, Set<String> supportedLocales, boolean preserveExisting) {
        this.poDirectory = poDirectory;
        this.supportedLocales = supportedLocales;
        this.preserveExisting = preserveExisting;
    }

    /**
     * Generates PO (Portable Object) files for the provided extraction result and supported locales.
     * This method creates the necessary directories, iterates over the supported locales,
     * and generates a PO file for each locale using the provided extracted messages.
     * It also validates that all generated PO files have the same number of entries.
     *
     * @param result an {@link ExtractionResult} containing the extracted messages and the supported locales
     *               for which the PO files need to be generated
     * @throws IOException if an I/O error occurs while creating directories, generating files,
     *                     or validating consistency
     */
    public void generatePoFiles(ExtractionResult result) throws IOException {
        Files.createDirectories(poDirectory);

        System.out.println("=== PO File Generation ===");
        System.out.println("Messages to generate: " + result.getExtractedMessages().size());
        System.out.println("Target locales: " + supportedLocales);

        for (String locale : supportedLocales) {
            generatePoFile(locale, result);
        }

        // Validate all files have same number of entries
        validateConsistency(result.getExtractedMessages().size());
    }

    /**
     * Generates a PO (Portable Object) file for a given locale using the extracted messages.
     * If `preserveExisting` is enabled and a PO file for the specified locale already exists,
     * it reads the existing translations and reuses them to maintain consistency with current messages.
     *
     * @param locale the locale identifier (e.g., "en", "fr", "es") for which the PO file will be generated
     * @param result an instance of {@link ExtractionResult} that contains the extracted messages
     *               and supported locales from the extraction process
     * @throws IOException if an I/O error occurs during file operations, such as reading or writing the PO file
     */
    private void generatePoFile(String locale, ExtractionResult result) throws IOException {
        Path poFile = poDirectory.resolve("messages_" + locale + ".po");

        System.out.println("Generating PO file for locale: " + locale);

        // Read existing translations if preserving (using robust parser)
        Map<String, String> existingTranslations = new HashMap<>();
        if (preserveExisting && Files.exists(poFile)) {
            existingTranslations = readExistingTranslations(poFile);
            System.out.println("  Loaded " + existingTranslations.size() + " existing translations");
        }

        // Generate new PO file with ALL current messages
        try (BufferedWriter writer = Files.newBufferedWriter(poFile)) {
            writePoHeader(writer, locale);

            int writtenCount = 0;
            for (ExtractedMessage message : result.getExtractedMessages().values()) {
                writePoEntry(writer, message, existingTranslations);
                writtenCount++;
            }

            System.out.println("  Generated " + writtenCount + " entries for " + locale);
        }
    }

    /**
     * Reads the existing translations from a .po file and returns them as a map.
     * Each translation is represented by a key-value pair where the key is the
     * `msgid` and the value is the corresponding `msgstr`.
     *
     * @param poFile the path to the .po file to read translations from
     * @return a map where keys are the `msgid` strings and values are the corresponding `msgstr` strings
     * @throws IOException if an I/O error occurs while reading the .po file
     */
    private Map<String, String> readExistingTranslations(Path poFile) throws IOException {
        Map<String, String> translations = new HashMap<>();
        List<String> lines = Files.readAllLines(poFile);

        String currentMsgId = null;
        StringBuilder currentMsgStr = new StringBuilder();
        boolean inMsgStr = false;
        boolean inMsgId = false;
        StringBuilder currentMsgIdBuilder = new StringBuilder();

        for (String line : lines) {
            line = line.trim();

            // Skip empty lines and comments (except hash comments which we handle separately)
            if (line.isEmpty() || (line.startsWith("#") && !line.startsWith("#. hash:"))) {
                continue;
            }

            if (line.startsWith("msgid ")) {
                // Save previous translation if exists
                if (currentMsgId != null && currentMsgStr.length() > 0) {
                    translations.put(currentMsgId, currentMsgStr.toString());
                }

                // Start new message
                Matcher matcher = MSGID_PATTERN.matcher(line);
                if (matcher.matches()) {
                    currentMsgIdBuilder.setLength(0);
                    currentMsgIdBuilder.append(unescapeString(matcher.group(1)));
                    currentMsgStr.setLength(0);
                    inMsgStr = false;
                    inMsgId = true;
                }

            } else if (line.startsWith("msgstr ")) {
                Matcher matcher = MSGSTR_PATTERN.matcher(line);
                if (matcher.matches()) {
                    currentMsgStr.setLength(0);
                    currentMsgStr.append(unescapeString(matcher.group(1)));
                    inMsgStr = true;
                    inMsgId = false;
                    currentMsgId = currentMsgIdBuilder.toString();
                }

            } else if (line.startsWith("\"") && line.endsWith("\"")) {
                // Continuation line
                Matcher matcher = CONTINUATION_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String continuation = unescapeString(matcher.group(1));
                    if (inMsgId) {
                        currentMsgIdBuilder.append(continuation);
                    } else if (inMsgStr) {
                        currentMsgStr.append(continuation);
                    }
                }
            }
        }

        // Save last translation
        if (currentMsgId != null && currentMsgStr.length() > 0) {
            translations.put(currentMsgId, currentMsgStr.toString());
        }

        return translations;
    }

    /**
     * Writes the header section of a PO file to the specified buffered writer.
     *
     * @param writer the BufferedWriter used to write the PO file content
     * @param locale the locale for which the translations are being generated
     * @throws IOException if an I/O error occurs while writing to the writer
     */
    private void writePoHeader(BufferedWriter writer, String locale) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        writer.write("# " + locale + " translations\n");
        writer.write("# Generated by Fluent i18n Maven Plugin\n");
        writer.write("msgid \"\"\n");
        writer.write("msgstr \"\"\n");
        writer.write("\"Project-Id-Version: Fluent i18n\\n\"\n");
        writer.write("\"Report-Msgid-Bugs-To: \\n\"\n");
        writer.write("\"POT-Creation-Date: " + timestamp + "\\n\"\n");
        writer.write("\"PO-Revision-Date: " + timestamp + "\\n\"\n");
        writer.write("\"Last-Translator: \\n\"\n");
        writer.write("\"Language-Team: \\n\"\n");
        writer.write("\"Language: " + locale + "\\n\"\n");
        writer.write("\"MIME-Version: 1.0\\n\"\n");
        writer.write("\"Content-Type: text/plain; charset=UTF-8\\n\"\n");
        writer.write("\"Content-Transfer-Encoding: 8bit\\n\"\n");
        writer.write("\"Plural-Forms: nplurals=2; plural=(n != 1);\\n\"\n");
        writer.write("\n");
    }

    /**
     * Writes a single PO file entry into the provided BufferedWriter. This method
     * handles the formatting of source locations, message hash, context, and translation
     * strings for a given message. It manages both singular and plural message types.
     *
     * @param writer the BufferedWriter instance to write the PO entry to
     * @param message the ExtractedMessage instance containing information about the message,
     *                including its text, context, type, hash, and source locations
     * @param existingTranslations a map containing existing translations for messages,
     *                             where keys are message texts and values are their translations
     * @throws IOException if an I/O error occurs while writing to the BufferedWriter
     */
    private void writePoEntry(BufferedWriter writer, ExtractedMessage message,
                              Map<String, String> existingTranslations) throws IOException {

        // Write source locations (deduplicated)
        message.getLocations().stream()
            .map(SourceLocation::toString)
            .distinct()
            .forEach(location -> {
                try {
                    writer.write("#: " + location + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        // Write hash comment (crucial for identification)
        writer.write("#. hash: " + message.getHash() + "\n");

        // Write context if available
        if (message.getContext() != null) {
            writer.write("#. context: " + message.getContext() + "\n");
        }

        // Handle plural forms
        if (message.getType() == MessageType.PLURAL && message.getContext() != null && message.getContext().startsWith("plural:")) {
            // Write individual plural form entry (not ICU MessageFormat)
            writer.write("msgid \"" + escapeString(message.getNaturalText()) + "\"\n");
            
            // Write translation (existing or empty)
            String existingTranslation = existingTranslations.get(message.getNaturalText());
            if (existingTranslation != null && !existingTranslation.trim().isEmpty()) {
                writer.write("msgstr \"" + escapeString(existingTranslation) + "\"\n");
            } else {
                writer.write("msgstr \"\"\n");
            }
        } else {
            // Write regular entry
            writer.write("msgid \"" + escapeString(message.getNaturalText()) + "\"\n");

            // Write translation (existing or empty)
            String existingTranslation = existingTranslations.get(message.getNaturalText());
            if (existingTranslation != null && !existingTranslation.trim().isEmpty()) {
                writer.write("msgstr \"" + escapeString(existingTranslation) + "\"\n");
            } else {
                writer.write("msgstr \"\"\n");
            }
        }

        writer.write("\n");
    }

    /**
     * Writes a pluralized message entry into the given BufferedWriter. The method handles the
     * formatting of the message and corresponding translation based on the provided data.
     *
     * @param writer the BufferedWriter to write the message entry to
     * @param message the ExtractedMessage containing the pluralized ICU MessageFormat string
     * @param existingTranslations a map containing existing translations with the original message as the key
     * @throws IOException if an I/O error occurs during writing
     */
    private void writePluralEntry(BufferedWriter writer, ExtractedMessage message,
                                  Map<String, String> existingTranslations) throws IOException {
        // The naturalText is now the complete ICU MessageFormat string
        String icuPluralFormat = message.getNaturalText();
        
        writer.write("msgid \"" + escapeString(icuPluralFormat) + "\"\n");
        
        // Write translation (existing or empty)
        String existingTranslation = existingTranslations.get(icuPluralFormat);
        if (existingTranslation != null && !existingTranslation.trim().isEmpty()) {
            writer.write("msgstr \"" + escapeString(existingTranslation) + "\"\n");
        } else {
            writer.write("msgstr \"\"\n");
        }
    }

    /**
     * Validates the consistency of translation files by comparing the number of entries
     * in each supported locale's PO file against the expected entry count.
     * If any file contains a different number of entries than expected, an error is logged
     * for the corresponding locale, and an exception is thrown if inconsistencies are found.
     *
     * @param expectedCount the expected number of entries that each PO file should contain
     * @throws IOException if any locale's PO file has a mismatch in the number of entries
     *                     or if an error occurs during file operations
     */
    private void validateConsistency(int expectedCount) throws IOException {
        System.out.println("=== Validation ===");
        boolean allValid = true;

        for (String locale : supportedLocales) {
            Path poFile = poDirectory.resolve("messages_" + locale + ".po");
            int actualCount = countPoEntries(poFile);

            if (actualCount != expectedCount) {
                System.err.println("ERROR: " + locale + " has " + actualCount + " entries, expected " + expectedCount);
                allValid = false;
            } else {
                System.out.println("✓ " + locale + ": " + actualCount + " entries");
            }
        }

        if (!allValid) {
            throw new IOException("PO file consistency validation failed");
        }

        System.out.println("✓ All locale files synchronized with " + expectedCount + " entries each");
    }

    /**
     * Counts the number of "msgid" entries in a PO file, excluding the header.
     * Each "msgid" entry starting with `msgid` but not equal to `msgid ""`
     * is considered as a valid entry. The method skips the file's header section
     * before counting the entries.
     *
     * @param poFile the path to the PO file to analyze
     * @return the number of "msgid" entries in the PO file
     * @throws IOException if an I/O error occurs while reading the file
     */
    private int countPoEntries(Path poFile) throws IOException {
        List<String> lines = Files.readAllLines(poFile);
        int count = 0;
        boolean skipHeader = true;

        for (String line : lines) {
            line = line.trim();

            if (skipHeader && line.equals("msgid \"\"")) {
                skipHeader = false;
                continue;
            }

            if (!skipHeader && line.startsWith("msgid ") && !line.equals("msgid \"\"")) {
                count++;
            }
        }

        return count;
    }

    /**
     * Escapes special characters in a string to make it suitable for use in contexts
     * where these characters have special meanings, such as JSON, CSV, or programming code.
     * The method replaces backslashes, double quotes, newlines, carriage returns, and tabs
     * with their respective escape sequences.
     *
     * @param str the input string to be escaped
     * @return the escaped string, with special characters replaced by their escape sequences
     */
    // String utilities (same as in your integrated library)
    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    /**
     * Unescapes a string by replacing specific escape sequences
     * (e.g., "\n", "\r", "\t", "\\\"", "\\\\") with their literal counterparts.
     *
     * @param str the string containing escaped characters to be unescaped
     * @return the unescaped version of the input string
     */
    private String unescapeString(String str) {
        return str.replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\");
    }
}