package com.example.scidataflow_json_csv_integration.service;

import com.example.scidataflow_json_csv_integration.exception.CsvWritingException;
import com.example.scidataflow_json_csv_integration.exception.JsonProcessingException;
import com.example.scidataflow_json_csv_integration.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JsonToCsvConverterService.
 * These tests validate the integration functionality between JSON reading and CSV writing
 * to ensure correct operation before integration into the general workflow.
 * 
 * @author Digital NAO Team
 * @version 1.0
 * @since 2025-09-21
 */
class JsonToCsvConverterServiceTest {

    private JsonToCsvConverterService converterService;

    @Mock
    private JsonReaderService jsonReaderService;

    @Mock
    private CsvWriterService csvWriterService;

    @TempDir
    Path tempDir;

    /**
     * Set up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converterService = new JsonToCsvConverterService(jsonReaderService, csvWriterService);
    }

    /**
     * Test successful JSON to CSV conversion.
     */
    @Test
    void testConvertJsonToCsv_ValidFiles_ShouldReturnPersonCount() throws Exception {
        // Arrange
        String jsonFilePath = "input.json";
        String csvFilePath = "output.csv";
        
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0),
            new Person(2L, "Jane", "Smith", "jane.smith@example.com", 28, "Marketing", 65000.0)
        );
        
        when(jsonReaderService.isValidJsonFile(jsonFilePath)).thenReturn(true);
        when(csvWriterService.isPathWritable(csvFilePath)).thenReturn(true);
        when(jsonReaderService.readPersonsFromJson(jsonFilePath)).thenReturn(persons);

        // Act
        int result = converterService.convertJsonToCsv(jsonFilePath, csvFilePath);

        // Assert
        assertEquals(2, result);
        verify(jsonReaderService).readPersonsFromJson(jsonFilePath);
        verify(csvWriterService).writePersonsToCsv(persons, csvFilePath);
    }

    /**
     * Test JSON to CSV conversion with custom delimiter.
     */
    @Test
    void testConvertJsonToCsv_WithCustomDelimiter_ShouldUseCustomFormat() throws Exception {
        // Arrange
        String jsonFilePath = "input.json";
        String csvFilePath = "output.csv";
        char delimiter = ';';
        char quoteChar = '\'';
        char escapeChar = '\\';
        
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );
        
        when(jsonReaderService.isValidJsonFile(jsonFilePath)).thenReturn(true);
        when(csvWriterService.isPathWritable(csvFilePath)).thenReturn(true);
        when(jsonReaderService.readPersonsFromJson(jsonFilePath)).thenReturn(persons);

        // Act
        int result = converterService.convertJsonToCsv(jsonFilePath, csvFilePath, delimiter, quoteChar, escapeChar);

        // Assert
        assertEquals(1, result);
        verify(jsonReaderService).readPersonsFromJson(jsonFilePath);
        verify(csvWriterService).writePersonsToCsv(persons, csvFilePath, delimiter, quoteChar, escapeChar);
    }

    /**
     * Test conversion with empty JSON file should return 0.
     */
    @Test
    void testConvertJsonToCsv_EmptyJsonFile_ShouldReturnZero() throws Exception {
        // Arrange
        String jsonFilePath = "empty.json";
        String csvFilePath = "output.csv";
        
        List<Person> emptyList = Arrays.asList();
        
        when(jsonReaderService.isValidJsonFile(jsonFilePath)).thenReturn(true);
        when(csvWriterService.isPathWritable(csvFilePath)).thenReturn(true);
        when(jsonReaderService.readPersonsFromJson(jsonFilePath)).thenReturn(emptyList);

        // Act
        int result = converterService.convertJsonToCsv(jsonFilePath, csvFilePath);

        // Assert
        assertEquals(0, result);
        verify(jsonReaderService).readPersonsFromJson(jsonFilePath);
        verify(csvWriterService, never()).writePersonsToCsv(any(), any());
    }

    /**
     * Test conversion with null JSON file path should throw exception.
     */
    @Test
    void testConvertJsonToCsv_NullJsonFilePath_ShouldThrowException() {
        // Arrange
        String csvFilePath = "output.csv";

        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertJsonToCsv(null, csvFilePath));
        
        assertTrue(exception.getMessage().contains("JSON file path cannot be null or empty"));
    }

    /**
     * Test conversion with null CSV file path should throw exception.
     */
    @Test
    void testConvertJsonToCsv_NullCsvFilePath_ShouldThrowException() {
        // Arrange
        String jsonFilePath = "input.json";

        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertJsonToCsv(jsonFilePath, null));
        
        assertTrue(exception.getMessage().contains("CSV file path cannot be null or empty"));
    }

    /**
     * Test conversion with unreadable JSON file should throw exception.
     */
    @Test
    void testConvertJsonToCsv_UnreadableJsonFile_ShouldThrowException() {
        // Arrange
        String jsonFilePath = "unreadable.json";
        String csvFilePath = "output.csv";
        
        when(jsonReaderService.isValidJsonFile(jsonFilePath)).thenReturn(false);

        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertJsonToCsv(jsonFilePath, csvFilePath));
        
        assertTrue(exception.getMessage().contains("JSON file is not readable or does not exist"));
    }

    /**
     * Test conversion with unwritable CSV path should throw exception.
     */
    @Test
    void testConvertJsonToCsv_UnwritableCsvPath_ShouldThrowException() {
        // Arrange
        String jsonFilePath = "input.json";
        String csvFilePath = "unwritable.csv";
        
        when(jsonReaderService.isValidJsonFile(jsonFilePath)).thenReturn(true);
        when(csvWriterService.isPathWritable(csvFilePath)).thenReturn(false);

        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertJsonToCsv(jsonFilePath, csvFilePath));
        
        assertTrue(exception.getMessage().contains("CSV file path is not writable"));
    }

    /**
     * Test conversion when JSON reading fails should propagate exception.
     */
    @Test
    void testConvertJsonToCsv_JsonReadingFails_ShouldPropagateException() throws Exception {
        // Arrange
        String jsonFilePath = "input.json";
        String csvFilePath = "output.csv";
        
        when(jsonReaderService.isValidJsonFile(jsonFilePath)).thenReturn(true);
        when(csvWriterService.isPathWritable(csvFilePath)).thenReturn(true);
        when(jsonReaderService.readPersonsFromJson(jsonFilePath))
            .thenThrow(new JsonProcessingException("JSON parsing failed"));

        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertJsonToCsv(jsonFilePath, csvFilePath));
        
        assertEquals("JSON parsing failed", exception.getMessage());
    }

    /**
     * Test conversion when CSV writing fails should propagate exception.
     */
    @Test
    void testConvertJsonToCsv_CsvWritingFails_ShouldPropagateException() throws Exception {
        // Arrange
        String jsonFilePath = "input.json";
        String csvFilePath = "output.csv";
        
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );
        
        when(jsonReaderService.isValidJsonFile(jsonFilePath)).thenReturn(true);
        when(csvWriterService.isPathWritable(csvFilePath)).thenReturn(true);
        when(jsonReaderService.readPersonsFromJson(jsonFilePath)).thenReturn(persons);
        doThrow(new CsvWritingException("CSV writing failed"))
            .when(csvWriterService).writePersonsToCsv(persons, csvFilePath);

        // Act & Assert
        CsvWritingException exception = assertThrows(CsvWritingException.class, 
            () -> converterService.convertJsonToCsv(jsonFilePath, csvFilePath));
        
        assertEquals("CSV writing failed", exception.getMessage());
    }

    /**
     * Test multiple JSON files to single CSV conversion.
     */
    @Test
    void testConvertMultipleJsonToCsv_ValidFiles_ShouldCombineAllPersons() throws Exception {
        // Arrange
        String[] jsonFilePaths = {"file1.json", "file2.json"};
        String csvFilePath = "output.csv";
        
        List<Person> persons1 = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );
        
        List<Person> persons2 = Arrays.asList(
            new Person(2L, "Jane", "Smith", "jane.smith@example.com", 28, "Marketing", 65000.0)
        );
        
        when(jsonReaderService.readPersonsFromJson("file1.json")).thenReturn(persons1);
        when(jsonReaderService.readPersonsFromJson("file2.json")).thenReturn(persons2);

        // Act
        int result = converterService.convertMultipleJsonToCsv(jsonFilePaths, csvFilePath);

        // Assert
        assertEquals(2, result);
        verify(jsonReaderService).readPersonsFromJson("file1.json");
        verify(jsonReaderService).readPersonsFromJson("file2.json");
        verify(csvWriterService).writePersonsToCsv(persons1, csvFilePath);
        verify(csvWriterService).appendPersonsToCsv(persons2, csvFilePath);
    }

    /**
     * Test multiple JSON files conversion with null array should throw exception.
     */
    @Test
    void testConvertMultipleJsonToCsv_NullArray_ShouldThrowException() {
        // Arrange
        String csvFilePath = "output.csv";

        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertMultipleJsonToCsv(null, csvFilePath));
        
        assertTrue(exception.getMessage().contains("JSON file paths array cannot be null or empty"));
    }

    /**
     * Test multiple JSON files conversion with empty array should throw exception.
     */
    @Test
    void testConvertMultipleJsonToCsv_EmptyArray_ShouldThrowException() {
        // Arrange
        String[] emptyArray = {};
        String csvFilePath = "output.csv";

        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertMultipleJsonToCsv(emptyArray, csvFilePath));
        
        assertTrue(exception.getMessage().contains("JSON file paths array cannot be null or empty"));
    }

    /**
     * Test getting conversion statistics.
     */
    @Test
    void testGetConversionStatistics_ValidFile_ShouldReturnStatistics() throws Exception {
        // Arrange
        String jsonFilePath = "input.json";
        
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0),
            new Person(2L, "Jane", "Smith", "jane.smith@example.com", 25, "Marketing", 65000.0),
            new Person(3L, "Bob", "Wilson", "bob.wilson@example.com", 35, "Engineering", 80000.0)
        );
        
        when(jsonReaderService.readPersonsFromJson(jsonFilePath)).thenReturn(persons);

        // Act
        String statistics = converterService.getConversionStatistics(jsonFilePath);

        // Assert
        assertNotNull(statistics);
        assertTrue(statistics.contains("Total Persons: 3"));
        assertTrue(statistics.contains("Unique Departments: 2"));
        assertTrue(statistics.contains("Average Age: 30.00"));
        assertTrue(statistics.contains("Average Salary: 73333.33"));
    }

    /**
     * Test getting conversion statistics when JSON reading fails should propagate exception.
     */
    @Test
    void testGetConversionStatistics_JsonReadingFails_ShouldPropagateException() throws Exception {
        // Arrange
        String jsonFilePath = "invalid.json";
        
        when(jsonReaderService.readPersonsFromJson(jsonFilePath))
            .thenThrow(new JsonProcessingException("JSON parsing failed"));

        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.getConversionStatistics(jsonFilePath));
        
        assertEquals("JSON parsing failed", exception.getMessage());
    }
}
