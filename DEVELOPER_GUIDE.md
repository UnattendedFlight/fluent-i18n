# Fluent i18n Developer Guide

This guide is for developers who want to contribute to Fluent i18n or understand its internal architecture.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Project Structure](#project-structure)
3. [Core Components](#core-components)
4. [Extension Points](#extension-points)
5. [Development Setup](#development-setup)
6. [Contributing Guidelines](#contributing-guidelines)
7. [Testing](#testing)
8. [Release Process](#release-process)

## Architecture Overview

Fluent i18n follows a modular architecture with clear separation of concerns:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Development   │    │   Build Time    │    │   Runtime       │
│                 │    │                 │    │                 │
│ Natural Text    │───▶│ Message         │───▶│ Translation     │
│ in Code         │    │ Extraction      │    │ Resolution      │
│                 │    │                 │    │                 │
│ I18n.translate()│    │ PO Generation   │    │ Hash-based      │
│ Annotations     │    │ Compilation     │    │ Lookup          │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Key Design Principles

1. **Natural Language First**: Developers write code using natural language text
2. **Hash-Based Keys**: Invisible hash-based keys
3. **Standard Workflow**: Industry-standard PO file workflow for translators
4. **Multiple Formats**: Support for JSON, Properties, and Binary output formats
5. **Extensible**: Pluggable extractors, compilers, and message sources

## Project Structure

```
fluent-i18n/
├── fluent-i18n-core/                    # Core functionality
│   ├── src/main/java/
│   │   └── com/naturaltext/fluent/i18n/
│   │       ├── I18n.java               # Main entry point
│   │       ├── annotation/              # Annotations for static analysis
│   │       │   ├── Message.java
│   │       │   └── Translatable.java
│   │       ├── compiler/                # PO file compilation
│   │       │   ├── TranslationCompiler.java
│   │       │   ├── PoFileParser.java
│   │       │   └── OutputWriter.java
│   │       ├── core/                    # Core i18n functionality
│   │       │   ├── MessageFormatter.java
│   │       │   ├── NaturalTextMessageSource.java
│   │       │   ├── PluralBuilder.java
│   │       │   └── HashGenerator.java
│   │       ├── extractor/               # Message extraction
│   │       │   ├── MessageExtractor.java
│   │       │   ├── JavaAnnotationExtractor.java
│   │       │   └── TemplateExtractor.java
│   │       └── maven/                   # Maven plugin
│   │           ├── ExtractMojo.java
│   │           ├── CompileMojo.java
│   │           └── PoFileGenerator.java
│   └── src/test/java/                   # Unit tests
├── fluent-i18n-spring-boot-starter/     # Spring Boot integration
│   └── src/main/java/
│       └── com/naturaltext/fluent/i18n/springboot/
│           ├── FluentI18nAutoConfiguration.java
│           ├── FluentI18nProperties.java
│           └── FluentI18nWebInterceptor.java
└── fluent-i18n-examples/                # Example applications
    └── simple-web-app/
```

## Core Components

### 1. Main Entry Point (`I18n.java`)

The main entry point provides a fluent API for translation:

```java
public final class I18n {
    // Simple translation
    public static String translate(String naturalText, Object... args)
    
    // Short alias
    public static String t(String naturalText, Object... args)
    
    // Pluralization
    public static PluralBuilder plural(Number count)
    
    // Context-aware translation
    public static ContextBuilder context(String context)
    
    // Lazy evaluation
    public static MessageDescriptor describe(String naturalText, Object... args)
}
```

### 2. Message Extraction

The extraction system scans source code for translatable content:

```java
public class MessageExtractor {
    private final List<SourceExtractor> extractors;
    
    public ExtractionResult extract() throws IOException {
        // Scan source directories
        // Extract messages using configured extractors
        // Generate hashes for each message
        // Return extraction result
    }
}
```

**Supported Extractors:**
- `JavaAnnotationExtractor`: Extracts from `@Message` and `@Translatable` annotations
- `JavaMethodCallExtractor`: Extracts from `I18n.translate()` calls
- `TemplateExtractor`: Extracts from template files (HTML, JSP, etc.)

### 3. PO File Compilation

The compiler converts PO files to efficient runtime formats:

```java
public class TranslationCompiler {
    private final Map<OutputFormat, OutputWriter> writers;
    
    public CompilationResult compile() throws IOException {
        // Parse PO files
        // Convert to runtime format
        // Write output files
    }
}
```

**Supported Output Formats:**
- `JsonOutputWriter`: JSON format for web applications
- `PropertiesOutputWriter`: Properties format for legacy systems
- `BinaryOutputWriter`: Binary format for high performance

### 4. Message Sources

Runtime message resolution through different sources:

```java
public interface NaturalTextMessageSource {
    TranslationResult resolve(String hash, String naturalText, Locale locale);
}
```

**Implementations:**
- `JsonNaturalTextMessageSource`: JSON-based message source
- `PropertiesNaturalTextMessageSource`: Properties-based message source
- `BinaryNaturalTextMessageSource`: Binary-based message source

## Extension Points

### 1. Custom Extractors

Create custom extractors for specific file types or patterns:

```java
public class CustomExtractor implements SourceExtractor {
    
    @Override
    public boolean canProcess(Path file) {
        return file.getFileName().toString().endsWith(".custom");
    }
    
    @Override
    public List<ExtractedMessage> extract(String content, String relativePath) {
        // Parse custom file format
        // Extract translatable messages
        // Return list of extracted messages
    }
}
```

### 2. Custom Output Writers

Implement custom output formats:

```java
public class CustomOutputWriter implements OutputWriter {
    
    @Override
    public Path write(TranslationData data, String locale, Path outputDir) 
            throws IOException {
        // Convert translation data to custom format
        // Write to output file
        // Return output file path
    }
}
```

### 3. Custom Hash Generators

Implement custom hashing strategies:

```java
public class CustomHashGenerator implements HashGenerator {
    
    @Override
    public String generateHash(String text) {
        // Implement custom hashing logic
        return customHash(text);
    }
}
```

### 4. Custom Message Sources

Create custom message sources for specific requirements:

```java
public class CustomMessageSource implements NaturalTextMessageSource {
    
    @Override
    public TranslationResult resolve(String hash, String naturalText, Locale locale) {
        // Implement custom resolution logic
        // Return translation result
    }
}
```

## Development Setup

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Building from Source

```bash
# Clone the repository
git clone https://github.com/your-org/fluent-i18n.git
cd fluent-i18n

# Build the project
mvn clean install

# Run tests
mvn test

# Build with examples
mvn clean install -DskipTests
```

### Running Examples

```bash
# Run the simple web app example
cd fluent-i18n-examples/simple-web-app
mvn spring-boot:run

# Access the application
open http://localhost:8080
```

### Development Workflow

1. **Make Changes**: Modify code in the appropriate module
2. **Run Tests**: `mvn test` to ensure tests pass
3. **Build**: `mvn clean install` to build all modules
4. **Test Examples**: Run examples to verify functionality
5. **Create PR**: Submit pull request with clear description

## Contributing Guidelines

### Code Style

- Follow Java coding conventions
- Use meaningful variable and method names
- Add comprehensive JavaDoc for public APIs
- Keep methods focused and single-purpose
- Use appropriate access modifiers

### Testing Requirements

- Unit tests for all new functionality
- Integration tests for complex workflows
- Test coverage should be >80%
- Include both positive and negative test cases

### Commit Messages

Use conventional commit format:

```
type(scope): description

[optional body]

[optional footer]
```

Examples:
- `feat(extractor): add support for Kotlin files`
- `fix(compiler): resolve PO file parsing issue`
- `docs(readme): update installation instructions`

### Pull Request Process

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/amazing-feature`
3. **Commit** your changes: `git commit -m 'feat: add amazing feature'`
4. **Push** to the branch: `git push origin feature/amazing-feature`
5. **Open** a Pull Request with clear description

### Issue Reporting

When reporting issues, please include:

- **Environment**: Java version, Maven version, OS
- **Steps to reproduce**: Clear, step-by-step instructions
- **Expected behavior**: What you expected to happen
- **Actual behavior**: What actually happened
- **Additional context**: Logs, screenshots, etc.

## Testing

### Unit Tests

Each module has comprehensive unit tests:

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl fluent-i18n-core

# Run with coverage
mvn test jacoco:report
```

### Integration Tests

Integration tests verify end-to-end workflows:

```bash
# Run integration tests
mvn verify -P integration-test

# Test with different configurations
mvn test -Dtest=*IntegrationTest
```

### Manual Testing

Test the complete workflow:

```bash
# 1. Build the project
mvn clean install

# 2. Run examples
cd fluent-i18n-examples/simple-web-app
mvn spring-boot:run

# 3. Test extraction
mvn fluent:i18n:extract

# 4. Test compilation
mvn fluent:i18n:compile

# 5. Verify output files
ls -la src/main/resources/i18n/
```

## Release Process

### Version Management

We use semantic versioning (MAJOR.MINOR.PATCH):

- **MAJOR**: Breaking changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

### Release Steps

1. **Update Version**: Update version in `pom.xml` files
2. **Update Changelog**: Document changes in `CHANGELOG.md`
3. **Run Tests**: Ensure all tests pass
4. **Build**: `mvn clean install -DskipTests`
5. **Tag Release**: `git tag v1.0.0`
6. **Push Tags**: `git push origin --tags`
7. **Create Release**: Create GitHub release with release notes

### Publishing to Maven Central

```bash
# Build and sign artifacts
mvn clean install -DskipTests

# Deploy to Maven Central
mvn deploy -P release
```

### Release Checklist

- [ ] All tests pass
- [ ] Documentation is updated
- [ ] Examples work correctly
- [ ] Version numbers are consistent
- [ ] Changelog is updated
- [ ] Release notes are prepared
- [ ] Maven Central deployment is successful

## Architecture Decisions

### Why Hash-Based Keys?

**Problem**: Traditional i18n uses string keys that are:
- Hard to maintain
- Error-prone
- Not developer-friendly

**Solution**: Hash-based keys provide:
- Automatic key generation
- No manual key management
- Better performance
- Invisible to developers

### Why PO Files?

**Problem**: Custom translation formats are:
- Not interoperable
- Hard for translators to use
- Proprietary

**Solution**: PO files provide:
- Industry standard format
- Wide tool support
- Translator familiarity
- Open format

### Why Natural Language?

**Problem**: Traditional key-based i18n:
- Requires context switching
- Hard to read and maintain
- Error-prone

**Solution**: Natural language provides:
- Self-documenting code
- Better developer experience
- Reduced errors
- Faster development

## Performance Considerations

### Hash Generation

- SHA-256 hashing for consistency
- Caching of generated hashes
- Thread-safe hash generation

### Message Resolution

- Efficient hash-based lookup
- Caching of resolved messages
- Lazy loading of translation files

### Compilation

- Incremental compilation
- Parallel processing where possible
- Optimized output formats

## Security Considerations

### Hash Collisions

- SHA-256 provides sufficient collision resistance
- Hash validation during compilation
- Error reporting for potential collisions

### Input Validation

- Validate PO file syntax
- Sanitize extracted messages
- Prevent injection attacks

### File Access

- Secure file operations
- Path traversal protection
- Proper resource cleanup

---

For more information, check the [examples directory](fluent-i18n-examples/) or open an issue on GitHub. 