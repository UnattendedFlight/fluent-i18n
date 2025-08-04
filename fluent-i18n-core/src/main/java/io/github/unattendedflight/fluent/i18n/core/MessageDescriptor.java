package io.github.unattendedflight.fluent.i18n.core;

import io.github.unattendedflight.fluent.i18n.I18n;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Descriptor for lazy message evaluation
 */
public class MessageDescriptor {
    private final String hash;
    private final String naturalText;
    private final Object[] args;
    
    public MessageDescriptor(String hash, String naturalText, Object[] args) {
        this.hash = hash;
        this.naturalText = naturalText;
        this.args = args != null ? Arrays.copyOf(args, args.length) : new Object[0];
    }
    
    private java.util.Locale getCurrentLocaleInternal() {
        try {
            return I18n.getCurrentLocale();
        } catch (Exception e) {
            return java.util.Locale.getDefault();
        }
    }
    
    /**
     * Resolve this message to a translated string for current locale.
     */
    public String resolve() {
       return I18n.resolve(this);
    }

    public String resolve(Locale locale) {
        return I18n.resolve(this, locale);
    }
    
    /**
     * Check if this message has a translation for the current locale
     */
    public boolean hasTranslation() {
        return hasTranslation(getCurrentLocaleInternal());
    }
    
    /**
     * Check if this message has a translation for the specified locale
     */
    public boolean hasTranslation(Locale locale) {
        return I18n.getMessageSource().exists(hash, locale);
    }
    
    /**
     * Create a new descriptor with additional arguments
     */
    public MessageDescriptor withArgs(Object... additionalArgs) {
        Object[] combinedArgs = new Object[args.length + additionalArgs.length];
        System.arraycopy(args, 0, combinedArgs, 0, args.length);
        System.arraycopy(additionalArgs, 0, combinedArgs, args.length, additionalArgs.length);
        return new MessageDescriptor(hash, naturalText, combinedArgs);
    }
    
    // Getters
    public String getHash() { return hash; }
    public String getNaturalText() { return naturalText; }
    public Object[] getArgs() { return Arrays.copyOf(args, args.length); }
    
    @Override
    public String toString() {
        return naturalText;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageDescriptor)) return false;
        MessageDescriptor that = (MessageDescriptor) o;
        return Objects.equals(hash, that.hash) && 
               Objects.equals(naturalText, that.naturalText) &&
               Arrays.equals(args, that.args);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hash, naturalText, Arrays.hashCode(args));
    }
}