package io.github.unattendedflight.fluent.i18n.util;

import io.github.unattendedflight.fluent.i18n.core.PluralForm;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IcuPluralExtractor {

  // Regex pattern to match plural forms: formname {content}
  // Handles nested braces by matching balanced braces
  private static final Pattern PLURAL_FORM_PATTERN =
      Pattern.compile("(\\w+)\\s*\\{([^{}]*(?:\\{[^{}]*\\}[^{}]*)*)\\}");

  // Pattern to extract the inner content of the ICU plural structure
  private static final Pattern ICU_CONTENT_PATTERN =
      Pattern.compile("\\{\\d+,\\s*plural,\\s*(.*)\\}$", Pattern.DOTALL);

  /**
   * Extracts all plural forms from an ICU string
   * @param icuString The ICU formatted string
   * @return Map of PluralForm to content string
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
   * Extract a specific plural form from ICU string
   * @param icuString The ICU formatted string
   * @param targetForm The form to extract
   * @return The content for the target form, or fallback
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
   * Parse string form name to PluralForm enum
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