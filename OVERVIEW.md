# Fluent i18n Overview

## What is Fluent i18n?

**Fluent i18n** is a revolutionary Java internationalization library that transforms how developers approach localization. Instead of using artificial translation keys, developers write code using natural language text, making internationalization more intuitive and maintainable.

## The Problem

Traditional i18n approaches have several pain points:

### 1. Artificial Keys
```java
// Traditional approach - hard to maintain
messageSource.getMessage("welcome.message", args, locale);
messageSource.getMessage("user.not.found", args, locale);
messageSource.getMessage("delete.user.confirmation", args, locale);
```

**Problems:**
- Keys are artificial and not human-readable
- Easy to make typos in keys
- Requires context switching between code and translation files
- Hard to maintain consistency

### 2. Complex Workflows
- Manual key management
- Separate translation files
- Difficult integration with translation tools
- Error-prone synchronization

### 3. Poor Developer Experience
- Learning curve for key naming conventions
- Debugging translation issues is difficult
- No IDE support for translation keys
- Hard to refactor

## The Solution

Fluent i18n introduces a **natural language first** approach:

```java
// Fluent i18n approach - natural and intuitive
I18n.translate("Welcome to our application!");
I18n.translate("User not found");
I18n.translate("Delete user confirmation");
```

**Benefits:**
- ✅ Self-documenting code
- ✅ No artificial keys to manage
- ✅ Natural language development
- ✅ Automatic extraction and compilation
- ✅ Standard PO file workflow for translators

## How It Works

### 1. Development Phase
Developers write code using natural language:

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

### 2. Build-Time Extraction
The Maven plugin automatically extracts all translatable messages:

```bash
mvn compile
```

This generates PO files like `messages_en.po`:

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

### 3. Translation Workflow
Translators work with standard PO files using familiar tools:
- **Poedit**: Desktop application
- **Lokalise**: Cloud-based platform
- **Any text editor**: PO files are plain text

### 4. Runtime Compilation
The plugin compiles PO files to efficient runtime formats:

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

### 5. Runtime Resolution
The library uses hash-based lookup for optimal performance:

```java
// Natural text is hashed for efficient lookup
String hash = hashGenerator.generateHash("Welcome to our application!");
TranslationResult result = messageSource.resolve(hash, naturalText, locale);
```

## Key Features

### 1. Natural Language API
```java
// Simple translation
String message = I18n.translate("Hello, world!");

// With parameters
String greeting = I18n.translate("Hello, {}!", userName);

// Short alias
String message = I18n.t("Welcome!");

// Context-aware translation
String button = I18n.context("button").translate("Submit");
String action = I18n.context("form").translate("Submit");
```

### 2. Pluralization Support
```java
// Handle different plural forms for different languages
String count = I18n.plural(itemCount)
    .zero("No items")
    .one("One item")
    .other("{} items")
    .format();
```

### 3. Annotations for Static Analysis
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

### 4. Template Integration
```html
<!-- Thymeleaf integration -->
<h1 th:text="${@i18nTemplateUtils.translate('Welcome')}">Welcome</h1>
<p th:text="${@i18nTemplateUtils.translate('Hello, {}!', userName)}">Hello message</p>
```

### 5. Spring Boot Integration
```yaml
fluent:
  i18n:
    enabled: true
    default-locale: en
    supported-locales: [en, es, fr, de]
    message-source:
      type: json
      basename: i18n/messages
```

## Architecture

### Core Components

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

### Module Structure

```
fluent-i18n/
├── fluent-i18n-core/                    # Core functionality
│   ├── I18n.java                       # Main entry point
│   ├── annotation/                      # Annotations
│   ├── compiler/                        # PO file compilation
│   ├── core/                           # Core i18n functionality
│   ├── extractor/                       # Message extraction
│   └── maven/                          # Maven plugin
├── fluent-i18n-spring-boot-starter/     # Spring Boot integration
└── fluent-i18n-examples/                # Example applications
```

### Extension Points

The library is designed to be extensible:

1. **Custom Extractors**: Support for new file types
2. **Custom Output Writers**: New runtime formats
3. **Custom Hash Generators**: Different hashing strategies
4. **Custom Message Sources**: Specialized resolution logic

## Benefits

### For Developers

1. **Natural Development**: Write code in your native language
2. **No Key Management**: Automatic hash-based keys
3. **Better IDE Support**: Natural language is easier to work with
4. **Reduced Errors**: No typos in artificial keys
5. **Faster Development**: Less context switching

### For Translators

1. **Standard Tools**: Use familiar PO file editors
2. **Context Information**: Natural text provides context
3. **No Key Learning**: Work with actual text, not codes
4. **Better Quality**: Natural text is easier to translate accurately

### For Teams

1. **Simplified Workflow**: Automatic extraction and compilation
2. **Better Collaboration**: Natural language bridges technical gaps
3. **Reduced Maintenance**: No manual key management
4. **Standard Process**: Industry-standard PO file workflow

## Supported Languages

Fluent i18n supports 16+ languages with proper pluralization rules:

- **English** (en)
- **Spanish** (es)
- **French** (fr)
- **German** (de)
- **Italian** (it)
- **Portuguese** (pt)
- **Dutch** (nl)
- **Polish** (pl)
- **Russian** (ru)
- **Chinese** (zh)
- **Japanese** (ja)
- **Korean** (ko)
- **Norwegian** (nb)
- **Swedish** (sv)
- **Danish** (da)
- **Finnish** (fi)

## Performance Features

1. **Hash-Based Lookup**: O(1) translation resolution
2. **Caching**: Generated hashes and resolved messages are cached
3. **Lazy Loading**: Translation files loaded on demand
4. **Multiple Formats**: JSON, Properties, and Binary output
5. **Thread-Safe**: Concurrent access support

## Getting Started

### Quick Start

1. **Add Dependencies**:
```xml
<dependency>
    <groupId>io.github.unattendedflight.fluent</groupId>
    <artifactId>fluent-i18n-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. **Configure Maven Plugin**:
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

3. **Write Natural Language Code**:
```java
String message = I18n.translate("Welcome to our application!");
```

4. **Build and Extract**:
```bash
mvn compile
```

## Comparison with Traditional i18n

| Aspect | Traditional i18n | Fluent i18n |
|--------|------------------|--------------|
| **Development** | Artificial keys | Natural language |
| **Maintenance** | Manual key management | Automatic extraction |
| **Translator Experience** | Learn key conventions | Work with natural text |
| **IDE Support** | Limited | Full natural language support |
| **Error Prevention** | Error-prone | Self-documenting |
| **Workflow** | Custom processes | Standard PO files |
| **Performance** | String-based lookup | Hash-based lookup |

## Use Cases

### 1. Web Applications
- Spring Boot applications
- REST APIs
- Template-based views

### 2. Desktop Applications
- JavaFX applications
- Swing applications
- Command-line tools

### 3. Microservices
- Service-to-service communication
- API responses
- Logging and monitoring

### 4. Libraries and Frameworks
- Framework messages
- Error messages
- Documentation

## Migration from Traditional i18n

### Step 1: Replace Key-Based Calls
```java
// Old way
messageSource.getMessage("welcome.message", args, locale);

// New way
I18n.translate("Welcome to our application!", args);
```

### Step 2: Update Annotations
```java
// Old way
@Value("${welcome.message}")
private String welcomeMessage;

// New way
@Translatable("Welcome to our application")
public static final String WELCOME = "Welcome to our application";
```

### Step 3: Configure Build Process
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

## Future Roadmap

### Planned Features

1. **Kotlin Support**: Native Kotlin integration
2. **Gradle Plugin**: Gradle build system support
3. **Database Integration**: Translation storage in databases
4. **Cloud Integration**: Translation management platforms
5. **Advanced Pluralization**: More complex plural rules
6. **Context Validation**: Automatic context validation
7. **Translation Memory**: Reuse of existing translations

### Community Goals

1. **Wider Language Support**: More languages and locales
2. **Framework Integrations**: More framework support
3. **Tool Ecosystem**: IDE plugins and tools
4. **Best Practices**: Community-driven guidelines
5. **Performance Optimization**: Enhanced performance features

## Conclusion

Fluent i18n represents a paradigm shift in Java internationalization. By prioritizing natural language development, it eliminates the complexity and error-proneness of traditional key-based approaches while maintaining the power and flexibility that developers need.

The library's architecture ensures that it's not just easier to use, but also more maintainable, performant, and scalable. Whether you're building a small application or a large-scale system, Fluent i18n provides the tools you need to create truly internationalized software.

**Ready to write code in your native language?** Get started with Fluent i18n today! 🚀 