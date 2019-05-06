package nl.jacbeekers;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class generateXml {

    private static final Logger logger = LogManager.getLogger(generateXml.class.getName());

    static Object lock = new Object();   // lock to synchronize nrRows

    String target ="tryout";
    String xsdFile ="dummy.xsd";
    int nrRows;
    int partNrFiles = 1;
    int nrFiles = 1;
    boolean firstElement =true;
    int nrElements =0;
    String rootElement ="root";

    private void logDebug(String msg){
        //System.out.println(msg);
        logger.debug(msg);

    }
    private void logWarning(String msg){
        //System.out.println("WARNING: " +msg);
        logger.warn(msg);
    }
    private void logVerbose(String msg) {
        logger.trace(msg);
    }

    private void failSession(String msg) throws SDKException {
        throw new SDKException(msg);
    }

    public String getTarget() {
        return target;
    }
    public void setTarget(String target) { this.target = target; }
    public String getXsdFile() { return this.xsdFile ; }
    public void setXsdFile(String xsdFile) { this.xsdFile = xsdFile; }


    public int getNrRows() {
        return nrRows;
    }
    public void setNrRows(int nrRows) { this.nrRows = nrRows; };


    public void generateXmlFile(ArrayList<HashMap<String, String>> data) throws SDKException, Exception {

        Iterator itr = data.iterator();
        while (itr.hasNext()) {
            setNrRows(getNrRows() +1);
            String currentTarget = getTarget() + "_" + getNrRows() + ".xml";
            Path pathXmlFile = Paths.get(currentTarget);
            try {
                Files.createFile(pathXmlFile);
                writeDataTo(currentTarget);
            } catch (FileAlreadyExistsException e) {
                failSession("xml file already exists: " + e.toString());
            } catch (IOException ioe) {
                failSession("could not create xml file: " + ioe.toString());
            }

            itr.next();

        }

    }

    private void writeDataTo(String pathXmlFile) throws SDKException, Exception {

        run(pathXmlFile);
    }

    private void setPartNrFiles(int partNrFiles) {
        this.partNrFiles = partNrFiles;
    }

    public int getPartNrFiles() {
        return this.partNrFiles;
    }

    private void setNrFiles(int nrFiles) {
        this.nrFiles = nrFiles;
    }

    public int getNrFiles() {
        return this.nrFiles;
    }


    public void run(String xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new FileReader(
                getXsdFile())));

        Document outputDoc = builder.newDocument();
        Element outputNode = outputDoc.createElement(getRootElement());
        //Element outputNode = null;

        recurse(document.getDocumentElement(), outputNode, outputDoc);

        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        StringWriter buffer = new StringWriter();

/*        transformer.transform(new DOMSource(outputNode),
                new StreamResult(buffer));
        System.out.println(buffer.toString());
        */
        StreamResult result = new StreamResult(xmlFile);
        transformer.transform(new DOMSource(outputNode), result);

    }

    public Element findFirst(Node parent) {

        Node child = parent.getFirstChild();
        while (child != null) {
            if(child.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) child;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    public void recurse(Node node, Node outputNode, Document outputDoc) {

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            logDebug("nodeName is >" +element.getNodeName() +"<.");
            if ("xs:element".equals(node.getNodeName())) {
                nrElements++;
                logDebug("nrElements is >" +nrElements +"<.");
                try {
                    String attr = element.getAttribute("name");
                    if(attr.equals(getRootElement()) && nrElements ==1) {
                        logDebug("Ignored rootElement >" +attr +"<. Already created.");
                    } else {
                        Element newElement = outputDoc.createElement(attr);
                        outputNode = outputNode.appendChild(newElement);
                        logDebug("elementName is >" + newElement.getNodeName() + "<.");
                        newElement.setNodeValue("ABC");
                    }
                } catch (DOMException e) {
                    try {
                        String attr = element.getAttribute("ref");
//                        Element newElement = outputDoc.createElement(attr);
                        logDebug("elementName >" + element.getNodeName() + "< is a ref. NOT SUPPORTED in this release. Ignored.");
//                        outputNode = outputNode.appendChild(newElement);
                    } catch(DOMException eRef) {
                        logWarning("Ignored element without a name or ref.");
                    }
                }

                // map elements from CSV values here?
            }
            if ("xs:attribute".equals(node.getNodeName())) {
                //TODO required attributes
            }
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            logVerbose("Processing child node >" + i + "< with name >" +
                list.item(i).getNodeName() +"<.");
            recurse(list.item(i), outputNode, outputDoc);
        }
    }

    public String getRootElement() {
        return this.rootElement;
    }
    public void setRootElement(String rootElement) {
        this.rootElement = rootElement;
    }
    }

class SDKException extends Exception {
    String msg;

    SDKException(String msg) {
        this.msg = msg;
    }

    public String toString() {
        return ("SDKException occurred: " + this.msg);
    }
}
