package io.github.unattendedflight.fluent.i18n.core;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Utility for formatting messages with parameters
 */
public class MessageFormatter {
    
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