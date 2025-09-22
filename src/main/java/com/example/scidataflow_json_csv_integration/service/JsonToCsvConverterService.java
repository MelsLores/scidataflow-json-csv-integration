package com.example.scidataflow_json_csv_integration.service;

import com.example.scidataflow_json_csv_integration.exception.CsvWritingException;
import com.example.scidataflow_json_csv_integration.exception.JsonProcessingException;
import com.example.scidataflow_json_csv_integration.model.Person;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Service class that orchestrates the JSON to CSV conversion process.
 * This service integrates JsonReaderService and CsvWriterService to provide
 * a complete workflow for converting JSON files to CSV format with configurable options.
 * 
 * @author Melany Rivera
 * @version 1.0
 * @since 21/09/2025
 */
@Slf4j
@Service
public class JsonToCsvConverterService {

    private final JsonReaderService jsonReaderService;
    private final CsvWriterService csvWriterService;

    /**
     * Constructs a JsonToCsvConverterService with the required dependencies.
     * Initializes the service with JSON reader and CSV writer components.
     * 
     * @param jsonReaderService service for reading JSON files
     * @param csvWriterService service for writing CSV files
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public JsonToCsvConverterService(JsonReaderService jsonReaderService, CsvWriterService csvWriterService) {
        this.jsonReaderService = jsonReaderService;
        this.csvWriterService = csvWriterService;
    }

    /**
     * Converts a JSON file containing person data to CSV format.
     * This method orchestrates the entire conversion process by reading the JSON file,
     * parsing the data into Person objects, and writing them to a CSV file using default delimiters.
     * 
     * @param jsonFilePath the path to the input JSON file
     * @param csvFilePath the path where the output CSV file will be created
     * @return the number of persons successfully converted
     * @throws JsonProcessingException if the JSON file cannot be read or parsed
     * @throws CsvWritingException if the CSV file cannot be written
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public int convertJsonToCsv(String jsonFilePath, String csvFilePath) 
            throws JsonProcessingException, CsvWritingException {
        
        log.info("Starting JSON to CSV conversion. Input: {}, Output: {}", jsonFilePath, csvFilePath);
        
        try {
            // Validate input parameters
            validateConversionParameters(jsonFilePath, csvFilePath);
            
            // Read persons from JSON file
            log.debug("Reading persons from JSON file: {}", jsonFilePath);
            List<Person> persons = jsonReaderService.readPersonsFromJson(jsonFilePath);
            
            if (persons.isEmpty()) {
                log.warn("No persons found in JSON file: {}", jsonFilePath);
                return 0;
            }
            
            log.debug("Successfully read {} persons from JSON file", persons.size());
            
            // Write persons to CSV file
            log.debug("Writing persons to CSV file: {}", csvFilePath);
            csvWriterService.writePersonsToCsv(persons, csvFilePath);
            
            log.info("Successfully converted {} persons from JSON to CSV. Output file: {}", 
                    persons.size(), csvFilePath);
            
            return persons.size();
            
        } catch (JsonProcessingException e) {
            log.error("Failed to read or parse JSON file during conversion", e);
            throw e;
        } catch (CsvWritingException e) {
            log.error("Failed to write CSV file during conversion", e);
            throw e;
        } catch (Exception e) {
            String errorMessage = "Unexpected error during JSON to CSV conversion: " + e.getMessage();
            log.error(errorMessage, e);
            throw new JsonProcessingException(errorMessage, e);
        }
    }

    /**
     * Converts a JSON file containing person data to CSV format with custom delimiter.
     * This method provides more control over the CSV output format.
     * 
    /**
     * Converts a JSON file containing person data to CSV format with custom formatting options.
     * This method allows full control over CSV formatting by specifying delimiter, quote, and escape characters.
     * 
     * @param jsonFilePath the path to the input JSON file
     * @param csvFilePath the path where the output CSV file will be created
     * @param delimiter the character to use as field delimiter in CSV
     * @param quoteChar the character to use for quoting fields in CSV
     * @param escapeChar the character to use for escaping special characters in CSV
     * @return the number of persons successfully converted
     * @throws JsonProcessingException if the JSON file cannot be read or parsed
     * @throws CsvWritingException if the CSV file cannot be written
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public int convertJsonToCsv(String jsonFilePath, String csvFilePath, char delimiter, char quoteChar, char escapeChar) 
            throws JsonProcessingException, CsvWritingException {
        
        log.info("Starting JSON to CSV conversion with custom format. Input: {}, Output: {}, Delimiter: '{}'", 
                jsonFilePath, csvFilePath, delimiter);
        
        try {
            // Validate input parameters
            validateConversionParameters(jsonFilePath, csvFilePath);
            
            // Read persons from JSON file
            log.debug("Reading persons from JSON file: {}", jsonFilePath);
            List<Person> persons = jsonReaderService.readPersonsFromJson(jsonFilePath);
            
            if (persons.isEmpty()) {
                log.warn("No persons found in JSON file: {}", jsonFilePath);
                return 0;
            }
            
            log.debug("Successfully read {} persons from JSON file", persons.size());
            
            // Write persons to CSV file with custom format
            log.debug("Writing persons to CSV file with custom format: {}", csvFilePath);
            csvWriterService.writePersonsToCsv(persons, csvFilePath, delimiter, quoteChar, escapeChar);
            
            log.info("Successfully converted {} persons from JSON to CSV with custom format. Output file: {}", 
                    persons.size(), csvFilePath);
            
            return persons.size();
            
        } catch (JsonProcessingException e) {
            log.error("Failed to read or parse JSON file during conversion", e);
            throw e;
        } catch (CsvWritingException e) {
            log.error("Failed to write CSV file during conversion", e);
            throw e;
        } catch (Exception e) {
            String errorMessage = "Unexpected error during JSON to CSV conversion: " + e.getMessage();
            log.error(errorMessage, e);
            throw new JsonProcessingException(errorMessage, e);
        }
    }

    /**
     * Converts multiple JSON files to a single CSV file.
     * This method reads multiple JSON files and combines their content into one CSV file.
     * 
     * @param jsonFilePaths array of paths to the input JSON files
     * @param csvFilePath the path where the output CSV file will be created
     * @return the total number of persons successfully converted from all files
     * @throws JsonProcessingException if any JSON file cannot be read or parsed
     * @throws CsvWritingException if the CSV file cannot be written
     * 
     * Ejemplo de uso:
     * <pre>
     * JsonToCsvConverterService converter = new JsonToCsvConverterService(jsonReader, csvWriter);
     * String[] jsonFiles = {"input/persons1.json", "input/persons2.json"};
     * try {
     *     int totalCount = converter.convertMultipleJsonToCsv(jsonFiles, "output/combined.csv");
     *     System.out.println("Converted " + totalCount + " persons from multiple files");
     * } catch (JsonProcessingException | CsvWritingException e) {
     *     // Handle the exception
     * }
     * </pre>
     */
    public int convertMultipleJsonToCsv(String[] jsonFilePaths, String csvFilePath) 
            throws JsonProcessingException, CsvWritingException {
        
        log.info("Starting multiple JSON to CSV conversion. {} input files, Output: {}", 
                jsonFilePaths != null ? jsonFilePaths.length : 0, csvFilePath);
        
        if (jsonFilePaths == null || jsonFilePaths.length == 0) {
            throw new JsonProcessingException("JSON file paths array cannot be null or empty");
        }
        
        if (csvFilePath == null || csvFilePath.trim().isEmpty()) {
            throw new CsvWritingException("CSV file path cannot be null or empty");
        }
        
        int totalConverted = 0;
        boolean isFirstFile = true;
        
        try {
            for (String jsonFilePath : jsonFilePaths) {
                log.debug("Processing JSON file: {}", jsonFilePath);
                
                // Read persons from current JSON file
                List<Person> persons = jsonReaderService.readPersonsFromJson(jsonFilePath);
                
                if (persons.isEmpty()) {
                    log.warn("No persons found in JSON file: {}", jsonFilePath);
                    continue;
                }
                
                // Write or append persons to CSV file
                if (isFirstFile) {
                    csvWriterService.writePersonsToCsv(persons, csvFilePath);
                    isFirstFile = false;
                } else {
                    csvWriterService.appendPersonsToCsv(persons, csvFilePath);
                }
                
                totalConverted += persons.size();
                log.debug("Added {} persons from file: {}", persons.size(), jsonFilePath);
            }
            
            log.info("Successfully converted {} total persons from {} JSON files to CSV. Output file: {}", 
                    totalConverted, jsonFilePaths.length, csvFilePath);
            
            return totalConverted;
            
        } catch (JsonProcessingException e) {
            log.error("Failed to read or parse JSON files during multiple file conversion", e);
            throw e;
        } catch (CsvWritingException e) {
            log.error("Failed to write CSV file during multiple file conversion", e);
            throw e;
        } catch (Exception e) {
            String errorMessage = "Unexpected error during multiple JSON to CSV conversion: " + e.getMessage();
            log.error(errorMessage, e);
            throw new JsonProcessingException(errorMessage, e);
        }
    }

    /**
     * Validates the conversion parameters.
     * 
     * @param jsonFilePath the JSON file path to validate
     * @param csvFilePath the CSV file path to validate
     * @throws JsonProcessingException if validation fails
     */
    private void validateConversionParameters(String jsonFilePath, String csvFilePath) throws JsonProcessingException {
        if (jsonFilePath == null || jsonFilePath.trim().isEmpty()) {
            throw new JsonProcessingException("JSON file path cannot be null or empty");
        }
        
        if (csvFilePath == null || csvFilePath.trim().isEmpty()) {
            throw new JsonProcessingException("CSV file path cannot be null or empty");
        }
        
        // Check if JSON file is readable
        if (!jsonReaderService.isValidJsonFile(jsonFilePath)) {
            throw new JsonProcessingException("JSON file is not readable or does not exist: " + jsonFilePath);
        }
        
        // Check if CSV file path is writable
        if (!csvWriterService.isPathWritable(csvFilePath)) {
            throw new JsonProcessingException("CSV file path is not writable: " + csvFilePath);
        }
    }

    /**
     * Gets conversion statistics for a JSON file.
     * This utility method provides information about a JSON file before conversion.
     * 
     * @param jsonFilePath the path to the JSON file to analyze
     * @return a string containing statistics about the JSON file
     * @throws JsonProcessingException if the JSON file cannot be read or parsed
     */
    public String getConversionStatistics(String jsonFilePath) throws JsonProcessingException {
        log.debug("Getting conversion statistics for file: {}", jsonFilePath);
        
        try {
            List<Person> persons = jsonReaderService.readPersonsFromJson(jsonFilePath);
            
            long uniqueDepartments = persons.stream()
                    .map(Person::getDepartment)
                    .filter(dept -> dept != null && !dept.trim().isEmpty())
                    .distinct()
                    .count();
            
            double averageAge = persons.stream()
                    .mapToInt(person -> person.getAge() != null ? person.getAge() : 0)
                    .average()
                    .orElse(0.0);
            
            double averageSalary = persons.stream()
                    .mapToDouble(person -> person.getSalary() != null ? person.getSalary() : 0.0)
                    .average()
                    .orElse(0.0);
            
            return String.format(
                "File: %s%n" +
                "Total Persons: %d%n" +
                "Unique Departments: %d%n" +
                "Average Age: %.2f%n" +
                "Average Salary: %.2f",
                jsonFilePath, persons.size(), uniqueDepartments, averageAge, averageSalary);
                
        } catch (JsonProcessingException e) {
            log.error("Failed to get conversion statistics", e);
            throw e;
        }
    }
}