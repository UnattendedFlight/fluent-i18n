package io.github.unattendedflight.fluent.i18n.compiler;

import java.time.LocalDateTime;

/**
 * Represents metadata information for a Portable Object (PO) file used in localization and translation.
 * This class encapsulates details such as project version, language, creation date, revision date,
 * and content type associated with the PO file.
 */
public class PoMetadata {
    /**
     * Represents the version of the project associated with the Portable Object (PO) file.
     * This variable stores a string indicating the version, which can be used to
     * identify and manage different iterations or releases of the project.
     */
    private String projectVersion;
    /**
     * Represents the language associated with a Portable Object (PO) file.
     * The language is identified using a standard language code (e.g., "en" for English).
     * It is used to specify the target language for the translation data contained
     * in the PO file.
     */
    private String language;
    /**
     * Represents the date and time when the metadata associated with the PO file was created.
     * This is typically used to track the initial creation timestamp of the file's metadata.
     */
    private LocalDateTime creationDate;
    /**
     * Represents the revision date of a Portable Object (PO) file.
     * This date indicates the most recent update or modification made to the file.
     * It is typically used in localization and translation contexts to track
     * changes and maintain version control of translation metadata.
     */
    private LocalDateTime revisionDate;
    /**
     * Specifies the content type of the Portable Object (PO) file, including the MIME type and character encoding.
     * The default value is "text/plain; charset=UTF-8", which indicates that the file is in plain text format
     * and uses UTF-8 character encoding.
     *
     * This field is typically used to define the metadata for a PO file to ensure proper usage and
     * compatibility across various localization tools and systems.
     */
    private String contentType = "text/plain; charset=UTF-8";
    
    /**
     * Retrieves the version information of the project associated with the metadata.
     *
     * @return the project version as a string
     */
    // Getters and setters
    public String getProjectVersion() { return projectVersion; }
    /**
     * Sets the project version for the PO metadata.
     * The project version typically represents the version of the project or file this metadata is associated with.
     *
     * @param projectVersion the version of the project to be set
     */
    public void setProjectVersion(String projectVersion) { this.projectVersion = projectVersion; }
    
    /**
     * Retrieves the language associated with the metadata.
     *
     * @return the language as a string representation.
     */
    public String getLanguage() { return language; }
    /**
     * Sets the language associated with this metadata instance.
     *
     * @param language the language code or identifier (e.g., "en", "fr") to be set
     */
    public void setLanguage(String language) { this.language = language; }
    
    /**
     * Retrieves the creation date of the metadata.
     *
     * @return the creation date as a LocalDateTime object.
     */
    public LocalDateTime getCreationDate() { return creationDate; }
    /**
     * Sets the creation date of the metadata.
     *
     * @param creationDate the date and time when the metadata is created
     */
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
    
    /**
     * Retrieves the revision date associated with the Portable Object (PO) metadata.
     *
     * @return the revision date as a {@link LocalDateTime}
     */
    public LocalDateTime getRevisionDate() { return revisionDate; }
    /**
     * Sets the revision date for the PO metadata.
     * The revision date typically represents the last modification timestamp of the associated data.
     *
     * @param revisionDate the {@link LocalDateTime} representing the revision date to be set
     */
    public void setRevisionDate(LocalDateTime revisionDate) { this.revisionDate = revisionDate; }
    
    /**
     * Retrieves the content type associated with the Portable Object (PO) file metadata.
     *
     * @return the content type as a string, typically specifying the MIME type
     *         and character set, e.g., "text/plain; charset=UTF-8".
     */
    public String getContentType() { return contentType; }
    /**
     * Sets the content type associated with the metadata.
     *
     * @param contentType the MIME type and character set information to be set,
     *                    typically in the format "type/subtype; charset=UTF-8".
     */
    public void setContentType(String contentType) { this.contentType = contentType; }
}