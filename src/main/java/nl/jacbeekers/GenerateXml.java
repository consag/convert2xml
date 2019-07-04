/*
 * MIT License
 *
 * Copyright (c) 2019 Jac. Beekers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package nl.jacbeekers;

import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GenerateXml {
    public GenerateXml() {
    }

    private static final org.apache.log4j.Logger logger = Logger.getLogger(GenerateXml.class.getName());
//    private static final Logger logger = LogManager.getLogger(GenerateXml.class.getName());

    private static Object lock = new Object();   // lock to synchronize nrRows
    private String target = Constants.DEFAULT_TARGET;
    private String xsdFile = Constants.DEFAULT_XSD;
    private String xsdPath = Constants.DEFAULT_XSD_PATH;
    private int nrRows;
    private int partNrFiles = 1;
    private int nrFiles = 1;
    private int nrElements = 0;
    private String rootElement = Constants.DEFAULT_ROOT_ELEMENT;
    private HashMap<String, String> entry = null;
    private String oneFilePerRow = Constants.DEFAULT_ONE_FILE_PER_ROW;
    private ArrayList<NodeInfo> elementList = new ArrayList<NodeInfo>();
    private XMLStreamWriter writer = null;
    private String resultCode = Constants.OK;
    private String resultMessage = Constants.getResultMessage(resultCode);

    private void logVerbose(String msg) {
        logger.trace(msg);
    }

    private void logDebug(String procName, String msg) {
        logger.debug(procName + " - " + msg);
    }
    private void logDebug(String msg) {
        logger.debug(msg);
    }

    private void logWarning(String msg) {
        logger.warn(msg);
    }

    private void logError(String resultCode, String msg) {
        setResult(resultCode, msg);
        logger.error(msg);
    }
    private void setResult(String resultCode, String msg) {
        setResultCode(resultCode);
        if(msg==null) {
            setResultMessage(Constants.getResultMessage(resultCode));
        } else {
            setResultMessage(Constants.getResultMessage(resultCode)
                    +": " + msg);
        }
    }

    private void logFatal(String resultCode) {
        logFatal(resultCode, Constants.getResultMessage(resultCode));
    }
    private void logFatal(String resultCode, String msg) {
        setResult(resultCode, msg);
        logger.fatal(msg);
    }

    private void failSession(String resultCode) {
        failSession(resultCode, null);
    }
    private void failSession(String resultCode, String msg) {
        logError(resultCode, msg);
    }


    public void generateXmlFile(String sourceFile)
            throws IOException, XMLStreamException {
        if ("Y".equals(getOneFilePerRow())) {
            generateOneXmlFilePerRow(sourceFile);
        } else {
            generateOneXmlFile(sourceFile);
        }
    }

    public void generateXmlFile(ArrayList<HashMap<String, String>> data) {

        if ("Y".equals(getOneFilePerRow())) {
            generateOneXmlFilePerRow(data);
        } else {
            generateOneXmlFile(data);
        }

    }

    public void generateOneXmlFilePerRow(String sourceFile) {
        logFatal(Constants.NOT_IMPLEMENTED,
                ": " + getClass().getName() + ".");
    }

    public void generateOneXmlFilePerRow(ArrayList<HashMap<String, String>> data) {
        for (HashMap<String, String> entry : data) {
            setNrRows(getNrRows() + 1);
            String currentTarget = getTarget() + "_" + getNrRows() + ".xml";
            Path pathXmlFile = Paths.get(currentTarget);
            try {
                Files.createFile(pathXmlFile);
                setEntry(entry);
                writeDataTo(currentTarget);
            } catch (FileAlreadyExistsException e) {
                failSession( Constants.XMLFILE_EXISTS,  e.toString());
            } catch (IOException ioe) {
                failSession( Constants.XMLFILE_CREATE_FAILED, ioe.toString());
            }
        }
    }

    public String getFormattedCurrentTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = sdf.format(date);

        return formattedTime;
    }

    private void initWriter()
            throws IOException, XMLStreamException {
        //xml
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        setWriter(factory.createXMLStreamWriter(new FileOutputStream(getTarget() + ".xml"), "UTF-8"));

    }

    private void writeHeader()
            throws XMLStreamException {
        getWriter().writeStartDocument("UTF-8", "1.0");
        getWriter().writeComment("Generated by " + getClass() + ". Started at " + getFormattedCurrentTime());
        getWriter().writeCharacters("\n");
    }

    private void writeComplexElementStart(NodeInfo node) throws XMLStreamException {
        getWriter().writeStartElement(node.getAttribute());
    }

    private void writeComplexElementEnd() throws XMLStreamException {
        getWriter().writeEndElement();
    }

    private void writeInitialComplexElementsStart()
            throws XMLStreamException {
        boolean stop = false;
        getWriter().writeStartElement(getRootElement());

            for (NodeInfo node : elementList) {
            if (node.getElementType() != null) {
                switch (node.getElementType()) {
                    case Constants.COMPLEX:
//                        getWriter().writeStartElement(node.getAttribute());
                        break;
                    default:
                        stop = true;
                        break;
                }
            }
            if (stop) break;
        }
    }

    private void writeInitialComplexElementsEnd()
            throws XMLStreamException {
        boolean stop = false;
        boolean wroteEndTime = false;
        for (NodeInfo node : elementList) {
            if (node.getElementType() != null) {
                switch (node.getElementType()) {
                    case Constants.COMPLEX:
                        if (! wroteEndTime) {
//                            getWriter().writeCharacters("\n");
//                            getWriter().writeComment("Generated by " + getClass() + " completed at " + getFormattedCurrentTime());
                            wroteEndTime = true;
                        }
//                        getWriter().writeEndElement();
                        break;
                    default:
                        stop = true;
                        break;
                }
            }
            if (stop) {
                break;
            }
        }
        getWriter().writeCharacters("\n");
        getWriter().writeComment("Generated by " + getClass() + ". Completed at " + getFormattedCurrentTime());
        getWriter().writeCharacters("\n");
        getWriter().writeEndElement();
    }

    public void initXmlFile() {
        try {
            initWriter();
            writeHeader();
            getXsdStructure();
            outInfoElementList();
            writeInitialComplexElementsStart();
        } catch (XMLStreamException e) {
            failSession( Constants.XMLSTREAM_ERROR, "Could not write initial elements to target xml file: " + e.toString());
        } catch (IOException ioe) {
            failSession( Constants.XMLFILE_CREATE_FAILED, ioe.toString());
        }

    }

    public void endXmlFile(){
        try {
            writeInitialComplexElementsEnd();
            getWriter().writeEndDocument();
            getWriter().close();
        } catch (XMLStreamException e) {
            failSession( Constants.XMLSTREAM_ERROR, "Could not write final closing elements: " + e.toString());
        }

    }

    public void generateOneXmlFile(ArrayList<HashMap<String, String>> data) {

        initXmlFile();

        int nrMap = 0;
        for (HashMap<String, String> map : data) {
            nrMap++;
            logDebug("Processing array entry >" + nrMap + "<.");
            String[] currentElements = map.values().toArray(new String[0]);
            writeDataElements(currentElements);
        }

        endXmlFile();
    }

    public void generateOneXmlFile(String sourceFile)
            throws IOException, XMLStreamException {

        initWriter();
        writeHeader();
        getXsdStructure();
        outInfoElementList();
        writeInitialComplexElementsStart();

        processSourceFile(sourceFile);

        endXmlFile();
    }

    private void processSourceFile(String sourceFile)
            throws IOException {
        //input
        File file = new File(sourceFile);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int nrLines = 0;
        while ((line = reader.readLine()) != null) {
            nrLines++;
            String[] values = line.split(";");
            logVerbose("line >" + nrLines + "< contains >" + values.length + "< values.");
            writeDataElements(values);
        }
    }

    public void writeDataElements(ArrayList<String> values)  {
        writeDataElements(values.toArray(new String[0]));

    }

    public void writeDataElements(String[] values) {
        String procName ="writeDataElements-StringArray";

        if (values.length == 1)
            logDebug(procName,"Line contains >" + values.length + "< field value.");
        else
            logDebug(procName,"Line contains >" + values.length + "< field values.");
        try {
            getWriter().writeCharacters("\n");
            int i = 0;
            //TODO: Complex structures are not supported yet. So count the number of Complex Type elements and close them at the end
            int complexTypes = 0;
            for (NodeInfo node : elementList) {
                if (node.getElementType() != null) {
                    switch (node.getElementType()) {
                        case Constants.COMPLEX:
                            if(node.getRef() == null || "".equals(node.getRef())) {
                                logDebug(procName, "Writing Complex element type info for attribute >" + node.getAttribute() + "<.");
                                writeComplexElementStart(node);
                                complexTypes++;
                            } else {
                                logDebug(procName, "Skipping Complex element type >" + node.getAttribute()
                                        + "< with a ref >" + node.getRef() + "<.");
                            }
                            break;
                        case Constants.SIMPLE:
                            logDebug(procName, "Writing Simple element type info for attribute >" + node.getAttribute() +"<.");
                            getWriter().writeStartElement(node.getAttribute());
                            if (i < values.length) {
                                getWriter().writeCharacters(values[i]);
                                i++;
                            } else {
                                //more xml elements than data elements
                                logVerbose("There are more xml elements than data fields.");
                            }
                            getWriter().writeEndElement();
                            break;
                        default:
                            logWarning("Ignored invalid or unsupported elementType >" + node.getElementType() + "<.");
                            break;
                    }
                }
            }
            for( i = 0 ; i< complexTypes; i++) {
                writeComplexElementEnd();
            }
        } catch (XMLStreamException e) {
            failSession("Could not write xml data: " + e.toString());
    }

}

    public void outInfoElementList() {
        if (elementList.size() == 1)
            logDebug("elementList contains >" + elementList.size() + "< entry.");
        else
            logDebug("elementList contains >" + elementList.size() + "< entries.");

        int elemNr = 0;
        for (NodeInfo elem : elementList) {
            elemNr++;
            logDebug("element# >" + elemNr + "< is >" + elem.getAttribute() + "< of type >" +
                    elem.getElementType() + "<.");
        }

    }

    public void writeDataTo(String xmlFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new FileReader(
                    getXsdPath() + getXsdFile())));

            Document outputDoc = builder.newDocument();
            Element outputNode = outputDoc.createElement(getRootElement());

            recurse(document.getDocumentElement(), outputNode, outputDoc);

            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(new DOMSource(outputNode), result);
        } catch (IOException e) {
            failSession(Constants.XSDNOTFOUND, getXsdPath() + getXsdFile());
        } catch (ParserConfigurationException e) {
            failSession(Constants.XML_PARSER_CONFIG_ERROR, "Writing xsd structure elements to xml: " + e.toString());
        } catch (TransformerConfigurationException e) {
            failSession(Constants.XML_TRANSFORM_ERROR, "Generating XML file >" + xmlFile + "<. Error: " + e.toString());
        } catch (TransformerException e) {
            failSession(Constants.XML_TRANSFORM_ERROR, "Generating XML file >" + xmlFile + "<. Error: " + e.toString());
        } catch (SAXException e) {
            failSession(Constants.SAX_EXCEPTION, "Generating XML file >" + xmlFile + "<. Error: " + e.toString());
        }

    }

    public void getXsdStructure() {
        //xsd
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
        DocumentBuilder builder = docFactory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new FileReader(
                    getXsdPath() + getXsdFile())));
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setAttribute("root");
            nodeInfo.setElementType("root");

            getXsdStructure(document.getDocumentElement(), nodeInfo);
        } catch (IOException e) {
            failSession(Constants.XSDNOTFOUND, getXsdPath() + getXsdFile());
        } catch (SAXException e) {
            failSession(Constants.SAX_EXCEPTION, "Parsing xsd >" + getXsdFile() +"<. Error: " + e.toString());
        } catch (ParserConfigurationException e) {
            failSession(Constants.XML_PARSER_CONFIG_ERROR, "Parsing xsd >" + getXsdFile() + "<. Error: ");
        }


    }

    private void getXsdStructure(Node node, NodeInfo nodeInfo) {
        String procName="getXsdStructure-node-nodeInfo";
        NodeInfo currentNodeWithAtribute = nodeInfo;

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            logDebug(procName, "nodeName is >" + element.getNodeName() + "<.");
            if ("xs:element".equals(node.getNodeName())) {
                nrElements++;
                logDebug(procName,"nrElements is >" + nrElements + "<.");
                try {
                    String ref ="";
                    String attr = element.getAttribute("name");
                    logDebug(procName,"attribute >" + attr + "<");
                    NodeInfo newNodeInfo = new NodeInfo();
                    if("".equals(attr)) {
                        logDebug(procName,"attribute is empty");
                        ref = element.getAttribute("ref");
                        if(currentNodeWithAtribute != null) {
                            if("".equals(ref)) {
                                logWarning("No attribute nor ref found under node >" + currentNodeWithAtribute.getAttribute() + "<.");
                            } else {
                                logDebug(procName, "Found a ref >" + ref + "< in node >" + currentNodeWithAtribute.getAttribute() + "<.");
                                currentNodeWithAtribute.setRef(ref);
                            }

                        } else {
                            logWarning("Found the ref >" + ref +"< without an attribute node.");
                        }
                        newNodeInfo.setRef(ref);
                        newNodeInfo.setElementType(Constants.REF);
                    } else {
                        currentNodeWithAtribute = newNodeInfo;
                        newNodeInfo.setAttribute(attr);
                        NodeList list = element.getChildNodes();
                        for (int i = 0; i < list.getLength(); i++) {
                            logDebug(procName,"Child for name >" + attr + "< has node name >" + list.item(i).getNodeName() + "<.");
                            if (Constants.COMPLEX.equals(list.item(i).getNodeName())) {
                                newNodeInfo.setElementType(Constants.COMPLEX);
                            }
                        }
                        //assume
                        if (newNodeInfo.getElementType() == null) {
                            newNodeInfo.setElementType(Constants.SIMPLE);
                        }
                        elementList.add(newNodeInfo);
                    }
                } catch (DOMException e) {
                        logWarning("Ignored element without a name or ref.");
                }

            }
            if ("xs:attribute".equals(node.getNodeName())) {
                //TODO required attributes
            }
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            getXsdStructure(list.item(i), currentNodeWithAtribute);
        }
    }


    public void recurse(Node node, Node outputNode, Document outputDoc) {
        String procName ="recurse";

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            logDebug(procName, "nodeName is >" + element.getNodeName() + "<.");
            if ("xs:element".equals(node.getNodeName())) {
                nrElements++;
                logDebug(procName,"nrElements is >" + nrElements + "<.");
                try {
                    String attr = element.getAttribute("name");
                    if (attr.equals(getRootElement()) && nrElements == 1) {
                        logDebug(procName,"Ignored rootElement >" + attr + "<. Already created.");
                    } else {
                        Element newElement = outputDoc.createElement(attr);
                        String val = getEntry().get(newElement.getNodeName());
                        newElement.setTextContent(val);
                        logDebug(procName,"elementName is >" + newElement.getNodeName() + "<.");
                        logDebug(procName,"nodeTextContent is >" + newElement.getTextContent() + "<.");
                        outputNode = outputNode.appendChild(newElement);
                    }
                } catch (DOMException e) {
                        logWarning("Ignored element without a name or ref.");
                }

            }
            if ("xs:attribute".equals(node.getNodeName())) {
                logDebug(procName, "found an xs:attribute node.");
                //TODO required attributes
            }
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            logVerbose("Processing child node >" + i + "< with name >" +
                    list.item(i).getNodeName() + "<.");
            recurse(list.item(i), outputNode, outputDoc);
        }
    }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getXsdFile() { return this.xsdFile; }
    public void setXsdFile(String xsdFile) { this.xsdFile = xsdFile; }

    public String getXsdPath() { return this.xsdPath; }
    public void setXsdPath(String xsdPath) { this.xsdPath = xsdPath; }

    public int getNrRows() { return nrRows; }
    public void setNrRows(int nrRows) { this.nrRows = nrRows; }

    public String getRootElement() { return this.rootElement; }
    public void setRootElement(String rootElement) { this.rootElement = rootElement; }

    public String getOneFilePerRow() { return oneFilePerRow; }
    public void setOneFilePerRow(String oneFilePerRow) { this.oneFilePerRow = oneFilePerRow; }

    public XMLStreamWriter getWriter() { return this.writer; }
    public void setWriter(XMLStreamWriter writer) { this.writer = writer; }

    private HashMap<String, String> getEntry() { return this.entry; }
    private void setEntry(HashMap<String, String> entry) { this.entry = entry; }

    public int getPartNrFiles() { return this.partNrFiles; }
    private void setPartNrFiles(int partNrFiles) { this.partNrFiles = partNrFiles; }

    public int getNrFiles() { return this.nrFiles; }
    private void setNrFiles(int nrFiles) { this.nrFiles = nrFiles; }

    public String getResultCode() { return this.resultCode; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }

    public String getResultMessage() { return this.resultMessage; }
    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }
}


class NodeInfo {
    String attribute;
    String elementType; // complex?
    String ref;

    public String getAttribute() {
        return this.attribute;
    }
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getElementType() {
        return this.elementType;
    }
    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getRef() { return this.ref;}
    public void setRef(String ref) { this.ref = ref; }

}