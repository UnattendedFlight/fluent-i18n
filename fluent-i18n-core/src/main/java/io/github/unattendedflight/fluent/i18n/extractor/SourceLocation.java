package io.github.unattendedflight.fluent.i18n.extractor;

/**
 * Represents a location in source code where a message was found
 */
public class SourceLocation {
    private final String filePath;
    private final int lineNumber;
    private final int columnNumber;
    
    public SourceLocation(String filePath, int lineNumber) {
        this(filePath, lineNumber, 0);
    }
    
    public SourceLocation(String filePath, int lineNumber, int columnNumber) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    
    public String getFilePath() { return filePath; }
    public int getLineNumber() { return lineNumber; }
    public int getColumnNumber() { return columnNumber; }
    
    @Override
    public String toString() {
        return filePath + ":" + lineNumber + 
               (columnNumber > 0 ? ":" + columnNumber : "");
    }
}