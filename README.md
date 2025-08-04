# Fluent i18n

[![Maven Central](https://img.shields.io/maven-central/v/io.github.unattendedflight.fluent/fluent-i18n-core)](https://search.maven.org/artifact/io.github.unattendedflight.fluent/fluent-i18n-core)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17+-blue.svg)](https://openjdk.java.net/)

**Fluent i18n** is a Java internationalization library that lets you write translatable strings using natural language in your code instead of artificial translation keys. Write your application in your native language, and let Fluent handle the translation workflow automatically.

## 🌟 Key Features

- **Natural Language Development**: Write code using actual human-readable text
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
<!-- Core library -->
<dependency>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-core</artifactId>
    <version>0.1.3</version>
</dependency>

<!-- Spring Boot Starter (optional) -->
<dependency>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-spring-boot-starter</artifactId>
    <version>0.1.3</version>
</dependency>
```

### 2. Configure Maven Plugin

```xml
<plugin>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-maven-plugin</artifactId>
    <version>0.1.3</version>
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
      type: json # or properties, binary
      cache-duration: PT1H # 1 hour
      basename: i18n/messages
      use-original-as-fallback: true
      log-missing-translations: true
    
    compilation:
      output-format: json, binary

    warm-up:
      enabled: true # Load specified locales into memory on launch
      locales: # These will be loaded into memory at startup
        - en
        - es

    web: # Webmvc configuration, uses interceptors
        enabled: true
        locale-parameter: lang # URL parameter for locale
        use-accept-language-header: true # Use Accept-Language header
        use-session: true # Store locale in session
```

## 📖 How It Works

### Traditional vs Fluent Approach

```java
// Traditional
messageSource.getMessage("welcome.message", args, locale);

// Fluent i18n
I18n.translate("Welcome to our application!");
```

### Development Workflow

1. **Development Phase**: Write your application using natural language text
2. **Build-Time Extraction**: Maven plugin automatically extracts all translatable messages
3. **Translation Workflow**: Translators work with standard PO files using tools like Poedit, Lokalise, or any text editor
4. **Runtime Compilation**: Plugin compiles PO files to runtime formats (JSON, Properties, or Binary)

### Example Workflow

```java
@Controller
public class UserController {
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("title", I18n.translate("User Management"));
        
        int userCount = userService.getUserCount();
        model.addAttribute("message", 
            I18n.plural(userCount)
                .zero("No users found")
                .one("One user found")
                .other("{} users found")
                .format());
        
        return "users";
    }
}
```

Build generates `messages_en.po`:
```po
msgid "User Management"
msgstr "User Management"

msgid "{0, plural, zero {No users found} one {One user found} other {# users found}}"
msgstr "{0, plural, zero {No users found} one {One user found} other {# users found}}"
```

## 🛠️ Advanced Usage

### Basic API

```java
// Simple translation
I18n.translate("Hello, world!");

// With parameters
I18n.translate("Hello, {}!", userName);

// Short alias
I18n.t("Welcome!");

// Context-aware translations
I18n.context("button").translate("Submit");

// Pluralization
I18n.plural(itemCount)
    .zero("No items")
    .one("One item")
    .other("{} items")
    .format();
```

### Annotations & Lazy Evaluation

```java
// Static analysis with annotations
@Translatable("Welcome to our application")
public static final String WELCOME = "Welcome to our application";

// Lazy evaluation
MessageDescriptor message = I18n.describe("Welcome to our amazing application!");
String translated = message.resolve(); // Uses current locale
```

### Template Integration

```html
<!-- Thymeleaf integration -->
<h1 th:text="${@i18nTemplateUtils.translate('Welcome')}">Welcome</h1>
<p th:text="${@i18nTemplateUtils.translate('Hello, {}!', userName)}">Hello message</p>
```

## 📁 Project Structure

- **fluent-i18n-core**: Core functionality (compiler, extractor, Maven plugin)
- **fluent-i18n-spring-boot-starter**: Spring Boot integration
- **fluent-i18n-maven-plugin**: Maven plugin for PO extraction and compilation
- **fluent-i18n-examples**: Example applications

## 🔧 Configuration Options

For detailed configuration options, see the [Configuration Guide](CONFIGURATION.md).

## 📚 Examples

Check out the [examples directory](fluent-i18n-examples/) for complete working applications:

- **Simple Web App**: Basic Spring Boot application with i18n demonstrating the complete workflow

## 🏗️ Architecture

Fluent i18n follows a modular architecture with some separation of concerns.



### Key Design Principles

1. **Natural Language First**: Developers write code using natural language text
2. **Hash-Based Keys**: Invisible hash-based keys for consistency
3. **Standard Workflow**: Industry-standard PO file workflow for translators
4. **Multiple Formats**: Support for JSON, Properties, and Binary output formats
5. **Extensible**: Pluggable extractors, compilers, and message sources

## 🚀 Performance

- **O(1) hash-based translation lookup**
- **Configurable caching**
- **Thread-safe operations**
- **Multiple output formats** (JSON, Properties, Binary)
- **Warm-up support** for pre-loading locales

## 🔒 Security

- **SHA-256 hashing** for collision resistance
- **Input validation** and sanitization
- **Secure file operations** with path traversal protection
- **Proper resource cleanup**

## 🤝 Contributing

I'd welcome contributions! Please see the [Contributing Guide](CONTRIBUTING.md) for details.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Ready to write code in your native language?** 🚀 