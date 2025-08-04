package io.github.unattendedflight.fluent.i18n.core;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Utility class for formatting messages using templates and arguments.
 * Supports both locale-aware formatting through MessageFormat and
 * basic string replacement as a fallback.
 */
public class MessageFormatter {
    
    /**
     * Formats a template string using the specified arguments and a given locale.
     * If the template or arguments are invalid, basic string replacement is used
     * as a fallback mechanism. This method leverages {@link MessageFormat} for
     * locale-aware formatting.
     *
     * @param template the template string containing placeholders, such as {0}, {1}, etc.
     * @param args an array of objects to be used as arguments to replace placeholders in the template
     * @param locale the {@link Locale} to apply during formatting
     * @return the formatted string with placeholders replaced by the provided arguments,
     *         or the original template if no arguments are provided
     */
    public static String format(String template, Object[] args, Locale locale) {
        if (template == null) return null;
        if (args == null || args.length == 0) return template;
        
        try {
            // Use MessageFormat for proper locale-aware formatting
            MessageFormat format = new MessageFormat(template, locale);
            return format.format(args);
        } catch (Exception e) {
            // Fallback to simple string replacement
            return simpleFormat(template, args);
        }
    }
    
    /**
     * Formats a string template by replacing placeholders with specified arguments.
     * Supports both indexed placeholders (e.g., {0}, {1}) and unindexed placeholders (e.g., {}).
     *
     * @param template the string template containing placeholders
     * @param args an array of arguments to replace the placeholders in the template
     * @return the formatted string with placeholders replaced by corresponding arguments
     */
    private static String simpleFormat(String template, Object[] args) {
        String result = template;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(args[i]));
        }
        // Handle {} placeholders (replace first occurrence)
        for (Object arg : args) {
            if (result.contains("{}")) {
                result = result.replaceFirst("\\{\\}", String.valueOf(arg));
            } else {
                break;
            }
        }
        return result;
    }
}