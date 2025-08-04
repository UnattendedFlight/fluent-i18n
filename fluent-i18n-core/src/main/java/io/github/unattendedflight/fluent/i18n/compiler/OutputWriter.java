package io.github.unattendedflight.fluent.i18n.compiler;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for writing translation data to different output formats
 */
public interface OutputWriter {
    
    /**
     * Write translation data to output file
     */
    Path write(TranslationData data, String locale, Path outputDirectory) throws IOException;
    
    /**
     * Get the output format this writer handles
     */
    OutputFormat getOutputFormat();
}