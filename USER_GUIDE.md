# Fluent i18n User Guide

This comprehensive guide will walk you through using Fluent i18n to internationalize your Java applications using natural language text.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Basic Usage](#basic-usage)
3. [Advanced Features](#advanced-features)
4. [Spring Boot Integration](#spring-boot-integration)
5. [Template Integration](#template-integration)
6. [Build Configuration](#build-configuration)
7. [Translation Workflow](#translation-workflow)
8. [Best Practices](#best-practices)
9. [Troubleshooting](#troubleshooting)

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Spring Boot 2.7+ (for Spring Boot integration)

### Project Setup

1. **Add Dependencies**

```xml
<dependencies>
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
</dependencies>
```

2. **Configure Maven Plugin**

```xml
<build>
    <plugins>
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
    </plugins>
</build>
```

3. **Create Directory Structure**

```
src/main/resources/
└── i18n/
    ├── po/                    # PO files (generated)
    │   ├── messages_en.po
    │   ├── messages_es.po
    │   └── messages_fr.po
    └── messages_en.json       # Compiled translations (generated)
```

## Basic Usage

### Simple Translation

The most basic usage is to translate natural language text:

```java
import io.github.unattendedflight.fluent.i18n.I18n;

public class WelcomeController {
    
    public String getWelcomeMessage() {
        return I18n.translate("Welcome to our application!");
    }
    
    public String getGreeting(String userName) {
        return I18n.translate("Hello, {}!", userName);
    }
}
```

### Short Alias

For convenience, you can use the short alias `t()`:

```java
String message = I18n.t("Welcome to our application!");
```

### Pluralization

Handle different plural forms for different languages:

```java
public String getUserCountMessage(int userCount) {
    return I18n.plural(userCount)
        .zero("No users registered yet")
        .one("One user is using our app")
        .other("{} users are using our app")
        .format();
}
```

### Context-Aware Translations

Disambiguate translations that might have different meanings in different contexts:

```java
// Button text
String submitButton = I18n.context("button").translate("Submit");

// Form submission
String submitAction = I18n.context("form").translate("Submit");
```

## Advanced Features

### Annotations for Static Analysis

Use annotations to mark translatable content for better extraction:

```java
public class Messages {
    
    @Translatable("Welcome to our application")
    public static final String WELCOME = "Welcome to our application";
    
    @Message("User not found")
    public static String userNotFound() {
        return "User not found";
    }
    
    @Message(value = "Delete user", context = "confirmation")
    public static String deleteUserConfirmation() {
        return "Delete user";
    }
}
```

### Lazy Evaluation

Create message descriptors for lazy evaluation:

```java
public class MessageService {
    
    public MessageDescriptor createWelcomeMessage(String userName) {
        return I18n.describe("Welcome, {}!", userName);
    }
    
    public void displayMessage(MessageDescriptor descriptor) {
        String translated = descriptor.resolve();
        // Display the translated message
    }
}
```

### Custom Hash Generator

For consistent hashing across builds, you can use a custom hash generator:

```java
public class CustomHashGenerator implements HashGenerator {
    
    @Override
    public String generateHash(String text) {
        // Your custom hashing logic
        return DigestUtils.sha256Hex(text);
    }
}

// Set the custom generator
I18n.setHashGenerator(new CustomHashGenerator());
```

## Spring Boot Integration

### Auto-Configuration

The Spring Boot starter provides automatic configuration. Just add the dependency and configure your application:

```yaml
fluent:
  i18n:
    enabled: true
    default-locale: en
    supported-locales: [en, es, fr, de]
    
    message-source:
      type: json
      basename: i18n/messages
      cache-duration: PT1H
      use-original-as-fallback: true
      log-missing-translations: true
    
    web:
      enabled: true
      locale-parameter: lang
      use-accept-language-header: true
      use-session: true
```

### Web Integration

The starter automatically configures locale handling:

```java
@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(Model model) {
        // Locale is automatically handled by the web interceptor
        model.addAttribute("welcomeMessage", 
            I18n.translate("Welcome to our application!"));
        
        return "home";
    }
}
```

### Manual Initialization

If you need manual control, you can initialize the message source:

```java
@Component
public class I18nInitializer {
    
    @PostConstruct
    public void initialize() {
        NaturalTextMessageSource messageSource = new JsonNaturalTextMessageSource(
            "i18n/messages",
            Set.of(Locale.ENGLISH, new Locale("es"), new Locale("fr")),
            Locale.ENGLISH
        );
        
        I18n.initialize(messageSource);
    }
}
```

## Template Integration

### Thymeleaf Integration

Use the `I18nTemplateUtils` bean in Thymeleaf templates:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${@i18nTemplateUtils.translate('Home Page')}">Home Page</title>
</head>
<body>
    <h1 th:text="${@i18nTemplateUtils.translate('Welcome')}">Welcome</h1>
    
    <p th:text="${@i18nTemplateUtils.translate('Hello, {}!', userName)}">
        Hello message
    </p>
    
    <!-- Pluralization in templates -->
    <p th:with="count=${userCount}">
        <span th:text="${@i18nTemplateUtils.plural(count)
            .zero('No users')
            .one('One user')
            .other('{} users')
            .format()}">User count</span>
    </p>
</body>
</html>
```

### JSP Integration

For JSP templates, you can use the static methods directly:

```jsp
<%@ page import="io.github.unattendedflight.fluent.i18n.I18n" %>

<h1><%= I18n.translate("Welcome") %></h1>
<p><%= I18n.translate("Hello, {}!", userName) %></p>
```

## Build Configuration

### Maven Plugin Configuration

Configure the Maven plugin for your project:

```xml
<plugin>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-core</artifactId>
    <version>1.0.0</version>
    <configuration>
        <!-- PO file directory -->
        <poDirectory>src/main/resources/i18n/po</poDirectory>
        
        <!-- Output directory for compiled translations -->
        <outputDirectory>src/main/resources/i18n</outputDirectory>
        
        <!-- Output formats -->
        <outputFormats>
            <format>json</format>
            <format>properties</format>
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
        </filePatterns>
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

### Build Process

1. **Extract Messages**: `mvn fluent:i18n:extract`
2. **Compile Translations**: `mvn fluent:i18n:compile`
3. **Full Build**: `mvn compile` (includes both extract and compile)

## Translation Workflow

### 1. Development Phase

Write your application using natural language text:

```java
@Controller
public class UserController {
    
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("title", I18n.translate("User Management"));
        model.addAttribute("description", I18n.translate("Manage application users"));
        
        int userCount = userService.getUserCount();
        model.addAttribute("userCountMessage", 
            I18n.plural(userCount)
                .zero("No users found")
                .one("One user found")
                .other("{} users found")
                .format());
        
        return "users";
    }
}
```

### 2. Message Extraction

Run the extraction goal to generate PO files:

```bash
mvn fluent:i18n:extract
```

This creates PO files like `messages_en.po`:

```po
msgid "User Management"
msgstr "User Management"

msgid "Manage application users"
msgstr "Manage application users"

msgctxt "plural"
msgid "No users found"
msgid_plural "{} users found"
msgstr[0] "No users found"
msgstr[1] "One user found"
msgstr[2] "{} users found"
```

### 3. Translation Process

Translators work with the PO files using tools like:
- **Poedit**: Desktop application for PO file editing
- **Lokalise**: Cloud-based translation management
- **Any text editor**: PO files are plain text

### 4. Compilation

After translation, compile the PO files to runtime formats:

```bash
mvn fluent:i18n:compile
```

This generates efficient runtime files like `messages_en.json`:

```json
{
  "hash1": "User Management",
  "hash2": "Manage application users",
  "hash3": {
    "zero": "No users found",
    "one": "One user found",
    "other": "{} users found"
  }
}
```

## Best Practices

### 1. Use Natural Language

✅ **Good**:
```java
I18n.translate("Welcome to our application!")
I18n.translate("User not found")
I18n.translate("Delete user confirmation")
```

❌ **Avoid**:
```java
I18n.translate("welcome.message")
I18n.translate("user.not.found")
I18n.translate("delete.user.confirmation")
```

### 2. Provide Context for Ambiguous Terms

```java
// Use context to disambiguate
I18n.context("button").translate("Submit")
I18n.context("form").translate("Submit")
I18n.context("email").translate("Submit")
```

### 3. Handle Pluralization Properly

```java
// Use plural builder for count-dependent messages
I18n.plural(itemCount)
    .zero("No items")
    .one("One item")
    .other("{} items")
    .format()
```

### 4. Use Annotations for Static Analysis

```java
@Translatable("Welcome message")
public static final String WELCOME = "Welcome message";

@Message("User not found")
public static String userNotFound() {
    return "User not found";
}
```

### 5. Organize Messages

Create dedicated message classes for better organization:

```java
public class UserMessages {
    
    @Translatable("User Management")
    public static final String TITLE = "User Management";
    
    @Translatable("Manage application users")
    public static final String DESCRIPTION = "Manage application users";
    
    @Message("User not found")
    public static String userNotFound() {
        return "User not found";
    }
}
```

### 6. Handle Missing Translations Gracefully

```java
// The library automatically falls back to original text
String message = I18n.translate("This will show in English if no translation exists");
```

## Troubleshooting

### Common Issues

#### 1. Messages Not Being Extracted

**Problem**: Messages aren't appearing in PO files.

**Solutions**:
- Check that the Maven plugin is configured correctly
- Verify source directories are included in configuration
- Ensure messages use `I18n.translate()` or annotations
- Check file patterns in plugin configuration

#### 2. Translations Not Loading

**Problem**: Runtime translations aren't working.

**Solutions**:
- Verify PO files are compiled to runtime format
- Check output directory configuration
- Ensure message source is properly initialized
- Check file paths and classpath

#### 3. Locale Not Switching

**Problem**: Application locale doesn't change.

**Solutions**:
- Verify web interceptor is enabled
- Check locale parameter configuration
- Ensure session handling is configured
- Verify Accept-Language header handling

#### 4. Performance Issues

**Problem**: Slow translation lookup.

**Solutions**:
- Use binary format for better performance
- Enable warm-up in configuration
- Consider caching strategies
- Use compiled translations instead of PO files at runtime

### Debug Configuration

Enable debug logging to troubleshoot issues:

```yaml
logging:
  level:
    io.github.unattendedflight.fluent.i18n: DEBUG
    io.github.unattendedflight.fluent.i18n.extractor: DEBUG
    io.github.unattendedflight.fluent.i18n.compiler: DEBUG
```

### Validation

Use the validate goal to check your setup:

```bash
mvn fluent:i18n:validate
```

This will verify:
- PO file syntax
- Translation completeness
- Configuration validity
- File structure

---

For more help, check the [examples directory](fluent-i18n-examples/) for complete working applications, or open an issue on GitHub. 