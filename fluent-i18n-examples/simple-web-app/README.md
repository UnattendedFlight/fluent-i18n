# Fluent i18n Demo Application

This example demonstrates how to use Fluent i18n for natural language internationalization in a Spring Boot web application.

## Features Demonstrated

- **Natural Text API**: Write human-readable strings directly in code
- **Automatic Extraction**: Maven plugin extracts translatable strings
- **PO File Workflow**: Standard gettext workflow for translators
- **Web Integration**: Locale switching via URL parameters, session, and headers
- **Template Support**: Thymeleaf integration with natural text

## Running the Application

1. Build the parent project first:
   ```bash
   cd ../..
   mvn clean install -DskipTests
   ```

2. Run the demo application:
   ```bash
   cd fluent-i18n-examples/simple-web-app
   mvn spring-boot:run
   ```

3. Open http://localhost:8080 in your browser

## Language Switching

- Click language links in the top-right corner
- Use URL parameter: `?lang=nb` for Norwegian
- The locale is stored in session and detected from Accept-Language header

## Maven Goals

Extract translatable messages:
```bash
mvn fluent-i18n:extract
```

Compile PO files to runtime format:
```bash
mvn fluent-i18n:compile
```

Validate translations:
```bash
mvn fluent-i18n:validate
```

Clean generated files:
```bash
mvn fluent-i18n:clean
```

## Generated Files

After running `mvn fluent-i18n:extract`:
- `src/main/resources/i18n/po/messages_en.po`
- `src/main/resources/i18n/po/messages_nb.po`
- `src/main/resources/i18n/po/messages_sv.po`
- `src/main/resources/i18n/po/messages_da.po`

After running `mvn fluent-i18n:compile`:
- `src/main/resources/i18n/messages_en.json`
- `src/main/resources/i18n/messages_nb.json`
- etc.

## Code Examples

### Java Controllers
```java
// Natural text with parameters
model.addAttribute("welcomeMessage", 
    I18n.translate("Welcome to our amazing application!"));

// Pluralization
I18n.plural(userCount)
    .zero("No users registered yet")
    .one("One user is using our app")
    .other("{} users are using our app")
    .format();

// Context-aware translations
I18n.context("order-status").translate(status);
```

### Thymeleaf Templates
```html
<!-- Direct translation -->
<h1 th:text="${@i18n.translate('Welcome to our application')}">Welcome</h1>

<!-- With parameters -->
<p th:text="${@i18n.translate('You have {} unread messages', unreadCount)}">Messages</p>
```

This demonstrates how Fluent i18n makes internationalization more developer-friendly by allowing natural language directly in code while maintaining professional translation workflows.