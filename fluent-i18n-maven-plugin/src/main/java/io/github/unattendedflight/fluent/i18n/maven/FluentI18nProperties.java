package io.github.unattendedflight.fluent.i18n.maven;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Configuration class for Fluent Internationalization (i18n) properties.
 * This class allows customization of localization functionality, including
 * enabling/disabling features, setting default locale, managing supported locales,
 * configuring message sources, and controlling web integration.
 * It also provides subconfigurations for warm-up procedures, extraction,
 * and compilation settings.
 */
public class FluentI18nProperties {
    
    /**
     * Indicates whether the Fluent I18n properties are enabled.
     *
     * This flag is used to control whether the internationalization
     * features provided by the Fluent I18n configuration should be
     * active. When set to {@code true}, the internationalization
     * system is enabled; when set to {@code false}, it is disabled.
     *
     * Default value: {@code true}.
     */
    private boolean enabled = true;
    
    /**
     * Represents the default locale used for internationalization.
     *
     * This locale determines the fallback language that will be used
     * if no specific locale is provided or if the requested locale is unsupported.
     *
     * The value of this field is initialized to a stripped default variant of
     * English (`Locale.ENGLISH.stripExtensions()`), ensuring that no extensions
     * are included in the locale specification.
     *
     * This field is mutable and can be modified via the corresponding setter
     * method to accommodate changes in localization requirements.
     */
    private Locale defaultLocale = Locale.ENGLISH.stripExtensions();
    
    /**
     * Represents the set of locales supported by the application for localization purposes.
     * This variable is initialized with a default set of supported locales, containing at least
     * the English locale without extensions.
     *
     * The contents of this set dictate which locale-specific resources are available and used
     * for message translations and other internationalization (i18n) functionality.
     *
     * This variable is intended to be configurable, allowing developers to specify a customized set
     * of supported locales as required by their application. It is used alongside other properties
     * for handling i18n features in the application.
     */
    private Set<Locale> supportedLocales = Set.of(Locale.ENGLISH.stripExtensions());
    
    /**
     * Configuration for the message source used in internationalization (i18n).
     * Controls aspects such as the type of message source, localization file base name,
     * caching behavior, fallback mechanism, and encoding.
     */
    private MessageSource messageSource = new MessageSource();

    /**
     * Represents a configuration for the warm-up process in Fluent i18n properties.
     *
     * This variable is an instance of the {@code WarmUp} class and is used to manage
     * properties related to the warm-up functionality, such as enabling or disabling
     * the warm-up process and specifying the set of locales involved.
     */
    private WarmUp warmUp = new WarmUp();
    
    /**
     * Configuration properties for web-related internationalization settings.
     *
     * The {@code Web} class provides options to customize the behavior of
     * web integration for locale determination, storage, and communication.
     * This includes enabling or disabling web integration, defining the
     * locale switching parameter, and managing the use of HTTP headers and
     * session storage for locale information.
     */
    private Web web = new Web();
    
    /**
     * Represents the configuration for the extraction process.
     * This includes settings such as whether extraction is enabled,
     * source encoding, and patterns for identifying elements
     * like method calls, annotations, or templates.
     */
    private Extraction extraction = new Extraction();
    
    /**
     * Represents the configuration for compilation of i18n resources.
     * The Compilation instance allows customization of various
     * aspects of the compilation process such as output format,
     * validation, and metadata inclusion.
     */
    private Compilation compilation = new Compilation();
    
    /**
     * Indicates whether the associated functionality or feature is enabled.
     *
     * @return true if the feature is enabled; false otherwise
     */
    // Getters and setters
    public boolean isEnabled() { return enabled; }
    /**
     * Sets the enabled state of the object.
     *
     * @param enabled a boolean indicating whether the feature should be enabled (true) or disabled (false)
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    /**
     * Retrieves the default locale configured for the application.
     *
     * @return the default locale as a {@code Locale} object
     */
    public Locale getDefaultLocale() { return defaultLocale; }
    /**
     * Sets the default locale for the application.
     *
     * @param defaultLocale the locale to set as the default. This value is used when no specific locale is provided for operations.
     */
    public void setDefaultLocale(Locale defaultLocale) { this.defaultLocale = defaultLocale; }
    
    /**
     * Retrieves the set of locales that are configured as supported.
     *
     * @return a set of {@link Locale} instances representing the supported locales
     */
    public Set<Locale> getSupportedLocales() { return supportedLocales; }
    /**
     * Sets the set of supported locales for the application.
     *
     * @param supportedLocales a set of {@code Locale} objects representing the locales supported
     *                         by the application. This determines the available options for
     *                         localized content and configurations.
     */
    public void setSupportedLocales(Set<Locale> supportedLocales) { this.supportedLocales = supportedLocales; }
    
    /**
     * Retrieves the {@link MessageSource} configuration that provides details
     * about the message source type, base name, cache duration, and other related properties.
     *
     * @return the {@link MessageSource} containing the configuration for message handling.
     */
    public MessageSource getMessageSource() { return messageSource; }
    /**
     * Sets the MessageSource configuration to provide localized messages.
     *
     * @param messageSource an instance of {@code MessageSource} containing properties
     *                      such as type, basename, cache duration, and settings for
     *                      handling translations and encoding
     */
    public void setMessageSource(MessageSource messageSource) { this.messageSource = messageSource; }

    /**
     * Retrieves the WarmUp configuration.
     *
     * @return the WarmUp configuration, which determines whether warm-up is enabled
     *         and specifies the locales for warm-up.
     */
    public WarmUp getWarmUp() { return warmUp; }
    /**
     * Sets the WarmUp configuration for fluent internationalization properties.
     * The WarmUp configuration includes properties to pre-load supported locales
     * and determine whether warm-up is enabled.
     *
     * @param warmUp the WarmUp object containing configuration details such as
     *               enabled status and pre-configured locales
     */
    public void setWarmUp(WarmUp warmUp) { this.warmUp = warmUp; }
    
    /**
     * Retrieves the web integration configuration.
     *
     * @return the instance of {@code Web} representing the configuration for web integration.
     */
    public Web getWeb() { return web; }
    /**
     * Sets the Web configuration for the FluentI18nProperties instance.
     *
     * @param web the Web configuration object containing settings for web integration
     */
    public void setWeb(Web web) { this.web = web; }
    
    /**
     * Retrieves the extraction configuration.
     *
     * @return the {@code Extraction} object containing extraction-related configuration settings.
     */
    public Extraction getExtraction() { return extraction; }
    /**
     * Sets the extraction configuration.
     *
     * @param extraction the extraction object containing settings such as
     *                   enabled status, source encoding, method call patterns,
     *                   annotation patterns, and template patterns
     */
    public void setExtraction(Extraction extraction) { this.extraction = extraction; }
    
    /**
     * Retrieves the compilation settings.
     *
     * @return the compilation configuration of type {@code Compilation}, which contains settings
     *         such as output format, validation preference, preservation of existing translations,
     *         minification option, and metadata inclusion.
     */
    public Compilation getCompilation() { return compilation; }
    /**
     * Sets the compilation configuration for the current properties.
     *
     * @param compilation the {@link Compilation} instance containing configuration
     *                    for output format, validation, preservation, minification,
     *                    and metadata inclusion.
     */
    public void setCompilation(Compilation compilation) { this.compilation = compilation; }
    
    /**
     * Represents the configuration for a message source used in localization and internationalization.
     * This class defines various properties for handling message files, caching, fallbacks,
     * and encoding to facilitate localized message retrieval.
     */
    public static class MessageSource {
        /**
         * Specifies the type of message source format used for localization and internationalization.
         * This determines the format in which localized message files are stored and interpreted.
         * Common examples include "json", "xml", "properties", etc.
         */
        private String type = "json";
        
        /**
         * Specifies the base name of the resource bundle used for retrieving localized messages.
         * This property defines the default location and prefix for message files
         * to be loaded for internationalization and localization purposes.
         * Example: "i18n/messages" would correspond to message files like "i18n/messages_en.properties".
         */
        private String basename = "i18n/messages";
        
        /**
         * Defines the duration for which message files will be cached.
         * This helps to enhance performance by reducing the need
         * to frequently reload message definitions from the source.
         * The default value is set to 1 hour.
         */
        private Duration cacheDuration = Duration.ofHours(1);
        
        /**
         * Indicates whether the original message should be used as a fallback
         * when a translation is missing for the requested locale.
         * If set to true, the system will return the original untranslated message
         * in cases where no localized message is available.
         * If set to false, it may return a null or alternative default response
         * depending on the implementation.
         */
        private boolean useOriginalAsFallback = true;
        
        /**
         * Indicates whether missing translations should be logged.
         * When set to true, the system logs entries for translations that
         * cannot be found in the message source files.
         */
        private boolean logMissingTranslations = true;
        
        /**
         * Specifies the character encoding used for reading and writing message files.
         * Commonly set to "UTF-8" to ensure compatibility with a wide range of characters
         * and languages in internationalization and localization processes.
         */
        private String encoding = "UTF-8";
        
        /**
         * Retrieves the type of the message source, typically representing the format
         * of message files (e.g., "json").
         *
         * @return the type of the message source as a string
         */
        // Getters and setters
        public String getType() { return type; }
        /**
         * Sets the type of message source configuration.
         *
         * @param type the new type of the message source, typically specifying the format
         *             of the source (e.g., 'json', 'properties', etc.)
         */
        public void setType(String type) { this.type = type; }
        
        /**
         * Retrieves the basename of the message source configuration, typically representing
         * the base path or prefix for the message files used in localization.
         *
         * @return the basename of the message source as a String
         */
        public String getBasename() { return basename; }
        /**
         * Sets the base name for the message source. The base name is used to locate
         * the resource bundle containing localized messages.
         *
         * @param basename the base name of the resource bundle (e.g., "i18n/messages").
         */
        public void setBasename(String basename) { this.basename = basename; }
        
        /**
         * Retrieves the cache duration for message files.
         *
         * @return the cache duration as a {@link Duration} object
         */
        public Duration getCacheDuration() { return cacheDuration; }
        /**
         * Sets the cache duration for the message source. This determines how long the
         * cached messages are retained before a reload is triggered.
         *
         * @param cacheDuration the duration for caching messages
         */
        public void setCacheDuration(Duration cacheDuration) { this.cacheDuration = cacheDuration; }
        
        /**
         * Indicates whether the original message should be used as a fallback when a
         * translation is missing.
         *
         * @return true if the original message is used as a fallback; false otherwise.
         */
        public boolean isUseOriginalAsFallback() { return useOriginalAsFallback; }
        /**
         * Sets whether to use the original message as a fallback when a translation is missing.
         *
         * @param useOriginalAsFallback a boolean indicating whether the original message should be used
         *                              as a fallback; if true, the original message will be displayed
         *                              when no translation is available.
         */
        public void setUseOriginalAsFallback(boolean useOriginalAsFallback) {
            this.useOriginalAsFallback = useOriginalAsFallback; 
        }
        
        /**
         * Determines if missing translations should be logged.
         *
         * @return true if missing translations are to be logged, false otherwise.
         */
        public boolean isLogMissingTranslations() { return logMissingTranslations; }
        /**
         * Sets whether missing translations should be logged.
         *
         * @param logMissingTranslations a boolean indicating if missing translations should be logged;
         *                                true to enable logging, false to disable it
         */
        public void setLogMissingTranslations(boolean logMissingTranslations) {
            this.logMissingTranslations = logMissingTranslations; 
        }
        
        /**
         * Retrieves the character encoding used by the message source configuration.
         *
         * @return the encoding as a string, typically representing the character set (e.g., "UTF-8").
         */
        public String getEncoding() { return encoding; }
        /**
         * Sets the encoding format for the message source.
         *
         * @param encoding the encoding format to be used (e.g., "UTF-8", "ISO-8859-1").
         */
        public void setEncoding(String encoding) { this.encoding = encoding; }
    }

    /**
     * Configuration class for the warm-up feature within the FluentI18nProperties context.
     * Determines whether the warm-up functionality is enabled and specifies the set of
     * locales involved in the warm-up process.
     */
    public static class WarmUp {
        /**
         * Indicates whether the warm-up functionality is enabled in the FluentI18nProperties context.
         * If set to {@code true}, the warm-up feature will be active, preparing the required locales
         * for use during application startup. If set to {@code false}, the warm-up feature will be
         * disabled, and no pre-loading of locales will occur.
         *
         * This variable is part of the warm-up configuration within FluentI18nProperties and is
         * typically used to optimize the initialization process by pre-loading localization data.
         */
        private boolean enabled = true;
        /**
         * A set of supported locales used during the warm-up process.
         *
         * This variable defines the set of locales that will participate in the
         * initialization or pre-loading of resources. By default, it is set to
         * include only the English locale.
         */
        private Set<Locale> locales = Set.of(Locale.ENGLISH);

        /**
         * Indicates whether the warm-up functionality is enabled.
         *
         * @return true if the warm-up functionality is enabled; false otherwise
         */
        // Getters and setters
        public boolean isEnabled() { return enabled; }
        /**
         * Sets the enabled status for the warm-up configuration.
         *
         * @param enabled a boolean indicating whether the warm-up functionality
         *                should be enabled (true) or disabled (false)
         */
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        /**
         * Retrieves the set of locales configured for the warm-up process.
         *
         * @return a set of Locale objects representing the supported locales for the warm-up functionality
         */
        public Set<Locale> getLocales() { return locales; }
        /**
         * Sets the set of locales used for the warm-up process.
         *
         * @param locales the set of {@code Locale} objects to be used in the warm-up
         *                process. Must not be null. If an empty set is provided, no
         *                locales will be considered for warm-up.
         */
        public void setLocales(Set<Locale> locales) { this.locales = locales; }
    }
    
    /**
     * Represents the configuration settings for web integration within FluentI18nProperties.
     * Provides options to customize behavior related to locale management,
     * HTTP headers, and session storage in web environments.
     */
    public static class Web {
        /**
         * Indicates whether the web integration features are enabled.
         * When set to {@code true}, the web-related functionalities, such as locale management
         * and HTTP headers handling, are active. If set to {@code false}, these features are disabled.
         */
        private boolean enabled = true;
        
        /**
         * Specifies the name of the HTTP parameter used to pass the locale information in web requests.
         *
         * This parameter is utilized to determine the language or regional settings for users accessing
         * the web application. The default value is "lang", but it can be customized based on the
         * requirements of the application.
         *
         * It is typically used when parsing request parameters or URLs to extract the locale identifier
         * provided by the client.
         */
        private String localeParameter = "lang";
        
        /**
         * Determines whether the "Accept-Language" HTTP header should be considered
         * when resolving the locale in a web environment.
         *
         * If true, the application will inspect the "Accept-Language" header sent
         * by the client to determine the preferred locale. This allows the locale
         * to be resolved based on the language preferences configured in the client's
         * browser or device.
         *
         * If false, the "Accept-Language" header will be ignored, and locale resolution
         * will rely on other mechanisms, such as URL parameters or session attributes.
         *
         * Default value is true.
         */
        private boolean useAcceptLanguageHeader = true;
        
        /**
         * Determines whether session storage is used for locale management in a web environment.
         *
         * When set to {@code true}, the application stores locale information in the user's session,
         * allowing the locale preference to persist across multiple requests within the same session.
         * When set to {@code false}, session storage is disabled, and the locale is typically determined
         * using other mechanisms, such as request parameters or HTTP headers.
         *
         * This configuration is particularly useful for applications relying on session-based state
         * to maintain consistent user preferences throughout their interaction with the application.
         */
        private boolean useSession = true;
        
        /**
         * Indicates whether the "Content-Language" HTTP header should be set for responses in web applications.
         * If true, the application generates and includes this header based on the current locale.
         * This can be useful for client-side processing or for informing intermediaries of the content's language.
         */
        private boolean setContentLanguageHeader = true;
        
        /**
         * Indicates whether the web integration configuration is enabled.
         *
         * @return true if the web integration configuration is enabled; false otherwise
         */
        // Getters and setters
        public boolean isEnabled() { return enabled; }
        /**
         * Enables or disables the specified configuration.
         *
         * @param enabled a boolean value where true enables the configuration
         *                and false disables it
         */
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        /**
         * Retrieves the name of the parameter used for specifying the locale in web requests.
         *
         * @return the locale parameter name as a string
         */
        public String getLocaleParameter() { return localeParameter; }
        /**
         * Sets the locale parameter used to identify the language or locale in web requests.
         *
         * @param localeParameter the name of the parameter (e.g., "lang") used to determine the locale
         */
        public void setLocaleParameter(String localeParameter) { this.localeParameter = localeParameter; }
        
        /**
         * Determines if the `Accept-Language` HTTP header is used for resolving the locale.
         *
         * This method retrieves the value of the `useAcceptLanguageHeader` property, which,
         * if true, indicates that the application uses the `Accept-Language` header sent
         * by the client's browser to determine the locale.
         *
         * @return true if the `Accept-Language` header is used for locale resolution;
         *         false otherwise
         */
        public boolean isUseAcceptLanguageHeader() { return useAcceptLanguageHeader; }
        /**
         * Sets whether the application should use the "Accept-Language" HTTP header
         * to determine the preferred locale.
         *
         * @param useAcceptLanguageHeader a boolean value indicating whether to use
         *        the "Accept-Language" header. If true, the application will consider
         *        the "Accept-Language" header in HTTP requests to infer the locale.
         *        If false, the header will be ignored for locale determination.
         */
        public void setUseAcceptLanguageHeader(boolean useAcceptLanguageHeader) {
            this.useAcceptLanguageHeader = useAcceptLanguageHeader; 
        }
        
        /**
         * Determines whether session storage is enabled for locale management.
         *
         * @return true if session storage is used for locale management, false otherwise
         */
        public boolean isUseSession() { return useSession; }
        /**
         * Sets whether session storage should be used for managing locale information.
         * This method updates the configuration to either enable or disable the use of
         * session storage within the web integration settings.
         *
         * @param useSession a boolean value indicating whether session storage is enabled
         *                   (true to enable session storage, false to disable it)
         */
        public void setUseSession(boolean useSession) { this.useSession = useSession; }
        
        /**
         * Indicates whether the application should set the `Content-Language` HTTP header
         * in web responses.
         *
         * @return true if the `Content-Language` header is enabled, false otherwise
         */
        public boolean isSetContentLanguageHeader() { return setContentLanguageHeader; }
        /**
         * Sets whether the "Content-Language" HTTP response header should be set.
         * This configuration defines whether the application should include the "Content-Language" header
         * when responding to HTTP requests, typically to indicate the language of the response.
         *
         * @param setContentLanguageHeader a boolean value indicating whether the "Content-Language" header
         *        should be included in HTTP responses. Set to true to enable the header, or false to disable it.
         */
        public void setSetContentLanguageHeader(boolean setContentLanguageHeader) {
            this.setContentLanguageHeader = setContentLanguageHeader; 
        }
    }
    
    /**
     * Represents the configuration for the extraction process. This includes settings
     * that enable or disable extraction, specify encoding for source files, and define
     * patterns for various components such as method calls, annotations, and templates.
     */
    public static class Extraction {
        /**
         * Indicates whether the extraction process is enabled or disabled.
         * By default, the extraction process is enabled when this field is set to true.
         */
        private boolean enabled = true;
        
        /**
         * Defines the character encoding used for processing source files during extraction.
         * The default value is "UTF-8".
         */
        private String sourceEncoding = "UTF-8";
        
        /**
         * Defines a list of patterns for method calls to be extracted during the processing
         * phase. Each pattern in the list represents a specific method call structure,
         * allowing the extraction mechanism to identify and process relevant methods
         * based on these patterns. Patterns can be expressed as strings that define
         * the desired method invocation format.
         */
        private List<String> methodCallPatterns = List.of();
        
        /**
         * Defines a list of patterns used for matching annotations in the extraction process.
         * These patterns are typically defined as strings and enable filtering or identification
         * of specific annotations within source code.
         */
        private List<String> annotationPatterns = List.of();
        
        /**
         * A list of patterns used to define templates for the extraction process.
         * These patterns specify the structure or components of templates that should
         * be recognized and processed during the extraction operation.
         */
        private List<String> templatePatterns = List.of();
        
        /**
         * Checks whether the extraction process is enabled.
         *
         * @return true if the extraction is enabled, false otherwise
         */
        // Getters and setters
        public boolean isEnabled() { return enabled; }
        /**
         * Sets whether the extraction process is enabled or disabled.
         *
         * @param enabled a boolean value where {@code true} enables the extraction process,
         *                and {@code false} disables it.
         */
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        /**
         * Retrieves the source encoding used for processing source files.
         *
         * @return the source encoding as a string
         */
        public String getSourceEncoding() { return sourceEncoding; }
        /**
         * Sets the encoding format to be used for source files.
         *
         * @param sourceEncoding the character encoding for source file processing
         */
        public void setSourceEncoding(String sourceEncoding) { this.sourceEncoding = sourceEncoding; }
        
        /**
         * Retrieves the list of patterns used for identifying method calls during the extraction process.
         *
         * @return a list of strings representing method call patterns.
         */
        public List<String> getMethodCallPatterns() { return methodCallPatterns; }
        /**
         * Sets the list of method call patterns to be used in the extraction configuration.
         * These patterns define the method calls that should be identified during the
         * extraction process.
         *
         * @param methodCallPatterns a list of strings representing the patterns for method calls
         */
        public void setMethodCallPatterns(List<String> methodCallPatterns) {
            this.methodCallPatterns = methodCallPatterns; 
        }
        
        /**
         * Returns a list of annotation patterns used in the extraction process.
         *
         * @return a list of annotation patterns as strings
         */
        public List<String> getAnnotationPatterns() { return annotationPatterns; }
        /**
         * Sets the list of patterns used to identify annotations during the extraction process.
         *
         * @param annotationPatterns a list of strings representing the patterns for annotation identification
         */
        public void setAnnotationPatterns(List<String> annotationPatterns) {
            this.annotationPatterns = annotationPatterns; 
        }
        
        /**
         * Retrieves the list of template patterns used in the extraction configuration.
         *
         * @return a list of strings representing the template patterns
         */
        public List<String> getTemplatePatterns() { return templatePatterns; }
        /**
         * Sets the list of template patterns to be used in the extraction process.
         *
         * @param templatePatterns a list of strings representing the patterns for template matching
         */
        public void setTemplatePatterns(List<String> templatePatterns) {
            this.templatePatterns = templatePatterns; 
        }

        /**
         * Generates a string representation of the Extraction object, including the enabled state,
         * source encoding, method call patterns, annotation patterns, and template patterns.
         *
         * @return a string describing the current state of the Extraction object.
         */
        @Override
        public String toString() {
            return "Extraction{\n" +
                "  enabled=" + enabled + ",\n" +
                "  sourceEncoding='" + sourceEncoding + "',\n" +
                "  methodCallPatterns=" + methodCallPatterns + ",\n" +
                "  annotationPatterns=" + annotationPatterns + ",\n" +
                "  templatePatterns=" + templatePatterns + "\n" +
                "}";
        }
    }
    
    /**
     * Represents the compilation configuration for fluent internationalization properties.
     * This class provides options to configure the output format, validation, translation
     * preservation, output minification, and metadata inclusion during compilation.
     */
    public static class Compilation {
        /**
         * Defines the desired format for the output generated during compilation.
         * The default value is "json".
         */
        private String outputFormat = "json";
        
        /**
         * Indicates whether validation is enabled or disabled during the compilation process.
         * When set to true, the system will perform validations to ensure the accuracy and correctness
         * of the configured properties. When set to false, the validation step is skipped, which can
         * increase compilation speed but may lead to unintended issues if the input data contains errors.
         */
        private boolean validation = true;
        
        /**
         * Indicates whether existing translations should be preserved during the compilation
         * process. If set to true, the compilation will keep any pre-existing translations
         * in the output, rather than overwriting or discarding them.
         */
        private boolean preserveExisting = true;
        
        /**
         * Determines whether the output should be minified during the compilation process.
         * When set to true, the generated output will have minimized whitespace and formatting,
         * reducing its size. When false, the output retains standard formatting for readability.
         */
        private boolean minifyOutput = false;
        
        /**
         * Indicates whether metadata should be included during the compilation process.
         * If set to true, metadata will be added to the output; if false, metadata will
         * be excluded.
         */
        private boolean includeMetadata = true;
        
        /**
         * Retrieves the configured output format for the compilation.
         *
         * @return the output format as a string.
         */
        // Getters and setters
        public String getOutputFormat() { return outputFormat; }
        /**
         * Sets the output format for the compilation process.
         * The provided format determines the structure or type of the output (e.g., JSON).
         *
         * @param outputFormat the desired output format. Expected formats could include "json", "xml", etc.
         */
        public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
        
        /**
         * Indicates whether validation is enabled for the compilation process.
         *
         * @return true if validation is enabled, false otherwise
         */
        public boolean isValidation() { return validation; }
        /**
         * Sets whether validation is enabled or disabled.
         *
         * @param validation a boolean value indicating the validation state.
         *                   If true, validation is enabled.
         *                   If false, validation is disabled.
         */
        public void setValidation(boolean validation) { this.validation = validation; }
        
        /**
         * Checks whether the existing translations should be preserved during the compilation process.
         *
         * @return true if existing translations are preserved; false otherwise.
         */
        public boolean isPreserveExisting() { return preserveExisting; }
        /**
         * Sets the preserveExisting property, which determines if existing translations
         * should be retained during certain operations.
         *
         * @param preserveExisting a boolean indicating whether to preserve existing translations.
         */
        public void setPreserveExisting(boolean preserveExisting) { this.preserveExisting = preserveExisting; }
        
        /**
         * Checks whether the output should be minified during the compilation process.
         *
         * @return true if the output is set to be minified, false otherwise
         */
        public boolean isMinifyOutput() { return minifyOutput; }
        /**
         * Sets whether the output should be minified during the compilation process.
         *
         * @param minifyOutput a boolean value where {@code true} enables output minification
         *                     and {@code false} disables it.
         */
        public void setMinifyOutput(boolean minifyOutput) { this.minifyOutput = minifyOutput; }
        
        /**
         * Checks whether metadata inclusion is enabled in the compilation configuration.
         *
         * @return true if metadata inclusion is enabled, false otherwise.
         */
        public boolean isIncludeMetadata() { return includeMetadata; }
        /**
         * Sets whether metadata should be included during the compilation process.
         *
         * @param includeMetadata a boolean value indicating if metadata should be included
         *                        (true to include metadata, false to exclude it).
         */
        public void setIncludeMetadata(boolean includeMetadata) { this.includeMetadata = includeMetadata; }

        /**
         * Returns a string representation of the Compilation object.
         * The string includes the values of outputFormat, validation, preserveExisting,
         * minifyOutput, and includeMetadata properties.
         *
         * @return a string representation of the Compilation object.
         */
        @Override
        public String toString() {
            return "Compilation{\n" +
                "  outputFormat='" + outputFormat + "',\n" +
                "  validation=" + validation + ",\n" +
                "  preserveExisting=" + preserveExisting + ",\n" +
                "  minifyOutput=" + minifyOutput + ",\n" +
                "  includeMetadata=" + includeMetadata + "\n" +
                "}";
        }
    }

    /**
     * Generates a string representation of the {@code FluentI18nProperties} object.
     * The returned string contains information about the internal state of the object,
     * including the values of the fields such as {@code enabled}, {@code defaultLocale},
     * {@code supportedLocales}, {@code messageSource}, {@code web}, {@code extraction},
     * and {@code compilation}.
     *
     * @return a string representation of the {@code FluentI18nProperties} object
     */
    @Override
    public String toString() {
        return "FluentI18nProperties{\n" +
            " enabled=" + enabled + ",\n" +
            " defaultLocale=" + defaultLocale + ",\n" +
            " supportedLocales=" + supportedLocales + ",\n" +
            " messageSource=" + messageSource + ",\n" +
            " web=" + web + ",\n" +
            " extraction=" + extraction + ",\n" +
            " compilation=" + compilation + "\n" +
            "}";

    }
}