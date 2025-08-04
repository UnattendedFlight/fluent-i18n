package io.github.unattendedflight.fluent.i18n.compiler;

import java.nio.file.Path;
import java.util.*;

/**
 * Result of translation compilation process
 */
public class CompilationResult {
    private final Map<String, Integer> processedLocales;
    private final Map<String, List<Path>> generatedFiles;
    private final List<CompilationError> errors;
    private final List<String> missingPoFiles;
    private final Map<String, TranslationStats> translationStats;
    
    private CompilationResult(Builder builder) {
        this.processedLocales = Map.copyOf(builder.processedLocales);
        this.generatedFiles = Map.copyOf(builder.generatedFiles);
        this.errors = List.copyOf(builder.errors);
        this.missingPoFiles = List.copyOf(builder.missingPoFiles);
        this.translationStats = Map.copyOf(builder.translationStats);
    }
    
    public Map<String, Integer> getProcessedLocales() { return processedLocales; }
    public Map<String, List<Path>> getGeneratedFiles() { return generatedFiles; }
    public List<CompilationError> getErrors() { return errors; }
    public List<String> getMissingPoFiles() { return missingPoFiles; }
    public Map<String, TranslationStats> getTranslationStats() { return translationStats; }
    
    public boolean isSuccessful() {
        return errors.isEmpty();
    }
    
    public int getTotalGeneratedFiles() {
        return generatedFiles.values().stream()
            .mapToInt(List::size)
            .sum();
    }
    
    /**
     * Get formatted translation summary for a locale
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
     * Get overall translation completion statistics
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
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Statistics for a single locale
     */
    public static class TranslationStats {
        private final int totalStrings;
        private final int translatedStrings;
        private final int missingStrings;
        private final double completionPercentage;
        
        public TranslationStats(int totalStrings, int translatedStrings) {
            this.totalStrings = totalStrings;
            this.translatedStrings = translatedStrings;
            this.missingStrings = totalStrings - translatedStrings;
            this.completionPercentage = totalStrings > 0 ? 
                (double) translatedStrings / totalStrings * 100.0 : 0.0;
        }
        
        public int getTotalStrings() { return totalStrings; }
        public int getTranslatedStrings() { return translatedStrings; }
        public int getMissingStrings() { return missingStrings; }
        public double getCompletionPercentage() { return completionPercentage; }
    }
    
    /**
     * Overall translation statistics across all locales
     */
    public static class OverallTranslationStats {
        private final int totalLocales;
        private final int completedLocales;
        private final double averageCompletion;
        
        public OverallTranslationStats(int totalLocales, int completedLocales, double averageCompletion) {
            this.totalLocales = totalLocales;
            this.completedLocales = completedLocales;
            this.averageCompletion = averageCompletion;
        }
        
        public int getTotalLocales() { return totalLocales; }
        public int getCompletedLocales() { return completedLocales; }
        public double getAverageCompletion() { return averageCompletion; }
        
        public String getSummary() {
            return String.format("Overall: %d/%d locales complete (%.1f%% average completion)", 
                               completedLocales, totalLocales, averageCompletion);
        }
    }
    
    public static class Builder {
        private final Map<String, Integer> processedLocales = new HashMap<>();
        private final Map<String, List<Path>> generatedFiles = new HashMap<>();
        private final List<CompilationError> errors = new ArrayList<>();
        private final List<String> missingPoFiles = new ArrayList<>();
        private final Map<String, TranslationStats> translationStats = new HashMap<>();
        
        public Builder addProcessedLocale(String locale, int entryCount) {
            processedLocales.put(locale, entryCount);
            return this;
        }
        
        public Builder addGeneratedFile(String locale, OutputFormat format, Path file) {
            String key = locale + ":" + format.name();
            generatedFiles.computeIfAbsent(key, k -> new ArrayList<>()).add(file);
            return this;
        }
        
        public Builder addError(CompilationError error) {
            errors.add(error);
            return this;
        }
        
        public Builder addMissingPoFile(String locale, Path poFile) {
            missingPoFiles.add(locale + ": " + poFile);
            return this;
        }
        
        public Builder addTranslationStats(String locale, TranslationData data) {
            int totalStrings = data.getEntryCount();
            int translatedStrings = (int) data.getEntries().values().stream()
                .filter(TranslationEntry::hasTranslation)
                .count();
            
            translationStats.put(locale, new TranslationStats(totalStrings, translatedStrings));
            return this;
        }
        
        public CompilationResult build() {
            return new CompilationResult(this);
        }
    }
}