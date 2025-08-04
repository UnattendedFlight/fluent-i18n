package io.github.unattendedflight.fluent.i18n.compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Robust PO file parser based on working integrated library approach
 */
public class PoFileParser {

    // Same patterns as the working PoCompiler
    private static final Pattern MSGID_PATTERN = Pattern.compile("^msgid\\s+\"(.*)\"$");
    private static final Pattern MSGID_PLURAL_PATTERN = Pattern.compile("^msgid_plural\\s+\"(.*)\"$");
    private static final Pattern MSGSTR_PATTERN = Pattern.compile("^msgstr\\s+\"(.*)\"$");
    private static final Pattern MSGSTR_INDEX_PATTERN = Pattern.compile("^msgstr\\[(\\d+)\\]\\s+\"(.*)\"$");
    private static final Pattern HASH_PATTERN = Pattern.compile("^#\\.\\s+hash:\\s+(.+)$");
    private static final Pattern CONTINUATION_PATTERN = Pattern.compile("^\"(.*)\"$");
    
    // ICU MessageFormat plural pattern
    private static final Pattern ICU_PLURAL_PATTERN = Pattern.compile("\\{0,\\s*plural,\\s*one\\s*\\{([^}]*)\\}\\s*other\\s*\\{([^}]*)\\}\\}");

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

        System.out.println("Parsing PO file: " + poFile + " with " + lines.size() + " lines");

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

        System.out.println("Parsed " + entries.size() + " translations from " + poFile);
        return new TranslationData(entries, metadata);
    }

    /**
     * Extract source location from lines around current position
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
     * Extract locale from filename (messages_en.po -> en)
     */
    private String extractLocaleFromFileName(String fileName) {
        if (fileName.startsWith("messages_") && fileName.endsWith(".po")) {
            return fileName.substring(9, fileName.length() - 3);
        }
        return null;
    }

    private String unescapeString(String str) {
        return str.replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\");
    }
}