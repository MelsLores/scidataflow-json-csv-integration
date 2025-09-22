package com.example.scidataflow_json_csv_integration.service;

import com.example.scidataflow_json_csv_integration.exception.CsvWritingException;
import com.example.scidataflow_json_csv_integration.model.Person;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service for writing CSV files from Person data.
 * Provides functionality to generate CSV content with different delimiters
 * and configuration options, including comprehensive error handling.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
@Slf4j
@Service
public class CsvWriterService {

    /**
     * Default CSV delimiter
     */
    private static final char DEFAULT_DELIMITER = ',';
    
    /**
     * Default CSV quote character
     */
    private static final char DEFAULT_QUOTE_CHAR = '"';
    
    /**
     * Default CSV escape character
     */
    private static final char DEFAULT_ESCAPE_CHAR = '\\';

    /**
     * Writes a list of Person objects to a CSV file using default configuration.
     * Creates a CSV file with comma delimiters and standard formatting.
     * 
     * @param persons the list of Person objects to write to CSV
     * @param filePath the path where the CSV file will be created
     * @throws CsvWritingException if the file cannot be written or created
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public void writePersonsToCsv(List<Person> persons, String filePath) throws CsvWritingException {
        writePersonsToCsv(persons, filePath, DEFAULT_DELIMITER, DEFAULT_QUOTE_CHAR, DEFAULT_ESCAPE_CHAR);
    }

    /**
     * Writes a list of Person objects to a CSV file with custom configuration.
     * Allows customization of CSV format including delimiter, quote character, and escape character.
     * 
     * @param persons the list of Person objects to write to CSV
     * @param filePath the path where the CSV file will be created
     * @param delimiter the character to use as field delimiter
     * @param quoteChar the character to use for quoting fields
     * @param escapeChar the character to use for escaping special characters
     * @throws CsvWritingException if the file cannot be written or created
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public void writePersonsToCsv(List<Person> persons, String filePath, char delimiter, char quoteChar, char escapeChar) 
            throws CsvWritingException {
        
        log.info("Starting to write {} persons to CSV file: {}", persons != null ? persons.size() : 0, filePath);
        
        // Validate input parameters
        validateInput(persons, filePath);
        
        CSVWriter csvWriter = null;
        FileWriter fileWriter = null;
        
        try {
            // Ensure parent directory exists
            ensureDirectoryExists(filePath);
            
            // Create file writer and CSV writer
            fileWriter = new FileWriter(filePath);
            csvWriter = new CSVWriter(fileWriter, delimiter, quoteChar, escapeChar, CSVWriter.DEFAULT_LINE_END);
            
            // Write CSV header
            writeHeader(csvWriter);
            
            // Write person data
            writePersonData(csvWriter, persons);
            
            log.info("Successfully wrote {} persons to CSV file: {}", persons.size(), filePath);
            
        } catch (IOException e) {
            String errorMessage = "Failed to write CSV file: " + filePath + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new CsvWritingException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Unexpected error while writing CSV file: " + filePath + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new CsvWritingException(errorMessage, e);
        } finally {
            // Close resources safely
            closeResources(csvWriter, fileWriter);
        }
    }

    /**
     * Writes an individual Person object to a CSV file.
     * Useful when you need to write individual person records.
     * 
     * @param person the Person object to write to CSV
     * @param filePath the path where the CSV file will be created
     * @throws CsvWritingException if the file cannot be written or created
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public void writePersonToCsv(Person person, String filePath) throws CsvWritingException {
        if (person == null) {
            throw new CsvWritingException("Person object cannot be null");
        }
        
        writePersonsToCsv(List.of(person), filePath);
    }

    /**
     * Appends a list of Person objects to an existing CSV file.
     * Adds new records to an existing CSV file without overwriting existing data.
     * 
     * @param persons the list of Person objects to append to the CSV
     * @param filePath the path to the existing CSV file
     * @throws CsvWritingException if the file cannot be written or accessed
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public void appendPersonsToCsv(List<Person> persons, String filePath) throws CsvWritingException {
        log.info("Starting to append {} persons to CSV file: {}", persons != null ? persons.size() : 0, filePath);
        
        // Validate input parameters
        validateInput(persons, filePath);
        
        CSVWriter csvWriter = null;
        FileWriter fileWriter = null;
        
        try {
            // Ensure parent directory exists
            ensureDirectoryExists(filePath);
            
            // Check if file exists to determine if header is needed
            boolean fileExists = Files.exists(Paths.get(filePath));
            
            // Create file writer in append mode and CSV writer
            fileWriter = new FileWriter(filePath, true);
            csvWriter = new CSVWriter(fileWriter, DEFAULT_DELIMITER, DEFAULT_QUOTE_CHAR, DEFAULT_ESCAPE_CHAR, CSVWriter.DEFAULT_LINE_END);
            
            // Write header only if file doesn't exist
            if (!fileExists) {
                writeHeader(csvWriter);
            }
            
            // Write person data
            writePersonData(csvWriter, persons);
            
            log.info("Successfully appended {} persons to CSV file: {}", persons.size(), filePath);
            
        } catch (IOException e) {
            String errorMessage = "Failed to append to CSV file: " + filePath + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new CsvWritingException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Unexpected error while appending to CSV file: " + filePath + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new CsvWritingException(errorMessage, e);
        } finally {
            // Close resources safely
            closeResources(csvWriter, fileWriter);
        }
    }

    /**
     * Validates input parameters for CSV writing operations.
     * 
     * @param persons the list of Person objects to validate
     * @param filePath the file path to validate
     * @throws CsvWritingException if validation fails
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private void validateInput(List<Person> persons, String filePath) throws CsvWritingException {
        if (persons == null) {
            throw new CsvWritingException("Persons list cannot be null");
        }
        
        if (persons.isEmpty()) {
            throw new CsvWritingException("Persons list cannot be empty");
        }
        
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new CsvWritingException("File path cannot be null or empty");
        }
        
        // Validate that the file path has a .csv extension
        if (!filePath.toLowerCase().endsWith(".csv")) {
            log.warn("File path does not have .csv extension: {}", filePath);
        }
    }

    /**
     * Ensures the parent directory of the file path exists.
     * 
     * @param filePath the file path whose parent directory must be created
     * @throws CsvWritingException if directory creation fails
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private void ensureDirectoryExists(String filePath) throws CsvWritingException {
        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();
            
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                log.debug("Created parent directories for file: {}", filePath);
            }
        } catch (IOException e) {
            throw new CsvWritingException("Failed to create parent directories for file: " + filePath, e);
        }
    }

    /**
     * Writes the CSV header row.
     * 
     * @param csvWriter the CSVWriter instance for writing
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private void writeHeader(CSVWriter csvWriter) {
        String[] header = {"ID", "First Name", "Last Name", "Email", "Age", "Department", "Salary"};
        csvWriter.writeNext(header);
        log.debug("CSV header written successfully");
    }

    /**
     * Writes person data to the CSV file.
     * 
     * @param csvWriter the CSVWriter instance for writing
     * @param persons the list of Person objects to write
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private void writePersonData(CSVWriter csvWriter, List<Person> persons) {
        for (Person person : persons) {
            String[] row = convertPersonToStringArray(person);
            csvWriter.writeNext(row);
        }
        log.debug("Person data written successfully");
    }

    /**
     * Converts a Person object to a string array for CSV writing.
     * 
     * @param person the Person object to convert
     * @return a string array containing the person's data
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private String[] convertPersonToStringArray(Person person) {
        return new String[] {
            person.getId() != null ? person.getId().toString() : "",
            person.getFirstName() != null ? person.getFirstName() : "",
            person.getLastName() != null ? person.getLastName() : "",
            person.getEmail() != null ? person.getEmail() : "",
            person.getAge() != null ? person.getAge().toString() : "",
            person.getDepartment() != null ? person.getDepartment() : "",
            person.getSalary() != null ? person.getSalary().toString() : ""
        };
    }

    /**
     * Safely closes CSV writer and file writer resources.
     * 
     * @param csvWriter the CSVWriter to close
     * @param fileWriter the FileWriter to close
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private void closeResources(CSVWriter csvWriter, FileWriter fileWriter) {
        try {
            if (csvWriter != null) {
                csvWriter.close();
            }
        } catch (IOException e) {
            log.error("Error closing CSV writer", e);
        }
        
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (IOException e) {
            log.error("Error closing file writer", e);
        }
    }

    /**
     * Verifies if a file path is valid for writing.
     * This utility method can be used to validate file paths before attempting to write.
     * 
     * @param filePath the file path to validate
     * @return true if the path is valid for writing, false otherwise
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public boolean isPathWritable(String filePath) {
        try {
            if (filePath == null || filePath.trim().isEmpty()) {
                return false;
            }
            
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();
            
            // Check if parent directory exists or can be created
            if (parentDir != null) {
                return Files.isWritable(parentDir) || !Files.exists(parentDir);
            }
            
            return true;
        } catch (Exception e) {
            log.warn("Error checking path writability for path: {}", filePath, e);
            return false;
        }
    }
}