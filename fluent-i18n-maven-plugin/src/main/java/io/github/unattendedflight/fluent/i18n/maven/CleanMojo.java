package io.github.unattendedflight.fluent.i18n.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Mojo that cleans up Fluent i18n-related files generated during the build process.
 * This includes removal of `.po`, `.pot`, `.json`, `.properties`, and `.bin` files,
 * as well as any empty directories that may be left over.
 *
 * This class extends the {@code AbstractFluentI18nMojo} class and is intended
 * to be used as part of a Maven plugin.
 *
 * Features:
 * - Deletes `.po` and `.pot` files from the configured PO directory.
 * - Deletes output localization files (e.g., `.json`, `.properties`, `.bin`)
 *   from the configured output directory for each supported locale.
 * - Deletes empty directories after cleaning files.
 *
 * This Mojo logs the total number of files cleaned and detailed information for each
 * deleted file or directory. If a file cannot be deleted, warnings are logged but do not halt execution.
 *
 * If the relevant directories do not exist or no files are found to delete, the cleaning process may
 * complete without any deletions or errors.
 *
 * Throws:
 * - {@code MojoExecutionException} if an I/O error occurs while processing files or directories.
 * - {@code MojoFailureException} for unexpected execution failures.
 */
@Mojo(name = "clean")
public class CleanMojo extends AbstractFluentI18nMojo {
    
    /**
     * Executes the Maven goal for cleaning fluent i18n files.
     * <p>
     * This method performs the following operations:
     * - Checks whether the goal execution should be skipped using {@code checkSkip()}.
     * - Logs the beginning of the cleaning process.
     * - Deletes PO files located in the specified directory, if they exist.
     * - Deletes output files (e.g., JSON, properties, and binary files) associated with supported locales in the designated output directory.
     * - Logs the total number of files that have been cleaned after completion.
     * <p>
     * If any part of the cleaning process fails, an exception is thrown to indicate the issue.
     *
     * @throws MojoExecutionException if an error occurs during the execution
     * @throws MojoFailureException if the execution fails due to an invalid configuration or other issues
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        checkSkip();
        
        getLog().info("Cleaning fluent i18n files...");
        
        int filesDeleted = 0;
        
        // Clean PO files
        if (Files.exists(poDirectory.toPath())) {
            filesDeleted += cleanDirectory(poDirectory.toPath(), "PO");
        }
        
        // Clean output files
        if (Files.exists(outputDirectory.toPath())) {
            filesDeleted += cleanOutputFiles(outputDirectory.toPath());
        }
        
        getLog().info("Cleaned " + filesDeleted + " fluent i18n files");
    }
    
    /**
     * Cleans the specified directory by traversing its file tree and deleting files and directories
     * based on certain conditions. Files are deleted if they meet the criteria specified in the
     * {@code shouldDelete} method. Empty subdirectories are also deleted except for the root directory.
     *
     * @param directory the root directory to clean
     * @param type a description of the type of files being cleaned (used for logging purposes)
     * @return the number of files successfully deleted
     * @throws MojoExecutionException if an error occurs during the cleanup process
     */
    private int cleanDirectory(Path directory, String type) throws MojoExecutionException {
        try {
            int[] count = {0};
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (shouldDelete(file)) {
                        Files.delete(file);
                        count[0]++;
                        getLog().debug("Deleted " + type + " file: " + file);
                    }
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    // Delete empty directories
                    if (Files.list(dir).findAny().isEmpty() && !dir.equals(directory)) {
                        Files.delete(dir);
                        getLog().debug("Deleted empty directory: " + dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return count[0];
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to clean " + type + " directory: " + directory, e);
        }
    }
    
    /**
     * Cleans up output files for all supported locales in the specified output directory.
     * This method deletes JSON, properties, and binary files associated with each locale
     * and keeps track of the total number of files successfully deleted.
     *
     * @param outputDir the path to the directory where the output files are located
     * @return the total number of files successfully deleted
     * @throws MojoExecutionException if an error occurs during the cleanup process
     */
    private int cleanOutputFiles(Path outputDir) throws MojoExecutionException {
        int count = 0;
        
        for (String locale : getSupportedLocalesSet()) {
            // Delete JSON files
            Path jsonFile = outputDir.resolve("messages_" + locale + ".json");
            if (Files.exists(jsonFile)) {
                try {
                    Files.delete(jsonFile);
                    count++;
                    getLog().debug("Deleted JSON file: " + jsonFile);
                } catch (IOException e) {
                    getLog().warn("Failed to delete " + jsonFile + ": " + e.getMessage());
                }
            }
            
            // Delete properties files
            Path propsFile = outputDir.resolve("messages_" + locale + ".properties");
            if (Files.exists(propsFile)) {
                try {
                    Files.delete(propsFile);
                    count++;
                    getLog().debug("Deleted properties file: " + propsFile);
                } catch (IOException e) {
                    getLog().warn("Failed to delete " + propsFile + ": " + e.getMessage());
                }
            }
            
            // Delete binary files
            Path binFile = outputDir.resolve("messages_" + locale + ".bin");
            if (Files.exists(binFile)) {
                try {
                    Files.delete(binFile);
                    count++;
                    getLog().debug("Deleted binary file: " + binFile);
                } catch (IOException e) {
                    getLog().warn("Failed to delete " + binFile + ": " + e.getMessage());
                }
            }
        }
        
        return count;
    }
    
    /**
     * Determines if the specified file should be deleted based on its name.
     *
     * @param file the path of the file to evaluate
     * @return true if the file should be deleted; false otherwise
     */
    private boolean shouldDelete(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.endsWith(".po") || 
               fileName.endsWith(".pot") || 
               fileName.startsWith("messages_");
    }
}