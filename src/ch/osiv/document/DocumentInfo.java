package ch.osiv.document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.osiv.helper.XMLHelper;

/**
 * Class which holds the annotation XML data
 *
 * @author
 */
public class DocumentInfo {

    private List<PageInfo>   pageInfoList;
    private int              docDpi;
    private TiffInfoResponse tiffInfo;
    private double           scalingFactor;

    /**
     * Constructor
     *
     * @param annotationFileName The annotation XML file
     * @throws XPathExpressionException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public DocumentInfo(String annotationFileName) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

        this(annotationFileName, null);

    }

    /**
     * Constructor
     *
     * @param annotationFileName The annotation XML file
     * @param tiffInfo           The TIFF info data (this is an optional parameter)
     * @throws XPathExpressionException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public DocumentInfo(String annotationFileName,
                        TiffInfoResponse tiffInfo) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

        this.pageInfoList = new ArrayList<>();
        this.tiffInfo     = tiffInfo;

        parseAnnotationFile(annotationFileName);
    }

    /**
     * When the DPI's don't match, change the width & height to the real values
     *
     * @param pageInfo
     */
    private void changePageSize(PageInfo pageInfo) {

        // sanity - skip when DPI is the same
        if (this.tiffInfo == null || docDpi == this.tiffInfo.getDpi()) {
            return;
        }

        // set height & width
        int pageIdx = pageInfo.getPageNumber();
        pageInfo.setPageHeight(this.tiffInfo.getPageHeightList().get(pageIdx));
        pageInfo.setPageWidth(this.tiffInfo.getPageWidthList().get(pageIdx));
    }

    /**
     * Getter for docDpi
     *
     * @return docDpi
     */
    public int getDocDpi() {
        return docDpi;
    }

    /**
     * Get the page info
     *
     * @param node
     * @param pageNumber
     * @return PageInfo object
     * @throws XPathExpressionException
     * @throws UnsupportedEncodingException
     */
    private PageInfo getPageInfo(Node node) throws XPathExpressionException, UnsupportedEncodingException {
        PageInfo pageInfo = new PageInfo(node, getScalingFactor());
        return pageInfo;
    }

    /**
     * Getter for pageInfoList
     *
     * @return pageInfoList
     */
    public List<PageInfo> getPageInfoList() {
        return pageInfoList;
    }

    /**
     * Getter for scalingFactor
     *
     * @return Scaling factor
     */
    public double getScalingFactor() {
        return scalingFactor;
    }

    /**
     * Parse the annotation file
     *
     * @param annotationFile The annotation xml file
     * @param tiffInfo       The TIFF info data (this is an optional parameter, needed for evt.
     *                       recalculations)
     */
    private void parseAnnotationFile(String annotationFile) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

        Document document     = XMLHelper.parseXmlFile(annotationFile);
        NodeList nodePageList = XMLHelper.getNodeList(document, "//document/docPages/page");

        // retrieve the docDPI from the xml file
        Double dpi = XMLHelper.getNodeNumberValue(document, "//document/docMeta/docDPI");
        if (dpi != null && !dpi.isNaN()) {
            this.docDpi = dpi.intValue();
        } else {
            this.docDpi = 300; // default dpi
        }

        // determine scaling factor
        // @formatter:off
        this.scalingFactor = (this.tiffInfo == null || this.tiffInfo.getDpi() == this.docDpi) ? 1.0
                                                                                              : ((double) this.docDpi) / this.tiffInfo.getDpi();
        // @formatter:on

        // get all info from the pages
        for (int pageElementNr = 0; pageElementNr < nodePageList.getLength(); pageElementNr++) {

            Node nodePage = nodePageList.item(pageElementNr);
            if (nodePage != null && nodePage.getNodeType() == Node.ELEMENT_NODE) {
                PageInfo pageInfo = getPageInfo(nodePage);
                pageInfoList.add(pageInfo);

                // when the DPI's don't match, set width+height from 'getTiffInfo'
                changePageSize(pageInfo);
            }
        }
    }

}
