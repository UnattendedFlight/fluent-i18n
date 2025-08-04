package io.github.unattendedflight.fluent.i18n.compiler;

/**
 * Represents the supported output formats for writing translation data.
 * Each output format is associated with a specific file extension.
 */
public enum OutputFormat {
    /**
     * Enum constant that represents the JSON output format for writing translation files.
     * The associated file extension for this format is "json".
     */
    JSON("json"),
    /**
     * Represents the PROPERTIES output format for writing translation data.
     * Associated with files having a ".properties" extension.
     */
    PROPERTIES("properties"),
    /**
     * Represents the BINARY output format for writing translation data.
     * The associated file extension for this format is "bin".
     */
    BINARY("bin");
    
    /**
     * The file extension associated with the specific output format.
     * This value identifies the suffix typically used in file names
     * for the corresponding format, such as "json" for JSON files.
     */
    private final String extension;
    
    /**
     * Constructs an OutputFormat instance with the specified file extension.
     *
     * @param extension the file extension associated with the output format
     */
    OutputFormat(String extension) {
        this.extension = extension;
    }
    
    /**
     * Returns the file extension associated with the output format.
     *
     * @return the string representation of the file extension corresponding to this output format.
     */
    public String getExtension() {
        return extension;
    }
    
    /**
     * Constructs a file name based on the specified locale and the file extension
     * associated with the output format.
     *
     * @param locale the locale identifier (e.g., "en", "fr") that will be included in the file name
     * @return the constructed file name in the format "messages_[locale].[extension]"
     */
    public String getFileName(String locale) {
        return "messages_" + locale + "." + extension;
    }
}