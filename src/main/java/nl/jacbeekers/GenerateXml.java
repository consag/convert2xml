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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(GenerateXml.class.getName());

    private static Object lock = new Object();   // lock to synchronize nrRows
    private String target = Constants.DEFAULT_TARGET;
    private String xsdFile = Constants.DEFAULT_XSD;
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
            throws IOException, XMLStreamException, ParserConfigurationException, SAXException, TransformerException, ConversionException {
        if ("Y".equals(getOneFilePerRow())) {
            generateOneXmlFilePerRow(sourceFile);
        } else {
            generateOneXmlFile(sourceFile);
        }
    }

    public void generateXmlFile(ArrayList<HashMap<String, String>> data)
            throws ConversionException {

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
        getWriter().writeComment("Generated by " + getClass() + " on " + getFormattedCurrentTime());
        getWriter().writeCharacters("\n");
    }

    private void writeInitialComplexElementsStart()
            throws XMLStreamException {
        boolean stop = false;
        for (NodeInfo node : elementList) {
            if (node.getElementType() != null) {
                switch (node.getElementType()) {
                    case Constants.COMPLEX:
                        getWriter().writeStartElement(node.getAttribute());
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
                            getWriter().writeCharacters("\n");
                            getWriter().writeComment("Generated by " + getClass() + " completed at " + getFormattedCurrentTime());
                            wroteEndTime = true;
                        }

                        getWriter().writeEndElement();
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
    }

    public void initXmlFile() throws ConversionException {
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

    public void endXmlFile() throws ConversionException{
        try {
            writeInitialComplexElementsEnd();
            getWriter().writeEndDocument();
            getWriter().close();
        } catch (XMLStreamException e) {
            failSession( Constants.XMLSTREAM_ERROR, "Could not write final closing elements: " + e.toString());
        }

    }

    public void generateOneXmlFile(ArrayList<HashMap<String, String>> data)
            throws ConversionException {

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
            throws ConversionException, IOException, XMLStreamException, ParserConfigurationException, SAXException {

        initWriter();
        writeHeader();
        getXsdStructure();
        outInfoElementList();
        writeInitialComplexElementsStart();

        processSourceFile(sourceFile);

        endXmlFile();
    }

    private void processSourceFile(String sourceFile)
            throws IOException, ConversionException {
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

    public void writeDataElements(ArrayList<String> values) throws ConversionException {
        writeDataElements((String[])values.toArray());

    }

    public void writeDataElements(String[] values) throws ConversionException {
        if (values.length == 1)
            logVerbose("Line contains >" + values.length + "< field value.");
        else
            logVerbose("Line contains >" + values.length + "< field values.");
        try {
            getWriter().writeCharacters("\n");
            int i = 0;
            for (NodeInfo node : elementList) {
                if (node.getElementType() != null) {
                    switch (node.getElementType()) {
                        case Constants.COMPLEX:
                            break;
                        case Constants.SIMPLE:
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
                    getXsdFile())));

            Document outputDoc = builder.newDocument();
            Element outputNode = outputDoc.createElement(getRootElement());

            recurse(document.getDocumentElement(), outputNode, outputDoc);

            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(new DOMSource(outputNode), result);
        } catch (IOException e) {
            failSession(Constants.XSDNOTFOUND, getXsdFile());
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
                    getXsdFile())));
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setAttribute("root");
            nodeInfo.setElementType("root");

            getXsdStructure(document.getDocumentElement(), nodeInfo);
        } catch (IOException e) {
            failSession(Constants.XSDNOTFOUND, getXsdFile());
        } catch (SAXException e) {
            failSession(Constants.SAX_EXCEPTION, "Parsing xsd >" + getXsdFile() +"<. Error: " + e.toString());
        } catch (ParserConfigurationException e) {
            failSession(Constants.XML_PARSER_CONFIG_ERROR, "Parsing xsd >" + getXsdFile() + "<. Error: ");
        }


    }

    private void getXsdStructure(Node node, NodeInfo nodeInfo) {

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            logDebug("nodeName is >" + element.getNodeName() + "<.");
            if ("xs:element".equals(node.getNodeName())) {
                nrElements++;
                logDebug("nrElements is >" + nrElements + "<.");
                try {
                    String attr = element.getAttribute("name");
                    logDebug("attribute >" + attr + "<");
                    NodeInfo newNodeInfo = new NodeInfo();
                    newNodeInfo.setAttribute(attr);
                    NodeList list = element.getChildNodes();
                    for (int i = 0; i < list.getLength(); i++) {
                        logDebug("Child for >" + attr + " is >" + list.item(i).getNodeName() + "<.");
                        if (Constants.COMPLEX.equals(list.item(i).getNodeName())) {
                            newNodeInfo.setElementType(Constants.COMPLEX);
                        }
                    }
                    //assume
                    if (newNodeInfo.getElementType() == null) {
                        newNodeInfo.setElementType(Constants.SIMPLE);
                    }
                    elementList.add(newNodeInfo);
                } catch (DOMException e) {
                    try {
                        String attr = element.getAttribute("ref");
                        logDebug("elementName >" + element.getNodeName() + "< is a ref. NOT SUPPORTED in this release. Ignored.");
                    } catch (DOMException eRef) {
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
            getXsdStructure(list.item(i), nodeInfo);
        }
    }


    public void recurse(Node node, Node outputNode, Document outputDoc) {

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            logDebug("nodeName is >" + element.getNodeName() + "<.");
            if ("xs:element".equals(node.getNodeName())) {
                nrElements++;
                logDebug("nrElements is >" + nrElements + "<.");
                try {
                    String attr = element.getAttribute("name");
                    if (attr.equals(getRootElement()) && nrElements == 1) {
                        logDebug("Ignored rootElement >" + attr + "<. Already created.");
                    } else {
                        Element newElement = outputDoc.createElement(attr);
                        String val = getEntry().get(newElement.getNodeName());
                        newElement.setTextContent(val);
                        logDebug("elementName is >" + newElement.getNodeName() + "<.");
                        logDebug("nodeTextContent is >" + newElement.getTextContent() + "<.");
                        outputNode = outputNode.appendChild(newElement);
                    }
                } catch (DOMException e) {
                    try {
                        String attr = element.getAttribute("ref");
//                        Element newElement = outputDoc.createElement(attr);
                        logDebug("elementName >" + element.getNodeName() + "< is a ref. NOT SUPPORTED in this release. Ignored.");
//                        outputNode = outputNode.appendChild(newElement);
                    } catch (DOMException eRef) {
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
                    list.item(i).getNodeName() + "<.");
            recurse(list.item(i), outputNode, outputDoc);
        }
    }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getXsdFile() { return this.xsdFile; }
    public void setXsdFile(String xsdFile) { this.xsdFile = xsdFile; }

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
}