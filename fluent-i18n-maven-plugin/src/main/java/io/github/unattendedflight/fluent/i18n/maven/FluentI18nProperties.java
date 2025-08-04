package io.github.unattendedflight.fluent.i18n.maven;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Configuration properties for Fluent i18n
 */
public class FluentI18nProperties {
    
    /**
     * Whether to enable fluent i18n
     */
    private boolean enabled = true;
    
    /**
     * Default locale
     */
    private Locale defaultLocale = Locale.ENGLISH.stripExtensions();
    
    /**
     * Supported locales
     */
    private Set<Locale> supportedLocales = Set.of(Locale.ENGLISH.stripExtensions());
    
    /**
     * Message source configuration
     */
    private MessageSource messageSource = new MessageSource();

    private WarmUp warmUp = new WarmUp();
    
    /**
     * Web-related configuration
     */
    private Web web = new Web();
    
    /**
     * Extraction configuration
     */
    private Extraction extraction = new Extraction();
    
    /**
     * Compilation configuration
     */
    private Compilation compilation = new Compilation();
    
    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public Locale getDefaultLocale() { return defaultLocale; }
    public void setDefaultLocale(Locale defaultLocale) { this.defaultLocale = defaultLocale; }
    
    public Set<Locale> getSupportedLocales() { return supportedLocales; }
    public void setSupportedLocales(Set<Locale> supportedLocales) { this.supportedLocales = supportedLocales; }
    
    public MessageSource getMessageSource() { return messageSource; }
    public void setMessageSource(MessageSource messageSource) { this.messageSource = messageSource; }

    public WarmUp getWarmUp() { return warmUp; }
    public void setWarmUp(WarmUp warmUp) { this.warmUp = warmUp; }
    
    public Web getWeb() { return web; }
    public void setWeb(Web web) { this.web = web; }
    
    public Extraction getExtraction() { return extraction; }
    public void setExtraction(Extraction extraction) { this.extraction = extraction; }
    
    public Compilation getCompilation() { return compilation; }
    public void setCompilation(Compilation compilation) { this.compilation = compilation; }
    
    public static class MessageSource {
        /**
         * Type of message source: json, properties, binary
         */
        private String type = "json";
        
        /**
         * Base name for message files
         */
        private String basename = "i18n/messages";
        
        /**
         * Cache duration for translations
         */
        private Duration cacheDuration = Duration.ofHours(1);
        
        /**
         * Whether to use the original text as fallback when translation is missing
         */
        private boolean useOriginalAsFallback = true;
        
        /**
         * Whether to log missing translations
         */
        private boolean logMissingTranslations = true;
        
        /**
         * Encoding for message files
         */
        private String encoding = "UTF-8";
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getBasename() { return basename; }
        public void setBasename(String basename) { this.basename = basename; }
        
        public Duration getCacheDuration() { return cacheDuration; }
        public void setCacheDuration(Duration cacheDuration) { this.cacheDuration = cacheDuration; }
        
        public boolean isUseOriginalAsFallback() { return useOriginalAsFallback; }
        public void setUseOriginalAsFallback(boolean useOriginalAsFallback) { 
            this.useOriginalAsFallback = useOriginalAsFallback; 
        }
        
        public boolean isLogMissingTranslations() { return logMissingTranslations; }
        public void setLogMissingTranslations(boolean logMissingTranslations) { 
            this.logMissingTranslations = logMissingTranslations; 
        }
        
        public String getEncoding() { return encoding; }
        public void setEncoding(String encoding) { this.encoding = encoding; }
    }

    public static class WarmUp {
        /**
         * Whether to enable warm-up
         */
        private boolean enabled = true;
        private Set<Locale> locales = Set.of(Locale.ENGLISH);

        // Getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Set<Locale> getLocales() { return locales; }
        public void setLocales(Set<Locale> locales) { this.locales = locales; }
    }
    
    public static class Web {
        /**
         * Whether to enable web integration
         */
        private boolean enabled = true;
        
        /**
         * Parameter name for locale switching
         */
        private String localeParameter = "lang";
        
        /**
         * Whether to use Accept-Language header
         */
        private boolean useAcceptLanguageHeader = true;
        
        /**
         * Whether to store locale in session
         */
        private boolean useSession = true;
        
        /**
         * Whether to set Content-Language header in responses
         */
        private boolean setContentLanguageHeader = true;
        
        // Getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public String getLocaleParameter() { return localeParameter; }
        public void setLocaleParameter(String localeParameter) { this.localeParameter = localeParameter; }
        
        public boolean isUseAcceptLanguageHeader() { return useAcceptLanguageHeader; }
        public void setUseAcceptLanguageHeader(boolean useAcceptLanguageHeader) { 
            this.useAcceptLanguageHeader = useAcceptLanguageHeader; 
        }
        
        public boolean isUseSession() { return useSession; }
        public void setUseSession(boolean useSession) { this.useSession = useSession; }
        
        public boolean isSetContentLanguageHeader() { return setContentLanguageHeader; }
        public void setSetContentLanguageHeader(boolean setContentLanguageHeader) { 
            this.setContentLanguageHeader = setContentLanguageHeader; 
        }
    }
    
    public static class Extraction {
        /**
         * Whether extraction is enabled
         */
        private boolean enabled = true;
        
        /**
         * Source encoding
         */
        private String sourceEncoding = "UTF-8";
        
        /**
         * Patterns for method calls
         */
        private List<String> methodCallPatterns = List.of();
        
        /**
         * Patterns for annotations
         */
        private List<String> annotationPatterns = List.of();
        
        /**
         * Patterns for templates
         */
        private List<String> templatePatterns = List.of();
        
        // Getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public String getSourceEncoding() { return sourceEncoding; }
        public void setSourceEncoding(String sourceEncoding) { this.sourceEncoding = sourceEncoding; }
        
        public List<String> getMethodCallPatterns() { return methodCallPatterns; }
        public void setMethodCallPatterns(List<String> methodCallPatterns) { 
            this.methodCallPatterns = methodCallPatterns; 
        }
        
        public List<String> getAnnotationPatterns() { return annotationPatterns; }
        public void setAnnotationPatterns(List<String> annotationPatterns) { 
            this.annotationPatterns = annotationPatterns; 
        }
        
        public List<String> getTemplatePatterns() { return templatePatterns; }
        public void setTemplatePatterns(List<String> templatePatterns) { 
            this.templatePatterns = templatePatterns; 
        }

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
    
    public static class Compilation {
        /**
         * Output format: json, properties, binary
         */
        private String outputFormat = "json";
        
        /**
         * Whether to validate translations during compilation
         */
        private boolean validation = true;
        
        /**
         * Whether to preserve existing translations
         */
        private boolean preserveExisting = true;
        
        /**
         * Whether to minify output
         */
        private boolean minifyOutput = false;
        
        /**
         * Whether to include metadata in output
         */
        private boolean includeMetadata = true;
        
        // Getters and setters
        public String getOutputFormat() { return outputFormat; }
        public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
        
        public boolean isValidation() { return validation; }
        public void setValidation(boolean validation) { this.validation = validation; }
        
        public boolean isPreserveExisting() { return preserveExisting; }
        public void setPreserveExisting(boolean preserveExisting) { this.preserveExisting = preserveExisting; }
        
        public boolean isMinifyOutput() { return minifyOutput; }
        public void setMinifyOutput(boolean minifyOutput) { this.minifyOutput = minifyOutput; }
        
        public boolean isIncludeMetadata() { return includeMetadata; }
        public void setIncludeMetadata(boolean includeMetadata) { this.includeMetadata = includeMetadata; }

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