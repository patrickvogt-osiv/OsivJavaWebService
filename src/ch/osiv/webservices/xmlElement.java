package ch.osiv.webservices;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class xmlElement {

    private String nodeType;
    private int    currentNodeIndex;
    private int    parentNodeIndex;
    private String parentNodeName;
    private String currentNodeName;
    private String currentNodeValue;

    public String getnodeType() {
        return nodeType;
    }

    public void setnodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public int getCurrentNodeIndex() {
        return currentNodeIndex;
    }

    public void setCurrentNodeIndex(int currentNodeIndex) {
        this.currentNodeIndex = currentNodeIndex;
    }

    public int getParentNodeIndex() {
        return parentNodeIndex;
    }

    public void setParentNodeIndex(int parentNodeIndex) {
        this.parentNodeIndex = parentNodeIndex;
    }

    public String getParentNodeName() {
        return parentNodeName;
    }

    public void setParentNodeName(String parentNodeName) {
        this.parentNodeName = parentNodeName;
    }

    public String getCurrentNodeName() {
        return currentNodeName;
    }

    public void setCurrentNodeName(String currentNodeName) {
        this.currentNodeName = currentNodeName;
    }

    public String getCurrentNodeValue() {
        return currentNodeValue;
    }

    public void setCurrentNodeValue(String currentNodeValue) {
        this.currentNodeValue = currentNodeValue;
    }

    public xmlElement(String nodeType,
                      int currentNodeIndex,
                      int parentNodeIndex,
                      String parentNodeName,
                      String currentNodeName,
                      String currentNodeValue) {
        super();
        this.nodeType         = nodeType;
        this.currentNodeIndex = currentNodeIndex;
        this.parentNodeIndex  = parentNodeIndex;
        this.parentNodeName   = parentNodeName;
        this.currentNodeName  = currentNodeName;
        this.currentNodeValue = currentNodeValue;
    }
}
