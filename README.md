# Fluent i18n

[![Maven Central](https://img.shields.io/maven-central/v/io.github.unattendedflight/fluent-i18n-core)](https://search.maven.org/artifact/io.github.unattendedflight.fluent/fluent-i18n-core)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-11+-blue.svg)](https://openjdk.java.net/)

**Fluent i18n** is a Java internationalization library that lets you write translatable strings using natural language in your code instead of artificial translation keys. Write your application in your native language, and let Fluent handle the translation workflow automatically.

## 🌟 Key Features

- **Natural Language Development**: Write code using actual human-readable text
- **Automatic Message Extraction**: Extract translatable messages from Java annotations and method calls
- **Standard PO File Workflow**: Generate and manage translations using industry-standard PO files
- **Hash-Based Keys**: Invisible hash-based keys for optimal performance
- **Spring Boot Integration**: Integration with Spring Boot applications
- **Multiple Output Formats**: Support for JSON, Properties, and Binary formats
- **Pluralization Support**: Built-in support for pluralization rules
- **Context-Aware Translations**: Disambiguate translations using context

## 🚀 Quick Start

### 1. Add Dependencies

```xml
<!-- Core library -->
<dependency>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-core</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Spring Boot Starter (optional) -->
<dependency>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Configure Maven Plugin

```xml
<plugin>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-core</artifactId>
    <version>1.0.0</version>
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

### 3. Write Natural Language Code

```java
@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(Model model) {
        // Use natural language text directly
        model.addAttribute("welcomeMessage", 
            I18n.translate("Welcome to our amazing application!"));
        model.addAttribute("welcomeSubtitle", 
            I18n.t("We are pleased to see you here."));
        
        // Pluralization example
        int userCount = userService.getUserCount();
        model.addAttribute("userCountMessage", 
            I18n.plural(userCount)
                .zero("No users registered yet")
                .one("One user is using our app")
                .other("{} users are using our app")
                .format());
        
        return "home";
    }
}
```

### 4. Configure Application

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
    
    message-source:
      type: json
      basename: i18n/messages
```

## 📖 How It Works

### 1. Development Phase
Write your application using natural language text:

```java
static final MessageDescriptor predefinedMessage = I18n.describe("This is also translated");
@Message("Translated as well for variable saying: {}")
static final String anotherPredefinedMessage = "Translated as well for variable saying: {}";

// ...

// Simple translation
String message = I18n.translate("Hello, world!");

// With parameters
String greeting = I18n.translate("Hello, {}!", userName);


// Pluralization
String count = I18n.plural(itemCount)
    .zero("No items")
    .one("One item")
    .other("{} items")
    .format();


// Predefined messages
String message = predefinedMessage.resolve(); // Uses current locale
String messageLocaleSpecified = predefinedMessage.resolve(Locale.forLanguageTag("nb")); // Specify locale

String arg = "BOOM!";
String messageWithArgs = anotherPredefinedMessage.withArgs(arg).resolve();
```

### 2. Build-Time Extraction
The Maven plugin automatically extracts all translatable messages:

```bash
mvn compile
```

This generates PO files like `messages_en.po`, `messages_es.po`, etc. in your projects resources directory.
It also compiles preliminary compilation files in the resources directory which will contain the empty translations.

### 3. Translation Workflow
Translators work with standard PO files using tools like Poedit, Lokalise, or any text editor.

### 4. Runtime Compilation
The plugin compiles PO files to runtime formats (JSON, Properties, or Binary).

## 🛠️ Advanced Usage

### Annotations for Static Analysis

```java
public class Messages {
    @Translatable("Welcome to our application")
    public static final String WELCOME = "Welcome to our application";
    
    @Message("User not found")
    public static String userNotFound() {
        return "User not found";
    }
}
```

### Context-Aware Translations

```java
// Disambiguate translations with context
String message = I18n.context("button")
    .translate("Submit");

String message2 = I18n.context("form")
    .translate("Submit");
```

### Template Integration

```html
<!-- Thymeleaf integration -->
<h1 th:text="${@i18nTemplateUtils.translate('Welcome')}">Welcome</h1>
<p th:text="${@i18nTemplateUtils.translate('Hello, {}!', userName)}">Hello message</p>
```

### Custom Hash Generator

```java
// Use custom hash generation for consistency
I18n.setHashGenerator(new CustomHashGenerator());
```

## 📁 Project Structure

```
fluent-i18n/
├── fluent-i18n-core/           # Core functionality
│   ├── compiler/               # PO file compilation
│   ├── extractor/              # Message extraction
│   ├── core/                   # Core i18n functionality
│   └── maven/                  # Maven plugin
├── fluent-i18n-spring-boot-starter/  # Spring Boot integration
└── fluent-i18n-examples/      # Example applications
```

## 🔧 Configuration Options

### Maven Plugin Configuration

```xml
<plugin>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-core</artifactId>
    <version>1.0.0</version>
    <configuration>
        <poDirectory>src/main/resources/i18n/po</poDirectory>
        <outputDirectory>src/main/resources/i18n</outputDirectory>
        <outputFormats>
            <format>json</format>
            <format>properties</format>
        </outputFormats>
        <supportedLocales>
            <locale>en</locale>
            <locale>es</locale>
            <locale>fr</locale>
        </supportedLocales>
    </configuration>
</plugin>
```

### Application Properties

```yaml
fluent:
  i18n:
    enabled: true
    default-locale: en
    supported-locales: [en, es, fr, de]
    
    message-source:
      type: json                    # json, properties, or binary
      basename: i18n/messages
      cache-duration: PT1H
      use-original-as-fallback: true
      log-missing-translations: true
    
    compilation:
      output-format: json
      validation: true
      preserve-existing: true
      minify-output: false
    
    warm-up:
      enabled: true         # Loads specified (or all if locales is empty) into memory on launch
      locales: [en, es, fr]
    
    web:
      enabled: true
      locale-parameter: lang
      use-accept-language-header: true
      use-session: true
```

## 📚 Examples

Check out the [examples directory](fluent-i18n-examples/) for complete working applications:

- **Simple Web App**: Basic Spring Boot application with i18n

## 🤝 Contributing

I'd welcome contributions! Please see the [Contributing Guide](CONTRIBUTING.md) for details.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Inspired by the need for more natural internationalization workflows
- Built on industry-standard PO file format
- Leverages Spring Boot's auto-configuration capabilities but is not required.

---

**Ready to write code in your native language?** Get started with Fluent i18n! 🚀 