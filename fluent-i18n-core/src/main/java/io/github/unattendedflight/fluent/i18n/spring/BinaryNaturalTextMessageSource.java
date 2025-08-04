package io.github.unattendedflight.fluent.i18n.spring;

import io.github.unattendedflight.fluent.i18n.core.NaturalTextMessageSource;
import io.github.unattendedflight.fluent.i18n.core.TranslationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The BinaryNaturalTextMessageSource class provides a mechanism to load and manage natural
 * text message translations stored in a binary format. It supports caching, locale-specific
 * lookups, and automatic reloading of translations when needed. The class implements the
 * NaturalTextMessageSource interface for consistent translation resolution.
 *
 * This class is designed to support a specific binary format, including a magic number,
 * versioning, and locale-based translation entries. The binary files are expected to
 * reside on the specified resource path and adhere to the required binary structure.
 *
 * Key features include:
 * - Translation loading from binary resource files with caching for optimization.
 * - Locale support with fallback to default locale if no translation is found.
 * - Warm-up capability to pre-load translations for specified locales.
 * - Automatic cache expiration and on-demand reloading of translations.
 *
 * Binary file format:
 * - Contains a magic number for validation, versioning, locale information, and translation entries.
 * - Translation entries include unique hash identifiers mapped to translated strings.
 */
public class BinaryNaturalTextMessageSource implements NaturalTextMessageSource {
    /**
     * Logger instance used for logging messages within the BinaryNaturalTextMessageSource class.
     * It is initialized with the class type to associate logged messages with the specific class.
     * This is a static and final variable, ensuring one logger instance is shared across all
     * instances of the class and cannot be modified.
     */
    private static final Logger logger = LoggerFactory.getLogger(BinaryNaturalTextMessageSource.class);
    /**
     * A magic number used to identify the specific binary file format handled by the
     * BinaryNaturalTextMessageSource class. The value is derived from the string "FL18"
     * and converted into a byte array using the platform's default character encoding.
     *
     * This field is used to validate the header of the binary file to ensure compatibility
     * with the expected format.
     */
    private static final byte[] MAGIC = "FL18".getBytes();
    /**
     * Defines the byte order used within the binary message source.
     *
     * This specifies the endianness for reading or writing data in binary files.
     * The value of this variable is set to {@code ByteOrder.LITTLE_ENDIAN},
     * indicating that the least significant byte of a word is stored in
     * the smallest address.
     *
     * Used in parsing and validating binary files for locale-specific
     * translations within the {@code BinaryNaturalTextMessageSource}.
     */
    private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    /**
     * Handles loading of resource files, such as binary translation files,
     * from a specified source. This is used to support the functionality of
     * the BinaryNaturalTextMessageSource.
     */
    private final ResourceLoader resourceLoader;
    /**
     * The base path for locating binary resource files used for translations.
     * This path is relative to the resource loader provided to the message source.
     * It determines the directory or location where translation files are stored.
     */
    private final String basePath;
    /**
     * Default locale used for resolving translations when a specific locale is not provided or
     * when the requested locale is unsupported.
     * <br>The default locale ensures fallback behavior for message resolution.
     */
    private final Locale defaultLocale;
    /**
     * A collection of locales that are supported by the system for translations or natural text processing.
     * This set defines the locales for which the system can provide translations or text resources,
     * ensuring consistent behavior across specified locales.
     */
    private final Set<Locale> supportedLocales;
    /**
     * Resolves resource patterns into resource objects using Ant-style path matching.
     * This variable is responsible for locating and resolving binary text message files
     * in the application's classpath or other configured resource locations.
     * The resolver allows for flexibility in loading resources (e.g., by using wildcards
     * for file matching) and ensures compatibility for resource loading within the application.
     */
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    /**
     * A thread-safe cache for storing translations.
     * The outer map uses string keys representing hash values of translations,
     * while the inner map contains key-value pairs where the keys are locales (as strings)
     * and values are the corresponding translations.
     *
     * This structure allows efficient retrieval of translations based on both hash and locale.
     */
    private final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();
    /**
     * A thread-safe map that associates cache identifiers (represented as strings)
     * with their corresponding timestamps. This is used to track the last update
     * time of cached resources within the {@code BinaryNaturalTextMessageSource}.
     */
    private final Map<String, Instant> cacheTimestamps = new ConcurrentHashMap<>();

    /**
     * Constructs a new instance of BinaryNaturalTextMessageSource with the specified parameters.
     *
     * @param resourceLoader the ResourceLoader used to load binary resources.
     * @param basePath the base path for resource files.
     * @param supportedLocales the set of supported locales. If null, the default locale will be used.
     * @param defaultLocale the default locale for translations. If null, Locale.ENGLISH will be used.
     */
    public BinaryNaturalTextMessageSource(ResourceLoader resourceLoader, String basePath, Set<Locale> supportedLocales, Locale defaultLocale) {
        this.resourceLoader = resourceLoader;
        this.basePath = basePath;
        this.defaultLocale = defaultLocale != null ? defaultLocale : Locale.ENGLISH;
        this.supportedLocales = supportedLocales != null ? supportedLocales : Set.of(defaultLocale);
        
        logger.info("BinaryNaturalTextMessageSource initialized with basePath: '{}'", basePath);
        logger.info("Supported locales: {}", supportedLocales);
        logger.info("Default locale: {}", defaultLocale);
    }

    /**
     * Resolves a translation for the provided hash and locale. If a translation exists
     * for the given hash and locale, it is retrieved from the cache or resources.
     * If no translation is found, a "not found" result with the fallback natural text is returned.
     *
     * @param hash the unique hash representing the text to translate
     * @param naturalText the fallback natural text to use if no translation is found
     * @param locale the locale for which the translation is requested
     * @return a {@code TranslationResult} containing the translated text if found;
     *         otherwise, a fallback with the natural text
     */
    @Override
    public TranslationResult resolve(String hash, String naturalText, Locale locale) {
        String localeStr = locale.toString();

        // Get translations with cache management
        Map<String, String> localeCache = getTranslations(locale);

        if (localeCache != null) {
            String translation = localeCache.get(hash);
            if (translation != null && !translation.isEmpty()) {
                return TranslationResult.found(translation);
            }
        }

        if (!locale.equals(defaultLocale)) {
            logger.debug("No translation found for hash '{}' (text: '{}') in locale '{}'",
                hash, naturalText, locale);
        }

        return TranslationResult.notFound(naturalText);
    }

    /**
     * Checks if a translation exists for the given hash in the specified locale.
     *
     * The method retrieves a cache of translations for the given locale and attempts
     * to locate a translation corresponding to the specified hash. A translation is
     * considered to exist if it is non-null and non-empty.
     *
     * @param hash the hash key representing the translation to search for
     * @param locale the locale in which to look for the translation
     * @return true if the translation exists, false otherwise
     */
    @Override
    public boolean exists(String hash, Locale locale) {
        Map<String, String> localeCache = getTranslations(locale);

        if (localeCache != null) {
            String translation = localeCache.get(hash);
            return translation != null && !translation.isEmpty();
        }

        return false;
    }

    /**
     * Retrieves the list of locales supported by this message source.
     *
     * @return an Iterable of Locale objects representing the supported locales
     */
    @Override
    public Iterable<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    /**
     * Reloads the binary translation cache by clearing all currently stored translations and their timestamps.
     * This method is primarily used to refresh the cache, ensuring that any outdated or stale data is removed.
     * It also logs an informational message to indicate that the cache has been cleared.
     *
     * The method affects the following:
     * - Clears the `cache`, which stores translation data.
     * - Clears the `cacheTimestamps`, which tracks when the cache entries were last updated.
     *
     * This operation may be invoked when updates to translation resources have been made that require
     * reloading or when a system-wide cache reset is needed.
     */
    @Override
    public void reload() {
        cache.clear();
        cacheTimestamps.clear();
        logger.info("Binary translation cache cleared");
    }

    /**
     * Warms up the binary translation cache for the specified locales. This method attempts to pre-load
     * translations for each provided locale in order to improve runtime performance.
     *
     * If no locales are provided, the warm-up process is skipped, and a warning is logged.
     * Any failures during the warm-up process for a specific locale are logged as warnings.
     *
     * @param locales an iterable collection of locales for which the binary translation cache should be warmed up
     */
    @Override
    public void warmUp(Iterable<Locale> locales) {
        logger.info("Warming up binary translation cache for locales");
        if (!locales.iterator().hasNext()) {
            logger.warn("No locales provided for warm-up, skipping");
            return;
        }

        int count = 0;
        for (Locale locale : locales) {
            try {
                logger.debug("Warming up binary translations for locale '{}'", locale);
                getTranslations(locale);
                count++;
            } catch (Exception e) {
                logger.warn("Failed to warm up binary translations for locale '{}': {}", locale, e.getMessage());
            }
        }
        logger.info("Binary translation cache warmed up for {} locales", count);
    }

    /**
     * Retrieves translations for the given locale, utilizing a caching mechanism
     * to improve performance.
     *
     * If the translations for the specified locale are not present in the cache
     * or the cache has expired, this method attempts to load the translations
     * from a binary file. Once loaded, the translations are stored in the cache
     * for faster future retrieval.
     *
     * @param locale the {@link Locale} for which translations are to be retrieved
     * @return a {@link Map} where the keys are translation keys and the values
     *         are the corresponding translations for the specified locale. If
     *         translations are not available, an empty map is returned.
     */
    private Map<String, String> getTranslations(Locale locale) {
        String localeStr = locale.toString();

        // Check cache validity
        Instant timestamp = cacheTimestamps.get(localeStr);
        Duration cacheDuration = Duration.ofMinutes(30); // Default cache duration

        if (timestamp != null && Duration.between(timestamp, Instant.now()).compareTo(cacheDuration) < 0) {
            return cache.getOrDefault(localeStr, Map.of());
        }

        // Load binary file
        Map<String, String> localeCache = loadBinaryFile(locale);
        if (localeCache != null) {
            cache.put(localeStr, localeCache);
            cacheTimestamps.put(localeStr, Instant.now());
        } else {
            // Store empty map to avoid repeated failed attempts
            cache.put(localeStr, Map.of());
            cacheTimestamps.put(localeStr, Instant.now());
        }

        return localeCache != null ? localeCache : Map.of();
    }

    /**
     * Loads a binary translation file for the given locale. Attempts to load translations
     * specific to the full locale, and if none are found, falls back to the language-only locale.
     *
     * @param locale the locale for which translations should be loaded
     * @return a map of translation keys to their corresponding translations, or null if no translations are found
     */
    private Map<String, String> loadBinaryFile(Locale locale) {
        String localeStr = locale.toString();

        // Try with full locale first (e.g., en_US)
        Map<String, String> translations = loadBinaryFileForLocale(localeStr);

        // If nothing found and locale has country, try with language only (e.g., en)
        if (translations.isEmpty() && !locale.getCountry().isEmpty()) {
            translations = loadBinaryFileForLocale(locale.getLanguage());
        }

        logger.debug("Loaded {} binary translations for locale '{}'", translations.size(), locale);
        return translations.isEmpty() ? null : translations;
    }

    /**
     * Loads a binary file containing translations for a specified locale.
     * The file is expected to be in the format "basePath_localeStr.bin" and located in the classpath.
     * If the file is not found, an empty map is returned.
     *
     * @param localeStr the string representation of the locale for which the translations are to be loaded
     * @return a map of translations where the key is the original text and the value is the translated text;
     *         returns an empty map if the file is not found or an error occurs
     */
    private Map<String, String> loadBinaryFileForLocale(String localeStr) {
        try {
            // Use the same pattern as JSON implementation: basePath + "_" + locale + ".bin"
            // But we need to use classpath: prefix to load from classpath
            String resourcePath = "classpath:" + basePath + "_" + localeStr + ".bin";
            Resource resource = resourceLoader.getResource(resourcePath);

            logger.info("Trying to load binary file for locale '{}' at path: {}", localeStr, resourcePath);
            logger.info("Resource exists: {}", resource.exists());
            logger.info("Resource URI: {}", resource.getURI());

            if (!resource.exists()) {
                logger.debug("Binary file not found at path: {}", resourcePath);
                return Map.of();
            }

            logger.debug("Found binary file at path: {}", resourcePath);
            Map<String, String> translations = parseBinaryFile(resource);
            logger.info("Loaded {} translations from binary file for locale '{}'", translations.size(), localeStr);
            return translations;

        } catch (IOException e) {
            logger.warn("Failed to load binary file for locale '{}': {}", localeStr, e.getMessage());
            return Map.of();
        }
    }

    /**
     * Parses a binary file resource and extracts translations as key-value pairs.
     *
     * @param resource the resource representing the binary file to parse
     * @return a map containing translations extracted from the binary file, where
     *         each key is the translation hash and the value is the corresponding translation text
     * @throws IOException if an error occurs while accessing or reading the binary file
     */
    private Map<String, String> parseBinaryFile(Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream();
             ReadableByteChannel channel = Channels.newChannel(inputStream)) {

            logger.info("Parsing binary file: {}", resource.getURI());
            
            // Read and validate header
            if (!readAndValidateHeader(channel)) {
                throw new IOException("Invalid binary file format for resource: " + resource.getURI());
            }

            // Read entries
            Map<String, String> translations = readEntries(channel);
            logger.info("Successfully parsed {} translations from binary resource: {}", translations.size(), resource.getURI());
            return translations;
        }
    }

    /**
     * Reads and validates the header of a binary file from the provided channel.
     * The method checks for a valid magic number, supported version, and reads the locale string length
     * while skipping the actual locale string content. Returns true if the header is valid, otherwise false.
     *
     * @param channel the ReadableByteChannel from which the header data of the binary file will be read
     * @return true if the header is valid, otherwise false
     * @throws IOException if an I/O error occurs while reading from the channel
     */
    private boolean readAndValidateHeader(ReadableByteChannel channel) throws IOException {
        // Read magic number (4 bytes)
        ByteBuffer magicBuffer = ByteBuffer.allocate(4);
        if (channel.read(magicBuffer) != 4) {
            return false;
        }
        magicBuffer.flip();

        byte[] magic = new byte[4];
        magicBuffer.get(magic);

        if (!Arrays.equals(magic, MAGIC)) {
            return false;
        }

        // Read version (2 bytes)
        ByteBuffer versionBuffer = ByteBuffer.allocate(2).order(BYTE_ORDER);
        if (channel.read(versionBuffer) != 2) {
            return false;
        }
        versionBuffer.flip();
        short version = versionBuffer.getShort();

        if (version != 1) {
            return false; // Unsupported version
        }

        // Read locale length and string (we'll skip the locale for now)
        ByteBuffer localeLengthBuffer = ByteBuffer.allocate(2).order(BYTE_ORDER);
        if (channel.read(localeLengthBuffer) != 2) {
            return false;
        }
        localeLengthBuffer.flip();
        short localeLength = localeLengthBuffer.getShort();

        // Skip locale string
        ByteBuffer localeBuffer = ByteBuffer.allocate(localeLength);
        if (channel.read(localeBuffer) != localeLength) {
            return false;
        }

        return true;
    }

    /**
     * Reads key-value entries from a binary channel and returns them as a map.
     * The entries are sequentially read from the channel, with each key and value
     * being read as strings of specific byte lengths.
     *
     * @param channel the source channel from which the entries are read
     * @return a map containing the key-value pairs read from the channel
     * @throws IOException if an I/O error occurs while reading from the channel
     */
    private Map<String, String> readEntries(ReadableByteChannel channel) throws IOException {
        // Read entry count
        ByteBuffer countBuffer = ByteBuffer.allocate(4).order(BYTE_ORDER);
        if (channel.read(countBuffer) != 4) {
            throw new IOException("Failed to read entry count");
        }
        countBuffer.flip();
        int entryCount = countBuffer.getInt();

        Map<String, String> translations = new ConcurrentHashMap<>();

        // Read each entry
        for (int i = 0; i < entryCount; i++) {
            String hash = readString(channel, 2); // hash length is 2 bytes
            String translation = readString(channel, 4); // translation length is 4 bytes

            if (hash != null && translation != null) {
                translations.put(hash, translation);
            }
        }

        return translations;
    }

    /**
     * Reads a string from the provided readable byte channel. The string length is
     * determined by first reading the specified number of length bytes, followed by
     * reading the string content based on the parsed length.
     *
     * @param channel the ReadableByteChannel from which to read the string
     * @param lengthBytes the number of bytes used to represent the string length
     *                    (e.g., 2 for unsigned short, 4 for integer)
     * @return the string read from the channel
     * @throws IOException if an I/O error occurs while reading from the channel or
     *                     if the expected length of data cannot be read
     */
    private String readString(ReadableByteChannel channel, int lengthBytes) throws IOException {
        // Read string length
        ByteBuffer lengthBuffer = ByteBuffer.allocate(lengthBytes).order(BYTE_ORDER);
        if (channel.read(lengthBuffer) != lengthBytes) {
            throw new IOException("Failed to read string length");
        }
        lengthBuffer.flip();

        int length;
        if (lengthBytes == 2) {
            length = lengthBuffer.getShort() & 0xFFFF; // unsigned short
        } else {
            length = lengthBuffer.getInt();
        }

        if (length == 0) {
            return "";
        }

        // Read string bytes
        ByteBuffer stringBuffer = ByteBuffer.allocate(length);
        if (channel.read(stringBuffer) != length) {
            throw new IOException("Failed to read string content");
        }
        stringBuffer.flip();

        byte[] stringBytes = new byte[length];
        stringBuffer.get(stringBytes);

        return new String(stringBytes, "UTF-8");
    }
}