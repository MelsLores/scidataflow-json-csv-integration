package com.example.scidataflow_json_csv_integration.service;

import com.example.scidataflow_json_csv_integration.exception.JsonProcessingException;
import com.example.scidataflow_json_csv_integration.model.Person;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para leer archivos JSON y convertirlos en objetos Java.
 * Proporciona funcionalidad para parsear diferentes tipos de archivos JSON,
 * incluyendo arrays de personas, objetos individuales y reportes científicos.
 * Ahora utiliza el sistema de ordenamiento inteligente de DataTransformService.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
@Slf4j
@Service
public class JsonReaderService {

    private final ObjectMapper objectMapper;
    private final DataTransformService dataTransformService;

    /**
     * Constructor that initializes JsonReaderService with a configured ObjectMapper
     * and DataTransformService for intelligent data transformation.
     * 
     * @param dataTransformService service for intelligent data transformation
     * @author Melany Rivera
     * @since 25/09/2025
     */
    public JsonReaderService(DataTransformService dataTransformService) {
        this.objectMapper = new ObjectMapper();
        this.dataTransformService = dataTransformService;
    }

    /**
     * Reads a JSON file and parses it into a list of Person objects using intelligent transformation.
     * This method uses the DataTransformService for universal data recognition and intelligent sorting.
     * Supports any JSON structure including mixed data types, arrays, objects, and strings.
     *
     * @param filePath the path to the JSON file to read
     * @return a list of Person objects parsed from the JSON file
     * @throws JsonProcessingException if the file cannot be read or parsed
     * @author Melany Rivera
     * @since 25/09/2025
     */
    public List<Person> readPersonsFromJson(String filePath) throws JsonProcessingException {
        log.info("Starting to read JSON file with intelligent transformation: {}", filePath);
        
        try {
            if (filePath == null || filePath.trim().isEmpty()) {
                throw new JsonProcessingException("File path cannot be null or empty");
            }
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new JsonProcessingException("File does not exist: " + filePath);
            }
            
            if (Files.isDirectory(path)) {
                throw new JsonProcessingException("Path is a directory, not a file: " + filePath);
            }
            
            if (!Files.isReadable(path)) {
                throw new JsonProcessingException("File is not readable: " + filePath);
            }
            
            if (Files.size(path) == 0) {
                throw new JsonProcessingException("File is empty: " + filePath);
            }

            // Read JSON as generic objects for universal processing
            JsonNode rootNode = objectMapper.readTree(new File(filePath));
            
            if (rootNode == null || rootNode.isNull()) {
                throw new JsonProcessingException("JSON file contains null content: " + filePath);
            }

            // Convert JsonNode to generic objects for DataTransformService processing
            List<Object> rawObjects = new ArrayList<>();
            
            if (rootNode.isArray()) {
                // Handle array of mixed objects
                for (JsonNode node : rootNode) {
                    Object obj = convertJsonNodeToObject(node);
                    if (obj != null) {
                        rawObjects.add(obj);
                    }
                }
                log.debug("Parsed {} objects from JSON array", rawObjects.size());
            } else {
                // Handle single object
                Object singleObj = convertJsonNodeToObject(rootNode);
                if (singleObj != null) {
                    rawObjects.add(singleObj);
                }
                log.debug("Parsed single object from JSON");
            }
            
            if (rawObjects.isEmpty()) {
                throw new JsonProcessingException("No valid data found in JSON file: " + filePath);
            }

            // Use DataTransformService for intelligent transformation and sorting
            String sourceType = determineSourceType(filePath);
            List<Person> transformedPersons = dataTransformService.transformToPersons(rawObjects, sourceType);
            
            if (transformedPersons == null || transformedPersons.isEmpty()) {
                throw new JsonProcessingException("Failed to transform JSON data to Person objects: " + filePath);
            }

            log.info("Successfully parsed and intelligently transformed {} objects to Person format from: {}", 
                    transformedPersons.size(), filePath);
            return transformedPersons;
            
        } catch (IOException e) {
            String errorMessage = "Failed to read JSON file: " + filePath + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new JsonProcessingException(errorMessage, e);
        } catch (JsonProcessingException e) {
            // Re-throw JsonProcessingException as-is
            throw e;
        } catch (Exception e) {
            String errorMessage = "Unexpected error while processing JSON file: " + filePath + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new JsonProcessingException(errorMessage, e);
        }
    }

    /**
     * Converts a JsonNode to a generic Object for processing by DataTransformService.
     * 
     * @param node the JsonNode to convert
     * @return converted Object or null if conversion fails
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private Object convertJsonNodeToObject(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        
        try {
            if (node.isTextual()) {
                return node.asText();
            } else if (node.isObject()) {
                return objectMapper.convertValue(node, Object.class);
            } else if (node.isArray()) {
                List<Object> list = new ArrayList<>();
                for (JsonNode arrayNode : node) {
                    Object item = convertJsonNodeToObject(arrayNode);
                    if (item != null) {
                        list.add(item);
                    }
                }
                return list;
            } else {
                return objectMapper.convertValue(node, Object.class);
            }
        } catch (Exception e) {
            log.warn("Failed to convert JsonNode to Object: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Determines the source type based on file name for logging purposes.
     * 
     * @param filePath the file path to analyze
     * @return source type string
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private String determineSourceType(String filePath) {
        if (filePath == null) return "unknown";
        
        String fileName = filePath.toLowerCase();
        if (fileName.contains("person") || fileName.contains("employee")) {
            return "person-data";
        } else if (fileName.contains("publication") || fileName.contains("scientometric")) {
            return "publication-data";
        } else if (fileName.contains("medical") || fileName.contains("patient")) {
            return "medical-data";
        } else if (fileName.contains("product") || fileName.contains("inventory")) {
            return "product-data";
        } else if (fileName.contains("student") || fileName.contains("university")) {
            return "student-data";
        } else if (fileName.contains("mixed") || fileName.contains("disordered")) {
            return "mixed-data";
        } else {
            return "generic-data";
        }
    }    /**
     * Reads a JSON file and returns the first person found.
     * This method is useful when expecting to process a single Person object.
     * 
     * @param filePath the path of the JSON file to read
     * @return the first Person object found in the JSON file
     * @throws JsonProcessingException if the file cannot be read, parsed, or contains no persons
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public Person readPersonFromJson(String filePath) throws JsonProcessingException {
        List<Person> persons = readPersonsFromJson(filePath);
        if (persons.isEmpty()) {
            throw new JsonProcessingException("No person found in JSON file: " + filePath);
        }
        return persons.get(0); // Return the first person
    }

    /**
     * Validates if a JSON file is valid and accessible for reading.
     * Checks file existence, read permissions, and that it is a regular file.
     * 
     * @param filePath the path of the JSON file to validate
     * @return true if the file is valid and readable, false otherwise
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public boolean isValidJsonFile(String filePath) {
        try {
            if (filePath == null || filePath.trim().isEmpty()) {
                return false;
            }
            
            Path path = Paths.get(filePath);
            return Files.exists(path) && Files.isReadable(path) && Files.isRegularFile(path);
        } catch (Exception e) {
            log.warn("Error validating JSON file path: {}", filePath, e);
            return false;
        }
    }
}
