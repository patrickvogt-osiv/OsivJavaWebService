package ch.osiv.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.osiv.Constant;

public class HocrToJsonConverterHelper {

    /**
     * generate a Json file with the help of the OCR file.
     * 
     * @param the full path of the OCR file
     * @return the full path of the Json file
     */
    public static String getContentFormatFromHocr(String FullPathName) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder      = factory.newDocumentBuilder();
        Document        document     = builder.parse(new File(FullPathName));
        NodeList        nListHead    = document.getElementsByTagName(Constant.HEAD);
        Node            head         = nListHead.item(0);
        NodeList        nListDivMeta = head.getChildNodes();
        for (int divMeta = 0; divMeta < nListDivMeta.getLength(); divMeta++) {
            Node DivMeta = nListDivMeta.item(divMeta);

            if (DivMeta != null && DivMeta.getNodeType() == Node.ELEMENT_NODE) {
                Element eElementDivPage = (Element) DivMeta;
                if (!eElementDivPage.getAttribute(Constant.HTTP_EQUIV).equals("") &&
                    !eElementDivPage.getAttribute(Constant.CONTENT).equals("")) {
                    return eElementDivPage.getAttribute(Constant.CONTENT);
                }
            }
        }
        return "";

    }

    /**
     * generate a Json file with the help of the OCR file.
     * 
     * @param the full path of the OCR file
     * @return the full path of the Json file
     */
    public static String generateJsonFileFromHocr(String FullPathName) throws ParserConfigurationException, SAXException, IOException {

        String path;
        String fileNameJSON;

        path         = FilenameUtils.getFullPath(FullPathName);
        fileNameJSON = FilenameUtils.getName(FullPathName) + Constant.JSON;

        FileWriter file = new FileWriter(path + fileNameJSON);

        file.write(hocrToJsonConverter(FullPathName).toString());

        file.close();

        return path + fileNameJSON;

    }

    /**
     * generate a Json file with the help of the OCR file.
     * 
     * @param the full path of the OCR file
     * @return JSONOBject that contain all the OCR data
     */
    public static JSONObject hocrToJsonConverter(String Filepath) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder  = factory.newDocumentBuilder();
        Document        document = builder.parse(new File(Filepath));

        JSONArray Responses = new JSONArray();

        JSONObject BodyJson = new JSONObject();

        BodyJson.put(Constant.RESPONSES, Responses);
        NodeList nListBody    = document.getElementsByTagName(Constant.BODY);
        Node     body         = nListBody.item(0);
        NodeList nListDivPage = body.getChildNodes();
        for (int divPageNumber = 0; divPageNumber < nListDivPage.getLength(); divPageNumber++) {
            Node DivPage = nListDivPage.item(divPageNumber);

            if (DivPage != null && DivPage.getNodeType() == Node.ELEMENT_NODE) {
                Element    eElementDivPage = (Element) DivPage;
                JSONObject contextJson     = new JSONObject();

                JSONObject ContextPropertyJson = new JSONObject();

                contextJson.put(Constant.CONTEXT, ContextPropertyJson);
                Responses.put(contextJson);
                ContextPropertyJson.put(Constant.PAGE_NUMBER,
                                        eElementDivPage.getAttribute(Constant.ID)
                                                       .replace(Constant.PAGE_, ""));
                ContextPropertyJson.put(Constant.URI, "");

                JSONObject fullTextAnnotationJson = new JSONObject();

                JSONArray ArrayPages = generatePage(eElementDivPage, DivPage);

                fullTextAnnotationJson.put(Constant.PAGE_, ArrayPages);
                fullTextAnnotationJson.put(Constant.TEXT, "");
                contextJson.put(Constant.FULL_TEXT_ANNOTATION, fullTextAnnotationJson);

            }
        }
        return BodyJson;
    }

    /**
     * generate a JsonArray for 1 page
     * 
     * @param the element of the divPage
     * @param the node for a divPage
     * @return JSONArray that contain all data for a page
     */
    private static JSONArray generatePage(Element eElementDivPage,
                                          Node divPage) {
        JSONArray  ArrayPages   = new JSONArray();
        JSONObject propertyJson = new JSONObject();
        Integer    width        = 0;
        Integer    heigh        = 0;
        Integer    resolution   = 0;

        String   string;
        String[] stringArray = eElementDivPage.getAttribute(Constant.TITLE).split(";");
        for (int i = 0; i < stringArray.length; i++) {
            string = stringArray[i];
            if (string.matches("(.*)" + Constant.BBOX + "(.*)")) {
                string = string.replace(Constant.BBOX, "");

                String[] numberArray = string.trim().split(" ");

                width = Integer.valueOf(numberArray[2]) - Integer.valueOf(numberArray[0]);
                heigh = Integer.valueOf(numberArray[3]) - Integer.valueOf(numberArray[1]);

            }
            if (string.matches("(.*)" + Constant.SCAN_RES + "(.*)")) {
                string = string.replace(Constant.SCAN_RES, "");

                String[] numberArray = string.trim().split(" ");

                resolution = Integer.valueOf(numberArray[0]);
            }
        }

        JSONObject pagePropertyJson = new JSONObject();
        ArrayPages.put(pagePropertyJson);
        pagePropertyJson.put(Constant.WIDTH, width);
        pagePropertyJson.put(Constant.HEIGHT, heigh);
        if (resolution > 0) {
            pagePropertyJson.put(Constant.RESOLUTION, resolution);
        } else {
            pagePropertyJson.put(Constant.RESOLUTION, 72);
        }
        pagePropertyJson.put(Constant.PROPERTY, propertyJson);

        JSONArray ArrayblocksJson = generateBlock(heigh, width, divPage);
        pagePropertyJson.put(Constant.BLOCKS, ArrayblocksJson);

        return ArrayPages;
    }

    /**
     * generate a block-JsonArray for 1 page
     * 
     * @param heigh of the page
     * @param width of the page
     * @param node  that contain all info for a page
     * @return JSONArray that contain all data for a page
     */
    private static JSONArray generateBlock(Integer heigh,
                                           Integer width,
                                           Node divPage) {

        JSONArray ArrayblocksJson   = new JSONArray();
        NodeList  nListocrBlockText = divPage.getChildNodes();
        for (int temp2 = 0; temp2 < nListocrBlockText.getLength(); temp2++) {
            Node ocrBlockText = nListocrBlockText.item(temp2);
            if (ocrBlockText != null && ocrBlockText.getNodeType() == Node.ELEMENT_NODE) {
                Element eElementocrBlockText = (Element) ocrBlockText;

                if (eElementocrBlockText.getAttribute(Constant.CLASSES)
                                        .contentEquals(Constant.OCR_CAREA)) {
                    NodeList nListocrBlockLang = ocrBlockText.getChildNodes();
                    for (int temp3 = 0; temp3 < nListocrBlockLang.getLength(); temp3++) {
                        Node ocrBlockLang = nListocrBlockLang.item(temp3);

                        if (ocrBlockLang != null &&
                            ocrBlockLang.getNodeType() == Node.ELEMENT_NODE) {
                            Element ElementocrBlockLang = (Element) ocrBlockLang;

                            NodeList nListocrBlockLine = ElementocrBlockLang.getChildNodes();
                            for (int temp4 = 0; temp4 < nListocrBlockLine.getLength(); temp4++) {
                                Node ocrBlockLine = nListocrBlockLine.item(temp4);

                                if (ocrBlockLine != null &&
                                    ocrBlockLine.getNodeType() == Node.ELEMENT_NODE) {
                                    Element ElementocrBlockLine = (Element) ocrBlockLine;

                                    NodeList nListocrBlockWord = ElementocrBlockLine.getChildNodes();

                                    for (int temp5 = 0; temp5 < nListocrBlockWord.getLength(); temp5++) {

                                        Node ocrBlockWord = nListocrBlockWord.item(temp5);

                                        if (ocrBlockWord != null &&
                                            ocrBlockWord.getNodeType() == Node.ELEMENT_NODE) {
                                            Element eElementocrBlockWord = (Element) ocrBlockWord;

                                            if (eElementocrBlockWord.getAttribute(Constant.CLASSES)
                                                                    .contentEquals(Constant.OCRX_WORD)) {
                                                JSONArray  Arrayparagraphs = generateParagraphs(eElementocrBlockWord,
                                                                                                heigh,
                                                                                                width);
                                                JSONObject paragraphs      = new JSONObject();
                                                ArrayblocksJson.put(paragraphs);
                                                paragraphs.put(Constant.PARAGRAPHS,
                                                               Arrayparagraphs);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ArrayblocksJson;
    }

    /**
     * generate a JSONArray for 1 paragraph
     * 
     * @param element of block-Word
     * @param heigh   of the page
     * @param width   of the page
     * @return JSONArray that contain all data for a paragraph
     */
    private static JSONArray generateParagraphs(Element eElementocrBlockWord,
                                                Integer heigh,
                                                Integer width) {
        JSONArray  Arrayparagraphs = new JSONArray();
        JSONObject paragrah        = new JSONObject();

        JSONObject boundingBox = generateBoundingBox(eElementocrBlockWord, heigh, width);
        Arrayparagraphs.put(paragrah);
        paragrah.put(Constant.BOUNDING_BOX, boundingBox);

        JSONArray ArrayWords = generateWords(eElementocrBlockWord, heigh, width);

        paragrah.put(Constant.WORDS, ArrayWords);
        return Arrayparagraphs;
    }

    private static JSONArray generateWords(Element eElementocrBlockWord,
                                           Integer heigh,
                                           Integer width) {
        JSONArray  ArrayWords = new JSONArray();
        JSONObject words      = new JSONObject();

        JSONObject boundingBox = generateBoundingBox(eElementocrBlockWord, heigh, width);

        JSONArray ArraySymbols = generateSymbols(eElementocrBlockWord);

        ArrayWords.put(words);
        words.put(Constant.BOUNDING_BOX, boundingBox);
        words.put(Constant.SYMBOLS, ArraySymbols);

        return ArrayWords;
    }

    /**
     * generate a BoundingBox for 1 paragraphe
     * 
     * @param element of block-Word
     * @param heigh   of the page
     * @param width   of the page
     * @return JSONObject that contain all data for a BoundingBox
     */
    private static JSONObject generateBoundingBox(Element eElementocrBlockWord,
                                                  Integer heigh,
                                                  Integer width) {

        JSONObject boundingBox             = new JSONObject();
        JSONArray  ArraynormalizedVertices = new JSONArray();

        String   bbox       = eElementocrBlockWord.getAttribute(Constant.TITLE).split(";")[0];
        String[] coordinate = bbox.replace(Constant.BBOX + " ", "").split(" ");

        float x1 = Float.valueOf(coordinate[0]) / width;
        float x2 = Float.valueOf(coordinate[2]) / width;
        float y1 = Float.valueOf(coordinate[1]) / heigh;
        float y2 = Float.valueOf(coordinate[3]) / heigh;

        JSONObject normalizedVertices12 = new JSONObject();

        normalizedVertices12.put(Constant.X, x1);
        normalizedVertices12.put(Constant.Y, y1);

        JSONObject normalizedVertices22 = new JSONObject();

        normalizedVertices22.put(Constant.X, x2);
        normalizedVertices22.put(Constant.Y, y1);
        JSONObject normalizedVertices32 = new JSONObject();

        normalizedVertices32.put(Constant.X, x1);
        normalizedVertices32.put(Constant.Y, y2);

        JSONObject normalizedVertices42 = new JSONObject();

        normalizedVertices42.put(Constant.X, x2);
        normalizedVertices42.put(Constant.Y, y2);

        ArraynormalizedVertices.put(normalizedVertices12);
        ArraynormalizedVertices.put(normalizedVertices22);
        ArraynormalizedVertices.put(normalizedVertices32);
        ArraynormalizedVertices.put(normalizedVertices42);

        boundingBox.put(Constant.NORMALIZED_VERTICES, ArraynormalizedVertices);

        return boundingBox;

    }

    /**
     * generate a JSONArray for 1 word
     * 
     * @param element of block-Word
     * @return JSONArray that contain all data for a word
     */
    private static JSONArray generateSymbols(Element eElementocrBlockWord) {
        JSONArray ArraySymbols = new JSONArray();

        for (int i = 0; i < eElementocrBlockWord.getTextContent().length(); i++) {

            JSONObject Symbols = new JSONObject();

            JSONObject property = new JSONObject();

            JSONObject languageCode = new JSONObject();

            languageCode.put(Constant.LANGUAGE_CODE, Constant.EN);

            property.put(Constant.DETECTED_lANGUAGE, languageCode);

            Symbols.put(Constant.PROPERTY, property);
            String text = String.valueOf(eElementocrBlockWord.getTextContent().charAt(i));
            Symbols.put(Constant.TEXT, text);
            Symbols.put(Constant.CONFIDENCE, 0.0);
            ArraySymbols.put(Symbols);
        }
        return ArraySymbols;
    }

}
