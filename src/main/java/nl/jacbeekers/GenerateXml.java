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

    static Object lock = new Object();   // lock to synchronize nrRows
    final String COMPLEX = "xs:complexType";
    final String SIMPLE = "xs:simpleType";
    String target = "tryout";
    String xsdFile = "dummy.xsd";
    int nrRows;
    int partNrFiles = 1;
    int nrFiles = 1;
    int nrElements = 0;
    String rootElement = "root";
    HashMap<String, String> entry = null;
    String oneFilePerRow = "N";
    ArrayList<NodeInfo> elementList = new ArrayList<NodeInfo>();

    private void logVerbose(String msg) {
        logger.trace(msg);
    }

    private void logDebug(String msg) {
        logger.debug(msg);
    }

    private void logWarning(String msg) {
        logger.warn(msg);
    }

    private void logError(String msg) {
        logger.error(msg);
    }

    private void logFatal(String msg) {
        logger.fatal(msg);
    }

    private void failSession(String msg) throws ConversionException {
        throw new ConversionException(msg);
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
            throws ConversionException, TransformerException, ParserConfigurationException, SAXException, IOException, XMLStreamException {

        if ("Y".equals(getOneFilePerRow())) {
            generateOneXmlFilePerRow(data);
        } else {
            generateOneXmlFile(data);
        }

    }

    public void generateOneXmlFilePerRow(String sourceFile)
            throws ConversionException, TransformerException, ParserConfigurationException, SAXException {
        logFatal("Generate one xml file for each line in a source file has not been implemented in "
                + getClass().getName() + ".");
    }

    public void generateOneXmlFilePerRow(ArrayList<HashMap<String, String>> data)
            throws ConversionException  {
        for (HashMap<String, String> entry : data) {
            setNrRows(getNrRows() + 1);
            String currentTarget = getTarget() + "_" + getNrRows() + ".xml";
            Path pathXmlFile = Paths.get(currentTarget);
            try {
                Files.createFile(pathXmlFile);
                setEntry(entry);
                writeDataTo(currentTarget);
            } catch (FileAlreadyExistsException e) {
                failSession("xml file already exists: " + e.toString());
            } catch (IOException ioe) {
                failSession("could not create xml file: " + ioe.toString());
            } catch (TransformerException te) {
                failSession("Transformer exception occurred. Could not write data to terget file: "+ te.toString());
            } catch (ParserConfigurationException pce) {
                failSession(("Parser Configuration exception occurred. could not write data to target file: " + pce.toString()));
            } catch (SAXException se) {
                failSession("SAXException occurred writing data to target file: " +se.toString());
            }
        }
    }

    public String getFormattedCurrentTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = sdf.format(date);

        return formattedTime;
    }

    private XMLStreamWriter initWriter()
            throws IOException, XMLStreamException {
        //xml
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(new FileOutputStream(getTarget() + ".xml"), "UTF-8");
        return writer;
    }

    private void writeHeader(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeComment("Generated by " + getClass() + " on " + getFormattedCurrentTime());
        writer.writeCharacters("\n");
    }

    private void writeInitialComplexElementsStart(XMLStreamWriter writer) throws XMLStreamException {
        boolean stop = false;
        for (NodeInfo node : elementList) {
            if (node.getElementType() != null) {
                switch (node.getElementType()) {
                    case COMPLEX:
                        writer.writeStartElement(node.getAttribute());
                        break;
                    default:
                        stop = true;
                        break;
                }
            }
            if (stop) break;
        }
    }

    private void writeInitialComplexElementsEnd(XMLStreamWriter writer)
            throws XMLStreamException {
        boolean stop = false;
        boolean wroteEndTime = false;
        for (NodeInfo node : elementList) {
            if (node.getElementType() != null) {
                switch (node.getElementType()) {
                    case COMPLEX:
                        if (! wroteEndTime) {
                            writer.writeCharacters("\n");
                            writer.writeComment("Generated by " + getClass() + " completed at " + getFormattedCurrentTime());
                            wroteEndTime = true;
                        }

                        writer.writeEndElement();
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

    public void generateOneXmlFile(ArrayList<HashMap<String, String>> data)
            throws ConversionException, IOException {
        XMLStreamWriter writer = null;
        try {
            writer = initWriter();
            writeHeader(writer);
            try {
                getXsdStructure();
                outInfoElementList();
                writeInitialComplexElementsStart(writer);
            } catch(ParserConfigurationException pce) {
                failSession("ParserConfigurationException occurred geeting XSD structure: " +pce.toString());
            } catch(SAXException se) {
                failSession("SAXException occurred getting XSD structure: " +se.toString());
            }
        } catch (XMLStreamException e) {
            failSession("XMLStreamException occurred. Could not write initial elements to target xml file: " + e.toString());
        }

        int nrMap = 0;
        for (HashMap<String, String> map : data) {
            nrMap++;
            logDebug("Processing array entry >" + nrMap + "<.");
            String[] currentElements = map.values().toArray(new String[0]);
            try {
                writeDataElements(currentElements, writer);
            } catch (XMLStreamException e) {
                failSession("XMLStreamException occurred. Could not write data elements: "+e.toString());
            }
        }
        try {
            writeInitialComplexElementsEnd(writer);
            writer.writeEndDocument();
            writer.close();
        } catch (XMLStreamException e) {
            failSession("XMLStreamException occurred. Could not write final closing elements: " + e.toString());
        }
    }

    public void generateOneXmlFile(String sourceFile)
            throws IOException, XMLStreamException, ParserConfigurationException, SAXException {

        XMLStreamWriter writer = initWriter();
        writeHeader(writer);
        getXsdStructure();
        outInfoElementList();
        writeInitialComplexElementsStart(writer);

        processSourceFile(sourceFile, writer);

        writeInitialComplexElementsEnd(writer);
        writer.writeEndDocument();
        writer.close();
    }

    private void processSourceFile(String sourceFile, XMLStreamWriter writer)
            throws IOException, XMLStreamException {
        //input
        File file = new File(sourceFile);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int nrLines = 0;
        while ((line = reader.readLine()) != null) {
            nrLines++;
            String[] values = line.split(";");
            logVerbose("line >" + nrLines + "< contains >" + values.length + "< values.");
            writeDataElements(values, writer);
        }
    }

    private void writeDataElements(String[] values, XMLStreamWriter writer)
            throws XMLStreamException {
        if (values.length == 1)
            logVerbose("Line contains >" + values.length + "< field value.");
        else
            logVerbose("Line contains >" + values.length + "< field values.");
        writer.writeCharacters("\n");
//            writer.writeComment("line >" +nrLines +"<.");
//            writer.writeCharacters("\n");
        int i = 0;
        for (NodeInfo node : elementList) {
            if (node.getElementType() != null) {
                switch (node.getElementType()) {
                    case COMPLEX:
                        break;
                    case SIMPLE:
                        writer.writeStartElement(node.getAttribute());
//                            writer.writeComment("some data here");
                        if (i < values.length) {
                            writer.writeCharacters(values[i]);
                            i++;
                        } else {
                            //more xml elements than data elements
                            logVerbose("There are more xml elements than data fields.");
                        }

                        writer.writeEndElement();
                        break;
                    default:
                        logWarning("Ignored invalid or unsupported elementType >" + node.getElementType() + "<.");
                        break;
                }
            }
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

    public void writeDataTo(String xmlFile)
            throws ParserConfigurationException, IOException, TransformerException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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

    }

    public void getXsdStructure()
            throws ParserConfigurationException, IOException, SAXException {
        //xsd
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new FileReader(
                getXsdFile())));
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setAttribute("root");
        nodeInfo.setElementType("root");

        getXsdStructure(document.getDocumentElement(), nodeInfo);


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
                        if (COMPLEX.equals(list.item(i).getNodeName())) {
                            newNodeInfo.setElementType(COMPLEX);
                        }
                    }
                    //assume
                    if (newNodeInfo.getElementType() == null) {
                        newNodeInfo.setElementType(SIMPLE);
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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getXsdFile() {
        return this.xsdFile;
    }

    public void setXsdFile(String xsdFile) {
        this.xsdFile = xsdFile;
    }


    public int getNrRows() {
        return nrRows;
    }

    public void setNrRows(int nrRows) {
        this.nrRows = nrRows;
    }

    public String getRootElement() {
        return this.rootElement;
    }

    public void setRootElement(String rootElement) {
        this.rootElement = rootElement;
    }

    public String getOneFilePerRow() { return oneFilePerRow; }
    public void setOneFilePerRow(String oneFilePerRow) { this.oneFilePerRow = oneFilePerRow; }

    private HashMap<String, String> getEntry() {
        return this.entry;
    }

    private void setEntry(HashMap<String, String> entry) {
        this.entry = entry;
    }

    public int getPartNrFiles() {
        return this.partNrFiles;
    }

    private void setPartNrFiles(int partNrFiles) {
        this.partNrFiles = partNrFiles;
    }

    public int getNrFiles() {
        return this.nrFiles;
    }

    private void setNrFiles(int nrFiles) {
        this.nrFiles = nrFiles;
    }

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