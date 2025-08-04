package io.github.unattendedflight.fluent.i18n.compiler;

/**
 * Represents a compilation error that occurred during the translation process.
 * This class encapsulates the locale where the error occurred, a descriptive
 * error message, and an optional cause of the error.
 */
public class CompilationError {
    /**
     * The locale in which the compilation error occurred.
     * This value typically represents a language or regional code (e.g., "en-US")
     * associated with the translation context where the error was encountered.
     */
    private final String locale;
    /**
     * The descriptive error message associated with this compilation error.
     * This message provides details about the nature of the error that occurred
     * during the translation process.
     */
    private final String message;
    /**
     * The underlying exception that triggered this compilation error.
     *
     * This field provides additional context about the root cause of the error, such as
     * a specific exception that occurred during the translation process. It can be useful
     * for debugging and detailed error analysis.
     */
    private final Exception cause;
    
    /**
     * Constructs a new CompilationError with the specified locale, message, and cause.
     *
     * @param locale the locale context in which the compilation error occurred
     * @param message a descriptive message detailing the reason for the compilation error
     * @param cause the underlying exception that caused the compilation error; may be null
     */
    public CompilationError(String locale, String message, Exception cause) {
        this.locale = locale;
        this.message = message;
        this.cause = cause;
    }
    
    /**
     * Retrieves the locale associated with this compilation error.
     *
     * @return the locale where the compilation error occurred as a String
     */
    public String getLocale() { return locale; }
    /**
     * Retrieves the error message associated with this compilation error.
     *
     * @return the descriptive error message as a String
     */
    public String getMessage() { return message; }
    /**
     * Retrieves the underlying cause of the compilation error.
     *
     * @return the exception that caused the error, or null if no cause is specified
     */
    public Exception getCause() { return cause; }
    
    /**
     * Returns a string representation of the compilation error, including the locale,
     * error message, and optionally the message of the underlying cause, if one exists.
     *
     * @return a string representation of the compilation error
     */
    @Override
    public String toString() {
        return locale + ": " + message + (cause != null ? " (" + cause.getMessage() + ")" : "");
    }
}