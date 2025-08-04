package io.github.unattendedflight.fluent.i18n.compiler;

import java.nio.file.Path;
import java.util.*;

/**
 * Represents the result of a translation compilation process.
 * This class provides information about the compilation, including
 * processed locales, generated files, encountered errors, missing translation files,
 * and translation statistics.
 */
public class CompilationResult {
    /**
     * A map that tracks the number of compilation results for each processed locale.
     * The keys represent locale identifiers (e.g., "en", "fr"), and the values indicate
     * the number of files or entities successfully processed for the corresponding locale.
     */
    private final Map<String, Integer> processedLocales;
    /**
     * A mapping of locale identifiers to the list of file paths for the generated output files
     * corresponding to each locale.
     *
     * This map is used to associate the generated output files with their respective locales during
     * the compilation process. Each locale identifier (key) corresponds to a list of file paths (value)
     * that represent the translation files generated for that locale.
     */
    private final Map<String, List<Path>> generatedFiles;
    /**
     * A collection of {@code CompilationError} instances encountered during
     * the compilation process.
     *
     * This list contains detailed information about each compilation error,
     * such as the locale in which the error occurred, the error message, and
     * optional details about the underlying cause of the errors. It provides
     * a comprehensive representation of any issues encountered during the
     * translation or compilation workflow.
     */
    private final List<CompilationError> errors;
    /**
     * A list of missing Portable Object (PO) files that were expected but not found during
     * the compilation process. Each entry in the list represents the filename of a missing
     * PO file.
     */
    private final List<String> missingPoFiles;
    /**
     * A mapping of locale identifiers to their corresponding translation statistics.
     * Each key in the map represents a locale, while the value provides statistics
     * about the total strings, translated strings, missing strings, and completion
     * percentage for that locale.
     */
    private final Map<String, TranslationStats> translationStats;
    
    /**
     * Constructs a `CompilationResult` instance using data provided by the `Builder`.
     *
     * @param builder the builder containing initialization data, including processed locales,
     *                generated files, errors, missing PO files, and translation statistics
     */
    private CompilationResult(Builder builder) {
        this.processedLocales = Map.copyOf(builder.processedLocales);
        this.generatedFiles = Map.copyOf(builder.generatedFiles);
        this.errors = List.copyOf(builder.errors);
        this.missingPoFiles = List.copyOf(builder.missingPoFiles);
        this.translationStats = Map.copyOf(builder.translationStats);
    }
    
    /**
     * Retrieves a map of processed locales and their respective counts.
     *
     * @return a Map where keys represent locale identifiers as Strings, and values represent the count of processed items for each locale
     */
    public Map<String, Integer> getProcessedLocales() { return processedLocales; }
    /**
     * Retrieves a map of generated files grouped by their associated locales.
     * The map keys represent locale strings, and the values are lists of file paths
     * corresponding to the files generated for each locale.
     *
     * @return a map where each key is a locale string and each value is a list of
     *         paths to the files generated for that locale.
     */
    public Map<String, List<Path>> getGeneratedFiles() { return generatedFiles; }
    /**
     * Retrieves the list of compilation errors encountered during the translation process.
     *
     * @return a list of CompilationError objects providing details about each error
     */
    public List<CompilationError> getErrors() { return errors; }
    /**
     * Retrieves a list of missing PO (Portable Object) files necessary for the compilation process.
     *
     * @return a list of strings representing the file paths or names of the missing PO files
     */
    public List<String> getMissingPoFiles() { return missingPoFiles; }
    /**
     * Retrieves the translation statistics for each locale.
     *
     * The returned map contains locale identifiers as keys and their corresponding
     * {@link TranslationStats} objects as values. Each {@link TranslationStats} object
     * provides information about the total strings, translated strings, missing strings,
     * and completion percentage for a specific locale.
     *
     * @return a map where the keys are locale identifiers (e.g., "en", "fr") and the
     *         values are {@link TranslationStats} objects containing translation metrics
     *         for those locales
     */
    public Map<String, TranslationStats> getTranslationStats() { return translationStats; }
    
    /**
     * Determines if the compilation process was successful.
     *
     * @return true if there are no compilation errors; false otherwise.
     */
    public boolean isSuccessful() {
        return errors.isEmpty();
    }
    
    /**
     * Calculates and returns the total number of generated files.
     *
     * The method sums up the sizes of all lists in the generatedFiles map,
     * where each list represents the paths of files generated for a specific
     * locale or context.
     *
     * @return the total count of generated files across all locales or contexts
     */
    public int getTotalGeneratedFiles() {
        return generatedFiles.values().stream()
            .mapToInt(List::size)
            .sum();
    }
    
    /**
     * Generates a summary of translation statistics for a given locale.
     * The summary includes the total number of strings, the number of translated strings,
     * the percentage of completion, and the number of missing strings.
     *
     * @param locale the locale for which translation statistics are to be summarized
     * @return a string containing the translation summary for the specified locale,
     *         or a message indicating that no data is available for the locale
     */
    public String getTranslationSummary(String locale) {
        TranslationStats stats = translationStats.get(locale);
        if (stats == null) {
            return locale + ": No translation data available";
        }
        
        int total = stats.getTotalStrings();
        int translated = stats.getTranslatedStrings();
        int missing = stats.getMissingStrings();
        double percentage = stats.getCompletionPercentage();
        
        return String.format("%s: %d/%d translations (%.1f%% complete, %d missing)", 
                           locale, translated, total, percentage, missing);
    }
    
    /**
     * Computes and returns overall translation statistics based on individual locale statistics.
     * The method calculates the total number of locales, the number of completed locales
     * (where all strings are translated), and the average completion percentage across all locales.
     *
     * @return an OverallTranslationStats object containing total locales, completed locales,
     *         and the average completion percentage.
     */
    public OverallTranslationStats getOverallStats() {
        int totalLocales = translationStats.size();
        int completedLocales = (int) translationStats.values().stream()
            .filter(stats -> stats.getTranslatedStrings() == stats.getTotalStrings())
            .count();
        
        double avgCompletion = translationStats.values().stream()
            .mapToDouble(TranslationStats::getCompletionPercentage)
            .average()
            .orElse(0.0);
        
        return new OverallTranslationStats(totalLocales, completedLocales, avgCompletion);
    }
    
    /**
     * Creates a new instance of the {@code Builder} class for constructing a {@code CompilationResult}.
     *
     * @return a new {@code Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Represents statistics related to the translation process for a specific locale or dataset.
     *
     * This class provides computed metrics including:
     * - Total number of strings in the dataset.
     * - Number of strings that have been successfully translated.
     * - Number of strings missing translation.
     * - Translation completion percentage.
     *
     * Instances of this class are immutable once created.
     */
    public static class TranslationStats {
        /**
         * The total number of strings in the dataset.
         *
         * This field represents the complete count of strings that compose the translation dataset,
         * regardless of their translation status.
         * It is a fixed value initialized at the time of object creation and does not change.
         */
        private final int totalStrings;
        /**
         * The number of strings that have been successfully translated.
         *
         * This variable stores the count of translated strings within a dataset
         * used to compute translation statistics. It directly contributes to
         * the calculation of missing strings and completion percentage.
         *
         * Context:
         * - Part of the `TranslationStats` class to represent translation-related metrics.
         * - Immutable after object construction.
         */
        private final int translatedStrings;
        /**
         * The number of strings that lack translation in the dataset.
         *
         * This value represents the disparity between the total number of strings in the dataset
         * and those strings that have been successfully translated. A high value indicates that
         * significant portions of the dataset remain untranslated.
         *
         * The value is computed during the construction of the enclosing object and remains
         * immutable once assigned.
         */
        private final int missingStrings;
        /**
         * Represents the percentage of translation strings that have been successfully translated.
         *
         * This value is calculated as the ratio of translated strings to total strings,
         * expressed as a percentage. If no strings are available (total is zero),
         * the value is set to 0.0.
         *
         * The value is immutable and reflects the completion status for the relevant dataset or locale.
         */
        private final double completionPercentage;
        
        /**
         * Constructs a TranslationStats object with the given total and translated strings.
         *
         * This constructor calculates the number of missing strings and the completion percentage
         * based on the provided total and translated strings.
         *
         * @param totalStrings the total number of strings in the dataset
         * @param translatedStrings the number of strings that have been translated
         */
        public TranslationStats(int totalStrings, int translatedStrings) {
            this.totalStrings = totalStrings;
            this.translatedStrings = translatedStrings;
            this.missingStrings = totalStrings - translatedStrings;
            this.completionPercentage = totalStrings > 0 ? 
                (double) translatedStrings / totalStrings * 100.0 : 0.0;
        }
        
        /**
         * Retrieves the total number of strings in the dataset.
         *
         * @return the total number of strings
         */
        public int getTotalStrings() { return totalStrings; }
        /**
         * Retrieves the total number of strings that have been successfully translated.
         *
         * @return the number of successfully translated strings
         */
        public int getTranslatedStrings() { return translatedStrings; }
        /**
         * Retrieves the total number of strings missing translation in the dataset.
         *
         * This method provides information about untranslated strings, which
         * can be useful for tracking the progress of the translation process.
         *
         * @return the number of strings that are missing translation
         */
        public int getMissingStrings() { return missingStrings; }
        /**
         * Retrieves the percentage of completion for translations.
         *
         * The completion percentage is calculated based on the ratio of translated strings
         * to the total number of strings, expressed as a percentage. If there are no strings
         * in total, the completion percentage is 0.0.
         *
         * @return the translation completion percentage as a double value
         */
        public double getCompletionPercentage() { return completionPercentage; }
    }
    
    /**
     * Represents overall translation statistics across all locales.
     * This class provides data about the total number of locales involved,
     * the number of fully completed locales, and the average completion
     * percentage across all locales.
     */
    public static class OverallTranslationStats {
        /**
         * The total number of locales for which translation data is being tracked.
         * This value represents the overall scope of locales included in the
         * translation statistics.
         */
        private final int totalLocales;
        /**
         * The number of locales with fully completed translations.
         * This value represents how many locales in the translation process
         * have achieved 100% completion, implying no untranslated content
         * remains for these locales.
         */
        private final int completedLocales;
        /**
         * Represents the average completion percentage of translations across all locales.
         * This value is a double that quantifies the extent to which the translation work
         * has been completed, averaging the completion percentages of all locales.
         */
        private final double averageCompletion;
        
        /**
         * Constructs an instance of OverallTranslationStats.
         *
         * @param totalLocales the total number of locales involved in the translations
         * @param completedLocales the number of locales with translations fully completed
         * @param averageCompletion the average completion percentage across all locales
         */
        public OverallTranslationStats(int totalLocales, int completedLocales, double averageCompletion) {
            this.totalLocales = totalLocales;
            this.completedLocales = completedLocales;
            this.averageCompletion = averageCompletion;
        }
        
        /**
         * Retrieves the total number of locales involved in the translation process.
         *
         * @return the total number of locales
         */
        public int getTotalLocales() { return totalLocales; }
        /**
         * Retrieves the number of locales that have been fully completed.
         *
         * @return the number of completed locales.
         */
        public int getCompletedLocales() { return completedLocales; }
        /**
         * Retrieves the average completion percentage across all locales.
         *
         * @return The average percentage of completion as a double.
         */
        public double getAverageCompletion() { return averageCompletion; }
        
        /**
         * Generates a summary of the overall translation statistics.
         *
         * The summary includes the number of fully completed locales,
         * the total number of locales, and the average percentage
         * completion of translations across all locales in a formatted string.
         *
         * @return A string summarizing the overall translation statistics
         *         in the format: "Overall: {completedLocales}/{totalLocales} locales complete ({averageCompletion}% average completion)".
         */
        public String getSummary() {
            return String.format("Overall: %d/%d locales complete (%.1f%% average completion)", 
                               completedLocales, totalLocales, averageCompletion);
        }
    }
    
    /**
     * The Builder class provides a mechanism for constructing a {@link CompilationResult} instance
     * by aggregating data such as processed locales, generated files, compilation errors, missing PO files,
     * and translation statistics during the translation process.
     *
     * This class follows the builder pattern to allow a step-by-step and flexible creation of
     * {@link CompilationResult} objects.
     */
    public static class Builder {
        /**
         * A map that stores information about processed locales in the translation compilation process.
         *
         * The map keys represent locale identifiers (e.g., "en", "fr"), while the values
         * represent the number of translation entries processed for each locale.
         *
         * This data can be used to track and measure the progress or outcomes of the
         * translation process for multiple locales.
         */
        private final Map<String, Integer> processedLocales = new HashMap<>();
        /**
         * A mapping of locale and output format identifiers to the list of file paths
         * representing the generated output files for each combination. The key is
         * constructed as a combination of the locale and output format (e.g., "en:JSON").
         *
         * This map is used to track all files created during the translation process.
         * It facilitates the organization and retrieval of the generated files for
         * post-processing or validation purposes.
         */
        private final Map<String, List<Path>> generatedFiles = new HashMap<>();
        /**
         * A collection of {@link CompilationError} instances encountered during the translation process.
         *
         * Each {@code CompilationError} in this list represents an error experienced
         * while processing translations, along with context such as the specific locale,
         * a descriptive error message, and optionally an exception identifying the root cause.
         *
         * This collection is used to aggregate all errors encountered during compilation
         * and is helpful for reporting or debugging the issues in the processing pipeline.
         */
        private final List<CompilationError> errors = new ArrayList<>();
        /**
         * A list of file paths representing the missing PO (Portable Object) files
         * that were expected but not found or could not be processed during the compilation.
         *
         * This variable is used to collect and track information about missing translations
         * for specific locales. Each entry in the list typically includes the locale and
         * the path of the missing PO file in a formatted string.
         */
        private final List<String> missingPoFiles = new ArrayList<>();
        /**
         * A map that stores translation statistics for various locales.
         *
         * Each entry in the map consists of a locale as the key (represented by a String)
         * and corresponding translation statistics (represented by a {@link TranslationStats} object).
         *
         * This map is used to aggregate translation data, such as the total number of strings,
         * the number of successfully translated strings, and the completion percentage, for each locale
         * processed during the translation compilation process.
         *
         * The data in this map is final and is intended to be immutable once populated within the
         * {@link CompilationResult.Builder} class using the*/
        private final Map<String, TranslationStats> translationStats = new HashMap<>();
        
        /**
         * Adds a processed locale and its corresponding entry count to the builder.
         *
         * @param locale the identifier of the locale being processed, typically in language-region format (e.g., "en-US")
         * @param entryCount the number of translation entries processed for the specified locale
         * @return the current instance of the Builder for method chaining
         */
        public Builder addProcessedLocale(String locale, int entryCount) {
            processedLocales.put(locale, entryCount);
            return this;
        }
        
        /**
         * Adds a generated file to the internal collection of files associated with a specific locale
         * and output format. This method enables tracking of files that have been created during the
         * compilation process.
         *
         * @param locale the locale identifier (e.g., "en_US") for which the file was generated
         * @param format the output format of the generated file (e.g., JSON, PROPERTIES, BINARY)
         * @param file the path to the generated file
         * @return the {@code Builder} instance to enable method chaining
         */
        public Builder addGeneratedFile(String locale, OutputFormat format, Path file) {
            String key = locale + ":" + format.name();
            generatedFiles.computeIfAbsent(key, k -> new ArrayList<>()).add(file);
            return this;
        }
        
        /**
         * Adds a compilation error to the list of errors being tracked by the Builder.
         *
         * @param error the {@code CompilationError} instance to be added, representing an error
         *              encountered during the compilation process
         * @return the current instance of {@code Builder}, allowing method chaining for further configuration
         */
        public Builder addError(CompilationError error) {
            errors.add(error);
            return this;
        }
        
        /**
         * Adds a missing PO file entry for the specified locale to the list of missing files.
         *
         * @param locale the locale associated with the missing PO file
         * @param poFile the path of the missing PO file
         * @return the Builder instance to allow method chaining
         */
        public Builder addMissingPoFile(String locale, Path poFile) {
            missingPoFiles.add(locale + ": " + poFile);
            return this;
        }
        
        /**
         * Adds translation statistics for a specific locale to the builder.
         * This method calculates the total number of strings and the count of translated strings
         * for the given locale and stores the resulting statistics in the builder.
         *
         * @param locale the locale for which translation statistics are being added
         * @param data the {@link TranslationData} object containing translation entries and metadata for the locale
         * @return the builder instance, allowing for method chaining
         */
        public Builder addTranslationStats(String locale, TranslationData data) {
            int totalStrings = data.getEntryCount();
            int translatedStrings = (int) data.getEntries().values().stream()
                .filter(TranslationEntry::hasTranslation)
                .count();
            
            translationStats.put(locale, new TranslationStats(totalStrings, translatedStrings));
            return this;
        }
        
        /**
         * Builds and returns a {@link CompilationResult} instance based on the data
         * accumulated in the {@link Builder}.
         *
         * @return a {@link CompilationResult} instance containing processed locales,
         *         generated files, compilation errors, missing PO files, and
         *         translation statistics.
         */
        public CompilationResult build() {
            return new CompilationResult(this);
        }
    }
}