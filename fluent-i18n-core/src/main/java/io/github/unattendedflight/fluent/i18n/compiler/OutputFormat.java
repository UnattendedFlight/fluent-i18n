package io.github.unattendedflight.fluent.i18n.compiler;

/**
 * Supported output formats for compiled translations
 */
public enum OutputFormat {
    JSON("json"),
    PROPERTIES("properties"), 
    BINARY("bin");
    
    private final String extension;
    
    OutputFormat(String extension) {
        this.extension = extension;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public String getFileName(String locale) {
        return "messages_" + locale + "." + extension;
    }
}