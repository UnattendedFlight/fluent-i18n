package io.github.unattendedflight.fluent.i18n.extractor;

import io.github.unattendedflight.fluent.i18n.core.HashGenerator;
import io.github.unattendedflight.fluent.i18n.core.Sha256HashGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Main orchestrator for extracting translatable messages from source code
 */
public class MessageExtractor {
    private final ExtractionConfig config;
    private final List<SourceExtractor> extractors;
    private final HashGenerator hashGenerator;
    private final Map<String, ExtractedMessage> discoveredMessages = new ConcurrentHashMap<>();
    
    public MessageExtractor(ExtractionConfig config) {
        this.config = config;
        this.hashGenerator = new Sha256HashGenerator();
        this.extractors = createExtractors();
    }
    
    /**
     * Extract messages from all configured source directories
     */
    public ExtractionResult extract() throws IOException {
        discoveredMessages.clear();
        
        for (Path sourceDir : config.getSourceDirectories()) {
            if (Files.exists(sourceDir)) {
                extractFromDirectory(sourceDir);
            }
        }
        
        return new ExtractionResult(
            new HashMap<>(discoveredMessages),
            config.getSupportedLocales()
        );
    }
    
    private void extractFromDirectory(Path sourceDir) throws IOException {
        try (Stream<Path> files = Files.walk(sourceDir)) {
            files.filter(this::shouldProcessFile)
                 .forEach(this::extractFromFile);
        }
    }
    
    private boolean shouldProcessFile(Path file) {
        if (!Files.isRegularFile(file)) return false;
        
        String fileName = file.getFileName().toString();
        return config.getFilePatterns().stream()
            .anyMatch(pattern -> fileName.matches(pattern));
    }
    
    private void extractFromFile(Path file) {
        try {
            String content = Files.readString(file, config.getSourceEncoding());
            String relativePath = config.getProjectRoot().relativize(file).toString();
            
            System.out.println("MessageExtractor: Processing file: " + relativePath);
            
            for (SourceExtractor extractor : extractors) {
                if (extractor.canProcess(file)) {
                    System.out.println("MessageExtractor: Using extractor: " + extractor.getClass().getSimpleName() + " for " + relativePath);
                    List<ExtractedMessage> messages = extractor.extract(content, relativePath);
                    for (ExtractedMessage message : messages) {
                        if (message.getType() == MessageType.PLURAL && message.getContext() != null && message.getContext().startsWith("plural:")) {
                            // For plural messages, generate hash from the complete ICU MessageFormat string
                            // The naturalText will be replaced with the ICU format in PoFileGenerator
                            String hash = hashGenerator.generateHash(message.getNaturalText());
                            message.setHash(hash);
                            
                            ExtractedMessage existing = discoveredMessages.get(hash);
                            if (existing != null) {
                                existing.addLocation(message.getLocations().get(0));
                            } else {
                                discoveredMessages.put(hash, message);
                            }
                        } else {
                            // Handle regular messages
                            String hash = hashGenerator.generateHash(message.getNaturalText());
                            message.setHash(hash);
                            
                            ExtractedMessage existing = discoveredMessages.get(hash);
                            if (existing != null) {
                                existing.addLocation(message.getLocations().get(0));
                            } else {
                                discoveredMessages.put(hash, message);
                            }
                        }
                    }
                } else {
                    System.out.println("MessageExtractor: Skipping extractor: " + extractor.getClass().getSimpleName() + " for " + relativePath);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process file: " + file, e);
        }
    }
    
    private List<SourceExtractor> createExtractors() {
        List<SourceExtractor> extractors = new ArrayList<>();
        
        // Java method call extractor (now includes plural extraction)
        extractors.add(new JavaMethodCallExtractor(config.getMethodCallPatterns()));
        
        // Annotation extractor  
        extractors.add(new JavaAnnotationExtractor(config.getAnnotationPatterns()));
        
        // Template extractor (Thymeleaf, JSP, etc.)
        extractors.add(new TemplateExtractor(config.getTemplatePatterns()));
        
        // Add custom extractors
        config.getCustomExtractors().forEach(extractors::add);
        
        return extractors;
    }
}