package com.automation.e2e.utils;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class XmlUtils {

    /**
     * Reads a static XML template file into a clean String format.
     */
    public static String readXmlTemplate(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException("Could not read XML file template at: " + filePath, e);
        }
    }

    /**
     * Parses an XML String, dynamically updates the text content of a specific tag,
     * and compiles it back into a valid XML String payload format.
     */
    public static String updateXmlTagValue(String xmlString, String tagName, String newValue) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

            NodeList list = doc.getElementsByTagName(tagName);
            if (list.getLength() > 0) {
                list.item(0).setTextContent(newValue);
            }

            // Transform document state context back into structural raw string blocks
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to dynamically modify XML tag payload context", e);
        }
    }
}