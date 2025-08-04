package io.github.unattendedflight.fluent.i18n.compiler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * Writes translation data to a custom binary format for faster loading
 * 
 * Binary format specification:
 * - Magic number: 4 bytes "FL18" (Fluent i18n)
 * - Version: 2 bytes (currently 1)
 * - Locale length: 2 bytes
 * - Locale string: variable length
 * - Entry count: 4 bytes
 * - For each entry:
 *   - Hash length: 2 bytes
 *   - Hash string: variable length
 *   - Translation length: 4 bytes
 *   - Translation string: variable length
 */
public class BinaryOutputWriter implements OutputWriter {
    private static final byte[] MAGIC = "FL18".getBytes();
    private static final short VERSION = 1;
    private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    
    private final CompilerConfig config;
    
    public BinaryOutputWriter(CompilerConfig config) {
        this.config = config;
    }
    
    @Override
    public Path write(TranslationData data, String locale, Path outputDirectory) throws IOException {
        Files.createDirectories(outputDirectory);
        Path outputFile = outputDirectory.resolve(OutputFormat.BINARY.getFileName(locale));
        
        try (FileChannel channel = FileChannel.open(outputFile, 
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            
            // Write header
            writeHeader(channel, locale);
            
            // Write entries
            writeEntries(channel, data);
        }
        
        return outputFile;
    }
    
    private void writeHeader(FileChannel channel, String locale) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8 + locale.length()).order(BYTE_ORDER);
        
        // Magic number
        buffer.put(MAGIC);
        
        // Version
        buffer.putShort(VERSION);
        
        // Locale length and string
        byte[] localeBytes = locale.getBytes("UTF-8");
        buffer.putShort((short) localeBytes.length);
        buffer.put(localeBytes);
        
        buffer.flip();
        channel.write(buffer);
    }
    
    private void writeEntries(FileChannel channel, TranslationData data) throws IOException {
        Map<String, TranslationEntry> entries = data.getEntries();
        
        // Write entry count
        ByteBuffer countBuffer = ByteBuffer.allocate(4).order(BYTE_ORDER);
        countBuffer.putInt(entries.size());
        countBuffer.flip();
        channel.write(countBuffer);
        
        // Write each entry
        for (Map.Entry<String, TranslationEntry> entry : entries.entrySet()) {
            writeEntry(channel, entry.getKey(), entry.getValue());
        }
    }
    
    private void writeEntry(FileChannel channel, String hash, TranslationEntry entry) throws IOException {
        String translation = entry.getTranslation();
        if (translation == null) {
            translation = ""; // Empty string for untranslated entries
        }
        
        byte[] hashBytes = hash.getBytes("UTF-8");
        byte[] translationBytes = translation.getBytes("UTF-8");
        
        // Calculate buffer size: hash length (2) + hash + translation length (4) + translation
        int bufferSize = 2 + hashBytes.length + 4 + translationBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize).order(BYTE_ORDER);
        
        // Hash length and string
        buffer.putShort((short) hashBytes.length);
        buffer.put(hashBytes);
        
        // Translation length and string
        buffer.putInt(translationBytes.length);
        buffer.put(translationBytes);
        
        buffer.flip();
        channel.write(buffer);
    }
    
    @Override
    public OutputFormat getOutputFormat() {
        return OutputFormat.BINARY;
    }
}