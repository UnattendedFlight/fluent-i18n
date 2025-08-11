# Contributing to Fluent i18n

Thank you for your interest in contributing to Fluent i18n! This document provides guidelines and information for contributors.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [How Can I Contribute?](#how-can-i-contribute)
3. [Development Setup](#development-setup)
4. [Pull Request Process](#pull-request-process)
5. [Code Style Guidelines](#code-style-guidelines)
6. [Testing Guidelines](#testing-guidelines)
7. [Documentation Guidelines](#documentation-guidelines)
8. [Release Process](#release-process)

## Code of Conduct

This project adheres to the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates.

**Bug Report Template:**

```markdown
**Environment:**
- Java Version: 
- Maven Version: 
- OS: 
- Fluent i18n Version: 

**Steps to Reproduce:**
1. 
2. 
3. 

**Expected Behavior:**
What you expected to happen

**Actual Behavior:**
What actually happened

**Additional Context:**
- Logs, screenshots, etc.
- Related issues
```

### Suggesting Enhancements

We welcome feature requests! Please use the enhancement issue template and include:

- **Use Case**: What problem does this solve?
- **Proposed Solution**: How should it work?
- **Alternatives Considered**: Other approaches you considered
- **Additional Context**: Any other relevant information

### Contributing Code

We welcome code contributions! Here's how to get started:

1. **Fork** the repository
2. **Create** a feature branch
3. **Make** your changes
4. **Test** your changes
5. **Submit** a pull request

## Development Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Git

### Getting Started

```bash
# Clone your fork
git clone https://github.com/your-username/fluent-i18n.git
cd fluent-i18n

# Add upstream remote
git remote add upstream https://github.com/UnattendedFlight/fluent-i18n.git

# Build the project
mvn clean install

# Run tests
mvn test
```

### IDE Setup

**IntelliJ IDEA:**
1. Open the project
2. Import Maven project
3. Configure Java 17+ SDK
4. Enable annotation processing

**Eclipse:**
1. Import as Maven project
2. Configure Java 17+ JRE
3. Enable annotation processing

**VS Code:**
1. Install Java Extension Pack
2. Open the project folder
3. Configure Java 17+ runtime

### Running Examples

```bash
# Run the simple web app example
cd fluent-i18n-examples/simple-web-app
mvn spring-boot:run

# Access the application
open http://localhost:8080
```

## Pull Request Process

### Before Submitting

1. **Update Documentation**: Update relevant documentation
2. **Add Tests**: Include tests for new functionality
3. **Run Tests**: Ensure all tests pass
4. **Check Style**: Verify code follows style guidelines
5. **Test Examples**: Run examples to verify functionality

### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Examples work correctly
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No breaking changes (or breaking changes documented)
```

### Review Process

1. **Automated Checks**: CI/CD pipeline runs tests and style checks
2. **Code Review**: Maintainers review the code
3. **Feedback**: Address any feedback or requested changes
4. **Merge**: Once approved, the PR is merged

## Code Style Guidelines

### Java Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Keep methods focused and single-purpose
- Add comprehensive JavaDoc for public APIs

### Naming Conventions

```java
// Classes: PascalCase
public class MessageExtractor { }

// Methods: camelCase
public void extractMessages() { }

// Constants: UPPER_SNAKE_CASE
public static final String DEFAULT_LOCALE = "en";

// Variables: camelCase
String messageText = "Hello, world!";
```

### Code Organization

```java
// 1. Package declaration
package io.github.unattendedflight.fluent.i18n;

// 2. Imports (organized)
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

// 3. Class declaration with JavaDoc
/**
 * Extracts translatable messages from source code.
 * 
 * @author Your Name
 * @since 1.0.0
 */
@Component
public class MessageExtractor {
    
    // 4. Constants
    private static final String DEFAULT_ENCODING = "UTF-8";
    
    // 5. Fields
    private final ExtractionConfig config;
    
    // 6. Constructor
    public MessageExtractor(ExtractionConfig config) {
        this.config = config;
    }
    
    // 7. Public methods
    public ExtractionResult extract() {
        // Implementation
    }
    
    // 8. Private methods
    private void processFile(Path file) {
        // Implementation
    }
}
```

### Exception Handling

```java
// Use specific exceptions
public void processFile(Path file) throws IOException {
    try {
        // File processing logic
    } catch (IOException e) {
        throw new IOException("Failed to process file: " + file, e);
    }
}

// Log and rethrow when appropriate
public void extract() {
    try {
        // Extraction logic
    } catch (Exception e) {
        logger.error("Extraction failed", e);
        throw new ExtractionException("Message extraction failed", e);
    }
}
```

## Testing Guidelines

### Unit Tests

- Test all public methods
- Include positive and negative test cases
- Use descriptive test method names
- Mock external dependencies

```java
@Test
void shouldExtractMessagesFromJavaFile() throws IOException {
    // Given
    Path javaFile = createTestJavaFile();
    MessageExtractor extractor = new MessageExtractor(config);
    
    // When
    ExtractionResult result = extractor.extract();
    
    // Then
    assertThat(result.getMessageCount()).isEqualTo(3);
    assertThat(result.getExtractedMessages())
        .containsKey("expected_message_hash");
}
```

### Integration Tests

- Test complete workflows
- Use real file systems and configurations
- Verify end-to-end functionality

```java
@Test
void shouldExtractAndCompileCompleteWorkflow() throws IOException {
    // Given
    setupTestProject();
    
    // When
    mvn("fluent:i18n:extract");
    mvn("fluent:i18n:compile");
    
    // Then
    assertThat(outputDirectory)
        .exists()
        .isDirectory();
    assertThat(outputDirectory.resolve("messages_en.json"))
        .exists()
        .isRegularFile();
}
```

### Test Coverage

- Aim for >80% code coverage
- Focus on critical paths
- Test error conditions
- Use parameterized tests for multiple scenarios

### Test Data

```java
// Use test fixtures
public class TestData {
    public static final String SAMPLE_JAVA_FILE = """
        @Controller
        public class TestController {
            public String getMessage() {
                return I18n.translate("Hello, world!");
            }
        }
        """;
    
    public static final String EXPECTED_PO_CONTENT = """
        msgid "Hello, world!"
        msgstr "Hello, world!"
        """;
}
```

## Documentation Guidelines

### JavaDoc

```java
/**
 * Extracts translatable messages from source code files.
 * 
 * <p>This class scans configured source directories for translatable content
 * using various extractors. It supports Java annotations, method calls, and
 * template files.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * ExtractionConfig config = ExtractionConfig.builder()
 *     .sourceDirectories(List.of(Paths.get("src/main/java")))
 *     .supportedLocales(Set.of("en", "es", "fr"))
 *     .build();
 * 
 * MessageExtractor extractor = new MessageExtractor(config);
 * ExtractionResult result = extractor.extract();
 * }</pre>
 * 
 * @author Your Name
 * @since 0.1.5
 * @see ExtractionConfig
 * @see ExtractionResult
 */
public class MessageExtractor {
    
    /**
     * Extracts messages from all configured source directories.
     * 
     * <p>This method scans all source directories and extracts translatable
     * messages using the configured extractors. It generates hashes for each
     * message and returns a comprehensive extraction result.</p>
     * 
     * @return the extraction result containing all discovered messages
     * @throws IOException if an I/O error occurs during extraction
     * @throws IllegalArgumentException if the configuration is invalid
     */
    public ExtractionResult extract() throws IOException {
        // Implementation
    }
}
```

### Documentation Updates

When adding new features, update relevant documentation:

- **README.md**: Update feature list and examples
- **CONFIGURATION.md**: Add configuration options and examples
- **CHANGELOG.md**: Document changes for the next release

### Code Comments

```java
// Use comments to explain complex logic
private void processPluralMessage(ExtractedMessage message) {
    // ICU MessageFormat requires special handling for plural forms
    // The naturalText will be replaced with ICU format during PO generation
    if (message.getType() == MessageType.PLURAL && 
        message.getContext() != null && 
        message.getContext().startsWith("plural:")) {
        
        // Generate hash from the complete ICU MessageFormat string
        String hash = hashGenerator.generateHash(message.getNaturalText());
        message.setHash(hash);
        
        // Merge with existing message if hash already exists
        ExtractedMessage existing = discoveredMessages.get(hash);
        if (existing != null) {
            existing.addLocation(message.getLocations().get(0));
        } else {
            discoveredMessages.put(hash, message);
        }
    }
}
```

## Release Process

### Version Management

We use [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

### Release Checklist

Before releasing:

- [ ] All tests pass
- [ ] Documentation is updated
- [ ] Examples work correctly
- [ ] Version numbers are consistent
- [ ] CHANGELOG.md is updated
- [ ] Release notes are prepared
- [ ] Maven Central deployment is configured

### Release Steps

```bash
# 1. Update version in pom.xml files
# 2. Update CHANGELOG.md
# 3. Run full test suite
mvn clean install

# 4. Create release tag
git tag v0.1.5
git push origin --tags

# 5. Create GitHub release
# 6. Deploy to Maven Central
mvn deploy -P release
```

## Getting Help

### Communication Channels

- **GitHub Issues**: For bug reports and feature requests
- **GitHub Discussions**: For questions and general discussion
- **Pull Requests**: For code contributions

### Resources

- [README.md](README.md): Quick start and overview
- [Configuration Guide](CONFIGURATION.md): Detailed configuration options
- [Examples](fluent-i18n-examples/): Working example applications
- [API Documentation](https://javadoc.io/doc/io.github.unattendedflight.fluent/fluent-i18n-core): Generated JavaDoc

### Mentoring

New contributors are welcome! We provide:

- **Code Reviews**: Detailed feedback on pull requests
- **Documentation**: Comprehensive guides and examples
- **Examples**: Working applications to learn from
- **Community**: Friendly and supportive community

## Recognition

Contributors are recognized in:

- **README.md**: List of contributors
- **CHANGELOG.md**: Credit for contributions
- **Release Notes**: Acknowledgment of contributions
- **GitHub**: Contributor statistics and profile

---

Thank you for contributing to Fluent i18n! Your contributions help make internationalization easier and more natural for developers worldwide. 