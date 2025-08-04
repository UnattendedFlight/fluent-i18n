package io.github.unattendedflight.fluent.i18n.maven;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base class for all Fluent i18n Maven goals
 */
public abstract class AbstractFluentI18nMojo extends AbstractMojo {
    
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    
    @Parameter(property = "fluent.i18n.supportedLocales", defaultValue = "en")
    protected String supportedLocales;
    
    @Parameter(property = "fluent.i18n.sourceDirectories")
    protected List<String> sourceDirectories;
    
    @Parameter(property = "fluent.i18n.poDirectory", defaultValue = "${project.basedir}/src/main/resources/i18n/po")
    protected File poDirectory;
    
    @Parameter(property = "fluent.i18n.outputDirectory", defaultValue = "${project.basedir}/src/main/resources/i18n")
    protected File outputDirectory;
    
    @Parameter(property = "fluent.i18n.encoding", defaultValue = "UTF-8")
    protected String encoding;
    
    @Parameter(property = "fluent.i18n.extractionPatterns")
    protected List<String> extractionPatterns;
    
    @Parameter(property = "fluent.i18n.outputFormat", defaultValue = "json")
    protected String outputFormat;
    
    @Parameter(property = "fluent.i18n.validateTranslations", defaultValue = "true")
    protected boolean validateTranslations;
    
    @Parameter(property = "fluent.i18n.preserveExisting", defaultValue = "true")
    protected boolean preserveExisting;
    
    @Parameter(property = "fluent.i18n.minifyOutput", defaultValue = "false")
    protected boolean minifyOutput;
    
    @Parameter(property = "fluent.i18n.includeMetadata", defaultValue = "true")
    protected boolean includeMetadata;
    
    @Parameter(property = "fluent.i18n.skip", defaultValue = "false")
    protected boolean skip;
    
    /**
     * Get supported locales as a set
     */
    protected Set<String> getSupportedLocalesSet() {
        return Arrays.stream(supportedLocales.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }
    
    /**
     * Get source directories, with defaults if not specified
     */
    protected List<Path> getSourceDirectoriesPaths() {
        if (sourceDirectories == null || sourceDirectories.isEmpty()) {
            return Arrays.asList(
                project.getBasedir().toPath().resolve("src/main/java"),
                project.getBasedir().toPath().resolve("src/main/resources"),
                project.getBasedir().toPath().resolve("src/test/java")
            );
        }
        
        return sourceDirectories.stream()
            .map(dir -> project.getBasedir().toPath().resolve(dir))
            .collect(Collectors.toList());
    }
    
    /**
     * Get extraction patterns with defaults
     */
    protected List<String> getExtractionPatternsList() {
        if (extractionPatterns == null || extractionPatterns.isEmpty()) {
            return Arrays.asList(
                "I18n\\.translate\\s*\\(\\s*\"([^\"]+)\"",
                "I18n\\.t\\s*\\(\\s*\"([^\"]+)\"",
                "I18n\\.context\\([^)]+\\)\\.translate\\s*\\(\\s*\"([^\"]+)\"",
                "@Translatable\\s*\\(\\s*\"([^\"]+)\"",
                "\\$\\{@i18n\\.translate\\('([^']+)'\\)\\}",
                "\\$\\{@i18n\\.t\\('([^']+)'\\)\\}"
            );
        }
        return extractionPatterns;
    }
    
    /**
     * Check if goal should be skipped
     */
    protected void checkSkip() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skipping fluent i18n goal execution");
            throw new MojoExecutionException("Goal execution skipped");
        }
    }
    
    /**
     * Log basic configuration
     */
    protected void logConfiguration() {
        getLog().info("Fluent i18n Configuration:");
        getLog().info("  Supported Locales: " + getSupportedLocalesSet());
        getLog().info("  Source Directories: " + getSourceDirectoriesPaths());
        getLog().info("  PO Directory: " + poDirectory);
        getLog().info("  Output Directory: " + outputDirectory);
        getLog().info("  Output Format: " + outputFormat);
        getLog().info("  Validate Translations: " + validateTranslations);
        getLog().info("  Source Encoding: " + encoding);
    }
    
    /**
     * Check if Spring configuration files exist
     */
    protected boolean hasSpringConfigurationFiles() {
        Path resourcesDir = project.getBasedir().toPath().resolve("src/main/resources");
        Path ymlFile = resourcesDir.resolve("application.yml");
        Path yamlFile = resourcesDir.resolve("application.yaml");
        Path propertiesFile = resourcesDir.resolve("application.properties");
        
        return java.nio.file.Files.exists(ymlFile) || java.nio.file.Files.exists(propertiesFile) || java.nio.file.Files.exists(yamlFile);
    }

    /**
     * Read configuration from application properties files
     */
    protected FluentI18nProperties readConfigProperties() throws IOException {
        FluentI18nConfigReader configReader = new FluentI18nConfigReader(getLog());
        return configReader.readConfiguration(Paths.get(project.getBasedir().getAbsolutePath()));
    }
    
    /**
     * Get configuration with application properties as primary source
     */
    protected FluentI18nProperties getConfiguration() throws IOException {
        // Always try to read from application properties first
        FluentI18nProperties config = readConfigProperties();
        
        // If no application config found, create default config
        if (config == null || !config.isEnabled()) {
            config = new FluentI18nProperties();
        }
        
        return config;
    }
}