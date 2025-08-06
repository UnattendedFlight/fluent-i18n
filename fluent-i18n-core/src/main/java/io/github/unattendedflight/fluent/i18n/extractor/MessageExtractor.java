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
 * Extracts translatable messages from source files based on configured patterns and source directories.
 * This class supports various types of extractors for handling different file formats and extraction methods.
 * It processes files recursively within the specified source directories, utilizes a collection of
 * configurable extractors to detect and extract messages, and generates a hash identifier for each message.
 */
public class MessageExtractor {
    /**
     * Represents the configuration used for message extraction in the {@code MessageExtractor} class.
     * This configuration defines various settings and parameters required to customize extraction behavior.
     * It is immutable and initialized through the constructor.
     */
    private final ExtractionConfig config;
    /**
     * A collection of {@link SourceExtractor} instances used for extracting messages
     * from various types of source files. Each extractor in the list implements the
     * {@link SourceExtractor} interface and is responsible for processing specific
     * file types and retrieving translation-related data.
     */
    private final List<SourceExtractor> extractors;
    /**
     * Component responsible for generating unique hash values for natural language text.
     * Used within the message extraction process to produce consistent and unique
     * identifiers for extracted messages, based on their content and optional context.
     * This allows efficient tracking, deduplication, and caching of messages during
     * the internationalization process.
     *
     * The implementation of this interface determines the specific hash generation
     * algorithm and may support custom contexts for increased specificity.
     *
     * @see HashGenerator
     */
    private final HashGenerator hashGenerator;
    /**
     * A thread-safe collection of discovered messages mapped by their unique hash.
     *
     * This variable serves as a repository for storing {@link ExtractedMessage} instances
     * identified during the extraction process. Each entry is keyed by a uniquely generated
     * hash string that represents the contextual and textual content of the message.
     *
     * The use of a {@link ConcurrentHashMap} ensures that the collection can safely
     * accommodate concurrent access, which is critical in multi-threaded extraction
     * operations where multiple threads may be processing source files simultaneously.
     */
    private final Map<String, ExtractedMessage> discoveredMessages = new ConcurrentHashMap<>();
    
    /**
     * Constructs a new instance of the MessageExtractor class.
     *
     * @param config the configuration object that specifies extraction-related settings
     */
    public MessageExtractor(ExtractionConfig config) {
        this.config = config;
        this.hashGenerator = new Sha256HashGenerator();
        this.extractors = createExtractors();
    }
    
    /**
     * Extracts internationalization messages from the configured source directories.
     * This method iterates through the specified source directories, processes the files
     * within them, and collects messages using the configured extraction mechanisms.
     *
     * It clears any previously discovered messages before starting the new extraction process.
     * After processing, it constructs an {@link ExtractionResult} containing the newly
     * extracted messages and the set of supported locales.
     *
     * @return an {@link ExtractionResult} encapsulating a map of extracted messages,
     *         where keys are unique message identifiers and values are {@link ExtractedMessage} objects,
     *         along with a set of supported locales.
     * @throws IOException if an I/O error occurs while reading files during the extraction process.
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
    
    /**
     * Processes all files in a given directory, extracting messages from those
     * files that meet specific criteria as defined by the `shouldProcessFile` method.
     * Each file that is eligible for processing is passed to the `extractFromFile` method.
     *
     * @param sourceDir the root directory containing files to process.
     * @throws IOException if an I/O error occurs while accessing the directory or files.
     */
    private void extractFromDirectory(Path sourceDir) throws IOException {
        try (Stream<Path> files = Files.walk(sourceDir)) {
            files.filter(this::shouldProcessFile)
                 .forEach(this::extractFromFile);
        }
    }
    
    /**
     * Determines whether a given file should be processed based on specific conditions.
     * The file must be a regular file and its name must match at least one of the file
     * patterns defined in the configuration.
     *
     * @param file the {@code Path} instance representing the file to evaluate
     * @return {@code true} if the file meets the criteria for processing; {@code false} otherwise
     */
    private boolean shouldProcessFile(Path file) {
        if (!Files.isRegularFile(file)) return false;
        
        String fileName = file.getFileName().toString();
        return config.getFilePatterns().stream()
            .anyMatch(fileName::matches);
    }
    
    /**
     * Extracts messages from the specified file, processes them using available extractors,
     * generates unique hashes for each message, and updates the discovered messages collection.
     *
     * @param file the path to the file to be processed for message extraction
     * @throws RuntimeException if an I/O error occurs while reading the file
     */
    private void extractFromFile(Path file) {
        try {
            String content = Files.readString(file, config.getSourceEncoding());
            String relativePath = config.getProjectRoot().relativize(file).toString();
            
            for (SourceExtractor extractor : extractors) {
                if (extractor.canProcess(file)) {
                    List<ExtractedMessage> messages = extractor.extract(content, relativePath);
                    for (ExtractedMessage message : messages) {
                      // Generate hash using contextKey for proper separation of contextual messages
                      String hash = message.getContextKey() != null ? 
                          hashGenerator.generateHash(message.getNaturalText(), message.getContextKey()) :
                          hashGenerator.generateHash(message.getNaturalText());
                      message.setHash(hash);
                      ExtractedMessage existing = discoveredMessages.get(hash);
                      if (existing != null) {
                          existing.addLocation(message.getLocations().getFirst());
                      } else {
                          discoveredMessages.put(hash, message);
                      }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process file: " + file, e);
        }
    }
    
    /**
     * Creates and returns a list of source extractors used for extracting messages
     * from various file types based on the configured patterns.
     *
     * The returned list includes:
     * - A Java method call extractor for extracting based on method call patterns.
     * - A Java annotation extractor for extracting based on annotation patterns.
     * - A template extractor for extracting from templates such as Thymeleaf or JSP.
     * - Any custom extractors specified in the configuration.
     *
     * @return a list of {@link SourceExtractor} instances configured for message extraction
     */
    private List<SourceExtractor> createExtractors() {
        List<SourceExtractor> extractors = new ArrayList<>();
        
        // Java method call extractor (now includes plural extraction)
        extractors.add(new JavaMethodCallExtractor(config.getMethodCallPatterns()));
        
        // Annotation extractor  
        extractors.add(new JavaAnnotationExtractor(config.getAnnotationPatterns()));
        
        // Template extractor (Thymeleaf, JSP, etc.)
        extractors.add(new TemplateExtractor(config.getTemplatePatterns()));
        
        // Add custom extractors
        extractors.addAll(config.getCustomExtractors());
        
        return extractors;
    }
}