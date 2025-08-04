package io.github.unattendedflight.fluent.i18n.springboot.fluenti18n;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Configuration properties for Fluent I18n.
 */
@ConfigurationProperties(prefix = "fluent.i18n")
public class FluentI18nProperties {
    
    /**
     * Indicates whether the Fluent I18n system is enabled or disabled.
     *
     * This flag is primarily used to determine if the internationalization (I18n)
     * features provided by the Fluent I18n framework are active within the application.
     *
     * When set to {@code true}, the system is active, allowing localization
     * actions, such as resolving messages and setting locales, to be performed.
     * Conversely, when set to {@code false}, the I18n functionality is effectively
     * disabled, and no localization features will be applied.
     */
    private boolean enabled = true;
    
    /**
     * The default locale used for the internationalization (I18n) system when no specific
     * locale is determined from other sources like URL parameters, session attributes,
     * or the "Accept-Language" header.
     *
     * This variable is initialized to {@link Locale#ENGLISH} with extensions stripped
     * to ensure compatibility with the I18n framework and prevent potential issues
     * caused by locale extensions.
     *
     * It acts as a fallback locale for resolving messages and determining language preferences
     * in cases where no other locale can be resolved, ensuring consistent behavior
     * across the application.
     */
    private Locale defaultLocale = Locale.ENGLISH.stripExtensions();
    
    /**
     * Defines the set of locales supported by the application for internationalization purposes.
     *
     * The locales in this set are used to determine which languages the application can handle,
     * such as resolving translations and ensuring consistency in the supported regions. By default,
     * this variable contains a single supported locale: English with all extensions removed.
     *
     * This set is utilized in various components, such as locale resolution logic in web requests,
     * ensuring the application only processes translations for predefined locales.
     */
    private Set<Locale> supportedLocales = Set.of(Locale.ENGLISH.stripExtensions());
    
    /**
     * Represents the configuration for the message source used in the internationalization (I18n) framework.
     *
     * This variable holds an instance of the {@code MessageSource} class, which provides details about how
     * translation messages are sourced and managed within the application. It includes configuration options
     * to define the type of message source (e.g., JSON, properties, binary), the base name for message files,
     * cache duration for translations, fallback behavior, and file encoding.
     *
     * The {@code messageSource} is a key component for enabling I18n functionality and is used to set up
     * the message resolution mechanism according to the application's localization requirements.
     */
    private MessageSource messageSource = new MessageSource();

    /**
     * Represents the warm-up configuration used to pre-load certain aspects of the
     * internationalization (I18n) system at application startup.
     *
     * The {@code warmUp} property encapsulates settings such as whether warm-up
     * functionality is enabled and defines the set of locales to be pre-loaded.
     * Pre-loading can improve runtime performance by initializing resources needed
     * for internationalization before requests are processed.
     *
     * By default, warm-up is enabled, and all supported locales are pre-loaded
     * unless a specific set of locales is defined.
     */
    private WarmUp warmUp = new WarmUp();
    
    /**
     * Represents the web configuration for the Fluent I18n system.
     *
     * This variable holds an instance of the {@link Web} class, which contains
     * properties related to integrating locale management into web applications,
     * such as enabling web support, configuring locale resolution behavior, and
     * determining how locales are stored and handled during web requests.
     *
     * The {@link Web} class provides options for enabling/disabling web integration,
     * specifying the parameter name for locale switching, using the Accept-Language
     * header, utilizing sessions for locale persistence, and setting the
     * Content-Language header in responses.
     */
    private Web web = new Web();
    
    /**
     * Represents the configuration settings for extraction of translatable items
     * in the internationalization (I18n) system.
     *
     * This variable holds an instance of the {@link Extraction} class, which defines
     * the parameters and patterns used to identify and process translatable components.
     * These include method call patterns, annotation patterns, and template patterns,
     * as well as configuration for enabling or disabling extraction and specifying
     * the source encoding.
     *
     * The {@link Extraction} component is particularly useful for scanning and
     * processing application codebases, identifying translatable strings in
     * different contexts, and facilitating their extraction for translation.
     */
    private Extraction extraction = new Extraction();
    
    /**
     * Represents the configuration for the compilation of translations.
     *
     * This field is an instance of the nested {@code Compilation} class, which provides various
     * configurable options for the translation compilation process, such as output format,
     * validation, preservation of existing translations, and more.
     *
     * The default configuration for the {@code Compilation} instance includes:
     * - Output format set to "json".
     * - Validation of translations enabled.
     * - Preservation of existing translations enabled.
     * - Minification of the output disabled.
     * - Inclusion of metadata in the output enabled.
     *
     * This field is utilized as part of the {@code FluentI18nProperties} class to manage
     * compilation-specific properties for the internationalization framework.
     */
    private Compilation compilation = new Compilation();
    
    /**
     * Retrieves the current enabled status of the configuration.
     *
     * @return true if the configuration is enabled, false otherwise
     */
    // Getters and setters
    public boolean isEnabled() { return enabled; }
    /**
     * Sets the enabled status of the FluentI18nProperties instance.
     *
     * @param enabled a boolean value indicating whether the configuration is enabled (true)
     *                or disabled (false)
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    /**
     * Retrieves the default locale configured in the application.
     *
     * This locale represents the fallback or base locale to be used when no specific locale
     * is specified or when the requested locale is not supported.
     *
     * @return the configured default {@link Locale} of the application
     */
    public Locale getDefaultLocale() { return defaultLocale; }
    /**
     * Sets the default locale to be used for the internationalization configuration.
     *
     * @param defaultLocale the Locale to set as the default locale
     */
    public void setDefaultLocale(Locale defaultLocale) { this.defaultLocale = defaultLocale; }
    
    /**
     * Retrieves the set of locales supported by the application for internationalization.
     *
     * @return a set of {@link Locale} objects representing the supported locales
     */
    public Set<Locale> getSupportedLocales() { return supportedLocales; }
    /**
     * Sets the supported locales for internationalization.
     *
     * @param supportedLocales a set of {@link Locale} objects defining the supported locales
     *                         for the application
     */
    public void setSupportedLocales(Set<Locale> supportedLocales) { this.supportedLocales = supportedLocales; }
    
    /**
     * Retrieves the {@link MessageSource} instance.
     *
     * @return the message source object, which provides configuration and properties
     *         for managing translations, such as type of source, base name, caching, and encoding.
     */
    public MessageSource getMessageSource() { return messageSource; }
    /**
     * Sets the message source configuration for internationalization.
     *
     * The MessageSource object defines the properties and behavior of the message source,
     * including message file type, basename, caching, fallback options, and encoding.
     * This method allows setting a specific message source to customize the way
     * translations and localization data are handled.
     *
     * @param messageSource the MessageSource object that contains configuration details
     *                      for resolving and caching translations.
     */
    public void setMessageSource(MessageSource messageSource) { this.messageSource = messageSource; }
    
    /**
     * Retrieves the {@link Web} configuration object, which contains properties for web integration.
     *
     * @return the {@link Web} configuration object that includes properties such as enabling web integration,
     *         locale parameter for switching, usage of Accept-Language header, session storage of locale,
     *         and setting the Content-Language header.
     */
    public Web getWeb() { return web; }
    /**
     * Sets the web integration properties for the FluentI18n module.
     *
     * @param web the {@code Web} instance containing configuration options
     *            for web integration, such as enabling/disabling features,
     *            locale parameter names, and session or header settings.
     */
    public void setWeb(Web web) { this.web = web; }

    /**
     * Retrieves the {@link WarmUp} configuration for the application.
     *
     * @return the {@link WarmUp} instance that holds information about the warm-up settings,
     *         including whether warm-up is enabled and the locales to warm up.
     */
    public WarmUp getWarmUp() { return warmUp; }
    /**
     * Sets the warm-up configuration for the application.
     *
     * @param warmUp the {@code WarmUp} configuration to be set, which specifies
     *               whether warm-up is enabled and the locales to warm up.
     */
    public void setWarmUp(WarmUp warmUp) { this.warmUp = warmUp; }
    
    /**
     * Retrieves the {@code Extraction} configuration instance.
     * This configuration contains various settings related to the extraction process,
     * including its enabled state, source encoding, and patterns for method calls,
     * annotations, and templates used in the internationalization (I18n) system.
     *
     * @return the {@code Extraction} instance containing the extraction settings
     */
    public Extraction getExtraction() { return extraction; }
    /**
     * Sets the extraction configuration for the internationalization (I18n) properties.
     *
     * @param extraction the {@link Extraction} object that defines settings such as whether extraction is enabled,
     *                   source encoding, and patterns for method calls, annotations, or templates related to translation.
     */
    public void setExtraction(Extraction extraction) { this.extraction = extraction; }
    
    /**
     * Retrieves the current Compilation configuration for the system.
     *
     * @return the Compilation object, which contains configuration settings such as output format,
     *         validation settings, metadata preferences, and other properties related to the compilation process.
     */
    public Compilation getCompilation() { return compilation; }
    /**
     * Sets the compilation configuration for the FluentI18n system.
     *
     * @param compilation the {@code Compilation} object containing the configuration
     *                    details such as output format, validation settings, and metadata
     *                    preferences for the compilation process
     */
    public void setCompilation(Compilation compilation) { this.compilation = compilation; }
    
    /**
     * Represents the configuration for managing message sources used in
     * an internationalization (I18n) system.
     *
     * The {@code MessageSource} class allows configuring various properties
     * related to message file handling, such as file type, base name, cache
     * duration, encoding, and fallback options. It provides the necessary
     * parameters for resolving translations and managing localization data.
     */
    public static class MessageSource {
        /**
         * Specifies the file type for message source files used in the internationalization (I18n) system.
         * Typically, this value determines the format of the message files (e.g., "json", "properties", "xml").
         *
         * By default, this is set to "json", indicating that the message files are expected to use
         * JSON format for storing translations. This value can be modified to suit different
         * formats supported within the system.
         */
        private String type = "json";
        
        /**
         * Specifies the base name of the resource bundle or message source used for
         * internationalization (I18n) purposes.
         *
         * This variable typically defines the starting path or identifier used to
         * locate message property files or equivalent resources that contain translations.
         * For example, if the base name is set to "i18n/messages" and the application's
         * default locale is English, the system might load a resource file named
         * "i18n/messages_en.properties" or an equivalent format, depending on the
         * underlying implementation.
         *
         * The base name provides flexibility to define and organize translation resources
         * effectively, while supporting locale-specific variations and fallback mechanisms.
         */
        private String basename = "i18n/messages";
        
        /**
         * Represents the duration for which the message source's cached translations remain valid.
         *
         * This property determines how long translations loaded from the message source
         * should be retained in memory before being considered stale and reloaded. A longer
         * cache duration can improve performance by reducing the frequency of reloads, while
         * a shorter duration ensures that updates to the underlying resource files are quickly
         * reflected.
         *
         * Defaults to 1 hour.
         */
        private Duration cacheDuration = Duration.ofHours(1);
        
        /**
         * Determines whether the original message key should be used as a fallback
         * when a translation is missing for the specified locale.
         *
         * If set to {@code true}, the I18n system will return the original message key
         * as a fallback instead of returning {@code null} or throwing an error when
         * a corresponding translation cannot be resolved. This is useful in scenarios
         * where failing silently with the original text is preferable.
         *
         * Default value is {@code true}.
         */
        private boolean useOriginalAsFallback = true;
        
        /**
         * Indicates whether missing translations should be logged.
         *
         * When set to {@code true}, the system logs warnings or notifications for
         * translations that are not found in the message source. This feature
         * helps in identifying missing internationalization keys during development
         * or runtime.
         */
        private boolean logMissingTranslations = true;
        
        /**
         * Specifies the character encoding used to read and write message files in the internationalization (I18n) system.
         *
         * This variable determines the byte-to-character and character-to-byte conversions that are applied when managing
         * translation files. The default value is "UTF-8", which supports a comprehensive range of characters from
         * various languages and scripts, ensuring compatibility and proper representation of internationalized messages.
         *
         * Changing this value impacts how the message source interprets and writes content. It should match the encoding
         * used in the translation files to avoid data corruption or misinterpretation of characters.
         */
        private String encoding = "UTF-8";
        
        /**
         * Retrieves the type of the message source configuration.
         *
         * @return the type of the message source, typically indicating the file format (e.g., "json").
         */
        // Getters and setters
        public String getType() { return type; }
        /**
         * Sets the file type used for message source configuration in the internationalization (I18n) system.
         * The file type determines the format in which message files are read and managed (e.g., "json", "xml", etc.).
         *
         * @param type the file type to be used for message source configuration
         */
        public void setType(String type) { this.type = type; }
        
        /**
         * Retrieves the base name used for locating the message source files in the
         * internationalization (I18n) system.
         *
         * The base name is typically a prefix that identifies the set of message
         * definition files that are used for resolving localized messages.
         *
         * @return the base name as a {@code String}, used to identify the location
         *         of message source files for I18n.
         */
        public String getBasename() { return basename; }
        /**
         * Sets the base name of the message source. The base name is typically used
         * to determine the location of the message resource files (e.g., `basename`
         * corresponds to files like `basename_en.properties`). This property
         * configures the primary file or directory to be used for resolving
         * message translations.
         *
         * @param basename the base name to set, representing the prefix or root
         *                 path for message resource files
         */
        public void setBasename(String basename) { this.basename = basename; }
        
        /**
         * Retrieves the duration for which message source caching is enabled.
         *
         * The cache duration defines how long the resolved messages are retained in memory
         * before being refreshed or invalidated. This helps in optimizing performance
         * by reducing repeated access to underlying message resources.
         *
         * @return the cache duration as a {@link Duration}, specifying the time period
         *         for caching message source data.
         */
        public Duration getCacheDuration() { return cacheDuration; }
        /**
         * Sets the cache duration for message source localization data.
         * The cache duration determines how long the message source will
         * cache the resolved translations before refreshing them.
         *
         * @param cacheDuration the duration for caching translations, specified as a {@link Duration}
         */
        public void setCacheDuration(Duration cacheDuration) { this.cacheDuration = cacheDuration; }
        
        /**
         * Determines whether the original text should be used as a fallback when no translation
         * is found for a specific locale.
         *
         * This property is especially useful in cases where untranslated text should
         * remain readable to the end user by displaying the original text instead of
         * an empty or absent translation.
         *
         * @return true if the original text should be used as a fallback when a translation
         *         is unavailable; false otherwise.
         */
        public boolean isUseOriginalAsFallback() { return useOriginalAsFallback; }
        /**
         * Configures whether the original untranslated value should be used as a fallback
         * when a translation is not available.
         *
         * @param useOriginalAsFallback true to use the original untranslated value as a fallback,
         *                              false to disable this behavior
         */
        public void setUseOriginalAsFallback(boolean useOriginalAsFallback) {
            this.useOriginalAsFallback = useOriginalAsFallback; 
        }
        
        /**
         * Checks whether logging of missing translations is enabled.
         *
         * This method returns the value of the {@code logMissingTranslations} property,
         * which determines if the system should log cases where a translation is missing.
         *
         * @return true if logging of missing translations is enabled, false otherwise
         */
        public boolean isLogMissingTranslations() { return logMissingTranslations; }
        /**
         * Sets whether missing translations should be logged.
         *
         * This method allows enabling or disabling the logging of missing translations
         * encountered during the resolution process. If enabled, missing translations
         * are logged, which can help in identifying untranslated messages during
         * runtime. Otherwise, missing translations are silently ignored.
         *
         * @param logMissingTranslations a boolean value indicating whether to log missing translations;
         *                                {@code true} to enable logging of missing translations,
         *                                {@code false} to disable it
         */
        public void setLogMissingTranslations(boolean logMissingTranslations) {
            this.logMissingTranslations = logMissingTranslations; 
        }
        
        /**
         * Retrieves the encoding used for reading message source files.
         *
         * @return the encoding as a string, typically representing a character set
         *         (e.g., "UTF-8") for interpreting text in message source files
         */
        public String getEncoding() { return encoding; }
        /**
         * Sets the encoding used for reading and processing message source files.
         *
         * The encoding determines how characters are represented when interacting with
         * message resources, ensuring proper handling of localized text.
         *
         * @param encoding the character encoding to be used, such as "UTF-8" or "ISO-8859-1"
         */
        public void setEncoding(String encoding) { this.encoding = encoding; }
    }

    /**
     * The WarmUp class provides configuration for enabling or disabling a warm-up
     * process and specifying the locales to warm up.
     */
    public static class WarmUp {
        /**
         * Indicates whether the warm-up process is enabled.
         *
         * When set to {@code true}, the warm-up process will be executed,
         * preparing necessary resources or configurations before the system
         * becomes fully operational. If set to {@code false}, the warm-up
         * process is disabled.
         */
        private boolean enabled = true;

        /**
         * A set of {@link Locale} objects specifying the target locales for the warm-up process.
         *
         * This field determines which locales will be initialized or preloaded if the warm-up process
         * is enabled. It is used to fine-tune the behavior of the warm-up mechanism by explicitly defining
         * the set of locales to be considered for initialization.
         *
         * The default value is an immutable empty set, indicating that no specific locales are configured by default.
         * This behavior can be modified by explicitly setting this field with a non-empty set of locales.
         */
        private Set<Locale> locales = Set.of();

        /**
         * Checks whether the warm-up process is enabled.
         *
         * @return true if the warm-up process is enabled, false otherwise
         */
        // Getters and setters
        public boolean isEnabled() { return enabled; }
        /**
         * Sets whether the warm-up process is enabled.
         *
         * @param enabled a boolean value indicating if the warm-up process should be enabled (true) or disabled (false)
         */
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        /**
         * Retrieves the set of locales that are to be used or configured for the warm-up process.
         *
         * @return a set of {@link Locale} instances representing the configured locales
         */
        public Set<Locale> getLocales() { return locales; }
        /**
         * Sets the set of locales to be used for warm-up configuration.
         *
         * @param locales the set of {@link Locale} objects to be set; these represent the locales to warm up
         */
        public void setLocales(Set<Locale> locales) { this.locales = locales; }
    }
    
    /**
     * Represents the web integration configuration settings for the application.
     *
     * This class provides various configuration options to manage how the application
     * interacts with web-based locales, HTTP headers, and session management. The settings
     * include enabling or disabling web integration, defining locale parameter names,
     * and configuring response headers for content localization.
     */
    public static class Web {
        /**
         * Indicates whether the web integration functionality is enabled.
         *
         * This flag is used to determine if the application should process web-based
         * integration aspects such as locale resolution, session management, and
         * Accept-Language header handling. If set to {@code true}, the integration is
         * active; otherwise, it is disabled.
         */
        private boolean enabled = true;
        
        /**
         * Represents the HTTP request parameter name used to determine the locale in the application.
         *
         * This variable specifies the name of the query parameter, such as "lang" or "locale",
         * that can be appended to HTTP requests to explicitly set the desired locale for
         * internationalization (I18n) purposes.
         *
         * For example, when the name is set to "lang", a request like `/example?lang=en`
         * can be used to select the English locale. The value is typically processed by handlers
         * such as interceptors to resolve and set the current locale for the application's context.
         *
         * The parameter provides flexibility in enabling users or applications to define or override
         * the locale via URL query strings.
         */
        private String localeParameter = "lang";
        
        /**
         * Indicates whether the application should utilize the `Accept-Language` HTTP header
         * to determine the preferred locale for the current request.
         *
         * If set to {@code true}, the application will parse and interpret the `Accept-Language`
         * header provided by the client to resolve the locale. This approach enables automatic
         * locale detection based on the client's browser or system settings.
         *
         * If set to {@code false}, the `Accept-Language` header will be ignored, and other
         * mechanisms, such as URL parameters or session attributes, will be used to determine
         * the locale.
         *
         * This setting is specifically applicable when localizing content for web-based
         * integrations and is part of the internationalization configuration for the application.
         */
        private boolean useAcceptLanguageHeader = true;
        
        /**
         * Indicates whether session-based locale storage should be enabled or not.
         *
         * When set to {@code true}, the application stores the user's locale information
         * in the HTTP session, allowing subsequent requests from the same session to
         * reuse the stored locale. This can improve performance by avoiding the need to
         * resolve the locale from request parameters or headers repeatedly.
         *
         * When set to {@code false}, the application does not use session storage for locale
         * information, relying solely on other mechanisms such as request parameters or
         * `Accept-Language` headers for locale resolution.
         */
        private boolean useSession = true;
        
        /**
         * Indicates whether the HTTP response should include the `Content-Language` header.
         *
         * When this flag is set to {@code true}, the application explicitly sets the
         * `Content-Language` response header based on the resolved locale. This helps
         * inform the client about the language used in the response content.
         *
         * If set to {@code false}, the header will not be included in the HTTP response,
         * which might be desirable in cases where the language information should not
         * be exposed or is irrelevant.
         */
        private boolean setContentLanguageHeader = true;
        
        /**
         * Retrieves the current enabled state of the web integration configuration.
         *
         * @return true if the web integration is enabled; false otherwise
         */
        // Getters and setters
        public boolean isEnabled() { return enabled; }
        /**
         * Enables or disables the feature for web integration configuration.
         *
         * This method sets the state of the `enabled` parameter, which determines if the web
         * integration functionality is active. The `enabled` property controls whether specific
         * web-based configurations, such as locale handling and HTTP headers, are applied.
         *
         * @param enabled a boolean value indicating whether web integration is enabled (true)
         *                or disabled (false)
         */
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        /**
         * Retrieves the name of the parameter used in web requests to specify the desired locale.
         * This parameter name is typically included in URL query strings to determine the user's locale preference.
         *
         * @return the name of the locale parameter as a String
         */
        public String getLocaleParameter() { return localeParameter; }
        /**
         * Sets the name of the locale parameter used for resolving the locale in web requests.
         * This parameter is typically passed as a query string in HTTP requests to indicate
         * the desired locale for the response.
         *
         * @param localeParameter the name of the locale parameter, e.g., "lang" or "locale"
         */
        public void setLocaleParameter(String localeParameter) { this.localeParameter = localeParameter; }
        
        /**
         * Indicates whether the application should use the `Accept-Language` header
         * sent by the client to determine the locale.
         *
         * This configuration option is typically used to enable or disable the
         * automatic locale determination based on the HTTP `Accept-Language` header.
         *
         * @return true if the `Accept-Language` header is used for locale resolution, false otherwise
         */
        public boolean isUseAcceptLanguageHeader() { return useAcceptLanguageHeader; }
        /**
         * Configures whether the "Accept-Language" HTTP header should be used for locale resolution.
         *
         * When set to {@code true}, the application will attempt to determine the user's preferred locale
         * based on the "Accept-Language" header provided by the client in the HTTP request.
         * When set to {@code false}, the "Accept-Language" header will not be considered during
         * locale resolution.
         *
         * @param useAcceptLanguageHeader a boolean indicating whether to use the "Accept-Language" header
         *                                for locale resolution. {@code true} to enable, {@code false} to disable.
         */
        public void setUseAcceptLanguageHeader(boolean useAcceptLanguageHeader) {
            this.useAcceptLanguageHeader = useAcceptLanguageHeader; 
        }
        
        /**
         * Checks whether session usage is enabled for managing locale information.
         *
         * @return {@code true} if session usage is enabled to store and retrieve
         *         locale information; {@code false} otherwise.
         */
        public boolean isUseSession() { return useSession; }
        /**
         * Sets whether session-based locale resolution is enabled or disabled.
         *
         * This method allows enabling or disabling the usage of HTTP sessions for
         * storing and retrieving locale information. When enabled, the locale
         * determined for a user request may be stored in the session, enabling
         * the same locale to be reused for subsequent requests. When disabled,
         * alternate mechanisms (e.g., URL parameters or HTTP headers) must be
         * used for locale resolution.
         *
         * @param useSession a boolean indicating whether session-based locale
         *                   resolution is enabled (true) or disabled (false)
         */
        public void setUseSession(boolean useSession) { this.useSession = useSession; }
        
        /**
         * Checks whether the response should include the Content-Language header.
         *
         * This method returns the value of the {@code setContentLanguageHeader} property,
         * which determines if the web response should contain the Content-Language HTTP header
         * based on the configured locale settings.
         *
         * @return {@code true} if the Content-Language header should be included in responses;
         *         {@code false} otherwise.
         */
        public boolean isSetContentLanguageHeader() { return setContentLanguageHeader; }
        /**
         * Configures whether the Content-Language header should be set in HTTP responses.
         * When enabled, the application will include the Content-Language header, which
         * specifies the natural language of the content provided in the response. This
         * setting can be used to enhance content localization support in web-based environments.
         *
         * @param setContentLanguageHeader a boolean indicating if the Content-Language header
         *                                 should be included in HTTP responses (true to include,
         *                                 false to exclude)
         */
        public void setSetContentLanguageHeader(boolean setContentLanguageHeader) {
            this.setContentLanguageHeader = setContentLanguageHeader; 
        }
    }
    
    /**
     * Represents the configuration for the extraction process in the internationalization (I18n) system.
     * This configuration allows specifying patterns and settings used to extract translatable content
     * from source code, annotations, and templates.
     */
    public static class Extraction {
        /**
         * Indicates whether the extraction process is enabled or disabled.
         *
         * This flag is primarily used to determine whether the extraction functionality
         * associated with the containing class should be active or inactive during runtime.
         * When set to {@code true}, the extraction process is enabled. When set to
         * {@code false}, the extraction process is disabled.
         *
         * By default, this variable is set to {@code true}.
         */
        private boolean enabled = true;
        
        /**
         * Specifies the character encoding to be used for processing source text or files.
         * This encoding is utilized when reading source content, such as templates or other
         * text-based resources, to ensure proper interpretation of characters.
         *
         * By default, the encoding is set to "UTF-8", which is a widely supported encoding
         * capable of representing a broad range of characters from various languages.
         *
         * This property is particularly important in internationalization (I18n) workflows
         * where proper handling of multi-lingual content is required.
         */
        private String sourceEncoding = "UTF-8";
        
        /**
         * A list of regular expression patterns used to identify specific method calls related
         * to translation in the codebase. These patterns are utilized for tasks such as extracting
         * translatable text, analyzing the source code for localization purposes, or validating the
         * usage of translation methods in a project.
         *
         * Each pattern corresponds to a specific format of translating text:
         * 1. I18n.translate("key"): Matches direct calls to the `translate` method.
         * 2. I18n.t("key"): Matches shorthand calls to the `t` method used for translations.
         * 3. I18n.context(...).translate("key"): Matches calls to the `translate` method within a
         *    specific context provided by the `context` method.
         */
        private List<String> methodCallPatterns = List.of(
            "I18n\\.translate\\s*\\(\\s*\"([^\"]+)\"",
            "I18n\\.t\\s*\\(\\s*\"([^\"]+)\"",
            "I18n\\.context\\([^)]+\\)\\.translate\\s*\\(\\s*\"([^\"]+)\""
        );
        
        /**
         * A list of regular expression patterns used to identify specific annotations
         * within the source code. Each pattern is used to match and extract the necessary
         * information from annotations that are relevant to the internationalization (I18n) process.
         *
         * This list contains patterns for detecting common annotation formats that encapsulate
         * translatable text, such as "@Translatable" and "@Message". These annotations are often
         * used to mark strings for localization and enable streamlined extraction of such strings.
         */
        private List<String> annotationPatterns = List.of(
            "@Translatable\\s*\\(\\s*\"([^\"]+)\"",
            "@Message\\s*\\(\\s*\"([^\"]+)\""
        );
        
        /**
         * Holds patterns used to identify and extract translatable strings embedded
         * within template files.
         *
         * The list includes regular expressions that match specific patterns of text
         * commonly used for internationalization in templates.
         * These patterns are designed to recognize constructs that represent
         * dynamic translations.
         *
         * For example, patterns can target specific syntax like
         * `${@i18n.translate('text')}` or `th:text="${@i18n.translate('text')}"`
         * within templates.
         */
        private List<String> templatePatterns = List.of(
            "\\$\\{@i18n\\.translate\\('([^']+)'\\)\\}",
            "th:text=\"\\$\\{@i18n\\.translate\\('([^']+)'\\)\\}\""
        );
        
        /**
         * Retrieves the current enabled state.
         *
         * @return true if the feature or functionality is enabled, false otherwise
         */
        // Getters and setters
        public boolean isEnabled() { return enabled; }
        /**
         * Sets whether the feature, functionality, or component is enabled or disabled.
         *
         * @param enabled a boolean value indicating the enabled state; {@code true} to enable or {@code false} to disable
         */
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        /**
         * Retrieves the source encoding used for processing files or data within
         * the current context or configuration.
         *
         * @return the source encoding as a String, typically representing a character
         *         encoding (e.g., "UTF-8").
         */
        public String getSourceEncoding() { return sourceEncoding; }
        /**
         * Sets the source encoding used for processing files.
         * This encoding determines how character data is interpreted when reading or writing source files.
         *
         * @param sourceEncoding the source encoding to set, typically specified as a valid charset name (e.g., "UTF-8", "ISO-8859-1")
         */
        public void setSourceEncoding(String sourceEncoding) { this.sourceEncoding = sourceEncoding; }
        
        /**
         * Retrieves the list of method call patterns that define specific syntax patterns
         * for identifying translatable strings within method calls. These patterns are
         * typically represented as regular expressions and are used for parsing and
         * extracting internationalized strings.
         *
         * @return a list of regular expressions representing method call patterns
         */
        public List<String> getMethodCallPatterns() { return methodCallPatterns; }
        /**
         * Sets the list of method call patterns. These patterns define regular expressions
         * that are used to identify specific method calls in a codebase for purposes such as
         * localization or internationalization (I18n).
         *
         * @param methodCallPatterns a list of strings representing regular expressions for method call patterns
         */
        public void setMethodCallPatterns(List<String> methodCallPatterns) {
            this.methodCallPatterns = methodCallPatterns; 
        }
        
        /**
         * Retrieves the list of annotation patterns used for extracting translatable strings.
         *
         * These patterns are typically regular expressions that match specific annotations
         * in the code, allowing the system to identify and process translatable text within annotations.
         *
         * @return a list of strings representing the annotation patterns
         */
        public List<String> getAnnotationPatterns() { return annotationPatterns; }
        /**
         * Sets the list of annotation patterns used to extract specific textual information
         * from annotated code elements. These patterns are used as regular expressions
         * to identify and capture translatable strings or other target text based on annotations.
         *
         * @param annotationPatterns a list of regular expression patterns for extracting
         *                           information from annotations
         */
        public void setAnnotationPatterns(List<String> annotationPatterns) {
            this.annotationPatterns = annotationPatterns; 
        }
        
        /**
         * Retrieves the list of patterns used to identify template-based internationalization keys.
         *
         * @return a list of string patterns that are applied to detect i18n keys in templates
         */
        public List<String> getTemplatePatterns() { return templatePatterns; }
        /**
         * Sets the list of template patterns to be used.
         * Template patterns define the specific string patterns used for identifying and replacing
         * placeholders in template files during internationalization processing.
         *
         * @param templatePatterns a list of string patterns representing the template structure
         */
        public void setTemplatePatterns(List<String> templatePatterns) {
            this.templatePatterns = templatePatterns; 
        }
    }
    
    /**
     * The Compilation class encapsulates various configuration settings
     * related to the compilation process in the Fluent I18n system.
     *
     * It provides options to customize the output format, enable or disable
     * validations, preserve existing files, minify the output, and include
     * metadata during the compilation phase.
     */
    public static class Compilation {
        /**
         * Specifies the format for the output generated during the compilation process.
         * By default, the value is set to "json".
         *
         * The value of this variable determines the format of the resulting output
         * (e.g., JSON, XML, etc.), and it can be customized to match specific
         * requirements by using the provided setter method.
         */
        private String outputFormat = "json";
        
        /**
         * Indicates whether the validation process should be enabled or disabled.
         *
         * This variable typically serves as a flag to control the behavior of validation-related
         * operations within a system or application. When set to {@code true}, validation processes
         * or checks are enabled. When set to {@code false}, these validations are disabled,
         * potentially bypassing certain constraints or verifications.
         */
        private boolean validation = true;
        
        /**
         * Indicates whether existing output should be preserved during the compilation process.
         *
         * If set to {@code true}, the current state of existing output will not be overwritten,
         * ensuring that preexisting outputs remain intact. This can be useful in scenarios
         * where retaining previously generated files or data is desired.
         *
         * By default, this value is set to {@code true}.
         */
        private boolean preserveExisting = true;
        
        /**
         * Indicates whether the output generated by the compilation process should be minified.
         *
         * When set to {@code true}, the output (such as JSON or other formats) will be generated
         * in a compact form, removing unnecessary whitespace and formatting. This is often used
         * to reduce the size of the output for optimized storage or transmission.
         *
         * When set to {@code false}, the output will retain its standard formatting, which may
         * include whitespace and indentation for better readability.
         *
         * Default value is {@code false}.
         */
        private boolean minifyOutput = false;
        
        /**
         * Specifies whether metadata should be included during the compilation process.
         *
         * When set to {@code true}, the compilation process will include relevant metadata
         * in the generated output. This can be used to annotate or provide additional
         * details about the compilation results.
         *
         * When set to {@code false}, metadata will be omitted from the output.
         */
        private boolean includeMetadata = true;
        
        /**
         * Retrieves the output format used for the compilation process.
         *
         * @return the output format as a {@code String}
         */
        // Getters and setters
        public String getOutputFormat() { return outputFormat; }
        /**
         * Updates the output format for the compilation process.
         *
         * @param outputFormat the desired output format, such as "json", "xml", etc.
         */
        public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
        
        /**
         * Determines whether validation is enabled for the current instance.
         *
         * @return true if validation is enabled; false otherwise
         */
        public boolean isValidation() { return validation; }
        /**
         * Sets the validation flag for the compilation process.
         *
         * @param validation the new value for the validation flag;
         *                   true to enable validation, false to disable it
         */
        public void setValidation(boolean validation) { this.validation = validation; }
        
        /**
         * Checks whether existing outputs are preserved during the process.
         *
         * @return true if existing outputs are preserved, false otherwise
         */
        public boolean isPreserveExisting() { return preserveExisting; }
        /**
         * Sets whether existing configurations or data should be preserved during operations.
         *
         * @param preserveExisting a boolean value indicating if existing data should remain unchanged
         *                         (true to preserve, false to overwrite)
         */
        public void setPreserveExisting(boolean preserveExisting) { this.preserveExisting = preserveExisting; }
        
        /**
         * Determines whether the output should be minified.
         *
         * @return true if the output should be minified, false otherwise
         */
        public boolean isMinifyOutput() { return minifyOutput; }
        /**
         * Sets whether the output should be minified.
         *
         * @param minifyOutput a boolean indicating whether the output should be minified.
         *                      If true, the output will be minified; otherwise, it will not.
         */
        public void setMinifyOutput(boolean minifyOutput) { this.minifyOutput = minifyOutput; }
        
        /**
         * Determines whether metadata should be included.
         *
         * @return true if metadata is to be included, false otherwise
         */
        public boolean isIncludeMetadata() { return includeMetadata; }
        /**
         * Sets whether metadata should be included in the output.
         *
         * @param includeMetadata a boolean flag indicating whether metadata should be included
         */
        public void setIncludeMetadata(boolean includeMetadata) { this.includeMetadata = includeMetadata; }
    }
}