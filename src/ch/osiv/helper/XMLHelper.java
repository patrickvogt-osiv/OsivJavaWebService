package ch.osiv.helper;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XMLHelper {

    /**
     * Convert the given XML to a JsonNode
     *
     * @param xmlString The XML string to convert
     * @return JsonNode
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    public static JsonNode convertXmlToJson(String xmlString) throws JsonMappingException, JsonProcessingException {

        XmlMapper xmlMapper = new XmlMapper();

        // remove surrounding double quotes (if present)
        if (xmlString != null && xmlString.length() >= 2 &&
            xmlString.startsWith("\"") &&
            xmlString.endsWith("\"")) {

            xmlString = xmlString.substring(1, xmlString.length() - 1);
        }

        // remove casted quotes (if present)
        if (xmlString.contains("\\\"")) {
            xmlString = xmlString.replace("\\\"", "\"");
        }

        // unescape HMLTL entities (like &lt;)
        xmlString = StringEscapeUtils.unescapeHtml4(xmlString);

        // convert XML to JsonNode
        JsonNode jsonNode = xmlMapper.readTree(xmlString);
        return jsonNode;
    }

    /**
     * Get a DocumentBuilder. NOTE: 'setNamespaceAware' is disabled
     *
     * @return DocumentBuilder object
     * @throws ParserConfigurationException
     */
    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);

        return factory.newDocumentBuilder();
    }

    /**
     * Get a Node object for a given xpath expression
     *
     * @return Node
     * @throws XPathExpressionException
     */
    public static Node getNode(Node baseNode,
                               String xmlPath) throws XPathExpressionException {

        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath        xPath        = xpathfactory.newXPath();

        XPathExpression expression = xPath.compile(xmlPath);
        return (Node) expression.evaluate(baseNode, XPathConstants.NODE);

    }

    /**
     * Get the node value of the given element name
     *
     * @param document The xml document
     * @param xmlPath  The XPath to get the value from
     * @return The node value as Boolean
     * @throws XPathExpressionException
     */
    public static Boolean getNodeBooleanValue(Document document,
                                              String xmlPath) throws XPathExpressionException {

        XPath xpath = getXPath();

        return (Boolean) xpath.compile(xmlPath).evaluate(document, XPathConstants.BOOLEAN);
    }

    /**
     * Get the node value of the given element name
     *
     * @param xmlBody The xml body as String
     * @param xmlPath The XPath to get the value from
     * @return The node value as Boolean
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
    public static Boolean getNodeBooleanValue(String xmlBody,
                                              String xmlPath) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        if (xmlBody == null || xmlBody.isBlank()) {
            return null;
        }

        DocumentBuilder builder  = getDocumentBuilder();
        Document        document = builder.parse(new InputSource(new StringReader(xmlBody)));

        return XMLHelper.getNodeBooleanValue(document, xmlPath);
    }

    /**
     * Get the node value of the given element name
     *
     * @param document The xml document object
     * @param xmlPath  The xpath expression
     * @return NodeList
     * @throws XPathExpressionException
     */
    public static NodeList getNodeList(Document document,
                                       String xmlPath) throws XPathExpressionException {

        XPath xpath = getXPath();

        return (NodeList) xpath.compile(xmlPath).evaluate(document, XPathConstants.NODESET);
    }

    /**
     * Get the node value of the given element name
     *
     * @param xmlBody The xml body as String
     * @param xmlPath The xpath expression
     * @return NodeList
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
    public static NodeList getNodeList(String xmlBody,
                                       String xmlPath) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        if (xmlBody == null || xmlBody.isBlank()) {
            return null;
        }

        DocumentBuilder builder  = getDocumentBuilder();
        Document        document = builder.parse(new InputSource(new StringReader(xmlBody)));

        return XMLHelper.getNodeList(document, xmlPath);
    }

    /**
     * Get the node value of the given element name
     *
     * @param document The xml document
     * @param xmlPath  The XPath to get the value from
     * @return The node value as Double
     * @throws XPathExpressionException
     */
    public static Double getNodeNumberValue(Document document,
                                            String xmlPath) throws XPathExpressionException {

        XPath xpath = getXPath();

        return (Double) xpath.compile(xmlPath).evaluate(document, XPathConstants.NUMBER);
    }

    /**
     * Get the node value of the given element name
     *
     * @param xmlBody The xml body as String
     * @param xmlPath The XPath to get the value from
     * @return The node value as Double
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
    public static Double getNodeNumberValue(String xmlBody,
                                            String xmlPath) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        if (xmlBody == null || xmlBody.isBlank()) {
            return null;
        }

        DocumentBuilder builder  = getDocumentBuilder();
        Document        document = builder.parse(new InputSource(new StringReader(xmlBody)));

        return XMLHelper.getNodeNumberValue(document, xmlPath);
    }

    /**
     * Get the node value of the given element name
     *
     * @param document The xml document
     * @param xmlPath  The XPath to get the value from
     * @return The node value as String
     * @throws XPathExpressionException
     */
    public static String getNodeStringValue(Document document,
                                            String xmlPath) throws XPathExpressionException {

        XPath xpath = getXPath();

        return (String) xpath.compile(xmlPath).evaluate(document, XPathConstants.STRING);
    }

    /**
     * Get the node value of the given element name
     *
     * @param xmlBody The xml body as String
     * @param xmlPath The XPath to get the value from
     * @return The text node value
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
    public static String getNodeStringValue(String xmlBody,
                                            String xmlPath) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        if (xmlBody == null || xmlBody.isBlank()) {
            return null;
        }

        DocumentBuilder builder  = getDocumentBuilder();
        Document        document = builder.parse(new InputSource(new StringReader(xmlBody)));

        return XMLHelper.getNodeStringValue(document, xmlPath);
    }

    /**
     * Get an XPath object
     *
     * @return XPath object
     */
    private static XPath getXPath() {

        XPathFactory xpathfactory = XPathFactory.newInstance();

        return xpathfactory.newXPath();
    }

    /**
     * Checks if the xmlPath exists in the xml body
     *
     * @param document The xml document
     * @param xmlPath  The XPath to get the check if it exists
     * @return true/false
     * @throws Exception
     */
    public static boolean hasNode(Document document,
                                  String xmlPath) throws Exception {

        NodeList nodes = getNodeList(document, xmlPath);

        return (nodes.getLength()) > 0;
    }

    /**
     * Checks if the xmlPath exists in the xml body
     *
     * @param xmlBody The xml body as String
     * @param xmlPath The XPath to get the check if it exists
     * @return true/false
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
    public static boolean hasNode(String xmlBody,
                                  String xmlPath) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        if (xmlBody == null || xmlBody.isBlank()) {
            return false;
        }

        NodeList nodes = getNodeList(xmlBody, xmlPath);

        return (nodes.getLength()) > 0;
    }

    /**
     * Parses an XML file
     *
     * @param filePath The file to parse
     * @return Document object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document parseXmlFile(String filePath) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilder builder   = getDocumentBuilder();
        File            inputFile = new File(filePath);

        return builder.parse(inputFile);
    }

    /**
     * Returns the XML representation of the Node tree
     *
     * @param node Starting node
     * @return String as XML
     * @throws JsonProcessingException
     */
    public static String toXmlString(Node node) throws JsonProcessingException {

        XmlMapper xmlMapper = new XmlMapper();

        String xmlString = xmlMapper.writeValueAsString(node);

        // Unescape HMLTL entities (like &lt;)
        return StringEscapeUtils.unescapeHtml4(xmlString);
    }

}
