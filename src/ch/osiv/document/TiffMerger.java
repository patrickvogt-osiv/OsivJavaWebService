package ch.osiv.document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.imaging.IMultipageImage;
import com.aspose.imaging.Image;
import com.aspose.imaging.fileformats.tiff.TiffImage;
import com.aspose.imaging.imageoptions.TiffOptions;

import ch.osiv.Constant;
import ch.osiv.helper.FileHelper;
import ch.osiv.helper.FileType;
import ch.osiv.helper.JsonConversionResponse;

/**
 * TiffMerger class
 *
 * @author Arno van der Ende
 */
public class TiffMerger {

    private static final Logger LOGGER = LoggerFactory.getLogger(TiffFileHandler.class);

    /**
     * Merge all Tiff files provided in a List of filenames
     *
     * @param tiffFiles          A list of tiff files which will be merged as the order in the List
     * @param targetFolder       The folder where to save the merged file
     * @param tiffExpectedFormat The tiff format (color)
     * @return Full path of the merged file
     */
    public Path mergeTiffFiles(List<Path> tiffFiles,
                               Path targetFolder,
                               int tiffExpectedFormat) {

        Date dateStart = new Date();
        LOGGER.info("{} Tiff merging started for {} files",
                    Constant.DATETIME_FORMAT.format(dateStart),
                    tiffFiles.size());

        if (tiffFiles.size() == 0) {
            throw new IllegalArgumentException("List of tiff files to merge is empty!");
        }

        List<Image> images = toImageList(tiffFiles);

        if (images.size() == 0) {
            throw new IllegalArgumentException("List of tiff images to merge is empty!");
        }

        Path mergedTiffPath = targetFolder.resolve(UUID.randomUUID().toString() + "_merged.tiff");

        try (Image resultImage = Image.create(images.toArray(new Image[0]))) {
            TiffOptions tiffOptions = new TiffOptions(tiffExpectedFormat);
            try {
                resultImage.save(mergedTiffPath.toString(), tiffOptions);
            } finally {
                resultImage.close();
            }
        } finally {
            images.forEach(image -> image.close());
            images.clear();
            Date dateFinish = new Date();
            LOGGER.info("{} Tiff merging completed in {} milliseconds",
                        Constant.DATETIME_FORMAT.format(dateFinish),
                        (dateFinish.getTime() - dateStart.getTime()));
        }

        return mergedTiffPath;
    }

    /**
     * Merge all Tiff files from a folder into one single tiff image
     *
     * @param sourceFolder       Folder path or String of comma separated file names
     * @param resultFileName     Resulting file name (default will be "mergedTiff.tiff")
     * @param tiffExpectedFormat The expected tiff format
     * @return JsonConversionResponse JSON response object containing the properties of the
     *         resulting Tiff file
     * @throws Exception
     */
    public JsonConversionResponse mergeTiffFiles(String sourceFolder,
                                                 String resultFileName,
                                                 int tiffExpectedFormat) throws Exception {

        Date dateStart = new Date();
        LOGGER.info("{} Tiff merging started for: {}",
                    Constant.DATETIME_FORMAT.format(dateStart),
                    sourceFolder);

        List<Image>            images   = new ArrayList<Image>();
        JsonConversionResponse response = new JsonConversionResponse();

        try {
            sourceFolder = URLDecoder.decode(sourceFolder, "UTF-8");
            if (resultFileName != null) {
                resultFileName = URLDecoder.decode(resultFileName, "UTF-8");
            }
        } catch (UnsupportedEncodingException e1) {
            response.setErrorStatus(true);
            response.setErrorMessage(Constant.ERROR_DECODING + e1.getMessage());
            LOGGER.error(Constant.ERROR_DECODING + e1.getMessage());
            return response;
        }

        //Initialize resultFileName if empty
        Path resultFilePath;
        if (resultFileName == null || resultFileName.isEmpty()) {
            resultFilePath = Paths.get(UUID.randomUUID().toString() + "_mergedTiff.tiff");
        } else {
            resultFilePath = Paths.get(resultFileName);
        }

        // Retrieve all tiff files (including subfolders) inside a String list
        try {
            if (sourceFolder.contains(",")) {
                images = toImageList(sourceFolder);
            } else if (Files.isDirectory(Paths.get(sourceFolder))) {
                Files.deleteIfExists(resultFilePath);
                List<Path> fileList = FileHelper.retrieveFilesListFromFolder(sourceFolder,
                                                                             FileType.tiff);
                images = toImageList(fileList);
            }

            if (images.size() == 0) {
                throw new IllegalArgumentException(Constant.DIRECTORY_NO_FILE + FileType.tiff);
            }

            try (Image resultImage = Image.create(images.toArray(new Image[0]))) {
                TiffOptions tiffOptions = new TiffOptions(tiffExpectedFormat);
                try {
                    resultImage.save(resultFilePath.toString(), tiffOptions);
                    //Create a new TiffImage object to get the PageCount (Aspose doesn't allow casting from Image to Tiff during create
                    TiffImage resultTiff = (TiffImage) Image.load(resultFilePath.toString());
                    try {
                        response.setSourceFile(sourceFolder);
                        response.setDestFile(resultFilePath.toAbsolutePath().toString());
                        response.setErrorStatus(false);
                        response.setPageCount(resultTiff.getPageCount());
                        return response;
                    } finally {
                        resultTiff.close();
                    }
                } finally {
                    resultImage.close();
                }
            } catch (Exception e) {
                response.setErrorStatus(true);
                response.setErrorMessage(Constant.ERROR_MERGE_TIFF_CREATE + e.getMessage());
                LOGGER.error(Constant.ERROR_MERGE_TIFF_CREATE + e.getMessage());
                return response;
            }
        } catch (IOException e) {
            response.setErrorStatus(true);
            response.setErrorMessage(Constant.ERROR_MERGE_TIFF_LIST + e.getMessage());
            LOGGER.error(Constant.ERROR_MERGE_TIFF_LIST + e.getMessage());
            return response;
        } catch (InvalidPathException e) {
            response.setErrorStatus(true);
            response.setErrorMessage(String.format(Constant.SOURCE_NOT_FOUND, sourceFolder) + ": " +
                                     e.getMessage());
            LOGGER.error(String.format(Constant.SOURCE_NOT_FOUND, sourceFolder) + ": " +
                         e.getMessage());
            return response;
        } finally {
            images.forEach(image -> image.close());
            images.clear();
            Date dateFinish = new Date();
            LOGGER.info("{} Tiff merging completed in {} milliseconds",
                        Constant.DATETIME_FORMAT.format(dateFinish),
                        (dateFinish.getTime() - dateStart.getTime()));
        }
    }

    /**
     * Creates a List of <a href="#{@link}">{@link Image} using a
     * <a href="#{@link}">{@link String} of file names
     *
     * @param pathList <a href="#{@link}">{@link String} String of comma separated file names
     * @return List of <a href="#{@link}">{@link Image}
     */
    private List<Image> toImageList(String pathList) {

        List<String> fileList = Arrays.asList(pathList.split(","));
        List<Image>  images   = new ArrayList<Image>();

        LOGGER.info("Found: {} files", fileList.size());

        fileList.forEach(path -> {
            try {
                LOGGER.info(path.toString());
                Image tiffImage = Image.load(path);
                if (tiffImage instanceof IMultipageImage) {
                    Collections.addAll(images, ((IMultipageImage) tiffImage).getPages());
                } else {
                    images.add(tiffImage);
                }
            } catch (Exception e) {
                LOGGER.error("Skip tiff file '" + path.toString() + "', because: " +
                             e.getMessage());
                return;
            }
        });
        return images;
    }

    /**
     * Creates a List of <a href="#{@link}">{@link Image} using a
     * <a href="#{@link}">{@link Path} list of file names
     *
     * @param pathList <a href="#{@link}">{@link Path} List of file names
     * @return List of <a href="#{@link}">{@link Image}
     */
    private List<Image> toImageList(List<Path> pathList) {

        List<Image> images = new ArrayList<Image>();

        LOGGER.info("Found: {} files", pathList.size());

        pathList.forEach(path -> {
            try {
                LOGGER.info(path.toString());
                Image tiffImage = Image.load(path.toString());
                if (tiffImage instanceof IMultipageImage) {
                    Collections.addAll(images, ((IMultipageImage) tiffImage).getPages());
                } else {
                    images.add(tiffImage);
                }
            } catch (Exception e) {
                LOGGER.error("Skip tiff file '" + path.toString() + "', because: " +
                             e.getMessage());
                return;
            }
        });
        return images;
    }

}
