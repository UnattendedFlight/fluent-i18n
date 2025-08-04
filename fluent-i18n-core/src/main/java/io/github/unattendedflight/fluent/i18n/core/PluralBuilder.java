package io.github.unattendedflight.fluent.i18n.core;

import io.github.unattendedflight.fluent.i18n.util.IcuPluralExtractor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * PluralBuilder is a class designed to handle pluralization based on a given count and locale.
 * It supports defining natural-language strings for different plural forms and provides
 * a method to format the appropriate plural form based on the count and locale.
 */
public class PluralBuilder {
    /**
     * Represents the numerical value used to determine the appropriate plural form.
     * This variable is essential in constructing pluralized text based on locale-specific
     * pluralization rules. It serves as the key input for mapping the number to the correct
     * plural form, such as zero, one, two, few, many, or other.
     *
     * This field is immutable, ensuring that the associated pluralization logic remains
     * consistent and thread-safe across operations.
     */
    private final Number count;
    /**
     * Represents the locale associated with the pluralization process.
     * This variable defines the language and region-specific rules
     * that influence how plural forms are determined for a given count.
     *
     * The locale is crucial for correctly resolving plural forms, as different
     * languages and regions may have unique pluralization rules. It is utilized
     * throughout the pluralization logic to apply these rules accurately
     * when formatting or generating pluralized messages.
     *
     * Immutable and set during the construction of the {@code PluralBuilder}.
     */
    private final Locale locale;
    /**
     * A reference to the {@link NaturalTextMessageSource} instance utilized for obtaining translations.
     * This field serves as the message source, enabling the resolving and retrieval of translated text
     * based on natural language inputs and locale-specific requirements.
     *
     * In the context of pluralization, this field is used to fetch translations corresponding to
     * specific plural forms, allowing support for rich localization capabilities.
     *
     * The {@code NaturalTextMessageSource} may support operations such as resolving translations
     * by hashes and natural text, checking for translation existence, retrieving supported locales,
     * and managing translation resource updates or reloads.
     */
    private final NaturalTextMessageSource messageSource;
    /**
     * A utility responsible for generating hash values for natural text inputs.
     * Utilized in the context of pluralization to uniquely identify and map
     * text forms to their respective hash representations.
     *
     * This field serves as a reference to an implementation of the HashGenerator
     * interface, which provides mechanisms to create hash identifiers for texts,
     * optionally including additional contextual information.
     */
    private final HashGenerator hashGenerator;
    /**
     * A mapping of {@link PluralForm} to their associated textual representations.
     * This field serves as a container for the different plural form strings
     * relevant to the current pluralization context.
     *
     * The keys in this map correspond to the various plural forms supported by
     * the system (e.g., ZERO, ONE, TWO, FEW, MANY, OTHER), and the values are
     * their respective string representations, typically used for message
     * formatting or localization purposes.
     *
     * This map is utilized by the {@code PluralBuilder} to store and manage
     * plural forms tied to specific natural text inputs, allowing the
     * generation of appropriate ICU plural formats or applying localized rules.
     *
     * Being final, the map itself cannot be reassigned, but its content
     * may be modified (e.g., adding or updating entries per plural form).
     */
    private final Map<PluralForm, String> forms = new HashMap<>();
    /**
     * A final instance of the `IcuPluralExtractor` used for processing ICU plural formatted strings.
     * This field is responsible for extracting plural forms and their corresponding content
     * from provided ICU strings. It serves as a utility to interpret plural-related
     * logic based on CLDR rules, aiding in the construction of localized pluralized messages.
     *
     * This field is immutable and initialized within the `PluralBuilder`, ensuring consistency
     * during the lifetime of the builder and its associated operations.
     */
    private final IcuPluralExtractor extractor = new IcuPluralExtractor();
    
    /**
     * Constructs a new PluralBuilder instance.
     *
     * @param count the number to determine the plural form; used to decide the appropriate plural category
     * @param locale the locale specifying the language and regional preferences for pluralization
     * @param messageSource the source of natural text messages for translation and localization
     * @param hashGenerator the hash generator used to create unique identifiers for natural text messages
     */
    public PluralBuilder(Number count, Locale locale,
                        NaturalTextMessageSource messageSource, 
                        HashGenerator hashGenerator) {
        this.count = count;
        this.locale = locale;
        this.messageSource = messageSource;
        this.hashGenerator = hashGenerator;
    }
    
    /**
     * Associates a natural language text with the ZERO plural form
     * and stores it in the plural forms map.
     *
     * @param naturalText the text representing the ZERO plural form
     * @return the current instance of PluralBuilder for method chaining
     */
    public PluralBuilder zero(String naturalText) {
        forms.put(PluralForm.ZERO, naturalText);
        return this;
    }
    
    /**
     * Adds a singular form of a natural language text to the plural forms
     * mapping, associating it with the {@code PluralForm.ONE} key. This method
     * is used in constructing pluralization rules for localized text.
     *
     * @param naturalText the natural language text representing the singular form
     * @return the current instance of {@code PluralBuilder} for method chaining
     */
    public PluralBuilder one(String naturalText) {
        forms.put(PluralForm.ONE, naturalText);
        return this;
    }
    
    /**
     * Associates the provided natural text with the plural form "TWO" in the plural builder.
     * This allows the plural form "TWO" to have a specific natural language representation.
     *
     * @param naturalText the textual representation to be used for the plural form "TWO"
     * @return the current instance of {@code PluralBuilder} for method chaining
     */
    public PluralBuilder two(String naturalText) {
        forms.put(PluralForm.TWO, naturalText);
        return this;
    }
    
    /**
     * Sets the "few" plural form text for the {@code PluralBuilder}. This method associates
     * the given natural language text with the {@code PluralForm.FEW} enumeration value.
     *
     * @param naturalText the natural language text to be used for the "few" plural form
     * @return the current instance of {@code PluralBuilder}, allowing for method chaining
     */
    public PluralBuilder few(String naturalText) {
        forms.put(PluralForm.FEW, naturalText);
        return this;
    }
    
    /**
     * Sets the "MANY" form of the plural text to the specified natural language text.
     *
     * @param naturalText the natural language text representing the "MANY" form of the plural
     * @return the current instance of {@code PluralBuilder} for method chaining
     */
    public PluralBuilder many(String naturalText) {
        forms.put(PluralForm.MANY, naturalText);
        return this;
    }
    
    /**
     * Sets the natural text for the "OTHER" plural form.
     *
     * @param naturalText the natural language text representing the "OTHER" plural form
     * @return the current instance of {@code PluralBuilder}, allowing for method chaining
     */
    public PluralBuilder other(String naturalText) {
        forms.put(PluralForm.OTHER, naturalText);
        return this;
    }
    
    /**
     * Formats the count into a localized pluralized string based on the locale and plural rules.
     * The method leverages predefined mappings of plural forms to natural text and retrieves translations
     * if a message source is provided. It handles placeholder replacements within the final output string.
     *
     * @return a formatted and localized pluralized string based on the count, locale, and available translations
     */
    public String format() {
        PluralForm form = PluralRules.determine(count, locale);
        String naturalText = forms.getOrDefault(form, forms.get(PluralForm.OTHER));
        
        if (naturalText == null) {
            return String.valueOf(count);
        }
        
        // Generate the complete ICU MessageFormat string
        String icuPluralFormat = generateIcuPluralFormat();
        
        // Generate hash from the complete ICU MessageFormat string
        String hash = hashGenerator.generateHash(icuPluralFormat);
        
        // Try to get translation
        String result = naturalText;
        if (messageSource != null) {
            TranslationResult translation = messageSource.resolve(hash, naturalText, locale);
            result = translation.getTranslation();
            
            // If the translation is an ICU format string, parse it to extract the appropriate form
            if (result.startsWith("{0, plural,")) {
                result = extractPluralFormFromIcu(result, form);
            }
        }
        
        // Replace placeholders
        return result.replace("{0}", String.valueOf(count))
                    .replace("{}", String.valueOf(count))
                    .replace("#", String.valueOf(count));
    }
    
    /**
     * Generates an ICU plural format string based on the plural forms and their associated values.
     * The resulting format adheres to the ICU MessageFormat syntax for pluralization,
     * where each plural form (e.g., zero, one, two, few, many, other) is mapped to a specific string value.
     * The method ensures the plural forms are listed in a consistent order as defined by the `PluralForm` enumeration.
     * Null or empty values for any plural form are ignored in the resulting format.
     *
     * @return a string representing the ICU plural format, constructed from available plural forms and their values
     */
    private String generateIcuPluralFormat() {
        StringBuilder sb = new StringBuilder("{0, plural, ");
        
        // Add all available forms in the order they were added
        boolean first = true;
        List<Map.Entry<PluralForm, String>> sortedForms = new ArrayList<>(forms.entrySet());
        // Force sort so zero, one, two, few, many, other are always in the same order
        sortedForms.sort((s1, s2) -> {
            int index1 = PluralForm.valueOf(s1.getKey().name()).ordinal();
            int index2 = PluralForm.valueOf(s2.getKey().name()).ordinal();
            return Integer.compare(index1, index2);
        });
        for (Map.Entry<PluralForm, String> entry : sortedForms) {
            // Skip null or empty forms
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }
            if (!first) {
                sb.append(" ");
            }
            sb.append(entry.getKey().name().toLowerCase())
              .append(" {")
              .append(entry.getValue())
              .append("}");
            first = false;
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Extracts the content associated with a specific plural form from a given ICU formatted string.
     * If the target plural form is not found, it falls back to the content for the "OTHER" plural form.
     * If neither the target form nor the "OTHER" form is available, it defaults to a string representation of the count.
     *
     * @param icuString The ICU formatted string containing plural forms.
     * @param targetForm The desired {@link PluralForm} to extract from the ICU string.
     * @return The text content corresponding to the specified plural form, or a fallback as described above.
     */
    private String extractPluralFormFromIcu(String icuString, PluralForm targetForm) {
        try {
            Map<PluralForm, String> icuForms = extractor.extractAllPluralForms(icuString);
            return icuForms.getOrDefault(targetForm, icuForms.getOrDefault(PluralForm.OTHER, String.valueOf(count)));

        } catch (Exception e) {
            // If parsing fails, fallback to original natural text
            return forms.getOrDefault(targetForm, forms.getOrDefault(PluralForm.OTHER, String.valueOf(count)));
        }
    }
    
    /**
     * Finds the index of the closing brace '}' that corresponds to the opening
     * brace '{' starting at the specified position in the given text.
     *
     * The method assumes that the input text contains a well-formed set of braces.
     * If the starting position does not point to an opening brace, or if no
     * closing brace is found, the method returns -1.
     *
     * @param text the input string containing braces
     * @param start the index of the starting position where to begin the search
     * @return the index of the corresponding closing brace if found; -1 otherwise
     */
    private int findClosingBrace(String text, int start) {
        int braceCount = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}