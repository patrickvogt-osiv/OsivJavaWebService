package ch.osiv.image;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import ch.osiv.helper.FileType;
import ch.osiv.helper.JsonSerializable;
import ch.osiv.helper.XMLHelper;

/**
 * TiffImageMetadata class
 *
 * @author Arno van der Ende
 */
public class TiffImageMetadata
    extends JsonSerializable {

    private Map<Integer, List<JsonNode>> pages;

    // constants
    public static final String COLORSPACETYPE      = "ColorSpaceType";
    public static final String COMPRESSIONTYPENAME = "CompressionTypeName";
    public static final String NODE_NAME           = "name";
    public static final String NODE_VALUE          = "value";

    /**
     * Default constructor
     */
    private TiffImageMetadata() {
        pages = new LinkedHashMap<Integer, List<JsonNode>>();
    }

    /**
     * Constructor to read a whole TIFF document
     *
     * @param tiffFileName The TIFF ile
     * @throws IOException
     */
    public TiffImageMetadata(File tiffFileName) throws IOException {
        this();
        readMetadata(tiffFileName);
    }

    /**
     * Constructor to read metadata of one single TIFF page
     *
     * @param pageMetadata The metadata object (imageReader.getImageMetadata(<pagenr>)
     * @para pageNr The page number which is only needed to set the 'Key' in the Map object
     * @throws IOException
     */
    public TiffImageMetadata(IIOMetadata pageMetadata,
                             int pageNr) throws IOException {

        this();
        List<JsonNode> formatList = getPageMetadataFormatList(pageMetadata);
        // add to Map where page-nr is the 'Key'
        pages.put(pageNr, formatList);
    }

    /**
     * Constructor to read metadata of a requested pageNr
     *
     * @param imageReader ImageReader - must already have the input-stream
     * @para pageNr The page number to get the metadata
     * @throws IOException
     */
    public TiffImageMetadata(ImageReader imageReader,
                             int pageNr) throws IOException {
        this(imageReader.getImageMetadata(pageNr), pageNr);
    }

    /**
     * Gets the 'name' of 'ColorSpaceType' tag out of a given page
     *
     * @param pageNr The page number of where to get the metadata value
     * @return The ColorSpaceType name
     */
    public String getColorSpaceType(int pageNr) {

        // get the list of JsonNodes
        List<JsonNode> list = this.pages.get(pageNr);

        // try to find the ColorSpaceType in one of the lists
        for (Iterator<JsonNode> iterator = list.iterator(); iterator.hasNext();) {
            JsonNode jsonNode = iterator.next();

            JsonNode targetJsonNode = jsonNode.findValue(COLORSPACETYPE);
            if (targetJsonNode == null) {
                continue; // try the next format list
            }

            if (targetJsonNode.has(NODE_NAME)) {
                return targetJsonNode.path(NODE_NAME).asText();
            }
        }
        return null;
    }

    /**
     * Gets the 'name' of 'ColorSpaceType' tag from all pages
     *
     * @return The ColorSpaceType values in an array
     */
    public List<String> getColorSpaceTypes() {

        List<String> colorSpaceTypes = new ArrayList<>();

        for (int pageNr = 0; pageNr < this.pages.size(); pageNr++) {
            colorSpaceTypes.add(getColorSpaceType(pageNr));
        }

        return colorSpaceTypes;
    }

    /**
     * Gets the 'value' of 'CompressionTypeName' tag out of a given page
     *
     * @param pageNr The page number of where to get the metadata value
     * @return The CompressionTypeName value
     */
    public String getCompressionTypeName(int pageNr) {

        // get the list of JsonNodes
        List<JsonNode> list = this.pages.get(pageNr);

        // try to find the ColorSpaceType in one of the lists
        for (Iterator<JsonNode> iterator = list.iterator(); iterator.hasNext();) {
            JsonNode jsonNode = iterator.next();

            JsonNode targetJsonNode = jsonNode.findValue(COMPRESSIONTYPENAME);
            if (targetJsonNode == null) {
                continue; // try the next format list
            }

            if (targetJsonNode.has(NODE_VALUE)) {
                return targetJsonNode.path(NODE_VALUE).asText();
            }
        }
        return null;
    }

    /**
     * Gets the 'value' of 'CompressionTypeName' tag from all pages
     *
     * @return The CompressionTypeName values in an array
     */
    public List<String> getCompressionTypeNames() {

        List<String> compressionArray = new ArrayList<>();

        for (int pageNr = 0; pageNr < this.pages.size(); pageNr++) {
            compressionArray.add(getCompressionTypeName(pageNr));
        }

        return compressionArray;
    }

    /**
     * Getter for NrPages
     *
     * @return NrPages
     */
    public int getNrPages() {
        return pages.size();
    }

    /**
     * Reads the metadata of a single page. It collects the data for each 'formatName'
     * and puts the data in a List of type JsonNode
     *
     * @param pageMetadata The metadata object
     * @return List of metadata
     * @throws JsonProcessingException
     */
    private List<JsonNode> getPageMetadataFormatList(IIOMetadata pageMetadata) throws JsonProcessingException {

        String         formatNames[] = pageMetadata.getMetadataFormatNames();
        List<JsonNode> formatList    = new ArrayList<JsonNode>();

        for (int i = 0; i < formatNames.length; i++) {

            String          formatName = formatNames[i];
            IIOMetadataNode node       = (IIOMetadataNode) pageMetadata.getAsTree(formatName);
            String          xmlString  = XMLHelper.toXmlString(node);
            JsonNode        jsonNode   = XMLHelper.convertXmlToJson(xmlString);

            formatList.add(jsonNode);
        }

        return formatList;
    }

    /**
     * Getter for pages property
     *
     * @return the pages
     */
    public Map<Integer, List<JsonNode>> getPages() {
        return pages;
    }

    /**
     * Read the metadata of the whole document
     *
     * @param tiffFileName The TIFF ile
     * @throws IOException
     */
    private void readMetadata(File tiffFileName) throws IOException {

        Iterator<ImageReader> tiffReaderIterator = ImageIO.getImageReadersBySuffix(FileType.tiff);

        if (tiffReaderIterator != null && tiffReaderIterator.hasNext()) {
            try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(tiffFileName)) {

                ImageReader imageReader = null;

                try {
                    imageReader = tiffReaderIterator.next();
                    imageReader.setInput(imageInputStream);

                    int numPages = imageReader.getNumImages(true);

                    // read the metadata of each page and saves it in the Map object
                    for (int pageNr = 0; pageNr < numPages; pageNr++) {
                        IIOMetadata    pageMetadata = imageReader.getImageMetadata(pageNr);
                        List<JsonNode> formatList   = getPageMetadataFormatList(pageMetadata);
                        // add to Map where page-nr is the 'Key'
                        pages.put(pageNr, formatList);
                    }
                } finally {
                    if (imageReader != null) {
                        imageReader.dispose();
                    }
                }
            }
        }
    }

}
