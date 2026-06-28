package com.automation.e2e.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Reads a static JSON template file into a clean String format.
     */
    public static String readJsonTemplate(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException("Could not read JSON file template at: " + filePath, e);
        }
    }

    /**
     * Dynamically updates or inserts a key-value attribute inside a JSON string structurally.
     */
    public static String updateJsonValue(String jsonString, String key, String value) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            if (rootNode.isObject()) {
                ((ObjectNode) rootNode).put(key, value);
            }
            return objectMapper.writeValueAsString(rootNode);
        } catch (IOException e) {
            throw new RuntimeException("Failed to dynamically modify JSON payload structure", e);
        }
    }

    /**
     * Converts any Java Object directly into a minified JSON String.
     */
    public static String convertObjectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize Object to JSON payload", e);
        }
    }
}