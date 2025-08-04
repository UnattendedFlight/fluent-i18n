package io.github.unattendedflight.fluent.i18n.compiler;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for writing translation data to various output formats.
 * Implementations of this interface are responsible for handling specific
 * output formats and performing the necessary file operations to persist
 * the translation data in the appropriate format.
 */
public interface OutputWriter {
    
    /**
     * Writes the provided translation data to a file in the specified output directory.
     * The file is written in the format associated with the implementing class.
     *
     * @param data the translation data to be written
     * @param locale the locale identifier (e.g., "en", "fr") for the translation data
     * @param outputDirectory the directory where the output file should be written
     * @return the {@link Path} of the file that was created
     * @throws IOException if an I/O error occurs during file writing
     */
    Path write(TranslationData data, String locale, Path outputDirectory) throws IOException;
    
    /**
     * Retrieves the output format handled by the implementing class.
     *
     * @return the output format supported by the implementation, represented as an OutputFormat enum value.
     */
    OutputFormat getOutputFormat();
}