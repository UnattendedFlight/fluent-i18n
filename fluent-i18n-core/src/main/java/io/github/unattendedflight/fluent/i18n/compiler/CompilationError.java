package io.github.unattendedflight.fluent.i18n.compiler;

/**
 * Represents an error during compilation
 */
public class CompilationError {
    private final String locale;
    private final String message;
    private final Exception cause;
    
    public CompilationError(String locale, String message, Exception cause) {
        this.locale = locale;
        this.message = message;
        this.cause = cause;
    }
    
    public String getLocale() { return locale; }
    public String getMessage() { return message; }
    public Exception getCause() { return cause; }
    
    @Override
    public String toString() {
        return locale + ": " + message + (cause != null ? " (" + cause.getMessage() + ")" : "");
    }
}