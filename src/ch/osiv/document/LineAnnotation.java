package ch.osiv.document;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Line annotation
 */
public class LineAnnotation
    extends AnnotationBase {

    /**
     * @return lineSize
     */
    public double getLineSize() {
        return lineSize;
    }

    private double lineSize;

    /**
     * Constructor
     *
     * @param annObjectChildList
     * @param scalingFactor
     */
    public LineAnnotation(NodeList annObjectChildList,
                          Double scalingFactor) {

        super(scalingFactor);

        for (int i = 0; i < annObjectChildList.getLength(); i++) {
            Node annObjectChild = annObjectChildList.item(i);
            if (annObjectChild != null && annObjectChild.getNodeType() == Node.ELEMENT_NODE) {
                if (annObjectChild.getNodeName().equals("lineInfo")) {
                    lineInfo(annObjectChild.getChildNodes());
                }
            }
        }
    }

    /**
     * lineInfo
     *
     * @param lineInfoChildList
     */
    private void lineInfo(NodeList lineInfoChildList) {
        for (int i = 0; i < lineInfoChildList.getLength(); i++) {
            Node lineInfoChild = lineInfoChildList.item(i);
            if (lineInfoChild != null && lineInfoChild.getNodeType() == Node.ELEMENT_NODE) {
                if (lineInfoChild.getNodeName().equals("pointArray")) {
                    String   pointArray = lineInfoChild.getTextContent().replace(";", ",");
                    String[] points     = pointArray.split(",");

                    this.setStartX(Double.valueOf(points[0]));
                    this.setStartY(Double.valueOf(points[1]));
                    this.setEndX(Double.valueOf(points[2]));
                    this.setEndY(Double.valueOf(points[3]));
                }
                if (lineInfoChild.getNodeName().equals("lineWidth")) {
                    lineSize = Double.valueOf(lineInfoChild.getTextContent());
                }
            }
        }
    }

}
