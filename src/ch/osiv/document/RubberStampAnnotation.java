package ch.osiv.document;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Rubber stamp annotation
 */
public class RubberStampAnnotation
    extends AnnotationBase {

    /**
     * @return fontName
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * @return fontColor
     */
    public String getFontColor() {
        return fontColor;
    }

    /**
     * @return fontItalic
     */
    public String getFontItalic() {
        return fontItalic;
    }

    /**
     * @return fontSize
     */
    public float getFontSize() {
        return fontSize;
    }

    /**
     * @return fontBold
     */
    public String getFontBold() {
        return fontBold;
    }

    /**
     * @return textString
     */
    public String getTextString() {
        return textString;
    }

    /**
     * @return centerText
     */
    public boolean getCenterText() {
        return centerText;
    }

    private String  fontColor;
    private String  fontName;
    private String  fontItalic;
    private float   fontSize;
    private String  fontBold;
    private String  textString;
    private boolean centerText;

    /**
     * Constructor
     *
     * @param fontColor
     * @param fontName
     * @param fontItalic
     * @param fontSize
     * @param fontBold
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @param textString
     * @param centerText
     */
    /**
     * @param fontColor
     * @param fontName
     * @param fontItalic
     * @param fontSize
     * @param fontBold
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @param textString
     * @param centerText
     */
    public RubberStampAnnotation(String fontColor,
                                 String fontName,
                                 String fontItalic,
                                 String fontSize,
                                 String fontBold,
                                 double startX,
                                 double startY,
                                 double endX,
                                 double endY,
                                 String textString,
                                 boolean centerText) {

        // set super members
        super(1.0);
        this.setStartX(startX);
        this.setStartY(startY);
        this.setEndX(endX);
        this.setEndY(endY);

        this.fontColor  = fontColor;
        this.fontName   = fontName;
        this.fontItalic = fontItalic;
        this.fontSize   = getAnnotationSize(fontSize);
        this.fontBold   = fontBold;
        this.textString = textString;
        this.setFontSizeInPixel(Integer.valueOf(fontSize.substring(0, fontSize.length() - 2)));
        this.centerText = centerText;

    }

    /**
     * Constructor
     *
     * @param annObjectChildList
     * @param scalingFactor
     */
    /**
     * @param annObjectChildList
     * @param scalingFactor
     */
    public RubberStampAnnotation(NodeList annObjectChildList,
                                 Double scalingFactor) {

        super(scalingFactor);

        for (int i = 0; i < annObjectChildList.getLength(); i++) {
            Node annObjectChild = annObjectChildList.item(i);
            if (annObjectChild != null && annObjectChild.getNodeType() == Node.ELEMENT_NODE) {
                switch (annObjectChild.getNodeName()) {
                    case "annStartX": {
                        this.setStartX(Double.valueOf(annObjectChild.getTextContent()));
                        break;
                    }
                    case "annStartY": {
                        this.setStartY(Double.valueOf(annObjectChild.getTextContent()));
                        break;
                    }
                    case "annWidth": {
                        this.setEndX(Double.valueOf(annObjectChild.getTextContent()));
                        break;
                    }
                    case "annHeight": {
                        this.setEndY(Double.valueOf(annObjectChild.getTextContent()));
                        break;
                    }
                    case "textString": {
                        Decoder decoder         = Base64.getDecoder();
                        byte[]  stringByteArray = decoder.decode(annObjectChild.getTextContent());
                        textString = new String(stringByteArray, StandardCharsets.UTF_16);
                        break;
                    }
                    case "fillInfo": {
                        fillInfo(annObjectChild.getChildNodes());
                        break;
                    }
                    case "fontInfo": {
                        fontInfo(annObjectChild.getChildNodes());
                        break;
                    }
                }
            }
        }
    }

    /**
     * @param fontInfoChildList
     */
    private void fontInfo(NodeList fontInfoChildList) {
        for (int i = 0; i < fontInfoChildList.getLength(); i++) {
            Node fontInfoChild = fontInfoChildList.item(i);
            if (fontInfoChild != null && fontInfoChild.getNodeType() == Node.ELEMENT_NODE) {

                switch (fontInfoChild.getNodeName()) {
                    case "fontName": {
                        fontName = fontInfoChild.getTextContent();
                        break;
                    }
                    case "fontItalic": {
                        fontItalic = fontInfoChild.getTextContent();
                        break;
                    }
                    case "fontBold": {
                        fontBold = fontInfoChild.getTextContent();
                        break;
                    }
                    case "fontSize": {
                        setFontSizeInPixel(Integer.valueOf(fontInfoChild.getTextContent()
                                                                        .substring(0,
                                                                                   fontInfoChild.getTextContent()
                                                                                                .length() -
                                                                                      2)));
                        fontSize = getAnnotationSize(fontInfoChild.getTextContent());
                        break;
                    }
                }
            }
        }
    }

    /**
     * @param fillInfoChildList
     */
    private void fillInfo(NodeList fillInfoChildList) {
        for (int i = 0; i < fillInfoChildList.getLength(); i++) {
            Node fillInfoChild = fillInfoChildList.item(i);
            if (fillInfoChild != null && fillInfoChild.getNodeType() == Node.ELEMENT_NODE) {
                if (fillInfoChild.getNodeName().equals("fillColor")) {
                    fontColor = fillInfoChild.getTextContent();
                }
            }
        }
    }

    /**
     * @return if its a pagination annotation
     */
    public Boolean isPaginationAnnotation() {
        if (fontColor.equals("000000"))
            return true;
        else
            return false;
    }

    /**
     * @param annotationSize
     * @return
     */
    private float getAnnotationSize(String annotationSize) {
        switch (annotationSize) {
            case "32px":
                return (float) 6.0;
            case "44px":
                return (float) 11.0;
            case "48px":
                return (float) 12.0;
            case "63px":
                return (float) 15.0;
            case "79px":
                return (float) 19.0;
            case "100px":
                return (float) 24.0;
            case "152px":
                return (float) 36.0;
            case "216px":
                return (float) 48.0;
            case "294px":
                return (float) 72.0;
            default:
                return (float) 15.0;
        }

    }

}
