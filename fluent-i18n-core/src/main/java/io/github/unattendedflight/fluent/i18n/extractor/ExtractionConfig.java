package io.github.unattendedflight.fluent.i18n.extractor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * Configuration for message extraction
 */
public class ExtractionConfig {
    private Path projectRoot = Path.of(".");
    private List<Path> sourceDirectories = Arrays.asList(
        Path.of("src/main/java"),
        Path.of("src/main/resources")
    );
    private Set<String> supportedLocales = Set.of();
    private Charset sourceEncoding = StandardCharsets.UTF_8;
    
    // File patterns to process
    private List<String> filePatterns = Arrays.asList(
        ".*\\.java$",
        ".*\\.html$", 
        ".*\\.jsp$",
        ".*\\.jspx$"
    );
    
    // Extraction patterns
    private List<String> methodCallPatterns = new ArrayList<>(Arrays.asList(
        "I18n\\.translate\\s*\\(\\s*\"([^\"]+)\"",
        "I18n\\.describe\\s*\\(\\s*\"([^\"]+)\"",
        "I18n\\.t\\s*\\(\\s*\"([^\"]+)\"",
        "I18n\\.context\\([^)]+\\)\\.translate\\s*\\(\\s*\"([^\"]+)\""
    ));
    
    private List<String> annotationPatterns = new ArrayList<>(Arrays.asList(
        "@Translatable\\s*\\(\\s*\"([^\"]+)\"",
        "@Message\\s*\\(\\s*\"([^\"]+)\""
    ));
    
    private List<String> templatePatterns = new ArrayList<>(Arrays.asList(
        "\\$\\{@i18n\\.translate\\('([^']+)'\\)\\}",
        "\\$\\{@i18nTemplateUtils\\.translate\\('([^']+)'\\)\\}",
        "\\$\\{@i18nTemplateUtils\\.t\\('([^']+)'\\)\\}",
        "th:text=\"\\$\\{@i18n\\.translate\\('([^']+)'\\)\\}\"",
        "th:text=\"\\$\\{@i18nTemplateUtils\\.translate\\('([^']+)'\\)\\}\"",
        "th:text=\"\\$\\{@i18nTemplateUtils\\.t\\('([^']+)'\\)\\}\"",
        "<spring:message\\s+code=\"([^\"]+)\""
    ));
    
    // Plural patterns
    private List<String> pluralPatterns = new ArrayList<>(Arrays.asList(
        "\\.zero\\s*\\(\\s*\"([^\"]+)\"\\)",
        "\\.one\\s*\\(\\s*\"([^\"]+)\"\\)",
        "\\.two\\s*\\(\\s*\"([^\"]+)\"\\)",
        "\\.few\\s*\\(\\s*\"([^\"]+)\"\\)",
        "\\.many\\s*\\(\\s*\"([^\"]+)\"\\)",
        "\\.other\\s*\\(\\s*\"([^\"]+)\"\\)"
    ));
    
    private List<SourceExtractor> customExtractors = new ArrayList<>();
    
    // Builder methods
    public static ExtractionConfig builder() {
        return new ExtractionConfig();
    }
    
    public ExtractionConfig projectRoot(Path root) {
        this.projectRoot = root;
        return this;
    }
    
    public ExtractionConfig sourceDirectories(List<Path> dirs) {
        this.sourceDirectories = new ArrayList<>(dirs);
        return this;
    }
    
    public ExtractionConfig supportedLocales(Set<String> locales) {
        this.supportedLocales = new HashSet<>(locales);
        return this;
    }
    
    public ExtractionConfig sourceEncoding(Charset encoding) {
        this.sourceEncoding = encoding;
        return this;
    }
    
    public ExtractionConfig addMethodCallPattern(String pattern) {
        this.methodCallPatterns.add(pattern);
        return this;
    }
    
    public ExtractionConfig setMethodCallPatterns(List<String> patterns) {
        this.methodCallPatterns = new ArrayList<>(patterns);
        return this;
    }
    
    public ExtractionConfig addAnnotationPattern(String pattern) {
        this.annotationPatterns.add(pattern);
        return this;
    }
    
    public ExtractionConfig setAnnotationPatterns(List<String> patterns) {
        this.annotationPatterns = new ArrayList<>(patterns);
        return this;
    }
    
    public ExtractionConfig addTemplatePattern(String pattern) {
        this.templatePatterns.add(pattern);
        return this;
    }
    
    public ExtractionConfig setTemplatePatterns(List<String> patterns) {
        this.templatePatterns = new ArrayList<>(patterns);
        return this;
    }
    
    public ExtractionConfig addCustomExtractor(SourceExtractor extractor) {
        this.customExtractors.add(extractor);
        return this;
    }
    
    // Getters
    public Path getProjectRoot() { return projectRoot; }
    public List<Path> getSourceDirectories() { return sourceDirectories; }
    public Set<String> getSupportedLocales() { return supportedLocales; }
    public Charset getSourceEncoding() { return sourceEncoding; }
    public List<String> getFilePatterns() { return filePatterns; }
    public List<String> getMethodCallPatterns() { return methodCallPatterns; }
    public List<String> getAnnotationPatterns() { return annotationPatterns; }
    public List<String> getTemplatePatterns() { return templatePatterns; }
    public List<String> getPluralPatterns() { return pluralPatterns; }
    public List<SourceExtractor> getCustomExtractors() { return customExtractors; }

    @Override
    public String toString() {
      return "ExtractionConfig{\n" + "  projectRoot=" + projectRoot + ",\n" +
          "  sourceDirectories=" + sourceDirectories + ",\n" +
          "  supportedLocales=" + supportedLocales + ",\n" +
          "  sourceEncoding=" + sourceEncoding + ",\n" +
          "  filePatterns=" + filePatterns + ",\n" +
          "  methodCallPatterns=" + methodCallPatterns + ",\n" +
          "  annotationPatterns=" + annotationPatterns + ",\n" +
          "  templatePatterns=" + templatePatterns + ",\n" +
          "  pluralPatterns=" + pluralPatterns + ",\n" +
          "  customExtractors=" + customExtractors + "\n" +
          '}';
    }
}