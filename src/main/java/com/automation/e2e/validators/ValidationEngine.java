package com.automation.e2e.validators;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

public class ValidationEngine {

    /**
     * Deep validates two JSON structures dynamically.
     * * @param expectedJson The reference/golden master JSON string.
     * @param actualJson   The JSON string extracted from the API or Database.
     * @param strictMode   Set to true for absolute alignment (including array order).
     * Set to false for lenient validation (ignores array order & missing properties).
     */
    public static void validateJsonPayloads(String expectedJson, String actualJson, boolean strictMode) {
        try {
            JSONCompareMode mode = strictMode ? JSONCompareMode.STRICT : JSONCompareMode.LENIENT;
            JSONAssert.assertEquals(expectedJson, actualJson, mode);
        } catch (Exception e) {
            throw new AssertionError("Deep JSON Structural Validation Failed: " + e.getMessage());
        }
    }

    /**
     * Confirms whether string data conforms to a structurally valid XML layout.
     * * @param xmlString The string payload to evaluate.
     * @return true if it parses successfully into a DOM Document object, false otherwise.
     */
    public static boolean isValidXml(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc != null;
        } catch (Exception e) {
            return false;
        }
    }
}