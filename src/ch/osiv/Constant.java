package ch.osiv;

import java.text.SimpleDateFormat;

@SuppressWarnings("javadoc")
public class Constant {

    public final static String RESPONSES               = "responses";
    public final static String BODY                    = "body";
    public final static String HEAD                    = "head";
    public final static String HTTP_EQUIV              = "http-equiv";
    public final static String CONTENT                 = "content";
    public final static String CONTEXT                 = "context";
    public final static String PAGE_NUMBER             = "pageNumber";
    public final static String ID                      = "id";
    public final static String PAGE_                   = "page_";
    public final static String URI                     = "uri";
    public final static String PAGES                   = "pages";
    public final static String TEXT                    = "text";
    public final static String FULL_TEXT_ANNOTATION    = "fullTextAnnotation";
    public final static String TITLE                   = "title";
    public final static String BBOX                    = "bbox";
    public final static String SCAN_RES                = "scan_res";
    public final static String WIDTH                   = "width";
    public final static String HEIGHT                  = "height";
    public final static String RESOLUTION              = "resolution";
    public final static String PROPERTY                = "property";
    public final static String BLOCKS                  = "blocks";
    public final static String CLASSES                 = "class";
    public final static String OCR_CAREA               = "ocr_carea";
    public final static String OCRX_WORD               = "ocrx_word";
    public final static String PARAGRAPHS              = "paragraphs";
    public final static String BOUNDING_BOX            = "boundingBox";
    public final static String WORDS                   = "words";
    public final static String SYMBOLS                 = "symbols";
    public final static String X                       = "x";
    public final static String Y                       = "y";
    public final static String NORMALIZED_VERTICES     = "normalizedVertices";
    public final static String LANGUAGE_CODE           = "languageCode";
    public final static String EN                      = "en";
    public final static String DETECTED_lANGUAGE       = "detectedLanguage";
    public final static String CONFIDENCE              = "confidence";
    public final static String JSON                    = ".json";
    public final static String ENG                     = "eng";
    public final static String NAME                    = "name";
    public final static String COMPRESSIONTYPENAME     = "CompressionTypeName";
    public final static String VALUE                   = "value";
    public final static String ERROR_DECODING          = "Error deconding the URL";
    public final static String ERROR_MERGE_TIFF_CREATE = "Error merging Tiff files, could not create result tiff from tiff files list: ";
    public final static String ERROR_MERGE_TIFF_LIST   = "Error merging Tiff files, could not parse the source folder: ";
    public final static String DIRECTORY_NO_FILE       = "Folder does not contain any files of type: ";
    public final static String SOURCE_NOT_FOUND        = "Source file '%s' not found";

    public final static SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
}
