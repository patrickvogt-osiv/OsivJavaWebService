package ch.osiv.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

import com.aspose.words.Document;
import com.aspose.words.NodeType;
import com.aspose.words.SaveFormat;

/**
 * FileHelper class
 */
public class FileHelper {

    /**
     * Validates if the given path exists
     *
     * @param path Path to check
     * @throws FileNotFoundException
     */
    public static void assertExists(Path path) throws FileNotFoundException {
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Path '" + path.toString() + "' does not exist");
        }
    }

    /**
     * Validates if the given path exists
     *
     * @param path Path to check
     * @throws FileNotFoundException
     */
    public static void assertExists(String path) throws FileNotFoundException {
        assertExists(Path.of(path));
    }

    /**
     * Validates if the given path is a directory
     *
     * @param path Path to check
     * @throws FileNotFoundException
     * @throws NotDirectoryException
     */
    public static void assertIsDirectory(Path path) throws FileNotFoundException, NotDirectoryException {
        assertExists(path);
        if (!Files.isDirectory(path)) {
            throw new NotDirectoryException("Path '" + path.toString() + "' is not a directory");
        }
    }

    /**
     * Validates if the given path is a file
     *
     * @param path Path to check
     * @throws IOException
     */
    public static void assertIsFile(Path path) throws IOException {
        assertExists(path);
        if (!Files.isRegularFile(path)) {
            throw new IOException("Path '" + path.toString() + "' is not a file");
        }
    }

    /**
     * Copies a given file to the destination directory or file
     *
     * @param fileToCopy File to copy
     * @param target     Target directory (must exist) or file (can be different then 'fileToCopy')
     * @return Target path
     * @throws IOException
     */
    public static Path copyFileTo(Path fileToCopy,
                                  Path target) throws IOException {

        Path targetFile = null;

        assertIsFile(fileToCopy);

        // when the target is an existing directory, the same file-name will be used
        // otherwise take target as the full result file-name
        if (Files.isDirectory(target)) {
            String fileName = getBaseName(fileToCopy);
            targetFile = target.resolve(fileName);
        } else {
            targetFile = target;
        }

        return Files.copy(fileToCopy, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Create a new file. Will remove the file first when it already exists
     *
     * @param filePath The file to create
     * @return The newly created file
     * @throws IOException
     */
    public static File createNewFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists())
            file.delete();

        file.createNewFile();

        return file;
    }

    /**
     * Returns the last-modified time of a file or folder
     *
     * @param path The file or folder
     * @return Instant of last-modified
     * @throws IOException
     */
    public static Instant getLastModifiedTime(Path path) throws IOException {

        assertExists(path);

        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);

        return attributes.lastModifiedTime().toInstant();
    }

    /**
     * Retrieves all the file with a given extension from a folder and its sub folders.
     * The method receives the root folder name (full pathname), the extension string
     * It then adds all the file contained in the folder to a <code>List</code> of
     * <a href="#{@link}">{@link Path}
     * then recursively does the same for every sub folder contained in the root folder
     *
     * @param folderName <code>String</code> (full pathname)
     * @param extension  <code>String</code>
     * @return fileArray List <a href="#{@link}">{@link Path}
     * @throws IOException if error during the folder parsing
     */
    public static List<Path> retrieveFilesListFromFolder(String folderName,
                                                         String extension) throws IOException {

        return retrieveFilesListFromFolder(folderName, extension, null);
    }

    /**
     * Retrieves all the file with a given extension from a folder and its sub folders.
     * The method receives the root folder name (full pathname), the extension string
     * It then adds all the file contained in the folder to a <code>List</code> of
     * <a href="#{@link}">{@link Path}
     * then recursively does the same for every sub folder contained in the root folder
     *
     * @param folderName     <code>String</code> (full pathname)
     * @param extension      <code>String</code>
     * @param sortComperator An optional argument to sort the list with this Comperator<Path>
     * @return fileArray List <a href="#{@link}">{@link Path}
     * @throws IOException if error during the folder parsing
     */
    public static List<Path> retrieveFilesListFromFolder(String folderName,
                                                         String extension,
                                                         Comparator<Path> sortComperator) throws IOException {

        Path folderPath = Paths.get(folderName);

        assertIsDirectory(folderPath);

        try (Stream<Path> walk = Files.walk(folderPath)) {

            List<Path> result = walk.filter(file -> file.toString().endsWith(extension))
                                    .collect(Collectors.toList());

            if (sortComperator != null) {
                result.sort(sortComperator);
            }

            return result;
        }
    }

    /**
     * Trim any blank lines for the end of a doc/docx document
     * 
     * @param document <code>com.aspose.word.Document</code>
     * @throws Exception
     */
    public static void trimBlankLinesFromDocument(Document document) throws Exception {
        while (document.getLastSection().getBody().getLastParagraph().toString(SaveFormat.TEXT)
                       .trim().equals("")) {
            if (document.getLastSection().getBody().getLastParagraph()
                        .getPreviousSibling() != null &&
                document.getLastSection().getBody().getLastParagraph().getPreviousSibling()
                        .getNodeType() != NodeType.PARAGRAPH)
                break;
            document.getLastSection().getBody().getLastParagraph().remove();

            // If the current section becomes empty, we should remove it.
            if (!document.getLastSection().getBody().hasChildNodes())
                document.getLastSection().remove();

            // We should exit the loop if the document becomes empty.
            if (!document.hasChildNodes())
                break;
        }
    }

    /**
     * Gets the <a href="#{@link}">{@link File} creation date as <code>long</code>
     * <code> EpochMilli </code> (the number of milliseconds from 1970-01-01T00:00:00Z.)
     * 
     * @param path file <a href="#{@link}">{@link File}
     * @return date of create
     */
    public static long getFileCreationDate(Path path) {
        try {
            BasicFileAttributes fileAttributes = Files.readAttributes(path,
                                                                      BasicFileAttributes.class);
            return fileAttributes.creationTime().toInstant().toEpochMilli();
        } catch (IOException e) {
            throw new RuntimeException(path.toString(), e);
        }
    }

    /**
     * Simple method for changing file extension using regex
     * The method replaces the last occurrence of the extension string
     * with the new extension in the the file name or
     * just adds the extension if the file has none
     *
     * @param fileName
     * @param newExtension
     * @return string The new file name
     */
    public static String replaceFileExtensionWith(String fileName,
                                                  String newExtension) {

        int i = fileName.lastIndexOf('.');
        if (i >= 0) {
            return fileName.substring(0, i + 1) + newExtension;
        }

        return fileName + "." + newExtension;
    }

    /**
     * Delete a folder and all its children
     *
     * @param dir folder The directory to delete
     * @return True when the directory is deleted
     */
    public static boolean deleteWholeDirectoryIfExists(File dir) {

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory())
                        deleteWholeDirectoryIfExists(file);
                    else
                        file.delete();
                }
            }
            dir.delete();
        }

        return (dir.exists()) ? false
                              : true;
    }

    /**
     * Ensures the directory exists. If not, it will be created
     *
     * @param dir The directory (cannot be a file)
     * @return True when the directory exists or is created
     */
    public static boolean ensureDirectoryExist(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Retuns a file or path its full dir path
     *
     * @param path The file or directory to get its dirname
     * @return dirname
     */
    public static String getDirPath(Path path) {
        return path.getParent().toAbsolutePath().toString();
    }

    /**
     * Retuns a file or path its full dir path
     *
     * @param path The file or directory to get its dirname
     * @return dirname
     */
    public static String getDirPath(String path) {
        return FileHelper.getDirPath(Path.of(path));
    }

    /**
     * Retuns a file or directory its name (with extension)
     *
     * @param file The file as Path
     * @return Filename
     */
    public static String getBaseName(Path file) {
        return FileHelper.getBaseName(file, true);
    }

    /**
     * Retuns a file or directory its name (with or without extension)
     *
     * @param file          The file as Path
     * @param withExtension When true, it will include the extension
     * @return Filename
     */
    public static String getBaseName(Path file,
                                     boolean withExtension) {

        Path fileBaseName = file.getFileName();
        int  lastDotIndex = fileBaseName.toString().lastIndexOf(".");

        if (withExtension)
            return fileBaseName.toString();

        return fileBaseName.toString().substring(0, lastDotIndex);
    }

    /**
     * Retuns a file or directory its name (with extension)
     *
     * @param fileName The file name
     * @return Filename
     */
    public static String getBaseName(String fileName) {
        return FileHelper.getBaseName(Path.of(fileName), true);
    }

    /**
     * Returns the file extension
     *
     * @param file The path of the file
     * @return string The file extension
     */
    public static String getFileExtension(Path file) {
        return FileHelper.getFileExtension(file.toString(), false);
    }

    /**
     * Returns the file extension
     *
     * @param fileName The name of the file (full path works too)
     * @return string The file extension
     */
    public static String getFileExtension(String fileName) {
        return FileHelper.getFileExtension(fileName, false);
    }

    /**
     * Simple method for returning the extension of a file
     * If the file has no extension it returns an empty string
     * NOTE: this method will not work for files with complex extensions
     * like archive.tar.gz
     *
     * @param fileName    The name of the file (full path works too)
     * @param toLowerCase True, will convert the extension to lowercase
     * @return string The file extension
     */
    public static String getFileExtension(String fileName,
                                          boolean toLowerCase) {
        String extension = FilenameUtils.getExtension(fileName);
        return (toLowerCase) ? extension.toLowerCase()
                             : extension;
    }

    /**
     * Get the Files from a directory
     *
     * @param dir The directory of which to retrieve the files
     * @return Array of files, when there are not files, an empty array is returned
     * @throws Exception
     */
    public static File[] getFiles(String dir) {
        return FileHelper.getFiles(dir, null);
    }

    /**
     * Get the Files from a directory
     *
     * @param dir      The directory of which to retrieve the files
     * @param wildcard Wildcard as an optional parameter (case insensitive)
     * @return Array of files, when there are not files, an empty array is returned
     * @throws Exception
     */
    public static File[] getFiles(String dir,
                                  String wildcard) {

        File[] files  = null;
        File   folder = new File(dir);

        // first convert the wildcard to a regex
        // @formatter:off
        String  regex   = (wildcard == null || wildcard.isBlank()) ? ""
                                                                   : wildcard.replace(".", "\\.")
                                                                             .replace("?", ".")
                                                                             .replace("*", ".*");
        // @formatter:on
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        // Define a filter using the wildcard parameter
        FilenameFilter filter = (File path,
                                 String name) -> {
            // skip directories
            if (Files.isDirectory(Path.of(path.toString(), name)))
                return false;
            // no wildcard, so include all files
            if (regex == null || regex.isBlank())
                return true;
            // return 'true' if it matches the wildcard
            return pattern.matcher(name).matches();
        };

        if (folder.isDirectory())
            files = folder.listFiles(filter);

        if (files == null)
            files = new File[0];

        return files;
    }

    /**
     * fileExists
     *
     * @param sourceFile File to check
     * @return true/false
     * @throws Exception
     */
    public static boolean fileExist(String sourceFile) throws Exception {
        File file = new File(sourceFile);
        return file.exists();
    }

    /**
     * Moves a given file to the destination directory or file
     * Can also be used for renaming
     *
     * @param fileToMove File to move
     * @param target     Target directory (must exist) or file (for renaming)
     * @return Target path
     * @throws IOException
     */
    public static Path moveFileTo(Path fileToMove,
                                  Path target) throws IOException {

        Path targetFile = null;

        assertIsFile(fileToMove);

        // when the target is an existing directory, the same file-name will be used
        // otherwise take target as the full result file-name
        if (Files.isDirectory(target)) {
            String fileName = getBaseName(fileToMove);
            targetFile = target.resolve(fileName);
        } else {
            targetFile = target;
        }

        return Files.move(fileToMove, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }
}
