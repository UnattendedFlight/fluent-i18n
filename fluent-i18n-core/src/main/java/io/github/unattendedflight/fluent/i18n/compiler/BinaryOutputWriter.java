package io.github.unattendedflight.fluent.i18n.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Writes translation data to a custom binary format for faster loading
 * 
 * Binary format specification:
 * - Magic number: 4 bytes "FL18" (Fluent i18n)
 * - Version: 1 byte (currently 2)
 * - Flags: 1 byte (bit 0: compressed, bit 1: fixed hash length)
 * - Locale length: VLQ + locale string
 * - Hash length: 1 byte (if fixed) or omitted (if variable)
 * - Entry count: VLQ
 * - For each entry:
 *   - Hash length: VLQ (if variable length hashes)
 *   - Hash string: variable length
 *   - Translation length: VLQ
 *   - Translation string: variable length
 */
public class BinaryOutputWriter implements OutputWriter {
    /**
     * Magic constant used as a file header identifier to ensure validity and compatibility of binary output files.
     * Encodes the format version and serves as a sanity check when reading files.
     * Changes here could break backward compatibility for file readers expecting "FL18".
     */
    private static final byte[] MAGIC = "FL18".getBytes();
    /**
     * Represents the binary data format version used in the output file format.
     * This ensures compatibility between the writer and any systems or tools
     * that read the generated binary data. Increment this version when making
     * breaking changes to the binary format to prevent unintended parsing issues.
     */
    private static final byte VERSION = 2;
    /**
     * Defines the byte order (endianness) used when writing binary data.
     *
     * Ensures consistent interpretation of multi-byte values, particularly when
     * interacting with systems or tools that may expect a specific endianness.
     * Assumed as LITTLE_ENDIAN since it's commonly used in most architectures.
     *
     * Changing this might cause incompatibilities with systems expecting this specific format.
     */
    private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    
    /**
     * Marks whether the output data is compressed. Used in the binary file format
     * header to ensure the reader correctly interprets the file's encoding.
     * Affects downstream processing, e.g., if decompression is needed.
     *
     * Critical for interoperability; ensure this is consistent with the actual
     * state of the data to prevent runtime errors or corruption during deserialization.
     */
    // Flag bits
    private static final byte FLAG_COMPRESSED = 0x01;
    /**
     * Indicates that the hash length for the binary output is fixed and must conform to a predetermined size.
     * Used to ensure consistent data structure during serialization/deserialization, which is critical for parsing
     * systems that expect uniform binary layouts. Avoids ambiguity in edge cases where variable hash lengths could
     * lead to unexpected data misalignment or errors in downstream processes.
     */
    private static final byte FLAG_FIXED_HASH_LENGTH = 0x02;
    
    /**
     * Defines configuration used for generating binary translation outputs.
     * Ensures consistent compilation behavior across multiple instances.
     * Critical to maintaining compatibility and adhering to specific
     * project requirements (e.g., fixed hash lengths or compression settings).
     */
    private final CompilerConfig config;
    /**
     * Indicates whether output data should be compressed when written to disk.
     *
     * Compression is typically enabled to reduce the size of binary output files,
     * which is especially crucial for large datasets or distribution over limited-bandwidth
     * environments. Disabling compression might be necessary in cases where performance
     * is prioritized over file size, or when compression-related overhead is undesired.
     *
     * Ensure downstream consumers of the output can handle compressed data if this is enabled.
     * Behavior may vary based on the implementation of the `compress` method.
     */
    private final boolean enableCompression;
    
    /**
     * Configures the binary output writer with the given compiler settings and default compression behavior.
     * Useful in cases where enabling compression is expected for typical use.
     *
     * @param config Compiler configuration containing settings necessary for output generation, such as target format rules. Assumes non-null input.
     */
    public BinaryOutputWriter(CompilerConfig config) {
        this(config, true);
    }
    
    /**
     * Configures a BinaryOutputWriter to compile translation data into a binary format.
     * Compression is optional but useful for reducing output size in production environments.
     *
     * @param config Compiler configuration that governs output generation specifics like
     *               encoding, validation, and formatting.
     * @param enableCompression Enables compression for the binary output. Adds processing cost,
     *                          so use judiciously in cases where speed outweighs size concerns.
     */
    public BinaryOutputWriter(CompilerConfig config, boolean enableCompression) {
        this.config = config;
        this.enableCompression = enableCompression;
    }
    
    /**
     * Writes translation data to a binary file format, optionally compressing the output.
     * Ensures output directories exist and generates an appropriately named file per locale.
     * Compression is applied if the configuration dictates so, which trades off file size for
     * increased processing time. Adheres to the BINARY output format.
     *
     * @param data the translation data to write, containing entries and associated metadata.
     * @param locale the locale code (e.g., "en", "fr") used to localize output file naming.
     * @param outputDirectory the directory where the binary file will be written. Must be writable.
     * @return the path to the file created, which includes the locale-specific name.
     * @throws IOException if an error occurs with file handling or directory creation.
     */
    @Override
    public Path write(TranslationData data, String locale, Path outputDirectory) throws IOException {
        Files.createDirectories(outputDirectory);
        Path outputFile = outputDirectory.resolve(OutputFormat.BINARY.getFileName(locale));
        
        // Generate binary data
        byte[] binaryData = generateBinaryData(data, locale);
        
        // Apply compression if enabled
        if (enableCompression) {
            binaryData = compress(binaryData);
        }
        
        // Write to file
        Files.write(outputFile, binaryData);
        
        return outputFile;
    }
    
    /**
     * Generates a binary representation of translation data for efficient storage and retrieval.
     * Leverages locale-specific data and metadata to create a format optimized for fast lookups.
     *
     * Ensures consistent binary structure by computing fixed hash lengths when applicable.
     * Handles edge cases such as empty translation entries or inconsistent hash lengths gracefully.
     *
     * @param data the translation data containing entries and metadata to be serialized.
     * @param locale the target locale, ensuring binary output aligns with the specified language/culture.
     * @return a byte array containing the serialized binary representation of the translation data.
     * @throws IOException if any errors occur during binary data generation, such as I/O stream issues.
     */
    private byte[] generateBinaryData(TranslationData data, String locale) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Map<String, TranslationEntry> entries = data.getEntries();
        
        // Analyze hash lengths to determine if they're all the same
        Integer fixedHashLength = getFixedHashLength(entries);
        
        // Write header
        writeHeader(baos, locale, fixedHashLength, entries.size());
        
        // Write entries
        writeEntries(baos, entries, fixedHashLength);
        
        return baos.toByteArray();
    }
    
    /**
     * Determines if all hash strings in the given map have a consistent byte length when encoded in UTF-8.
     * Returns the fixed length if all hashes are uniform, or null if hashes are of varying lengths.
     * An empty map will return null for clarity.
     *
     * This can be used in contexts where a fixed-length hash is required, such as binary serialization,
     * ensuring compatibility by rejecting inconsistent data.
     *
     * @param entries a map of hash strings (keys) to their translation entries (values)
     * @return the fixed length of the hashes if consistent, or null if lengths are variable or the map is empty
     */
    private Integer getFixedHashLength(Map<String, TranslationEntry> entries) {
        if (entries.isEmpty()) return null;
        
        Integer hashLength = null;
        for (String hash : entries.keySet()) {
            byte[] hashBytes = hash.getBytes(StandardCharsets.UTF_8);
            if (hashLength == null) {
                hashLength = hashBytes.length;
            } else if (hashLength != hashBytes.length) {
                return null; // Variable length hashes
            }
        }
        return hashLength;
    }
    
    /**
     * Writes a binary header to the specified output stream, including metadata and configuration flags.
     *
     * @param baos the output stream where the header will be written
     * @param locale the locale identifier used for the translation data (e.g., "en", "fr")
     * @param fixedHashLength optional, specifies a fixed length for hash values if defined, or null for variable length
     * @param entryCount the number of entries in the translation dataset; affects header size
     * @throws IOException if an error occurs while writing to the output stream
     *
     * Flags and format are impacted by `enableCompression` and `fixedHashLength`.
     * Ensures compatibility with downstream consumers relying on specific binary formats.
     * Locale is UTF-8 encoded to support internationalization.
     * A fixedHashLength requires non-null to signal fixed-length entry encoding.
     */
    private void writeHeader(ByteArrayOutputStream baos, String locale, Integer fixedHashLength, int entryCount) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024).order(BYTE_ORDER);
        
        // Magic number
        buffer.put(MAGIC);
        
        // Version
        buffer.put(VERSION);
        
        // Flags
        byte flags = 0;
        if (enableCompression) flags |= FLAG_COMPRESSED;
        if (fixedHashLength != null) flags |= FLAG_FIXED_HASH_LENGTH;
        buffer.put(flags);
        
        // Locale
        byte[] localeBytes = locale.getBytes(StandardCharsets.UTF_8);
        writeVLQ(buffer, localeBytes.length);
        buffer.put(localeBytes);
        
        // Fixed hash length (if applicable)
        if (fixedHashLength != null) {
            buffer.put(fixedHashLength.byteValue());
        }
        
        // Entry count
        writeVLQ(buffer, entryCount);
        
        // Write buffer to output stream
        buffer.flip();
        byte[] headerBytes = new byte[buffer.remaining()];
        buffer.get(headerBytes);
        baos.write(headerBytes);
    }
    
    /**
     * Writes translation entries into the provided output stream in a specific binary format,
     * ensuring efficient memory usage and proper encoding.
     *
     * @param baos the output stream to write the encoded entries to; handles buffered writes for performance
     * @param entries a map where keys represent hash values (e.g., translation keys), and values are translation entries;
     *                entries must be non-null, and empty translations are normalized to an empty string
     * @param fixedHashLength the fixed length for hash values, or null if the hash length is variable;
     *                        ensures compatibility with different hash length configurations
     * @throws IOException if an error occurs during stream or buffer operations
     *
     * Handles edge cases like null translations by converting them to empty strings and ensures the buffer
     * is cleared before it overflows. `fixedHashLength` affects whether hash lengths are explicitly encoded,
     * optimizing storage for fixed-length configurations.
     */
    private void writeEntries(ByteArrayOutputStream baos, Map<String, TranslationEntry> entries, Integer fixedHashLength) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8192).order(BYTE_ORDER);
        
        for (Map.Entry<String, TranslationEntry> entry : entries.entrySet()) {
            String hash = entry.getKey();
            String translation = entry.getValue().getTranslation();
            if (translation == null) translation = "";
            
            byte[] hashBytes = hash.getBytes(StandardCharsets.UTF_8);
            byte[] translationBytes = translation.getBytes(StandardCharsets.UTF_8);
            
            // Ensure buffer has enough space
            int requiredSpace = 10 + hashBytes.length + translationBytes.length;
            if (buffer.remaining() < requiredSpace) {
                // Flush buffer
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                baos.write(data);
                buffer.clear();
            }
            
            // Hash length (only if variable)
            if (fixedHashLength == null) {
                writeVLQ(buffer, hashBytes.length);
            }
            
            // Hash
            buffer.put(hashBytes);
            
            // Translation length and content
            writeVLQ(buffer, translationBytes.length);
            buffer.put(translationBytes);
        }
        
        // Flush remaining buffer
        buffer.flip();
        if (buffer.hasRemaining()) {
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            baos.write(data);
        }
    }
    
    /**
     * Encodes an integer as a VLQ (Variable-Length Quantity) and writes it to the provided buffer.
     * Useful for efficiently encoding integers in binary formats where smaller values occupy fewer bytes.
     *
     * @param buffer the buffer to write the encoded value into; must have adequate capacity to store the resulting bytes
     * @param value the integer to encode, must be non-negative; handles overflow by shifting into multiple bytes
     */
    private void writeVLQ(ByteBuffer buffer, int value) {
        while (value >= 0x80) {
            buffer.put((byte) (value | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }
    
    /**
     * Compresses the given data using GZIP compression. Optimizes storage for large data sets
     * like translation binaries, while ensuring compatibility with downstream systems that
     * support GZIP-compressed content.
     *
     * Edge case: Handles empty or null input gracefully (throws an exception for null).
     * Efficient for repeated calls due to in-memory compression using a ByteArrayOutputStream.
     *
     * @param data the byte array to compress; must not be null
     * @return the compressed data as a byte array
     * @throws IOException if an I/O error occurs during compression
     */
    private byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(data);
        }
        return baos.toByteArray();
    }
    
    /**
     * Specifies that this writer outputs data in a binary format.
     * Useful for performance-critical applications or those requiring compact, non-human-readable files.
     *
     * @return the binary output format constant, ensuring correct file structure and extension for serialized data.
     */
    @Override
    public OutputFormat getOutputFormat() {
        return OutputFormat.BINARY;
    }
}