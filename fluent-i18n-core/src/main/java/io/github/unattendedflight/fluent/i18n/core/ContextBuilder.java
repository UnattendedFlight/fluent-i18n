package io.github.unattendedflight.fluent.i18n.core;

import io.github.unattendedflight.fluent.i18n.I18n;
import java.util.Locale;

/**
 * Builder class for creating and managing a context-based translation system.
 * This class provides methods to translate natural text and create message descriptors
 * within a specific context. It utilizes a message source for resolving translations
 * and a hash generator for generating unique identifiers for each natural text entry.
 */
public class ContextBuilder {
    /**
     * Represents the context in which translations are resolved and message descriptors
     * are created. This context is used to disambiguate messages that may otherwise have
     * identical natural text but are intended for different usages.
     *
     * The context is utilized in hash generation for natural text entries to ensure unique
     * identifiers, allowing translations to be appropriately scoped and resolved.
     *
     * It plays a critical role in the translation lifecycle by functioning as a namespace
     * for message resolution and generation.
     */
    private String context;

    /**
     * Represents a unique identifier or qualifier to disambiguate translations
     * and natural text within a given context.
     *
     * Used as part of the translation process to ensure localized text can be
     * differentiated across different usage scenarios, avoiding accidental collisions
     * when the same text string appears in multiple contexts. The `contextKey`
     * plays a critical role in generating hashes for translations and resolving
     * locale-specific text accurately.
     *
     * Edge case: Ensure this key is unique within its intended scope to prevent
     * incorrect translations. Duplication or overly generic keys may undermine
     * the integrity of localized content.
     */
    private final String contextKey;
    /**
     * Provides the source of translations for natural text within the context.
     * This variable is used to resolve translations based on a specific locale and
     * a generated hash tied to the natural text. It serves as the primary means
     * to fetch translations or determine their availability.
     *
     * The implementation of the message source allows retrieving translations
     * for a given hash, checking the existence of a translation for a specific
     * locale, reloading the source of translations, and optionally warming up
     * to preload translations for performance optimization.
     *
     * Used by methods like {@code translate} to fetch localized content and
     * by {@code describe} to create message descriptors representing translatable
     * components within a given context.
     */
    private final NaturalTextMessageSource messageSource;
    /**
     * An immutable instance of {@code HashGenerator} used for generating unique, consistent hashes
     * for natural text, optionally within a specific context.
     * This field is primarily utilized to associate a unique identifier with a given piece of
     * natural text, enabling efficient translation and message resolution in a context-aware manner.
     * It plays a crucial role in the process of contextualizing and retrieving translations
     * or message descriptors in the {@code ContextBuilder}.
     */
    private final HashGenerator hashGenerator;
    
    /**
     * Constructs a ContextBuilder with the specified context, message source,
     * and hash generator.
     *
     * @param context The specific context for this builder, used to differentiate
     *                translations and natural text within a localized system.
     * @param messageSource The message source used to resolve translations for natural
     *                      text based on the generated hash and target locale.
     * @param hashGenerator The hash generator responsible for producing consistent
     *                      hashes for natural text, optionally including context.
     */
    public ContextBuilder(String contextKey, String context, NaturalTextMessageSource messageSource,
                         HashGenerator hashGenerator) {
        this.contextKey = contextKey;
        this.context = context;
        this.messageSource = messageSource;
        this.hashGenerator = hashGenerator;
    }

    public ContextBuilder description(String context) {
        this.context = context;
        return this;
    }
    
    /**
     * Translates the given natural language text into a localized and formatted text
     * based on the current context and locale. If the translation for the text is not
     * found, the method falls back to the original text and applies any supplied arguments.
     *
     * @param naturalText the natural language text to be translated
     * @param args optional arguments to be applied to the translated text using a formatter
     * @return the translated and formatted text if a match is found in the message source;
     *         otherwise, the original text formatted with the supplied arguments
     */
    public String translate(String naturalText, Object... args) {
        String contextualHash = hashGenerator.generateHash(naturalText, contextKey);
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
     * Creates a {@link MessageDescriptor} for the provided natural text and arguments.
     * This method generates a contextual hash based on the natural text and the builder's context
     * and uses it to construct a message descriptor for lazy evaluation or translation resolution.
     *
     * @param naturalText  The natural text to be described, typically representing a message or key.
     * @param args         Optional arguments to be used for message formatting. These can
     *                     include placeholders to be replaced when the message is resolved.
     * @return A {@link MessageDescriptor} containing the natural text, its contextual hash,
     *         and the provided arguments.
     */
    public MessageDescriptor describe(String naturalText, Object... args) {
        String contextualHash = hashGenerator.generateHash(naturalText, contextKey);
        return new MessageDescriptor(contextualHash, naturalText, args);
    }
}