package io.github.unattendedflight.fluent.i18n.compiler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

/**
 * Configuration for translation compilation
 */
public class CompilerConfig {
    private Path poDirectory = Path.of("src/main/resources/i18n/po");
    private Path outputDirectory = Path.of("src/main/resources/i18n");
    private Set<String> supportedLocales = Set.of("en", "nb", "sv");
    private Set<OutputFormat> outputFormats = EnumSet.of(OutputFormat.JSON);
    private Charset encoding = StandardCharsets.UTF_8;
    private boolean preserveExisting = true;
    private boolean validateTranslations = true;
    private boolean minifyOutput = false;
    private boolean includeMetadata = true;
    
    // Builder pattern
    public static CompilerConfig builder() {
        return new CompilerConfig();
    }
    
    public CompilerConfig poDirectory(Path directory) {
        this.poDirectory = directory;
        return this;
    }
    
    public CompilerConfig outputDirectory(Path directory) {
        this.outputDirectory = directory;
        return this;
    }
    
    public CompilerConfig supportedLocales(Set<String> locales) {
        this.supportedLocales = Set.copyOf(locales);
        return this;
    }
    
    public CompilerConfig outputFormats(OutputFormat... formats) {
        this.outputFormats = EnumSet.of(formats[0], formats);
        return this;
    }
    
    public CompilerConfig minifyOutput(boolean minify) {
        this.minifyOutput = minify;
        return this;
    }
    
    public CompilerConfig validateTranslations(boolean validate) {
        this.validateTranslations = validate;
        return this;
    }
    
    // Getters
    public Path getPoDirectory() { return poDirectory; }
    public Path getOutputDirectory() { return outputDirectory; }
    public Set<String> getSupportedLocales() { return supportedLocales; }
    public Set<OutputFormat> getOutputFormats() { return outputFormats; }
    public Charset getEncoding() { return encoding; }
    public boolean isPreserveExisting() { return preserveExisting; }
    public boolean isValidateTranslations() { return validateTranslations; }
    public boolean isMinifyOutput() { return minifyOutput; }
    public boolean isIncludeMetadata() { return includeMetadata; }
}