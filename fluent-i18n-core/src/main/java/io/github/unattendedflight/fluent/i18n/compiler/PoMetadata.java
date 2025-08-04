package io.github.unattendedflight.fluent.i18n.compiler;

import java.time.LocalDateTime;

/**
 * Metadata from PO file header
 */
public class PoMetadata {
    private String projectVersion;
    private String language;
    private LocalDateTime creationDate;
    private LocalDateTime revisionDate;
    private String contentType = "text/plain; charset=UTF-8";
    
    // Getters and setters
    public String getProjectVersion() { return projectVersion; }
    public void setProjectVersion(String projectVersion) { this.projectVersion = projectVersion; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
    
    public LocalDateTime getRevisionDate() { return revisionDate; }
    public void setRevisionDate(LocalDateTime revisionDate) { this.revisionDate = revisionDate; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
}