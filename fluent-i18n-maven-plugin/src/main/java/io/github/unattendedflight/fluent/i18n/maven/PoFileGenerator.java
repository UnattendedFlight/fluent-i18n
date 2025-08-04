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
 * Simple and robust PO file generator based on working integrated i18n library approach
 */
public class PoFileGenerator {

    // Patterns for parsing existing PO files (based on PoCompiler.java)
    private static final Pattern MSGID_PATTERN = Pattern.compile("^msgid\\s+\"(.*)\"$");
    private static final Pattern MSGSTR_PATTERN = Pattern.compile("^msgstr\\s+\"(.*)\"$");
    private static final Pattern HASH_PATTERN = Pattern.compile("^#\\.\\s+hash:\\s+(.+)$");
    private static final Pattern CONTINUATION_PATTERN = Pattern.compile("^\"(.*)\"$");

    private final Path poDirectory;
    private final Set<String> supportedLocales;
    private final boolean preserveExisting;

    public PoFileGenerator(Path poDirectory, Set<String> supportedLocales, boolean preserveExisting) {
        this.poDirectory = poDirectory;
        this.supportedLocales = supportedLocales;
        this.preserveExisting = preserveExisting;
    }

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
     * Read existing translations using the same robust parsing logic as PoCompiler
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
     * Validate that all locale files have the same number of entries
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
     * Count msgid entries (excluding header)
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

    // String utilities (same as in your integrated library)
    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private String unescapeString(String str) {
        return str.replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\");
    }
}