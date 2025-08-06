package io.github.unattendedflight.fluent.i18n.core;

import io.github.unattendedflight.fluent.i18n.config.FluentConfig;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Factory for creating NaturalTextMessageSource instances based on configuration.
 * This factory provides framework-agnostic message source creation.
 */
public class MessageSourceFactory {
    
    private static final Logger logger = Logger.getLogger(MessageSourceFactory.class.getName());
    
    /**
     * Creates a message source based on the provided configuration.
     * The factory will determine the appropriate message source type based on
     * the configuration and available resources.
     *
     * @param config the configuration to use
     * @return a NaturalTextMessageSource instance
     */
    public static NaturalTextMessageSource createMessageSource(FluentConfig config) {
        String basePath = config.getBasePath();
        Set<Locale> supportedLocales = config.getSupportedLocales();
        Locale defaultLocale = config.getDefaultLocale();
        
        // Try to create a message source based on available resources
        NaturalTextMessageSource messageSource = tryCreateMessageSource(basePath, supportedLocales, defaultLocale, config);
        
        if (messageSource == null) {
            logger.warning(String.format("No suitable message source found for base path: %s. Using fallback.", basePath));
            messageSource = createFallbackMessageSource(config);
        }
        
        return messageSource;
    }
    
    /**
     * Attempts to create a message source based on the available resources.
     *
     * @param basePath the base path for translation files
     * @param supportedLocales the supported locales
     * @param defaultLocale the default locale
     * @param config the configuration containing message source type preference
     * @return a message source or null if none can be created
     */
    private static NaturalTextMessageSource tryCreateMessageSource(String basePath, Set<Locale> supportedLocales, Locale defaultLocale, FluentConfig config) {
        FluentConfig.MessageSourceType preferredType = config.getMessageSourceType();
        
        // If AUTO, try to detect the best available format
        if (preferredType == FluentConfig.MessageSourceType.AUTO) {
            // Try binary format first (most efficient)
            if (hasBinaryFiles(basePath, supportedLocales)) {
                logger.fine(String.format("Creating binary message source for base path: %s", basePath));
                return createBinaryMessageSource(basePath, supportedLocales, defaultLocale);
            }
            
            // Try JSON format
            if (hasJsonFiles(basePath, supportedLocales)) {
                logger.fine(String.format("Creating JSON message source for base path: %s", basePath));
                return createJsonMessageSource(basePath, supportedLocales, defaultLocale);
            }
            
            // Try properties format
            if (hasPropertiesFiles(basePath, supportedLocales)) {
                logger.fine(String.format("Creating properties message source for base path: %s", basePath));
                return createPropertiesMessageSource(basePath, supportedLocales, defaultLocale);
            }
        } else {
            // Use the specifically configured type
            switch (preferredType) {
                case BINARY:
                    if (hasBinaryFiles(basePath, supportedLocales)) {
                        logger.fine(String.format("Creating binary message source for base path: %s", basePath));
                        return createBinaryMessageSource(basePath, supportedLocales, defaultLocale);
                    }
                    break;
                case JSON:
                    if (hasJsonFiles(basePath, supportedLocales)) {
                        logger.fine(String.format("Creating JSON message source for base path: %s", basePath));
                        return createJsonMessageSource(basePath, supportedLocales, defaultLocale);
                    }
                    break;
                case PROPERTIES:
                    if (hasPropertiesFiles(basePath, supportedLocales)) {
                        logger.fine(String.format("Creating properties message source for base path: %s", basePath));
                        return createPropertiesMessageSource(basePath, supportedLocales, defaultLocale);
                    }
                    break;
            }
        }
        
        return null;
    }
    
    /**
     * Creates a fallback message source that returns the original text.
     *
     * @param config the configuration
     * @return a fallback message source
     */
    private static NaturalTextMessageSource createFallbackMessageSource(FluentConfig config) {
        return new NaturalTextMessageSource() {
            @Override
            public TranslationResult resolve(String hash, String naturalText, Locale locale) {
                return TranslationResult.notFound(naturalText);
            }
            
            @Override
            public boolean exists(String hash, Locale locale) {
                return false;
            }
            
            @Override
            public Iterable<Locale> getSupportedLocales() {
                return config.getSupportedLocales();
            }
        };
    }
    
    /**
     * Checks if binary translation files exist for the given locales.
     *
     * @param basePath the base path
     * @param supportedLocales the supported locales
     * @return true if binary files exist
     */
    private static boolean hasBinaryFiles(String basePath, Set<Locale> supportedLocales) {
        for (Locale locale : supportedLocales) {
            String resourcePath = basePath + "/messages_" + locale.toLanguageTag() + ".bin";
            if (resourceExists(resourcePath)) {
                logger.fine("Found binary file: " + resourcePath);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if JSON translation files exist for the given locales.
     *
     * @param basePath the base path
     * @param supportedLocales the supported locales
     * @return true if JSON files exist
     */
    private static boolean hasJsonFiles(String basePath, Set<Locale> supportedLocales) {
        for (Locale locale : supportedLocales) {
            String resourcePath = basePath + "/messages_" + locale.toLanguageTag() + ".json";
            if (resourceExists(resourcePath)) {
                logger.fine("Found JSON file: " + resourcePath);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if properties translation files exist for the given locales.
     *
     * @param basePath the base path
     * @param supportedLocales the supported locales
     * @return true if properties files exist
     */
    private static boolean hasPropertiesFiles(String basePath, Set<Locale> supportedLocales) {
        for (Locale locale : supportedLocales) {
            String resourcePath = basePath + "/messages_" + locale.toLanguageTag() + ".properties";
            if (resourceExists(resourcePath)) {
                logger.fine("Found properties file: " + resourcePath);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if a resource exists in the classpath.
     *
     * @param resourcePath the resource path to check
     * @return true if the resource exists
     */
    private static boolean resourceExists(String resourcePath) {
        try (InputStream is = MessageSourceFactory.class.getClassLoader().getResourceAsStream(resourcePath)) {
            return is != null;
        } catch (Exception e) {
            logger.fine("Error checking resource existence: " + resourcePath + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates a binary message source.
     *
     * @param basePath the base path
     * @param supportedLocales the supported locales
     * @param defaultLocale the default locale
     * @return a binary message source
     */
    private static NaturalTextMessageSource createBinaryMessageSource(String basePath, Set<Locale> supportedLocales, Locale defaultLocale) {
        return new CoreBinaryMessageSource(basePath, supportedLocales, defaultLocale);
    }
    
    /**
     * Creates a JSON message source.
     *
     * @param basePath the base path
     * @param supportedLocales the supported locales
     * @param defaultLocale the default locale
     * @return a JSON message source
     */
    private static NaturalTextMessageSource createJsonMessageSource(String basePath, Set<Locale> supportedLocales, Locale defaultLocale) {
        return new CoreJsonMessageSource(basePath, supportedLocales, defaultLocale);
    }
    
    /**
     * Creates a properties message source.
     *
     * @param basePath the base path
     * @param supportedLocales the supported locales
     * @param defaultLocale the default locale
     * @return a properties message source
     */
    private static NaturalTextMessageSource createPropertiesMessageSource(String basePath, Set<Locale> supportedLocales, Locale defaultLocale) {
        return new CorePropertiesMessageSource(basePath, supportedLocales, defaultLocale);
    }

    /**
     * CoreBinaryMessageSource loads and resolves translations from binary resources for supported locales.
     * Efficiently caches translations in-memory to optimize repeated lookups.
     *
     * Business Context:
     * - Designed for performance in high-traffic applications requiring i18n support.
     * - Ensures fallback behavior to a default locale when requested locale lacks a translation.
     * - Handles compressed and uncompressed binary resource files, validating file integrity.
     *
     * Edge Cases:
     * - Gracefully handles missing, malformed, or unsupported binary files without halting execution.
     * - Safeguards against invalid version headers or corrupt entries during parsing.
     *
     * Key Considerations:
     * - Fallback to default locale only occurs when the hash is missing, even if natural text exists.
     * - Uses VLQ encoding for flexibility in binary formats; errors are logged, not thrown, for resilience.
     */
    private static class CoreBinaryMessageSource implements NaturalTextMessageSource {
        private static final byte[] MAGIC = "FL18".getBytes(StandardCharsets.UTF_8);
        private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
        private static final byte FLAG_COMPRESSED = 0x01;
        private static final byte FLAG_FIXED_HASH_LENGTH = 0x02;
        
        private final String basePath;
        private final Set<Locale> supportedLocales;
        private final Locale defaultLocale;
        private final Map<Locale, Map<String, String>> cache = new ConcurrentHashMap<>();
        
        /**
         * Sets up the message source for resolving translations from binary files.
         *
         * @param basePath the root directory for locating binary message files; critical for resolving resources.
         * @param supportedLocales the set of locales the message source explicitly supports; ensures fallback logic when a requested locale isn't available.
         * @param defaultLocale the locale to fall back on when a translation is missing or unsupported; ensures predictable behavior in edge cases.
         */
        public CoreBinaryMessageSource(String basePath, Set<Locale> supportedLocales, Locale defaultLocale) {
            this.basePath = basePath;
            this.supportedLocales = supportedLocales;
            this.defaultLocale = defaultLocale;
        }
        
        /**
         * Resolves a translation for the given hash and locale, falling back to the default locale if necessary.
         * If no translation is available, uses the provided natural text as a fallback.
         *
         * Business logic ensures consistent behavior for missing or incomplete translations,
         * prioritizing locale-specific results while maintaining a reasonable fallback strategy.
         * Assumes translations for unsupported locales are pre-validated, reducing redundant checks.
         *
         * @param hash the unique identifier for the translation entry; must match the precomputed translation hash
         * @param naturalText the fallback text to use if neither locale-specific nor default translations are available
         * @param locale the desired locale for the translation, determines both primary and fallback lookup paths
         * @return a TranslationResult representing either the translation found or a fallback with natural text
         */
        @Override
        public TranslationResult resolve(String hash, String naturalText, Locale locale) {
            Map<String, String> translations = getTranslations(locale);
            String translation = translations.get(hash);
            
            if (translation != null && !translation.isBlank()) {
                return TranslationResult.found(translation);
            }
            
            // Try default locale fallback
            if (!locale.equals(defaultLocale)) {
                Map<String, String> defaultTranslations = getTranslations(defaultLocale);
                String defaultTranslation = defaultTranslations.get(hash);
                if (defaultTranslation != null && !defaultTranslation.isBlank()) {
                    return TranslationResult.found(defaultTranslation);
                }
            }
            
            return TranslationResult.notFound(naturalText);
        }
        
        /**
         * Checks if a translation identified by the given hash exists for the specified locale.
         *
         * This leverages cached translations to avoid redundant lookups and ensures that missing
         * resources or unsupported locales do not cause errors. The fallback behavior relies on
         * translation validity being guaranteed during cache initialization.
         *
         * Edge Cases:
         * - If the locale is unsupported, an empty map is returned from {@code getTranslations()},
         *   causing this method to return {@code false}.
         * - Hash lookup is strictly case-sensitive and depends on exact matching.
         *
         * @param hash the unique identifier for the translation entry, typically a precomputed hash value
         * @param locale the locale in which the translation is sought
         * @return {@code true} if the translation exists for the given hash and locale; {@code false} otherwise
         */
        @Override
        public boolean exists(String hash, Locale locale) {
            Map<String, String> translations = getTranslations(locale);
            return translations.containsKey(hash);
        }
        
        /**
         * Provides the set of locales explicitly supported by this message source.
         *
         * Ensures downstream processes (e.g., translation resolution, fallback logic) can validate
         * or restrict operations to supported locales. Critical for avoiding redundant lookups and
         * enforcing predictable resource availability. Returns an empty collection if no locales are supported.
         *
         * @return an iterable containing supported locales; may be empty but never null
         */
        @Override
        public Iterable<Locale> getSupportedLocales() {
            return supportedLocales;
        }
        
        /**
         * Retrieves cached translations for the specified locale, loading them if not already cached.
         *
         * Ensures efficient access to localized resources by leveraging an in-memory cache. Automatically
         * invokes a loading mechanism if translations for the given locale are missing, minimizing I/O overhead
         * in subsequent calls.
         *
         * Business Rules:
         * - Returns an empty map if the locale is unsupported or the resource fails to load.
         * - The input locale must not be null; use the default locale fallback logic for missing data upstream.
         *
         * @param locale the locale for which translations are requested; determines the resource lookup path.
         * @return a map of translation keys to localized strings for the requested locale, or an empty map if unavailable.
         */
        private Map<String, String> getTranslations(Locale locale) {
            return cache.computeIfAbsent(locale, this::loadTranslations);
        }
        
        /**
         * Loads translations for the specified locale from a binary resource file.
         *
         * Prioritizes fetching resources for locale-specific translations, gracefully
         * handling missing files or corrupted contents by returning an empty map.
         * Logs detailed warnings for issues like missing files or read errors.
         * Guarantees non-null output to ensure stability in downstream translation logic.
         *
         * @param locale the locale for which translations are being loaded; strongly affects
         *               resource lookup by constructing a path using the locale's language tag.
         * @return a map containing translation entries for the specified locale;
         *         returns an empty map if the resource cannot be found or fails to load.
         */
        private Map<String, String> loadTranslations(Locale locale) {
            String resourcePath = basePath + "/messages_" + locale.toLanguageTag() + ".bin";
            
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    logger.fine("Binary resource not found: " + resourcePath);
                    return new HashMap<>();
                }
                
                byte[] data = is.readAllBytes();
                return parseBinaryFile(data, resourcePath);
                
            } catch (Exception e) {
                logger.warning("Error loading binary resource: " + resourcePath + " - " + e.getMessage());
                return new HashMap<>();
            }
        }
        
        /**
         * Parses a binary file to extract translations, handling compression and version-specific formats.
         *
         * @param data the raw binary file data; may be GZIP-compressed and must follow expected header and format rules
         * @param resourcePath the resource identifier for the file, used for logging/debugging in case of errors
         * @return a map of translation keys to their corresponding translations; guaranteed to be non-null, potentially empty
         * @throws IOException if decompression fails, the file format is invalid, or unsupported versions are encountered
         *
         * Business Notes:
         * - Validates GZIP compression using magic bytes before attempting to decompress.
         * - Supports multiple binary formats by delegating to version-specific logic, ensuring backward compatibility.
         * - Header structure and entry limits are strictly enforced to avoid processing malformed data.
         * - Logs processing success or detailed failure reasons for traceability.
         */
        private Map<String, String> parseBinaryFile(byte[] data, String resourcePath) throws IOException {
            // Check for GZIP compression
            boolean isCompressed = data.length >= 2 && 
                (data[0] & 0xFF) == 0x1F && (data[1] & 0xFF) == 0x8B;
                
            if (isCompressed) {
                try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                     GZIPInputStream gzis = new GZIPInputStream(bais)) {
                    data = gzis.readAllBytes();
                } catch (IOException e) {
                    throw new IOException("Failed to decompress binary file: " + resourcePath, e);
                }
            }
            
            ByteBuffer buffer = ByteBuffer.wrap(data).order(BYTE_ORDER);
            
            // Read and validate header
            BinaryHeader header = readAndValidateHeader(buffer, resourcePath);
            if (header == null) {
                throw new IOException("Invalid binary file format for resource: " + resourcePath);
            }
            
            // Read entries based on version
            Map<String, String> translations;
            if (header.version == 1) {
                translations = readEntriesV1(buffer);
            } else if (header.version == 2) {
                translations = readEntriesV2(buffer, header);
            } else {
                throw new IOException("Unsupported binary file version: " + header.version);
            }
            
            logger.fine("Successfully parsed " + translations.size() + " translations from binary resource: " + resourcePath);
            return translations;
        }
        
        /**
         * Parses and validates the binary file header for structural integrity and version compatibility.
         *
         * Ensures the buffer contains sufficient data, validates the file's magic number,
         * and verifies the version is supported. Logs warnings for invalid or unsupported
         * headers, preventing downstream processing of malformed or incompatible files.
         *
         * Handles edge cases like:
         * - Insufficient data in the buffer.
         * - Invalid magic numbers or corrupted file headers.
         * - Unsupported version numbers (<1 or >2).
         *
         * In version 2 headers:
         * - Reads optional fixed hash length based on flags.
         * - Skips locale data to align buffer position for entry parsing.
         *
         * @param buffer       ByteBuffer containing binary file data to be parsed.
         * @param resourcePath Logical or physical identifier for logging context in errors.
         * @return {@code BinaryHeader} if validation is successful; {@code null} on failure.
         */
        private BinaryHeader readAndValidateHeader(ByteBuffer buffer, String resourcePath) {
            if (buffer.remaining() < MAGIC.length + 2) {
                logger.warning("Binary file too small: " + resourcePath);
                return null;
            }
            
            // Check magic number
            byte[] magic = new byte[MAGIC.length];
            buffer.get(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                logger.warning("Invalid magic number in binary file: " + resourcePath);
                return null;
            }
            
            // Read version
            byte version = buffer.get();
            if (version < 1 || version > 2) {
                logger.warning("Unsupported version in binary file: " + version + " for " + resourcePath);
                return null;
            }
            
            byte flags = 0;
            Integer fixedHashLength = null;
            int entryCount = 0;
            
            if (version >= 2) {
                flags = buffer.get();
                
                // Skip locale (VLQ length + string)
                int localeLength = readVLQ(buffer);
                buffer.position(buffer.position() + localeLength);
                
                // Read fixed hash length if applicable
                if ((flags & FLAG_FIXED_HASH_LENGTH) != 0) {
                    fixedHashLength = (int) buffer.get();
                }
                
                // Read entry count
                entryCount = readVLQ(buffer);
            }
            
            return new BinaryHeader(version, flags, fixedHashLength, entryCount);
        }
        
        /**
         * Parses and extracts key-value pairs from a V1 binary translation buffer.
         *
         * Each "key" (hash) and its corresponding "value" (translation) are read sequentially,
         * ensuring data consistency and proper handling of entry length boundaries.
         *
         * Business Rules:
         * - Stops processing and returns partial results on malformed data or buffer underflow.
         * - Logs errors and gracefully handles unexpected exceptions without blocking execution.
         * - Enforces strict UTF-8 encoding for both keys and values.
         * - Invalid hash or translation lengths (e.g., non-positive or exceeding remaining buffer)
         *   terminate processing to avoid corrupted results.
         *
         * Edge Cases:
         * - A corrupt or truncated buffer returns only successfully read entries.
         * - Excessive lengths exceeding buffer remaining capacity are interpreted as corruption.
         * - Does not attempt retries or rollback on malformed entries.
         *
         * @param buffer a {@link ByteBuffer} containing raw V1 translation data; must not be null or empty.
         * @return a map of hash keys to their corresponding translations. Only valid entries are included.
         */
        private Map<String, String> readEntriesV1(ByteBuffer buffer) {
            Map<String, String> translations = new HashMap<>();
            
            while (buffer.hasRemaining()) {
                try {
                    // Read hash length and hash
                    int hashLength = buffer.getInt();
                    if (hashLength <= 0 || hashLength > buffer.remaining()) {
                        break;
                    }
                    
                    byte[] hashBytes = new byte[hashLength];
                    buffer.get(hashBytes);
                    String hash = new String(hashBytes, StandardCharsets.UTF_8);
                    
                    // Read translation length and translation
                    int translationLength = buffer.getInt();
                    if (translationLength < 0 || translationLength > buffer.remaining()) {
                        break;
                    }
                    
                    byte[] translationBytes = new byte[translationLength];
                    buffer.get(translationBytes);
                    String translation = new String(translationBytes, StandardCharsets.UTF_8);
                    
                    translations.put(hash, translation);
                } catch (Exception e) {
                    logger.warning("Error reading V1 entry: " + e.getMessage());
                    break;
                }
            }
            
            return translations;
        }
        
        /**
         * Reads key-value translation entries from a v2-formatted binary buffer using the provided header.
         *
         * The header's `flags` and `fixedHashLength` determine how hash lengths are read,
         * enabling flexible or fixed-length hash formats. Safeguards ensure the process halts gracefully
         * on invalid or incomplete data (e.g., corrupt buffer, mismatched lengths).
         * Entries exceeding buffer limits or non-UTF-8 encodings will log warnings and stop processing.
         *
         * @param buffer the binary buffer containing translation data; must be positioned correctly to read entries
         * @param header metadata describing the entry structure and format; defines limits and parsing logic
         * @return a map of hash keys to translated strings; may be incomplete if errors occur during reading
         */
        private Map<String, String> readEntriesV2(ByteBuffer buffer, BinaryHeader header) {
            Map<String, String> translations = new HashMap<>();
            boolean hasFixedHashLength = (header.flags & FLAG_FIXED_HASH_LENGTH) != 0;
            
            for (int i = 0; i < header.entryCount && buffer.hasRemaining(); i++) {
                try {
                    // Read hash
                    int hashLength = hasFixedHashLength && header.fixedHashLength != null ? 
                        header.fixedHashLength : readVLQ(buffer);
                    
                    if (hashLength <= 0 || hashLength > buffer.remaining()) {
                        break;
                    }
                    
                    byte[] hashBytes = new byte[hashLength];
                    buffer.get(hashBytes);
                    String hash = new String(hashBytes, StandardCharsets.UTF_8);
                    
                    // Read translation
                    int translationLength = readVLQ(buffer);
                    if (translationLength < 0 || translationLength > buffer.remaining()) {
                        break;
                    }
                    
                    byte[] translationBytes = new byte[translationLength];
                    buffer.get(translationBytes);
                    String translation = new String(translationBytes, StandardCharsets.UTF_8);
                    
                    translations.put(hash, translation);
                } catch (Exception e) {
                    logger.warning("Error reading V2 entry: " + e.getMessage());
                    break;
                }
            }
            
            return translations;
        }
        
        /**
         * Decodes a VLQ (Variable Length Quantity) value from the provided buffer.
         *
         * VLQ encoding is used to store variable-length integers efficiently, with higher bits flagging
         * whether more bytes are part of the value. This method reads up to 32 bits max; longer sequences
         * trigger an exception. Essential for parsing compact, size-prefixed binary data.
         *
         * @param buffer the byte buffer to read from; must contain enough bytes to decode the VLQ value
         * @return the decoded integer value
         * @throws IllegalStateException if the VLQ sequence exceeds 32 bits, indicating a malformed value
         */
        private int readVLQ(ByteBuffer buffer) {
            int result = 0;
            int shift = 0;
            
            while (buffer.hasRemaining()) {
                byte b = buffer.get();
                result |= (b & 0x7F) << shift;
                if ((b & 0x80) == 0) {
                    break;
                }
                shift += 7;
                if (shift >= 32) {
                    throw new IllegalStateException("VLQ value too large");
                }
            }
            
            return result;
        }
        
        /**
         * Represents metadata for a binary structure, enabling validation and parsing of
         * a fixed-format binary file.
         *
         * The `version` helps track format changes, ensuring compatibility.
         * `flags` can denote optional settings or features for the binary data.
         * `fixedHashLength`, if non-null, imposes strict hash length validation.
         * `entryCount` provides a quick reference to the total entries, aiding in efficient allocation or validation.
         *
         * Assumes inputs are validated upstream to preserve runtime performance.
         */
        private static class BinaryHeader {
            final int version;
            final byte flags;
            final Integer fixedHashLength;
            final int entryCount;
            
            BinaryHeader(int version, byte flags, Integer fixedHashLength, int entryCount) {
                this.version = version;
                this.flags = flags;
                this.fixedHashLength = fixedHashLength;
                this.entryCount = entryCount;
            }
        }
    }
    
    /**
     * Framework-agnostic JSON message source implementation.
     */
    private static class CoreJsonMessageSource implements NaturalTextMessageSource {
        private final String basePath;
        private final Set<Locale> supportedLocales;
        private final Locale defaultLocale;
        private final Map<Locale, Map<String, String>> cache = new ConcurrentHashMap<>();
        
        public CoreJsonMessageSource(String basePath, Set<Locale> supportedLocales, Locale defaultLocale) {
            this.basePath = basePath;
            this.supportedLocales = supportedLocales;
            this.defaultLocale = defaultLocale;
        }
        
        @Override
        public TranslationResult resolve(String hash, String naturalText, Locale locale) {
            Map<String, String> translations = getTranslations(locale);
            String translation = translations.get(hash);
            
            if (translation != null && !translation.isBlank()) {
                return TranslationResult.found(translation);
            }
            
            // Try default locale fallback
            if (!locale.equals(defaultLocale)) {
                Map<String, String> defaultTranslations = getTranslations(defaultLocale);
                String defaultTranslation = defaultTranslations.get(hash);
                if (defaultTranslation != null && !defaultTranslation.isBlank()) {
                    return TranslationResult.found(defaultTranslation);
                }
            }
            
            return TranslationResult.notFound(naturalText);
        }
        
        @Override
        public boolean exists(String hash, Locale locale) {
            Map<String, String> translations = getTranslations(locale);
            return translations.containsKey(hash);
        }
        
        @Override
        public Iterable<Locale> getSupportedLocales() {
            return supportedLocales;
        }
        
        private Map<String, String> getTranslations(Locale locale) {
            return cache.computeIfAbsent(locale, this::loadTranslations);
        }
        
        private Map<String, String> loadTranslations(Locale locale) {
            String resourcePath = basePath + "/messages_" + locale.toLanguageTag() + ".json";
            
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    logger.fine("JSON resource not found: " + resourcePath);
                    return new HashMap<>();
                }
                
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                return parseJsonContent(content, resourcePath);
                
            } catch (Exception e) {
                logger.warning("Error loading JSON resource: " + resourcePath + " - " + e.getMessage());
                return new HashMap<>();
            }
        }
        
        private Map<String, String> parseJsonContent(String content, String resourcePath) {
            Map<String, String> translations = new HashMap<>();
            
            try {
                // Simple JSON parsing without external dependencies
                content = content.trim();
                if (!content.startsWith("{") || !content.endsWith("}")) {
                    logger.warning("Invalid JSON format in: " + resourcePath);
                    return translations;
                }
                
                // Remove outer braces
                content = content.substring(1, content.length() - 1).trim();
                
                // Parse key-value pairs
                boolean inString = false;
                boolean escapeNext = false;
                StringBuilder currentKey = new StringBuilder();
                StringBuilder currentValue = new StringBuilder();
                boolean parsingKey = true;
                boolean foundColon = false;
                
                for (int i = 0; i < content.length(); i++) {
                    char c = content.charAt(i);
                    
                    if (escapeNext) {
                        if (parsingKey && foundColon == false) {
                            currentKey.append(c);
                        } else if (foundColon) {
                            currentValue.append(c);
                        }
                        escapeNext = false;
                        continue;
                    }
                    
                    if (c == '\\') {
                        escapeNext = true;
                        continue;
                    }
                    
                    if (c == '"') {
                        inString = !inString;
                        continue;
                    }
                    
                    if (!inString) {
                        if (c == ':' && parsingKey) {
                            foundColon = true;
                            parsingKey = false;
                            continue;
                        } else if (c == ',' && foundColon) {
                            // End of current pair
                            String key = currentKey.toString().trim();
                            String value = currentValue.toString().trim();
                            if (!key.isEmpty() && !value.isEmpty()) {
                                translations.put(key, value);
                            }
                            
                            currentKey.setLength(0);
                            currentValue.setLength(0);
                            parsingKey = true;
                            foundColon = false;
                            continue;
                        } else if (Character.isWhitespace(c)) {
                            continue;
                        }
                    }
                    
                    if (parsingKey && !foundColon) {
                        currentKey.append(c);
                    } else if (foundColon) {
                        currentValue.append(c);
                    }
                }
                
                // Handle last pair
                if (foundColon) {
                    String key = currentKey.toString().trim();
                    String value = currentValue.toString().trim();
                    if (!key.isEmpty() && !value.isEmpty()) {
                        translations.put(key, value);
                    }
                }
                
            } catch (Exception e) {
                logger.warning("Error parsing JSON content in: " + resourcePath + " - " + e.getMessage());
            }
            
            logger.fine("Successfully parsed " + translations.size() + " translations from JSON resource: " + resourcePath);
            return translations;
        }
    }
    
    /**
     * Framework-agnostic properties message source implementation.
     */
    private static class CorePropertiesMessageSource implements NaturalTextMessageSource {
        private final String basePath;
        private final Set<Locale> supportedLocales;
        private final Locale defaultLocale;
        private final Map<Locale, Map<String, String>> cache = new ConcurrentHashMap<>();
        
        /**
         * Constructs a message source to load and manage translations based on properties files.
         *
         * @param basePath the base path where property files for translations are located; must be valid
         *                 and accessible to ensure proper resource loading.
         * @param supportedLocales a set of locales explicitly supported by this source; ensures only
         *                         specified languages are handled, avoiding unintended fallback behavior.
         * @param defaultLocale the default locale to fall back on when a translation is missing;
         *                      minimizes unexpected failures or inconsistent UX in unsupported languages.
         */
        public CorePropertiesMessageSource(String basePath, Set<Locale> supportedLocales, Locale defaultLocale) {
            this.basePath = basePath;
            this.supportedLocales = supportedLocales;
            this.defaultLocale = defaultLocale;
        }
        
        /**
         * Resolves a translation for the given hash and locale. If no matching translation is found
         * for the specified locale, attempts to fall back to the default locale. If no translation
         * exists in either, the naturalText is returned as a fallback message.
         *
         * @param hash the identifier for the desired translation; must be non-null and valid.
         * @param naturalText the fallback text to use when no translation is found; ensures UX continuity.
         * @param locale the locale to search for the translation; determines the targeted language.
         * @return a TranslationResult encapsulating the found translation or fallback values.
         *
         * Handles edge cases like:
         * - Missing translations in both the requested and default locales.
         * - Blank or invalid translations in the source files.
         * Guarantees a meaningful result is always returned, minimizing user-facing errors.
         */
        @Override
        public TranslationResult resolve(String hash, String naturalText, Locale locale) {
            Map<String, String> translations = getTranslations(locale);
            String translation = translations.get(hash);
            
            if (translation != null && !translation.isBlank()) {
                return TranslationResult.found(translation);
            }
            
            // Try default locale fallback
            if (!locale.equals(defaultLocale)) {
                Map<String, String> defaultTranslations = getTranslations(defaultLocale);
                String defaultTranslation = defaultTranslations.get(hash);
                if (defaultTranslation != null && !defaultTranslation.isBlank()) {
                    return TranslationResult.found(defaultTranslation);
                }
            }
            
            return TranslationResult.notFound(naturalText);
        }
        
        /**
         * Checks if a translation exists for the given hash in the specified locale.
         *
         * This method verifies the presence of a translation key (hash) in the translations
         * for the given locale. Since translations are cached, changes to the underlying
         * properties files won't reflect unless the cache is refreshed. Edge cases include
         * unsupported locales, which would typically return an empty translation map, causing
         * the method to return false.
         *
         * @param hash the unique key representing the translation
         * @param locale the locale to look up the translation in
         * @return true if the translation exists for the given hash and locale; false otherwise
         */
        @Override
        public boolean exists(String hash, Locale locale) {
            Map<String, String> translations = getTranslations(locale);
            return translations.containsKey(hash);
        }
        
        /**
         * Returns an iterable of locales explicitly supported by this message source.
         *
         * @return a collection of locales that are available for translation, ensuring
         *         strict adherence to defined languages and preventing fallback behavior
         *         into undesirable or unsupported locales. This enforces quality control
         *         in internationalized applications where supported locales are pre-defined.
         */
        @Override
        public Iterable<Locale> getSupportedLocales() {
            return supportedLocales;
        }
        
        /**
         * Fetches translations for the specified locale, leveraging a cache for efficiency.
         * Falls back to loading translations from properties if not already cached.
         *
         * Ensures thread-safe, lazy initialization of translations via `computeIfAbsent`.
         *
         * @param locale the desired locale for which translations are fetched; must match a
         *               supported locale to retrieve meaningful data.
         * @return a map of message keys to their translated values for the given locale.
         *         Returns an empty map if the locale is unsupported, the resource is missing,
         *         or an error occurs during loading.
         */
        private Map<String, String> getTranslations(Locale locale) {
            return cache.computeIfAbsent(locale, this::loadTranslations);
        }
        
        /**
         * Loads translation key-value pairs for a specific locale from a properties file, ensuring fallback and resilience.
         *
         * @param locale the target locale to load translations for; must match the naming conventions of the properties file.
         * @return a map containing translation keys and their corresponding localized values; returns an empty map if the
         *         resource is missing or an error occurs.
         *
         * Fails gracefully by logging warnings if the resource is missing or can't be loaded, avoiding unexpected crashes.
         * Assumes UTF-8 encoding for property files to support comprehensive internationalization.
         * Be mindful of cases where no translations exist for a locale, resulting in an empty map rather than null.
         */
        private Map<String, String> loadTranslations(Locale locale) {
            String resourcePath = basePath + "/messages_" + locale.toLanguageTag() + ".properties";
            
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    logger.fine("Properties resource not found: " + resourcePath);
                    return new HashMap<>();
                }
                
                Properties properties = new Properties();
                properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                
                Map<String, String> translations = new HashMap<>();
                for (String key : properties.stringPropertyNames()) {
                    translations.put(key, properties.getProperty(key));
                }
                
                logger.fine("Successfully parsed " + translations.size() + " translations from properties resource: " + resourcePath);
                return translations;
                
            } catch (Exception e) {
                logger.warning("Error loading properties resource: " + resourcePath + " - " + e.getMessage());
                return new HashMap<>();
            }
        }
    }
} 