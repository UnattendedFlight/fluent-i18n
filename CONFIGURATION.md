# Fluent i18n Configuration Guide

This guide covers all configuration options for Fluent i18n, including Maven plugin settings, application properties, and advanced configuration.

## Table of Contents

1. [Maven Plugin Configuration](#maven-plugin-configuration)
2. [Application Properties](#application-properties)
3. [Spring Boot Auto-Configuration](#spring-boot-auto-configuration)
4. [Custom Configuration](#custom-configuration)
5. [Environment-Specific Settings](#environment-specific-settings)

## Maven Plugin Configuration

### Basic Configuration

```xml
<plugin>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-maven-plugin</artifactId>
    <version>0.1.5</version>
    <executions>
        <execution>
            <goals>
                <goal>extract</goal>
                <goal>compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Advanced Configuration

```xml
<plugin>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-maven-plugin</artifactId>
    <version>0.1.5</version>
    <configuration>
        <!-- PO file directory -->
        <poDirectory>src/main/resources/i18n/po</poDirectory>
        
        <!-- Output directory for compiled translations -->
        <outputDirectory>src/main/resources/i18n</outputDirectory>
        
        <!-- Output formats -->
        <outputFormats>
            <format>json</format>
            <format>properties</format>
            <format>binary</format>
        </outputFormats>
        
        <!-- Supported locales -->
        <supportedLocales>
            <locale>en</locale>
            <locale>es</locale>
            <locale>fr</locale>
            <locale>de</locale>
        </supportedLocales>
        
        <!-- Source directories to scan -->
        <sourceDirectories>
            <sourceDirectory>src/main/java</sourceDirectory>
            <sourceDirectory>src/main/resources</sourceDirectory>
        </sourceDirectories>
        
        <!-- File patterns to include -->
        <filePatterns>
            <filePattern>.*\\.java$</filePattern>
            <filePattern>.*\\.html$</filePattern>
            <filePattern>.*\\.jsp$</filePattern>
        </filePatterns>
        
        <!-- File patterns to exclude -->
        <excludePatterns>
            <excludePattern>.*/test/.*</excludePattern>
            <excludePattern>.*/generated/.*</excludePattern>
        </excludePatterns>
        
        <!-- Hash generation settings -->
        <hashAlgorithm>SHA-256</hashAlgorithm>
        <includeContextInHash>true</includeContextInHash>
        
        <!-- Compilation settings -->
        <validatePoFiles>true</validatePoFiles>
        <preserveExistingTranslations>true</preserveExistingTranslations>
        <minifyOutput>false</minifyOutput>
        
        <!-- Logging -->
        <verbose>false</verbose>
        <logMissingTranslations>true</logMissingTranslations>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>extract</goal>
                <goal>compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Plugin Goals

#### Extract Goal

Extracts translatable messages from source code:

```bash
mvn fluent-i18n:extract
```

**Configuration options:**
- `poDirectory`: Directory for PO files
- `sourceDirectories`: Directories to scan
- `filePatterns`: File patterns to include
- `excludePatterns`: File patterns to exclude
- `hashAlgorithm`: Hash generation algorithm
- `includeContextInHash`: Include context in hash generation

#### Compile Goal

Compiles PO files to runtime formats:

```bash
mvn fluent-i18n:compile
```

**Configuration options:**
- `outputDirectory`: Output directory for compiled files
- `outputFormats`: Output formats (json, properties, binary)
- `supportedLocales`: Locales to compile
- `validatePoFiles`: Validate PO file syntax
- `preserveExistingTranslations`: Preserve existing translations
- `minifyOutput`: Minify output files

#### Validate Goal

Validates PO files and configuration:

```bash
mvn fluent-i18n:validate
```

## Application Properties

### Basic Configuration

```yaml
fluent:
  i18n:
    enabled: true
    default-locale: en
    supported-locales:
      - en
      - es
      - fr
      - de
```

### Message Source Configuration

```yaml
fluent:
  i18n:
    message-source:
      type: json                    # json, properties, or binary
      basename: i18n/messages
      cache-duration: PT1H          # ISO 8601 duration
      use-original-as-fallback: true
      log-missing-translations: true
      encoding: UTF-8
      reloadable: false
```

**Message Source Types:**

- **json**: JSON format (recommended for web applications)
- **properties**: Properties format
- **binary**: Binary format (high performance, smaller size)

### Compilation Configuration

```yaml
fluent:
  i18n:
    compilation:
      output-format: json           # json, properties, binary, or comma-seperated string thereof
      validation: true
      preserve-existing: true
      minify-output: false
      encoding: UTF-8
```

### Warm-up Configuration

Pre-load locales into memory at startup:

```yaml
fluent:
  i18n:
    warm-up:
      enabled: true
      locales:                      # Specific locales to warm up
        - en
        - es
      # If locales is empty, all supported locales will be loaded
```

### Web Configuration

Spring Boot web integration settings:

```yaml
fluent:
  i18n:
    web:
      enabled: true
      locale-parameter: lang        # URL parameter for locale
      use-accept-language-header: true
      use-session: true
      session-attribute-name: fluent-i18n-locale
      cookie-name: fluent-i18n-locale
      cookie-max-age: 86400        # 24 hours in seconds
      default-time-zone: UTC
```

### Logging Configuration

```yaml
fluent:
  i18n:
    logging:
      level: INFO                   # DEBUG, INFO, WARN, ERROR
      log-missing-translations: true
      log-extraction-details: false
      log-compilation-details: false
```

## Spring Boot Auto-Configuration

### Auto-Configuration Properties

The Spring Boot starter provides automatic configuration with sensible defaults:

```yaml
# Auto-configuration can be disabled
fluent:
  i18n:
    auto-configuration:
      enabled: true
      web-configuration: true
      message-source-configuration: true
```

### Custom Message Source Bean

Override the default message source:

```java
@Configuration
public class I18nConfiguration {
    
    @Bean
    @Primary
    public NaturalTextMessageSource customMessageSource() {
        return new JsonNaturalTextMessageSource(
            "i18n/messages",
            Set.of(Locale.ENGLISH, new Locale("es"), new Locale("fr")),
            Locale.ENGLISH
        );
    }
}
```

### Custom Locale Resolver

```java
@Configuration
public class LocaleConfiguration {
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }
    
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
}
```

## Custom Configuration

### Custom Hash Generator

```java
@Component
public class CustomHashGenerator implements HashGenerator {
    
    @Override
    public String generateHash(String text) {
        // Custom hashing logic
        return DigestUtils.sha256Hex(text);
    }
}
```

### Custom Message Source

```java
@Component
public class CustomMessageSource implements NaturalTextMessageSource {
    
    @Override
    public TranslationResult resolve(String hash, String naturalText, Locale locale) {
        // Custom resolution logic
        // Return translation result
    }
}
```

### Custom Extractor

```java
@Component
public class CustomExtractor implements SourceExtractor {
    
    @Override
    public boolean canProcess(Path file) {
        return file.getFileName().toString().endsWith(".custom");
    }
    
    @Override
    public List<ExtractedMessage> extract(String content, String relativePath) {
        // Custom extraction logic
        return extractedMessages;
    }
}
```

## Environment-Specific Settings

### Development Environment

```yaml
# application-dev.yml
fluent:
  i18n:
    logging:
      level: DEBUG
      log-missing-translations: true
    compilation:
      validation: true
      preserve-existing: false
    web:
      enabled: true
```

### Production Environment

```yaml
# application-prod.yml
fluent:
  i18n:
    logging:
      level: WARN
      log-missing-translations: false
    compilation:
      validation: false
      preserve-existing: true
      minify-output: true
    warm-up:
      enabled: true
      locales:
        - en
        - es
        - fr
```

### Testing Environment

```yaml
# application-test.yml
fluent:
  i18n:
    enabled: false  # Disable for unit tests
    logging:
      level: ERROR
```

## Configuration Validation

### Maven Plugin Validation

```bash
# Validate plugin configuration
mvn fluent-i18n:validate

# Check for configuration issues
mvn fluent-i18n:validate -Dverbose=true
```

### Application Properties Validation

The Spring Boot starter validates configuration on startup:

```java
@ConfigurationProperties(prefix = "fluent.i18n")
@Validated
public class FluentI18nProperties {
    
    @NotNull
    private Locale defaultLocale;
    
    @NotEmpty
    private Set<Locale> supportedLocales;
    
    // Validation annotations ensure proper configuration
}
```

## Troubleshooting Configuration

### Common Issues

1. **PO files not generated**: Check source directories and file patterns
2. **Translations not loading**: Verify output directory and file formats
3. **Locale not switching**: Check web configuration and interceptors
4. **Performance issues**: Enable warm-up and use binary format

### Debug Configuration

Enable debug logging:

```yaml
logging:
  level:
    io.github.unattendedflight.fluent.i18n: DEBUG
    io.github.unattendedflight.fluent.i18n.extractor: DEBUG
    io.github.unattendedflight.fluent.i18n.compiler: DEBUG
```

### Configuration Testing

Test your configuration:

```bash
# Test extraction
mvn fluent-i18n:extract -Dverbose=true

# Test compilation
mvn fluent-i18n:compile -Dverbose=true

# Test validation
mvn fluent-i18n:validate
```

---

For more information, see the [README.md](README.md) for quick start examples or check the [examples directory](../fluent-i18n-examples/) for complete working applications. 