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
 * Cleans generated translation files
 */
@Mojo(name = "clean")
public class CleanMojo extends AbstractFluentI18nMojo {
    
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
    
    private boolean shouldDelete(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.endsWith(".po") || 
               fileName.endsWith(".pot") || 
               fileName.startsWith("messages_");
    }
}