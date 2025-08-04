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
 * Message source that reads translations from binary files
 *
 * Supports the custom binary format created by BinaryOutputWriter
 */
public class BinaryNaturalTextMessageSource implements NaturalTextMessageSource {
    private static final Logger logger = LoggerFactory.getLogger(BinaryNaturalTextMessageSource.class);
    private static final byte[] MAGIC = "FL18".getBytes();
    private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    private final ResourceLoader resourceLoader;
    private final String basePath;
    private final Locale defaultLocale;
    private final Set<Locale> supportedLocales;
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    private final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();
    private final Map<String, Instant> cacheTimestamps = new ConcurrentHashMap<>();

    public BinaryNaturalTextMessageSource(ResourceLoader resourceLoader, String basePath, Set<Locale> supportedLocales, Locale defaultLocale) {
        this.resourceLoader = resourceLoader;
        this.basePath = basePath;
        this.defaultLocale = defaultLocale != null ? defaultLocale : Locale.ENGLISH;
        this.supportedLocales = supportedLocales != null ? supportedLocales : Set.of(defaultLocale);
        
        logger.info("BinaryNaturalTextMessageSource initialized with basePath: '{}'", basePath);
        logger.info("Supported locales: {}", supportedLocales);
        logger.info("Default locale: {}", defaultLocale);
    }

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

    @Override
    public boolean exists(String hash, Locale locale) {
        Map<String, String> localeCache = getTranslations(locale);

        if (localeCache != null) {
            String translation = localeCache.get(hash);
            return translation != null && !translation.isEmpty();
        }

        return false;
    }

    @Override
    public Iterable<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    @Override
    public void reload() {
        cache.clear();
        cacheTimestamps.clear();
        logger.info("Binary translation cache cleared");
    }

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