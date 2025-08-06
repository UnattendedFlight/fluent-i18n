package io.github.unattendedflight.fluent.i18n.compiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

class BinaryOutputWriterTest {

  private static final byte[] MAGIC = "FL18".getBytes();
  private static final byte FLAG_COMPRESSED = 0x01;
  private static final byte FLAG_FIXED_HASH_LENGTH = 0x02;

  @Test
  void testBinaryOutputWriterUncompressed(@TempDir Path tempDir) throws IOException {
    // Create test data
    Map<String, TranslationEntry> entries = new HashMap<>();
    entries.put("hash1", new TranslationEntry("Hello", "Bonjour", "test.java:1"));
    entries.put("hash2", new TranslationEntry("Goodbye", "Au revoir", "test.java:2"));

    PoMetadata metadata = new PoMetadata();
    metadata.setProjectVersion("1.0");
    metadata.setLanguage("fr");

    TranslationData data = new TranslationData(entries, metadata);

    // Create compiler config
    CompilerConfig config = CompilerConfig.builder()
        .outputDirectory(tempDir)
        .outputFormats(OutputFormat.BINARY);

    // Write binary file without compression
    BinaryOutputWriter writer = new BinaryOutputWriter(config, false);
    Path outputFile = writer.write(data, "fr", tempDir);

    // Verify file exists
    assertTrue(Files.exists(outputFile));
    assertTrue(Files.size(outputFile) > 0);

    // Verify file name
    assertEquals("messages_fr.bin", outputFile.getFileName().toString());

    // Read and verify binary file structure
    byte[] fileContent = Files.readAllBytes(outputFile);
    ByteBuffer buffer = ByteBuffer.wrap(fileContent).order(ByteOrder.LITTLE_ENDIAN);

    // Verify magic number
    byte[] magic = new byte[4];
    buffer.get(magic);
    assertArrayEquals(MAGIC, magic);

    // Verify version
    byte version = buffer.get();
    assertEquals(2, version);

    // Verify flags (should not have compression flag)
    byte flags = buffer.get();
    assertEquals(FLAG_FIXED_HASH_LENGTH, flags); // Should have fixed hash length since both hashes are same length

    // Verify locale
    int localeLength = readVLQ(buffer);
    assertEquals(2, localeLength);
    byte[] localeBytes = new byte[localeLength];
    buffer.get(localeBytes);
    assertEquals("fr", new String(localeBytes, StandardCharsets.UTF_8));

    // Verify fixed hash length
    int fixedHashLength = buffer.get() & 0xFF;
    assertEquals(5, fixedHashLength); // "hash1" and "hash2" are both 5 characters

    // Verify entry count
    int entryCount = readVLQ(buffer);
    assertEquals(2, entryCount);

    // Verify entries can be read
    Map<String, String> readEntries = new HashMap<>();
    for (int i = 0; i < entryCount; i++) {
      // Hash (fixed length, no length prefix)
      byte[] hashBytes = new byte[fixedHashLength];
      buffer.get(hashBytes);
      String hash = new String(hashBytes, StandardCharsets.UTF_8);

      // Translation
      int translationLength = readVLQ(buffer);
      byte[] translationBytes = new byte[translationLength];
      buffer.get(translationBytes);
      String translation = new String(translationBytes, StandardCharsets.UTF_8);

      readEntries.put(hash, translation);
    }

    // Verify content
    assertEquals(2, readEntries.size());
    assertEquals("Bonjour", readEntries.get("hash1"));
    assertEquals("Au revoir", readEntries.get("hash2"));

    System.out.println("Uncompressed binary file created successfully: " + outputFile);
    System.out.println("File size: " + Files.size(outputFile) + " bytes");
  }

  @Test
  void testBinaryOutputWriterCompressed(@TempDir Path tempDir) throws IOException {
    // Create test data
    Map<String, TranslationEntry> entries = new HashMap<>();
    entries.put("hash1", new TranslationEntry("Hello", "Bonjour", "test.java:1"));
    entries.put("hash2", new TranslationEntry("Goodbye", "Au revoir", "test.java:2"));

    PoMetadata metadata = new PoMetadata();
    metadata.setProjectVersion("1.0");
    metadata.setLanguage("fr");

    TranslationData data = new TranslationData(entries, metadata);

    // Create compiler config
    CompilerConfig config = CompilerConfig.builder()
        .outputDirectory(tempDir)
        .outputFormats(OutputFormat.BINARY);

    // Write binary file with compression (default)
    BinaryOutputWriter writer = new BinaryOutputWriter(config, true);
    Path outputFile = writer.write(data, "fr", tempDir);

    // Verify file exists
    assertTrue(Files.exists(outputFile));
    assertTrue(Files.size(outputFile) > 0);

    // Read compressed file
    byte[] compressedContent = Files.readAllBytes(outputFile);

    // Verify it's compressed (GZIP magic)
    assertEquals((byte) 0x1f, compressedContent[0]);
    assertEquals((byte) 0x8b, compressedContent[1]);

    // Decompress and verify content
    byte[] decompressedContent = decompress(compressedContent);
    ByteBuffer buffer = ByteBuffer.wrap(decompressedContent).order(ByteOrder.LITTLE_ENDIAN);

    // Verify magic number
    byte[] magic = new byte[4];
    buffer.get(magic);
    assertArrayEquals(MAGIC, magic);

    // Verify version
    byte version = buffer.get();
    assertEquals(2, version);

    System.out.println("Compressed binary file created successfully: " + outputFile);
    System.out.println("Compressed size: " + Files.size(outputFile) + " bytes");
    System.out.println("Decompressed size: " + decompressedContent.length + " bytes");
    double compressionRatio = (1.0 - (double)Files.size(outputFile) / decompressedContent.length) * 100;
    System.out.println("Compression ratio: " + String.format("%.1f%%", compressionRatio));

    // Note: For very small files, GZIP overhead can make compressed files larger
    // This is normal and expected for files under ~100 bytes
    if (compressionRatio < 0) {
      System.out.println("Note: Compression resulted in larger file due to GZIP overhead on small data");
    }
  }

  @Test
  void testBinaryOutputWriterVariableHashLength(@TempDir Path tempDir) throws IOException {
    // Create test data with different hash lengths
    Map<String, TranslationEntry> entries = new HashMap<>();
    entries.put("a", new TranslationEntry("Hello", "Bonjour", "test.java:1"));
    entries.put("verylonghashkey", new TranslationEntry("Goodbye", "Au revoir", "test.java:2"));

    PoMetadata metadata = new PoMetadata();
    metadata.setProjectVersion("1.0");
    metadata.setLanguage("fr");

    TranslationData data = new TranslationData(entries, metadata);

    // Create compiler config
    CompilerConfig config = CompilerConfig.builder()
        .outputDirectory(tempDir)
        .outputFormats(OutputFormat.BINARY);

    // Write binary file without compression
    BinaryOutputWriter writer = new BinaryOutputWriter(config, false);
    Path outputFile = writer.write(data, "fr", tempDir);

    // Read and verify binary file structure
    byte[] fileContent = Files.readAllBytes(outputFile);
    ByteBuffer buffer = ByteBuffer.wrap(fileContent).order(ByteOrder.LITTLE_ENDIAN);

    // Verify magic number
    byte[] magic = new byte[4];
    buffer.get(magic);
    assertArrayEquals(MAGIC, magic);

    // Verify version
    byte version = buffer.get();
    assertEquals(2, version);

    // Verify flags (should NOT have fixed hash length flag)
    byte flags = buffer.get();
    assertEquals(0, flags & FLAG_FIXED_HASH_LENGTH); // Should not have fixed hash length

    // Skip locale
    int localeLength = readVLQ(buffer);
    buffer.position(buffer.position() + localeLength);

    // No fixed hash length byte should be present since flag is not set
    // Entry count
    int entryCount = readVLQ(buffer);
    assertEquals(2, entryCount);

    // Verify entries can be read with variable hash lengths
    Map<String, String> readEntries = new HashMap<>();
    for (int i = 0; i < entryCount; i++) {
      // Hash length prefix + hash
      int hashLength = readVLQ(buffer);
      byte[] hashBytes = new byte[hashLength];
      buffer.get(hashBytes);
      String hash = new String(hashBytes, StandardCharsets.UTF_8);

      // Translation
      int translationLength = readVLQ(buffer);
      byte[] translationBytes = new byte[translationLength];
      buffer.get(translationBytes);
      String translation = new String(translationBytes, StandardCharsets.UTF_8);

      readEntries.put(hash, translation);
    }

    // Verify content
    assertEquals(2, readEntries.size());
    assertEquals("Bonjour", readEntries.get("a"));
    assertEquals("Au revoir", readEntries.get("verylonghashkey"));

    System.out.println("Variable hash length binary file created successfully: " + outputFile);
    System.out.println("File size: " + Files.size(outputFile) + " bytes");
  }

  @Test
  void testEmptyTranslationData(@TempDir Path tempDir) throws IOException {
    // Create empty test data
    Map<String, TranslationEntry> entries = new HashMap<>();
    PoMetadata metadata = new PoMetadata();
    metadata.setProjectVersion("1.0");
    metadata.setLanguage("en");

    TranslationData data = new TranslationData(entries, metadata);

    // Create compiler config
    CompilerConfig config = CompilerConfig.builder()
        .outputDirectory(tempDir)
        .outputFormats(OutputFormat.BINARY);

    // Write binary file
    BinaryOutputWriter writer = new BinaryOutputWriter(config, false);
    Path outputFile = writer.write(data, "en", tempDir);

    // Verify file exists and has valid structure
    assertTrue(Files.exists(outputFile));
    assertTrue(Files.size(outputFile) > 0);

    byte[] fileContent = Files.readAllBytes(outputFile);
    ByteBuffer buffer = ByteBuffer.wrap(fileContent).order(ByteOrder.LITTLE_ENDIAN);

    // Verify magic
    byte[] magic = new byte[4];
    buffer.get(magic);
    assertArrayEquals(MAGIC, magic);

    // Skip to entry count
    buffer.get(); // version
    buffer.get(); // flags
    int localeLength = readVLQ(buffer);
    buffer.position(buffer.position() + localeLength);

    // Entry count should be 0
    int entryCount = readVLQ(buffer);
    assertEquals(0, entryCount);

    System.out.println("Empty binary file created successfully: " + outputFile);
    System.out.println("File size: " + Files.size(outputFile) + " bytes");
  }

  @Test
  void testBinaryOutputWriterCompressionBenefits(@TempDir Path tempDir) throws IOException {
    // Create test data with more entries to demonstrate compression benefits
    Map<String, TranslationEntry> entries = new HashMap<>();
    for (int i = 0; i < 50; i++) {
      entries.put("message_key_" + i, new TranslationEntry(
          "This is a sample message number " + i + " that contains some text",
          "Ceci est un exemple de message numéro " + i + " qui contient du texte",
          "test.java:" + i
      ));
    }

    PoMetadata metadata = new PoMetadata();
    metadata.setProjectVersion("1.0");
    metadata.setLanguage("fr");

    TranslationData data = new TranslationData(entries, metadata);

    // Create compiler config
    CompilerConfig config = CompilerConfig.builder()
        .outputDirectory(tempDir)
        .outputFormats(OutputFormat.BINARY);

    // Write uncompressed file
    BinaryOutputWriter uncompressedWriter = new BinaryOutputWriter(config, false);
    Path uncompressedFile = uncompressedWriter.write(data, "fr_uncompressed", tempDir);

    // Write compressed file
    BinaryOutputWriter compressedWriter = new BinaryOutputWriter(config, true);
    Path compressedFile = compressedWriter.write(data, "fr_compressed", tempDir);

    long uncompressedSize = Files.size(uncompressedFile);
    long compressedSize = Files.size(compressedFile);
    double compressionRatio = (1.0 - (double)compressedSize / uncompressedSize) * 100;

    System.out.println("Large data set compression test:");
    System.out.println("Uncompressed size: " + uncompressedSize + " bytes");
    System.out.println("Compressed size: " + compressedSize + " bytes");
    System.out.println("Compression ratio: " + String.format("%.1f%%", compressionRatio));

    // With larger data, compression should be beneficial
    assertTrue(compressionRatio > 0, "Compression should reduce file size for larger data sets");
    assertTrue(compressionRatio > 20, "Should achieve at least 20% compression on repetitive text data");
  }

  @Test
  void testGetOutputFormat() {
    CompilerConfig config = CompilerConfig.builder()
        .outputFormats(OutputFormat.BINARY);

    BinaryOutputWriter writer = new BinaryOutputWriter(config);
    assertEquals(OutputFormat.BINARY, writer.getOutputFormat());
  }

  private int readVLQ(ByteBuffer buffer) {
    int value = 0;
    int shift = 0;
    byte b;
    do {
      b = buffer.get();
      value |= (b & 0x7F) << shift;
      shift += 7;
    } while ((b & 0x80) != 0);
    return value;
  }

  private byte[] decompress(byte[] compressedData) throws IOException {
    try (GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(compressedData))) {
      return gzipIn.readAllBytes();
    }
  }
}