package io.github.unattendedflight.fluent.i18n.util;

import io.github.unattendedflight.fluent.i18n.I18n;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Currency;
import java.util.Locale;

/**
 * Utility methods for common internationalization tasks
 */
public final class I18nUtils {
    
    private I18nUtils() {} // Utility class
    
    /**
     * Format a number according to the current locale
     */
    public static String formatNumber(Number number) {
        return formatNumber(number, I18n.getCurrentLocale());
    }
    
    /**
     * Format a number according to the specified locale
     */
    public static String formatNumber(Number number, Locale locale) {
        return NumberFormat.getNumberInstance(locale).format(number);
    }
    
    /**
     * Format currency according to the current locale
     */
    public static String formatCurrency(Number amount, Currency currency) {
        return formatCurrency(amount, currency, I18n.getCurrentLocale());
    }
    
    /**
     * Format currency according to the specified locale
     */
    public static String formatCurrency(Number amount, Currency currency, Locale locale) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(currency);
        return formatter.format(amount);
    }
    
    /**
     * Format percentage according to the current locale
     */
    public static String formatPercent(Number percentage) {
        return formatPercent(percentage, I18n.getCurrentLocale());
    }
    
    /**
     * Format percentage according to the specified locale
     */
    public static String formatPercent(Number percentage, Locale locale) {
        return NumberFormat.getPercentInstance(locale).format(percentage);
    }
    
    /**
     * Format date according to the current locale (medium style)
     */
    public static String formatDate(LocalDate date) {
        return formatDate(date, FormatStyle.MEDIUM, I18n.getCurrentLocale());
    }
    
    /**
     * Format date according to the specified locale and style
     */
    public static String formatDate(LocalDate date, FormatStyle style, Locale locale) {
        return date.format(DateTimeFormatter.ofLocalizedDate(style).withLocale(locale));
    }
    
    /**
     * Format datetime according to the current locale (medium style)
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return formatDateTime(dateTime, FormatStyle.MEDIUM, I18n.getCurrentLocale());
    }
    
    /**
     * Format datetime according to the specified locale and style
     */
    public static String formatDateTime(LocalDateTime dateTime, FormatStyle style, Locale locale) {
        return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(style).withLocale(locale));
    }
    
    /**
     * Create a conditional message based on boolean value
     */
    public static String conditionalMessage(boolean condition, String trueMessage, String falseMessage) {
        return condition ? I18n.translate(trueMessage) : I18n.translate(falseMessage);
    }
    
    /**
     * Create a status message with context
     */
    public static String statusMessage(String status) {
        return I18n.context("status").translate(status);
    }
    
    /**
     * Create an error message with context
     */
    public static String errorMessage(String error) {
        return I18n.context("errors").translate(error);
    }
}