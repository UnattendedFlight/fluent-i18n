# Fluent i18n

[![Maven Central](https://img.shields.io/maven-central/v/io.github.unattendedflight.fluent/fluent-i18n-core)](https://search.maven.org/artifact/io.github.unattendedflight.fluent/fluent-i18n-core)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17+-blue.svg)](https://openjdk.java.net/)

**Fluent i18n** is a Java internationalization library that lets you write translatable strings using natural language in your code instead of artificial translation keys. Write your application in your native language, and let Fluent handle the translation workflow automatically.

## 🌟 Key Features

- **Natural Language Development**: Write code using actual human-readable text
- **Framework Agnostic**: Core library works with any Java framework (Spring Boot, JavaFX, Console apps, etc.)
- **Flexible Configuration**: Support for `fluent.yml` configuration files or programmatic configuration
- **Automatic Message Extraction**: Extract translatable messages from Java annotations and method calls
- **Standard PO File Workflow**: Generate and manage translations using industry-standard PO files
- **Hash-Based Keys**: Invisible hash-based keys instead of artificial keys, ensuring consistency across translations
- **Spring Boot Integration**: Seamless integration with Spring Boot 3+ applications
- **Multiple Output Formats**: Support for JSON, Properties, and Binary formats
- **Pluralization Support**: Built-in support for pluralization rules
- **Context-Aware Translations**: Disambiguate translations for your translators using context

## 🚀 Quick Start

### 1. Add Dependencies

```xml
<!-- Core library (framework agnostic) -->
<dependency>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-core</artifactId>
    <version>0.1.6</version>
</dependency>

<!-- Spring Boot Starter (optional) -->
<dependency>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-spring-boot-starter</artifactId>
    <version>0.1.6</version>
</dependency>
```

### 2. Configure fluent-i18n

Create a `fluent.yml` file in your classpath (e.g., `src/main/resources/fluent.yml`):

```yaml
# Base path where translation files are located
basePath: "i18n"

# Supported locales for the application
supportedLocales:
  - "en"
  - "fr"
  - "de"
  - "es"

# Default locale to use when no specific locale is set
defaultLocale: "en"

# Character encoding for reading translation files
encoding: "UTF-8"

# Message source type (auto, binary, json, properties)
# auto: automatically detect the best available format
# binary: use binary format (most efficient)
# json: use JSON format
# properties: use properties format
messageSourceType: "auto"

# Caching configuration
caching:
  enabled: true
  timeoutSeconds: 300

# Auto-reload configuration (for development)
autoReload:
  enabled: false
  intervalSeconds: 60

# Whether to enable fallback to original text when translation is not found
fallback: true

# Whether to log missing translations (useful for development)
logMissingTranslations: false
```

### 3. Configure Maven Plugin

```xml
<plugin>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-maven-plugin</artifactId>
    <version>0.1.6</version>
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

### 4. Use in Your Code

#### Framework-Agnostic Usage

```java
import io.github.unattendedflight.fluent.i18n.I18n;

// Initialize with configuration
I18n.initialize();

// Or initialize with custom configuration
FluentConfig config = new FluentConfig()
    .supportedLocales("en", "fr", "de")
    .defaultLocale("en")
    .basePath("i18n");
I18n.initialize(config);

// Use natural language in your code
String message = I18n.translate("Hello, welcome to our application!");
String formatted = I18n.translate("Hello {0}, you have {1} new messages", "John", 5);

// Pluralization support
String plural = I18n.plural(3)
    .one("You have 1 message")
    .other("You have {} messages")
    .format();
```

#### Spring Boot Integration

With the Spring Boot starter, fluent-i18n is automatically configured:

```java
@RestController
public class WelcomeController {
    
    @GetMapping("/welcome")
    public String welcome(@RequestParam String name) {
        // Use natural language - no translation keys needed!
        return I18n.translate("Welcome {0} to our application!", name);
    }
}
```

## 📁 Configuration Options

### fluent.yml Configuration

The `fluent.yml` file supports the following configuration options:

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `basePath` | String | `"i18n"` | Base path for translation files |
| `supportedLocales` | List<String> | `["en"]` | Supported locale codes |
| `defaultLocale` | String | `"en"` | Default locale |
| `encoding` | String | `"UTF-8"` | Character encoding |
| `messageSourceType` | String | `"auto"` | Message source type (`auto`, `binary`, `json`, `properties`) |
| `caching.enabled` | Boolean | `true` | Enable translation caching |
| `caching.timeoutSeconds` | Long | `300` | Cache timeout in seconds |
| `autoReload.enabled` | Boolean | `false` | Enable auto-reload |
| `autoReload.intervalSeconds` | Long | `60` | Auto-reload interval |
| `fallback` | Boolean | `true` | Enable fallback to original text |
| `logMissingTranslations` | Boolean | `false` | Log missing translations |

### Message Source Types

Fluent i18n supports multiple message source types:

- **`auto`** (default): Automatically detects the best available format (binary > JSON > properties)
- **`binary`**: Uses binary format for maximum performance
- **`json`**: Uses JSON format for easy debugging and editing
- **`properties`**: Uses Java properties format for compatibility

### Programmatic Configuration

```java
FluentConfig config = new FluentConfig()
    .basePath("i18n")
    .supportedLocales("en", "fr", "de", "es")
    .defaultLocale("en")
    .encoding(StandardCharsets.UTF_8)
    .messageSourceType(FluentConfig.MessageSourceType.AUTO)
    .enableCaching(true)
    .cacheTimeoutSeconds(300)
    .enableAutoReload(false)
    .autoReloadIntervalSeconds(60)
    .enableFallback(true)
    .logMissingTranslations(false);

I18n.initialize(config);
```

### Spring Boot Configuration

In Spring Boot applications, you can configure fluent-i18n using application properties:

```yaml
# application.yml
fluent:
  i18n:
    enabled: true
    config-location: "classpath:fluent.yml"
    web:
      enabled: true
```

## 🔧 Framework Integration

### Spring Boot

The Spring Boot starter provides automatic configuration:

1. **Auto-configuration**: Automatically configures fluent-i18n when Spring Boot is detected
2. **Locale Resolution**: Integrates with Spring's locale resolution system
3. **Web Interceptor**: Automatically sets up locale handling for web requests
4. **Configuration Properties**: Supports configuration through `application.yml`

### Other Frameworks

For non-Spring frameworks (JavaFX, Console apps, etc.), use the core library directly:

```java
// Initialize with fluent.yml
I18n.initialize();

// Or initialize with custom configuration
FluentConfig config = new FluentConfig()
    .supportedLocales("en", "fr")
    .defaultLocale("en");
I18n.initialize(config);

// Use in your application
String message = I18n.translate("Hello, world!");
```

## 📝 Translation Workflow

1. **Write Code**: Use natural language in your code
2. **Extract Messages**: Run `mvn fluent-i18n:extract` to extract translatable messages
3. **Translate**: Edit the generated PO files with translations
4. **Compile**: Run `mvn fluent-i18n:compile` to compile translations
5. **Deploy**: Translation files are automatically loaded by the library

## 🎯 Advanced Features

### Context-Aware Translations

```java
// Use context to disambiguate translations
String message = I18n.context("email")
    .translate("Subject");
```

### Pluralization

```java
// Handle pluralization automatically
String message = I18n.plural(count)
    .zero("No messages")
    .one("1 message")
    .other("{} messages")
    .format();
```

### Message Descriptors

```java
// Create reusable message descriptors
MessageDescriptor welcomeMsg = I18n.describe("Welcome {0}!", "name");
String message = I18n.resolve(welcomeMsg, "John");
```

## 🔍 Troubleshooting

### Common Issues

1. **Translation files not found**: Check the `basePath` configuration
2. **Missing translations**: Enable `logMissingTranslations` for debugging
3. **Encoding issues**: Ensure `encoding` is set correctly in `fluent.yml`

### Debug Configuration

Enable debug logging to see what's happening:

```yaml
# fluent.yml
logMissingTranslations: true
```

```yaml
# application.yml (Spring Boot)
logging:
  level:
    io.github.unattendedflight.fluent.i18n: DEBUG
```

## 📚 Examples

See the [examples directory](fluent-i18n-examples/) for complete working examples:

- **Simple Web App**: Spring Boot application with fluent-i18n integration
- **Console App**: Framework-agnostic usage example

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details. 