package io.github.unattendedflight.fluent.i18n.compiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BinaryOutputWriterTest {

    @Test
    void testBinaryOutputWriter(@TempDir Path tempDir) throws IOException {
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
        
        // Write binary file
        BinaryOutputWriter writer = new BinaryOutputWriter(config);
        Path outputFile = writer.write(data, "fr", tempDir);
        
        // Verify file exists
        assertTrue(Files.exists(outputFile));
        assertTrue(Files.size(outputFile) > 0);
        
        // Verify file name
        assertEquals("messages_fr.bin", outputFile.getFileName().toString());
        
        // Read and verify binary file
        byte[] fileContent = Files.readAllBytes(outputFile);
        
        // Check magic number
        assertEquals('F', fileContent[0]);
        assertEquals('L', fileContent[1]);
        assertEquals('1', fileContent[2]);
        assertEquals('8', fileContent[3]);
        
        // Check version (little endian)
        assertEquals(1, fileContent[4]);
        assertEquals(0, fileContent[5]);
        
        // Check locale length (little endian)
        assertEquals(2, fileContent[6]);
        assertEquals(0, fileContent[7]);
        
        // Check locale string
        assertEquals('f', fileContent[8]);
        assertEquals('r', fileContent[9]);
        
        // Check entry count (little endian)
        assertEquals(2, fileContent[10]);
        assertEquals(0, fileContent[11]);
        assertEquals(0, fileContent[12]);
        assertEquals(0, fileContent[13]);
        
        System.out.println("Binary file created successfully: " + outputFile);
        System.out.println("File size: " + Files.size(outputFile) + " bytes");
    }
} 