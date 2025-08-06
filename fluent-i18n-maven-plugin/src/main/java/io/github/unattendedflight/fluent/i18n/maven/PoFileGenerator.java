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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The PoFileGenerator class is responsible for generating .po files for localization purposes.
 * It supports creating and updating .po files with translations based on extracted messages and
 * existing translations using hash-based lookups for reliable preservation of translations.
 *
 * This class provides functionality to:
 * - Generate .po files for specified locales using hash-based translation preservation.
 * - Preserve existing translations by matching on message hash rather than content.
 * - Write necessary metadata and translation entries into .po files.
 * - Validate that all .po files are consistent in terms of the number of entries.
 *
 * Key Methods:
 * - generatePoFiles(ExtractionResult): Generates .po files for all supported locales based on
 *   extracted messages and existing translations (if enabled).
 * - readExistingTranslationsByHash(Path): Reads translations from an existing .po file using hash-based lookup.
 * - {@code writePoHeader(BufferedWriter, String)}: Writes the standard header for a .po file.
 * - {@code writePoEntry(BufferedWriter, ExtractedMessage, Map<String, String>)}: Writes a single translation entry
 *   using hash-based translation lookup.
 * - validateConsistency(int): Ensures consistency of the generated .po files.
 */
public class PoFileGenerator {
    private static final Logger log = Logger.getLogger(PoFileGenerator.class.getName());

    // Patterns for parsing existing PO files
    private static final Pattern MSGSTR_PATTERN = Pattern.compile("^msgstr\\s+\"(.*)\"$");
    private static final Pattern CONTINUATION_PATTERN = Pattern.compile("^\"(.*)\"$");

    private final Path poDirectory;
    private final Set<String> supportedLocales;
    private final String defaultLocale;
    private final boolean preserveExisting;

    /**
     * Constructs an instance of the PoFileGenerator class.
     *
     * @param poDirectory the directory where .po files will be generated or read from
     * @param supportedLocales a set of strings representing the locales that are supported
     * @param preserveExisting a boolean flag indicating whether to preserve existing translations in .po files
     */
    public PoFileGenerator(Path poDirectory, Set<String> supportedLocales, String defaultLocale, boolean preserveExisting) {
        this.poDirectory = poDirectory;
        this.supportedLocales = supportedLocales;
        this.defaultLocale = defaultLocale;
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

        log.fine("=== PO File Generation ===");
        log.fine("Messages to generate: " + result.getExtractedMessages().size());
        log.fine("Target locales: " + supportedLocales);

        for (String locale : supportedLocales) {
            generatePoFile(locale, result);
        }

        // Validate all files have same number of entries
        validateConsistency(result.getExtractedMessages().size());
    }

    /**
     * Generates a PO (Portable Object) file for a given locale using the extracted messages.
     * If preserveExisting is enabled and a PO file for the specified locale already exists,
     * it reads the existing translations using hash-based lookup and reuses them.
     *
     * @param locale the locale identifier (e.g., "en", "fr", "es") for which the PO file will be generated
     * @param result an instance of {@link ExtractionResult} that contains the extracted messages
     * @throws IOException if an I/O error occurs during file operations
     */
    private void generatePoFile(String locale, ExtractionResult result) throws IOException {
        Path poFile = poDirectory.resolve("messages_" + locale + ".po");

        log.fine("Generating PO file for locale: " + locale);

        // Read existing translations if preserving (hash-based lookup)
        Map<String, String> existingTranslations = new HashMap<>();
        if (preserveExisting && Files.exists(poFile)) {
            existingTranslations = readExistingTranslationsByHash(poFile);
            log.fine("  Loaded " + existingTranslations.size() + " existing translations");
        }

        // Generate new PO file with ALL current messages
        try (BufferedWriter writer = Files.newBufferedWriter(poFile)) {
            writePoHeader(writer, locale);

            int writtenCount = 0;
            for (ExtractedMessage message : result.getExtractedMessages().values()) {
              if (locale.equals(defaultLocale)) {
                writePoEntry(writer, message, Map.of(
                    message.getHash(), message.getNaturalText()
                )); // Default language is translated to itself to ensure key-only references still work.
              } else {
                writePoEntry(writer, message, existingTranslations);
              }
                writtenCount++;
            }

            log.fine("  Generated " + writtenCount + " entries for " + locale);
        }
    }

    /**
     * Reads existing translations from a .po file using hash-based lookups.
     * This method parses the PO file to extract hash-translation pairs, where each
     * translation is identified by its hash comment rather than its msgid content.
     *
     * @param poFile the path to the .po file to read translations from
     * @return a map where keys are hash strings and values are the corresponding msgstr translations
     * @throws IOException if an I/O error occurs while reading the .po file
     */
    private Map<String, String> readExistingTranslationsByHash(Path poFile) throws IOException {
        Map<String, String> translations = new HashMap<>();
        List<String> lines = Files.readAllLines(poFile);

        String currentHash = null;
        StringBuilder currentMsgStr = new StringBuilder();
        boolean inMsgStr = false;
        boolean inHeader = true;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // Skip empty lines
            if (line.isEmpty()) {
                // If we were building a translation and hit an empty line, save it
                if (currentHash != null && currentMsgStr.length() > 0) {
                    translations.put(currentHash, currentMsgStr.toString());
                    currentHash = null;
                    currentMsgStr.setLength(0);
                    inMsgStr = false;
                }
                continue;
            }

            // Skip header section (until we hit the first real msgid)
            if (inHeader) {
                if (line.equals("msgid \"\"") || line.startsWith("msgstr \"\"") || line.startsWith("\"")) {
                    continue;
                } else if (line.startsWith("#:") || line.startsWith("#.")) {
                    inHeader = false; // We've hit the first real entry
                } else {
                    continue;
                }
            }

            // Extract hash from comment
            if (line.startsWith("#. hash: ")) {
                // Save previous translation if we have one
                if (currentHash != null && currentMsgStr.length() > 0) {
                    translations.put(currentHash, currentMsgStr.toString());
                }
                
                currentHash = line.substring("#. hash: ".length()).trim();
                currentMsgStr.setLength(0);
                inMsgStr = false;
                continue;
            }
            
            // Skip other comments and msgid lines
            if (line.startsWith("#") || line.startsWith("msgid ")) {
                continue;
            }

            // Start of msgstr
            if (line.startsWith("msgstr ")) {
                Matcher matcher = MSGSTR_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String msgstr = unescapeString(matcher.group(1));
                    currentMsgStr.setLength(0);
                    currentMsgStr.append(msgstr);
                    inMsgStr = true;
                }
                continue;
            }

            // Handle continuation lines for msgstr
            if (inMsgStr && line.startsWith("\"") && line.endsWith("\"")) {
                Matcher matcher = CONTINUATION_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String continuation = unescapeString(matcher.group(1));
                    currentMsgStr.append(continuation);
                }
                continue;
            }
        }

        // Handle last entry
        if (currentHash != null && currentMsgStr.length() > 0) {
            translations.put(currentHash, currentMsgStr.toString());
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
     * Writes a single PO file entry using hash-based translation lookup.
     * This method formats source locations, message hash, context, and translation
     * strings for a given message, using the message hash to preserve existing translations.
     *
     * @param writer the BufferedWriter instance to write the PO entry to
     * @param message the ExtractedMessage instance containing information about the message
     * @param existingTranslations a map containing existing translations keyed by hash
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

        // Write hash comment (crucial for hash-based identification)
        writer.write("#. hash: " + message.getHash() + "\n");

        // Write context if available
        if (message.getContext() != null) {
            writer.write("#. context: " + message.getContext() + "\n");
        }

        // Write msgid
        writer.write("msgid \"" + escapeString(message.getNaturalText()) + "\"\n");

        // Write translation using hash-based lookup
        String existingTranslation = existingTranslations.get(message.getHash());
        if (existingTranslation != null && !existingTranslation.trim().isEmpty()) {
            writer.write("msgstr \"" + escapeString(existingTranslation) + "\"\n");
        } else {
            writer.write("msgstr \"\"\n");
        }

        writer.write("\n");
    }

    /**
     * Validates the consistency of translation files by comparing the number of entries
     * in each supported locale's PO file against the expected entry count.
     *
     * @param expectedCount the expected number of entries that each PO file should contain
     * @throws IOException if any locale's PO file has a mismatch in the number of entries
     */
    private void validateConsistency(int expectedCount) throws IOException {
        log.fine("=== Validation ===");
        boolean allValid = true;

        for (String locale : supportedLocales) {
            Path poFile = poDirectory.resolve("messages_" + locale + ".po");
            int actualCount = countPoEntries(poFile);

            if (actualCount != expectedCount) {
                System.err.println("ERROR: " + locale + " has " + actualCount + " entries, expected " + expectedCount);
                allValid = false;
            } else {
                log.fine("✓ " + locale + ": " + actualCount + " entries");
            }
        }

        if (!allValid) {
            throw new IOException("PO file consistency validation failed");
        }

        log.fine("✓ All locale files synchronized with " + expectedCount + " entries each");
    }

    /**
     * Counts the number of msgid entries in a PO file, excluding the header.
     *
     * @param poFile the path to the PO file to analyze
     * @return the number of msgid entries in the PO file
     * @throws IOException if an I/O error occurs while reading the file
     */
    private int countPoEntries(Path poFile) throws IOException {
        List<String> lines = Files.readAllLines(poFile);
        int count = 0;
        boolean skipHeader = true;

        for (String line : lines) {
            line = line.trim();

            // Skip header section
            if (skipHeader && line.equals("msgid \"\"")) {
                skipHeader = false;
                continue;
            }

            // Count real msgid entries (not the header)
            if (!skipHeader && line.startsWith("msgid ") && !line.equals("msgid \"\"")) {
                count++;
            }
        }

        return count;
    }

    /**
     * Escapes special characters in a string for PO file format.
     *
     * @param str the input string to be escaped
     * @return the escaped string
     */
    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    /**
     * Unescapes a string by replacing escape sequences with their literal counterparts.
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