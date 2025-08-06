package io.github.unattendedflight.fluent.i18n.compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The PoFileParser class is responsible for parsing `.po` files, extracting translation
 * entries, and converting them to an intermediate representation used within the application.
 *
 * It supports:
 * - Reading `msgid`, `msgid_plural`, and `msgstr` entries.
 * - Processing translations in both single and plural forms.
 * - Parsing comments, including source location and hash metadata.
 * - Handling continuation lines and message constructs that span multiple lines.
 * - Detecting and parsing ICU MessageFormat plural patterns.
 *
 * The output of the parser is a {@code TranslationData} object, which contains all parsed
 * translation entries along with metadata extracted from the file.
 *
 * Features:
 * - Automatically extracts hashes, source locations, and message content.
 * - Handles standard PO file constructs and extends support for ICU plural patterns.
 * - Maintains internal state to associate related lines in the file.
 *
 * Usage of this class involves passing the path of the `.po` file to the {@code parse()} method,
 * which returns a {@code TranslationData} object containing the structured representation of the
 * translations.
 */
public class PoFileParser {

  private static final Logger log = Logger.getLogger(PoFileParser.class.getName());

    /**
     * Defines the pattern used to match the msgid entry in a PO file.
     * This pattern identifies lines that start with "msgid" followed
     * by one or more whitespace characters and a quoted string.
     *
     * For example, it matches lines like:
     * msgid "example text"
     *
     * The pattern captures the content of the quoted string as a group,
     * allowing extraction of the actual translation key.
     */
    // Same patterns as the working PoCompiler
    private static final Pattern MSGID_PATTERN = Pattern.compile("^msgid\\s+\"(.*)\"$");
    /**
     * A regular expression pattern used to match and capture the content of
     * the `msgid_plural` entries in a PO file.
     *
     * This pattern identifies lines in the PO file that start with the
     * `msgid_plural` keyword followed by whitespace and a quoted string.
     * The quoted string is captured for further processing or extraction.
     */
    private static final Pattern MSGID_PLURAL_PATTERN = Pattern.compile("^msgid_plural\\s+\"(.*)\"$");
    /**
     * A regular expression pattern used to match and extract the content of
     * a "msgstr" line within a PO (Portable Object) file. The pattern captures
     * any string content following the "msgstr" keyword enclosed in quotes.
     *
     * This pattern is used to identify and extract the translated message
     * string in PO file entries.
     */
    private static final Pattern MSGSTR_PATTERN = Pattern.compile("^msgstr\\s+\"(.*)\"$");
    /**
     * A compiled regular expression pattern used to match and extract indexed
     * translations from a PO file.
     *
     * The pattern is specifically designed for parsing lines that represent
     * indexed `msgstr` entries in the PO file, which follow the format:
     * `msgstr[index] "translated_text"`.
     *
     * Groups captured by the pattern:
     * 1. The index of the `msgstr` entry, captured as a numeric group.
     * 2. The translated text associated with the `msgstr` entry, captured as a string group.
     *
     * This pattern is utilized during the parsing process to identify and
     * retrieve translation entries with multiple plural forms in PO files.
     */
    private static final Pattern MSGSTR_INDEX_PATTERN = Pattern.compile("^msgstr\\[(\\d+)\\]\\s+\"(.*)\"$");
    /**
     * A compiled regular expression pattern used to identify and extract hash information
     * from lines in a PO file. The pattern matches lines that begin with "#.", followed by
     * "hash:", and one or more whitespace characters, then captures the hash value.
     *
     * The hash value is extracted as a group from the matching line, enabling further
     * processing or usage in the context of parsing translation data. This is typically used
     * to identify source hash information embedded in the PO file's comments.
     */
    private static final Pattern HASH_PATTERN = Pattern.compile("^#\\.\\s+hash:\\s+(.+)$");
    /**
     * A regular expression pattern used to match and capture strings enclosed in double quotes.
     * The pattern is typically utilized to identify and extract the content within
     * the double quotes while parsing translation file entries.
     *
     * This is applied when continuation lines in a PO file need to be processed, ensuring
     * that complete string values spanning multiple lines are correctly combined and unescaped.
     */
    private static final Pattern CONTINUATION_PATTERN = Pattern.compile("^\"(.*)\"$");
    
    /**
     * Compiled regular expression pattern used to match ICU MessageFormat plural patterns.
     *
     * This pattern identifies strings containing plural formatting in ICU syntax, specifically
     * patterns with "one" and "other" cases formatted as:
     *
     * {0, plural, one {text_for_one} other {text_for_other}}
     *
     * It captures the text content within the "one" and "other" cases of the plural structure
     * as separate groups.
     *
     * Fields captured:
     * - Group 1: Content inside the "one" case.
     * - Group 2: Content inside the "other" case.
     */
    // ICU MessageFormat plural pattern
    private static final Pattern ICU_PLURAL_PATTERN = Pattern.compile("\\{0,\\s*plural,\\s*one\\s*\\{([^}]*)\\}\\s*other\\s*\\{([^}]*)\\}\\}");

    /**
     * Parses a PO (Portable Object) file to extract translation entries and metadata.
     *
     * The method reads the specified PO file line by line and processes translation
     * data, including singular and plural forms, source location, and metadata.
     *
     * @param poFile the path to the PO file being parsed
     * @return a {@code TranslationData} object containing all parsed translation entries and metadata
     * @throws IOException if an I/O error occurs while reading the file
     */
    public TranslationData parse(Path poFile) throws IOException {
        Map<String, TranslationEntry> entries = new HashMap<>();
        List<String> lines = Files.readAllLines(poFile);

        String currentHash = null;
        String currentMsgId = null;
        String currentMsgIdPlural = null;
        StringBuilder currentMsgStr = new StringBuilder();
        Map<Integer, String> currentPluralForms = new HashMap<>();
        boolean inMsgStr = false;
        boolean inMsgId = false;
        boolean inPlural = false;
        StringBuilder currentMsgIdBuilder = new StringBuilder();

        log.fine("Parsing PO file: " + poFile + " with " + lines.size() + " lines");

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (line.startsWith("#. hash:")) {
                // Save previous message if exists before processing new hash
                if (currentHash != null && currentMsgId != null) {
                    String translation = currentMsgStr.length() > 0 ? currentMsgStr.toString() : "";
                    String sourceLocation = extractSourceLocation(lines, i);
                    
                    if (currentMsgIdPlural != null) {
                        // This is a plural entry
                        entries.put(currentHash, new TranslationEntry(currentMsgId, currentMsgIdPlural, currentPluralForms, sourceLocation));
                    } else {
                        // This is a regular entry
                        entries.put(currentHash, new TranslationEntry(currentMsgId, translation, sourceLocation));
                    }
                }

                // Reset state for new entry
                currentMsgId = null;
                currentMsgIdPlural = null;
                currentMsgStr = new StringBuilder();
                currentPluralForms.clear();
                inMsgStr = false;
                inMsgId = false;
                inPlural = false;
                currentMsgIdBuilder = new StringBuilder();

                // Extract new hash
                Matcher matcher = HASH_PATTERN.matcher(line);
                if (matcher.matches()) {
                    currentHash = matcher.group(1).trim();
                }

            } else if (line.startsWith("msgid ")) {
                // Start new message
                Matcher matcher = MSGID_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String msgId = unescapeString(matcher.group(1));
                    currentMsgIdBuilder.setLength(0);
                    currentMsgIdBuilder.append(msgId);
                    currentMsgStr.setLength(0);
                    inMsgStr = false;
                    inMsgId = true;
                    inPlural = false;
                    
                    // Check if this is an ICU plural format
                    Matcher icuMatcher = ICU_PLURAL_PATTERN.matcher(msgId);
                    if (icuMatcher.matches()) {
                        // This is an ICU plural entry
                        String oneForm = icuMatcher.group(1).trim();
                        String otherForm = icuMatcher.group(2).trim();
                        currentMsgId = oneForm; // Use one form as the main text
                        currentMsgIdPlural = otherForm;
                        inPlural = true;
                    }
                }

            } else if (line.startsWith("msgid_plural ")) {
                // Start plural form
                Matcher matcher = MSGID_PLURAL_PATTERN.matcher(line);
                if (matcher.matches()) {
                    currentMsgIdPlural = unescapeString(matcher.group(1));
                    inPlural = true;
                    inMsgId = false;
                    inMsgStr = false;
                }

            } else if (line.startsWith("msgstr ")) {
                Matcher matcher = MSGSTR_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String msgStr = unescapeString(matcher.group(1));
                    currentMsgStr.setLength(0);
                    currentMsgStr.append(msgStr);
                    inMsgStr = true;
                    inMsgId = false;
                    inPlural = false;
                    currentMsgId = currentMsgIdBuilder.toString();
                    
                    // If this is a plural entry and the msgstr is also in ICU format, parse it
                    if (inPlural && currentMsgIdPlural != null) {
                        Matcher icuMatcher = ICU_PLURAL_PATTERN.matcher(msgStr);
                        if (icuMatcher.matches()) {
                            // Parse the ICU plural translation
                            String oneTranslation = icuMatcher.group(1).trim();
                            String otherTranslation = icuMatcher.group(2).trim();
                            currentPluralForms.put(0, oneTranslation);
                            currentPluralForms.put(1, otherTranslation);
                        }
                    }
                }

            } else if (line.matches("^msgstr\\[\\d+\\]\\s+\".*\"$")) {
                // Handle plural forms: msgstr[0], msgstr[1], etc.
                Matcher matcher = MSGSTR_INDEX_PATTERN.matcher(line);
                if (matcher.matches()) {
                    int index = Integer.parseInt(matcher.group(1));
                    String translation = unescapeString(matcher.group(2));
                    currentPluralForms.put(index, translation);
                    inPlural = true;
                    inMsgId = false;
                    inMsgStr = false;
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

        // Save last message
        if (currentHash != null && currentMsgId != null) {
            String translation = currentMsgStr.length() > 0 ? currentMsgStr.toString() : "";
            String sourceLocation = extractSourceLocation(lines, lines.size() - 1);
            
            if (currentMsgIdPlural != null) {
                // This is a plural entry
                entries.put(currentHash, new TranslationEntry(currentMsgId, currentMsgIdPlural, currentPluralForms, sourceLocation));
            } else {
                // This is a regular entry
                entries.put(currentHash, new TranslationEntry(currentMsgId, translation, sourceLocation));
            }
        }

        // Create simple metadata
        PoMetadata metadata = new PoMetadata();
        metadata.setLanguage(extractLocaleFromFileName(poFile.getFileName().toString()));

        log.fine("Parsed " + entries.size() + " translations from " + poFile);
        return new TranslationData(entries, metadata);
    }

    /**
     * Extracts the source location comment from a list of strings preceding the given index.
     * Searches for a line starting with "#: " and returns its content.
     * The search is restricted to the last 5 lines before the current index.
     *
     * @param lines the list of strings representing the contents of a file
     * @param currentIndex the index in the list from which the backward search begins
     * @return the source location string extracted from a matching comment line,
     *         or null if no matching line is found
     */
    private String extractSourceLocation(List<String> lines, int currentIndex) {
        // Look backwards for source location comment
        for (int i = Math.max(0, currentIndex - 5); i < currentIndex; i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("#: ")) {
                return line.substring(3);
            }
        }
        return null;
    }

    /**
     * Extracts the locale identifier from a given file name, assuming the file name follows
     * the pattern "messages_[locale].po". If the file name does not match this pattern, returns null.
     *
     * @param fileName the name of the file from which the locale will be extracted
     * @return the extracted locale identifier if the file name matches the expected pattern, or null otherwise
     */
    private String extractLocaleFromFileName(String fileName) {
        if (fileName.startsWith("messages_") && fileName.endsWith(".po")) {
            return fileName.substring(9, fileName.length() - 3);
        }
        return null;
    }

    /**
     * Converts an input string by unescaping common escape sequences such as
     * newlines, carriage returns, tabs, quotes, and backslashes.
     *
     * @param str the input string containing escape sequences to be processed
     * @return a new string with escape sequences replaced by their corresponding
     *         characters
     */
    private String unescapeString(String str) {
        return str.replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\");
    }
}