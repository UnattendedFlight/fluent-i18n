package io.github.unattendedflight.fluent.i18n.extractor;

/**
 * Represents a location in a source code file.
 *
 * This class encapsulates information about the file path, line number,
 * and optionally, the column number of a specific location in the source code.
 * It is typically used to track where specific elements, messages, or annotations
 * are found within a source file.
 */
public class SourceLocation {
    /**
     * Represents the file path associated with a specific location in the source code.
     * This is typically the path to the source file where the location is defined.
     */
    private final String filePath;
    /**
     * Represents the line number within a source file.
     *
     * This field specifies the exact line in the source code file where
     * a particular element or event is located. It is primarily used to
     * provide precise location details in conjunction with the file path
     * and, optionally, the column number, for debugging, logging, or
     * annotation purposes.
     */
    private final int lineNumber;
    /**
     * Represents the column number associated with a specific location in a source file.
     * This field holds the zero-based column position where a particular element or piece of code
     * is located within a line in the source code. If the column number is not specified,
     * its value defaults to 0.
     */
    private final int columnNumber;
    
    /**
     * Constructs a new SourceLocation instance with the specified file path and line number.
     * The column number is set to 0 by default.
     *
     * @param filePath   the path to the source file
     * @param lineNumber the line number within the source file
     */
    public SourceLocation(String filePath, int lineNumber) {
        this(filePath, lineNumber, 0);
    }
    
    /**
     * Constructs a new SourceLocation instance.
     *
     * This constructor initializes the instance with the provided file path,
     * line number, and column number, which represent a specific location in
     * a source code file.
     *
     * @param filePath the path to the source file where this location is defined
     * @param lineNumber the line number in the source file where this location is defined
     * @param columnNumber the column number in the source file where this location is defined
     */
    public SourceLocation(String filePath, int lineNumber, int columnNumber) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    
    /**
     * Retrieves the file path associated with this source location.
     *
     * @return the file path as a string
     */
    public String getFilePath() { return filePath; }
    /**
     * Retrieves the line number represented by this source location.
     *
     * @return the line number as an integer
     */
    public int getLineNumber() { return lineNumber; }
    /**
     * Retrieves the column number associated with this source location.
     * The column number typically represents the horizontal position within
     * a line in a source code file.
     *
     * @return the column number, or 0 if no specific column number is defined
     */
    public int getColumnNumber() { return columnNumber; }
    
    /**
     * Returns a string representation of the source location, including the file path,
     * line number, and optionally the column number if it is greater than zero.
     *
     * The format of the returned string is:
     * <code>filePath:lineNumber[:columnNumber]</code>
     *
     * @return a string representation of this source location
     */
    @Override
    public String toString() {
        return filePath + ":" + lineNumber + 
               (columnNumber > 0 ? ":" + columnNumber : "");
    }
}