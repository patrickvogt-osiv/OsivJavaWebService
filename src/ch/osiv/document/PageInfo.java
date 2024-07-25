package ch.osiv.document;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Gets the annotation data from the annotation xml file for a <page> element
 *
 * @author
 */
public class PageInfo {

    /**
     * @return pageNumber
     */
    public Integer getPageNumber() {
        return pageNumber;
    }

    /**
     * @return pageWidth
     */
    public double getPageWidth() {
        return pageWidth;
    }

    /**
     * @param pageWidth
     */
    public void setPageWidth(double pageWidth) {
        this.pageWidth = pageWidth;
    }

    /**
     * @return pageHeight
     */
    public double getPageHeight() {
        return pageHeight;
    }

    /**
     * @param pageHeight
     */
    public void setPageHeight(double pageHeight) {
        this.pageHeight = pageHeight;
    }

    /**
     * @return lineAnnotationList
     */
    public List<LineAnnotation> getLineAnnotationList() {
        return lineAnnotationList;
    }

    /**
     * @return rubberStampAnnotationList
     */
    public List<RubberStampAnnotation> getRubberStampAnnotationList() {
        return rubberStampAnnotationList;
    }

    /**
     * @return paginationAnnotationList
     */
    public List<PaginationAnnotation> getPaginationAnnotationList() {
        return paginationAnnotationList;
    }

    private int    pageNumber;
    private double pageWidth;
    private double pageHeight;
    private double scalingFactor;

    List<LineAnnotation>        lineAnnotationList;
    List<RubberStampAnnotation> rubberStampAnnotationList;
    List<PaginationAnnotation>  paginationAnnotationList;

    /**
     * Constructor
     *
     * @param page          Node
     * @param scalingFactor The scaling factor when the DPI is different
     * @throws XPathExpressionException
     * @throws UnsupportedEncodingException
     */
    /**
     * @param page
     * @param scalingFactor
     * @throws XPathExpressionException
     * @throws UnsupportedEncodingException
     */
    public PageInfo(Node page,
                    double scalingFactor) throws XPathExpressionException, UnsupportedEncodingException {

        lineAnnotationList        = new ArrayList<>();
        paginationAnnotationList  = new ArrayList<>();
        rubberStampAnnotationList = new ArrayList<>();
        this.scalingFactor        = scalingFactor;

        NodeList pageChildList = page.getChildNodes();
        for (int i = 0; i < pageChildList.getLength(); i++) {
            Node pageChild = pageChildList.item(i);
            if (pageChild != null && pageChild.getNodeType() == Node.ELEMENT_NODE) {
                switch (pageChild.getNodeName()) {
                    case "pageMeta":
                        pageMeta(pageChild);
                        break;

                    case "pageObjects":
                        pageObjects(pageChild);
                        break;
                }
            }
        }
    }

    /**
     * PageObjects
     *
     * @param pageChild
     * @throws UnsupportedEncodingException
     */
    /**
     * @param pageChild
     * @throws UnsupportedEncodingException
     */
    private void pageObjects(Node pageChild) throws UnsupportedEncodingException {
        NodeList pageObjectList = pageChild.getChildNodes();
        for (int i = 0; i < pageObjectList.getLength(); i++) {
            Node pageObject = pageObjectList.item(i);
            if (pageObject != null && pageObject.getNodeType() == Node.ELEMENT_NODE) {
                annMeta(pageObject);
            }
        }
    }

    /**
     * AnnMeta
     *
     * @param pageObject
     * @throws UnsupportedEncodingException
     */
    /**
     * @param pageObject
     * @throws UnsupportedEncodingException
     */
    private void annMeta(Node pageObject) throws UnsupportedEncodingException {
        NodeList pageObjectChildList = pageObject.getChildNodes();

        for (int i = 0; i < pageObjectChildList.getLength(); i++) {
            Node pageObjectChild = pageObjectChildList.item(i);
            if (pageObjectChild != null && pageObjectChild.getNodeType() == Node.ELEMENT_NODE &&
                pageObjectChild.getNodeName().equals("annMeta")) {

                NodeList annMetaChildList = pageObjectChild.getChildNodes();

                for (int j = 0; j < annMetaChildList.getLength(); j++) {
                    Node annMetaChild = annMetaChildList.item(j);
                    if (annMetaChild != null && annMetaChild.getNodeType() == Node.ELEMENT_NODE &&
                        annMetaChild.getNodeName().equals("annType")) {

                        switch (annMetaChild.getTextContent()) {
                            case "Line":
                                lineAnnotationList.add(new LineAnnotation(pageObject.getChildNodes(),
                                                                          this.scalingFactor));
                                break;
                            case "Rubber Stamp":
                                String layerID = annMetaChild.getNextSibling().getNextSibling()
                                                             .getTextContent();
                                if (layerID.equals("Pagination.ann")) {
                                    PaginationAnnotation paginationAnnotation = new PaginationAnnotation(pageObject.getChildNodes(),
                                                                                                         this.scalingFactor);
                                    paginationAnnotationList.add(paginationAnnotation);
                                } else {
                                    RubberStampAnnotation rubberStampAnnotation = new RubberStampAnnotation(pageObject.getChildNodes(),
                                                                                                            this.scalingFactor);
                                    if (rubberStampAnnotation.isPaginationAnnotation()) {
                                        PaginationAnnotation paginationAnnotation = new PaginationAnnotation(pageObject.getChildNodes(),
                                                                                                             this.scalingFactor);
                                        paginationAnnotationList.add(paginationAnnotation);
                                    } else {
                                        rubberStampAnnotationList.add(rubberStampAnnotation);
                                    }
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    /**
     * PageMeta
     *
     * @param pageChild
     */
    /**
     * @param pageChild
     */
    private void pageMeta(Node pageChild) {
        NodeList pageMetaInfoList = pageChild.getChildNodes();
        for (int i = 0; i < pageMetaInfoList.getLength(); i++) {
            Node pageMetaInfo = pageMetaInfoList.item(i);
            if (pageMetaInfo != null && pageMetaInfo.getNodeType() == Node.ELEMENT_NODE) {
                switch (pageMetaInfo.getNodeName()) {
                    case "pageNumber":
                        pageNumber = Integer.valueOf(pageMetaInfo.getTextContent());
                        break;
                    case "pageWidth":
                        pageWidth = Double.valueOf(pageMetaInfo.getTextContent());
                        break;
                    case "pageHeight":
                        pageHeight = Double.valueOf(pageMetaInfo.getTextContent());
                        break;
                }
            }
        }
    }

}
