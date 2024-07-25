package ch.osiv.webservices;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("XMLService")
public class XMLService {

    static List<xmlElement> xmlElementList = null;
    static int              currentNodeIndex;

    @GET
    @Path("ReadXML")
    @Produces({
            MediaType.APPLICATION_JSON, "text/json", "application/*+json"
    })
    public List<xmlElement> ReadXML(@QueryParam("xmlFile")
    String xmlFile) throws Exception {

        Document doc = null;
        currentNodeIndex = 0;
        xmlElementList   = new ArrayList<xmlElement>();

        xmlFile = xmlFile.replace("\\", "/");
        File file = new File(xmlFile);

        if (file.exists()) {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            try {
                doc = dBuilder.parse(file);
            } catch (Exception e) {
                System.out.println("Error: -> File cannot be analysed :" + e.getMessage());
            }

            if (doc != null && doc.hasChildNodes()) {
                printNote(doc.getChildNodes(), ">", "documentRoot", 0);
            }
        } else {
            System.out.println("Error: -> No file found");
        }

        return xmlElementList;

    }

    private void printNote(NodeList nodeList,
                           String stufe,
                           String parentNodeName,
                           int parentNodeIndex) {

        currentNodeIndex = currentNodeIndex + 1;

        for (int count = 0; count < nodeList.getLength(); count++) {

            Node tempNode = nodeList.item(count);

            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

                // get node name and value
                //System.out.println("Node:" + currentNodeIndex + ":Parent:" + parentNodeIndex + stufe + parentNodeName + "." + tempNode.getNodeName() + "=" + tempNode.getTextContent());
                xmlElementList.add(new xmlElement("Node",
                                                  currentNodeIndex,
                                                  parentNodeIndex,
                                                  parentNodeName,
                                                  tempNode.getNodeName(),
                                                  tempNode.getTextContent()));

                if ((tempNode.hasAttributes())) {

                    // get attributes names and values
                    NamedNodeMap nodeMap = tempNode.getAttributes();

                    for (int i = 0; i < nodeMap.getLength(); i++) {

                        Node node = nodeMap.item(i);

                        //System.out.println("-->" + prevTempNode.getNodeName());
                        try {
                            //System.out.println("Attr:" + currentNodeIndex + "Parent:" + parentNodeIndex + stufe +  parentNodeName + "." + node.getNodeName() + "= " + node.getNodeValue());
                            xmlElementList.add(new xmlElement("Attribute",
                                                              currentNodeIndex,
                                                              parentNodeIndex,
                                                              parentNodeName,
                                                              tempNode.getNodeName(),
                                                              node.getNodeValue()));

                        } catch (Exception e) {
                            System.out.println("Error: -> " + e.getMessage());
                        }
                    }
                }

                if (tempNode.hasChildNodes()) {
                    // loop again if has child nodes					
                    printNote(tempNode.getChildNodes(),
                              "-" + stufe,
                              parentNodeName + "." + tempNode.getNodeName(),
                              currentNodeIndex);
                }

            }

        }

    }

}
