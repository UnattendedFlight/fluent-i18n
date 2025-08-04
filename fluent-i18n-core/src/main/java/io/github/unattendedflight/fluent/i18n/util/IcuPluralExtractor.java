package io.github.unattendedflight.fluent.i18n.util;

import io.github.unattendedflight.fluent.i18n.core.PluralForm;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting plural forms and their associated content from ICU-formatted strings.
 *
 * This class provides methods for parsing strings formatted according to the ICU MessageFormat
 * specifications to retrieve content associated with specific plural forms.
 *
 * The class supports extracting all plural forms as well as specific plural forms, based on
 * predefined linguistic rules represented by the {@code PluralForm} enumeration.
 *
 * Internally, the extraction process makes use of regular expressions to parse the input
 * ICU string, handling both top-level and nested structures in the ICU format.
 */
public class IcuPluralExtractor {

  /**
   * Regex pattern to match plural forms in ICU strings.
   *
   * This pattern is used to extract plural forms from text in the format: `formName {content}`.
   * It captures the plural form name (e.g., ONE, OTHER) and the associated content, handling
   * nested braces within the content. This is particularly useful for parsing ICU-style
   * pluralization strings that are commonly used in internationalization and localization processes.
   *
   * The pattern groups:
   * - Group 1: The name of the plural form (e.g., ZERO, ONE, FEW).
   * - Group 2: The content associated with that plural form, including support for balanced nested braces.
   */
  private static final Pattern PLURAL_FORM_PATTERN =
      Pattern.compile("(\\w+)\\s*\\{([^{}]*(?:\\{[^{}]*\\}[^{}]*)*)\\}");

  /**
   * A regular expression pattern that matches and extracts the inner content
   * of an ICU (International Components for Unicode) plural structure.
   *
   * The pattern is designed to locate strings formatted in the ICU message syntax,
   * specifically those containing pluralization logic. It identifies strings that
   * start with a placeholder index (e.g., "{0") followed by the keyword "plural,"
   * and extracts everything between the opening and closing braces after that.
   *
   * The captured group (group 1) of this pattern corresponds to the inner content
   * representing pluralization rules and associated text forms.
   *
   * Example format that this pattern matches:
   * "{0, plural, one {item} other {items}}"
   *
   * This pattern uses the DOTALL flag so that the wildcard "." matches line terminators,
   * enabling support for multi-line ICU strings.
   */
  private static final Pattern ICU_CONTENT_PATTERN =
      Pattern.compile("\\{\\d+,\\s*plural,\\s*(.*)\\}$", Pattern.DOTALL);

  /**
   * Extracts all plural forms and their associated content from a given ICU formatted string.
   *
   * This method analyzes an ICU string that uses the "{0, plural, ...}" pattern, identifies all
   * defined plural forms (e.g., "one", "many", "other"), and maps each plural form to its
   * corresponding content. If the string cannot be parsed or does not match the expected
   * pattern, an empty map will be returned.
   *
   * @param icuString The ICU formatted string containing plural forms and their definitions.
   * @return A map where keys are {@link PluralForm} values representing plural categories,
   *         and the values are the corresponding content strings extracted from the ICU string.
   */
  public Map<PluralForm, String> extractAllPluralForms(String icuString) {
    Map<PluralForm, String> forms = new HashMap<>();

    try {
      // Extract the inner content (everything after "{0, plural, " and before final "}")
      Matcher contentMatcher = ICU_CONTENT_PATTERN.matcher(icuString.trim());
      if (!contentMatcher.find()) {
        return forms;
      }

      String innerContent = contentMatcher.group(1);

      // Find all plural forms using regex
      Matcher formMatcher = PLURAL_FORM_PATTERN.matcher(innerContent);

      while (formMatcher.find()) {
        String formName = formMatcher.group(1).toLowerCase();
        String content = formMatcher.group(2).trim();

        // Convert form name to enum
        PluralForm pluralForm = parsePluralForm(formName);
        if (pluralForm != null) {
          forms.put(pluralForm, content);
        }
      }

    } catch (Exception e) {
      // Log error or handle as needed
      System.err.println("Failed to parse ICU string: " + icuString);
    }

    return forms;
  }

  /**
   * Extracts the appropriate plural form string from an ICU-formatted string based on the target plural form.
   * If the target form is not found, it falls back to the "OTHER" plural form.
   * If no valid form is available, a default fallback value is returned.
   *
   * @param icuString the ICU-formatted string containing pluralization rules and corresponding values
   * @param targetForm the specific plural form to extract from the ICU string
   * @return the string corresponding to the target plural form, or a fallback value if the target form is not found
   */
  public String extractPluralForm(String icuString, PluralForm targetForm) {
    Map<PluralForm, String> allForms = extractAllPluralForms(icuString);

    // Try to get the target form first
    String result = allForms.get(targetForm);
    if (result != null) {
      return result;
    }

    // Fallback to OTHER if target form not found
    if (targetForm != PluralForm.OTHER) {
      result = allForms.get(PluralForm.OTHER);
      if (result != null) {
        return result;
      }
    }

    // Final fallback
    return String.valueOf(0); // or whatever default you prefer
  }

  /**
   * Parses a given string into a corresponding {@code PluralForm} enumeration value.
   * This method converts the input {@code formName} to uppercase and attempts to match
   * it to a predefined {@code PluralForm} constant. If the input does not correspond to
   * a known {@code PluralForm}, it logs an error message and returns {@code null}.
   *
   * @param formName the name of the plural form as a string, expected to match one of the predefined {@code PluralForm} constants
   * @return the corresponding {@code PluralForm} if the input matches a valid constant, or {@code null} if the input is invalid
   */
  private PluralForm parsePluralForm(String formName) {
    try {
      return PluralForm.valueOf(formName.toUpperCase());
    } catch (IllegalArgumentException e) {
      // Handle unknown form names
      System.err.println("Unknown plural form: " + formName);
      return null;
    }
  }
}