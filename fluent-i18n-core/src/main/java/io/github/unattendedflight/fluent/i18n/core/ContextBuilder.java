package io.github.unattendedflight.fluent.i18n.core;

import io.github.unattendedflight.fluent.i18n.I18n;
import java.util.Locale;

/**
 * Builder for context-aware translations
 */
public class ContextBuilder {
    private final String context;
    private final NaturalTextMessageSource messageSource;
    private final HashGenerator hashGenerator;
    
    public ContextBuilder(String context, NaturalTextMessageSource messageSource, 
                         HashGenerator hashGenerator) {
        this.context = context;
        this.messageSource = messageSource;
        this.hashGenerator = hashGenerator;
    }
    
    /**
     * Translate natural text within this context
     */
    public String translate(String naturalText, Object... args) {
        String contextualHash = hashGenerator.generateHash(naturalText, context);
        Locale locale = I18n.getCurrentLocale();
        
        if (messageSource != null) {
            TranslationResult result = messageSource.resolve(contextualHash, naturalText, locale);
            if (result.isFound()) {
                return MessageFormatter.format(result.getTranslation(), args, locale);
            }
        }
        
        // Fallback to original text
        return MessageFormatter.format(naturalText, args, locale);
    }
    
    /**
     * Create a message descriptor within this context
     */
    public MessageDescriptor describe(String naturalText, Object... args) {
        String contextualHash = hashGenerator.generateHash(naturalText, context);
        return new MessageDescriptor(contextualHash, naturalText, args);
    }
}