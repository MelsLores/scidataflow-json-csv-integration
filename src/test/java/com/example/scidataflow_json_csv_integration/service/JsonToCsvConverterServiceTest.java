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
 * Comprehensive test suite that validates the integration functionality between
 * JSON reading and CSV writing services. Tests ensure proper coordination between
 * services, error handling, and end-to-end conversion scenarios using mocked dependencies.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
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
     * Initializes Mockito mocks and creates a fresh JsonToCsvConverterService instance
     * with mocked dependencies to ensure test isolation and controlled behavior.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converterService = new JsonToCsvConverterService(jsonReaderService, csvWriterService);
    }

    /**
     * Test successful JSON to CSV conversion.
     * Verifies that the converter service properly coordinates between JSON reader
     * and CSV writer services to successfully convert a JSON file containing Person
     * data to CSV format. Tests the happy path scenario with valid inputs and
     * confirms the correct number of processed persons is returned.
     * 
     * @throws Exception if conversion process fails
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testConvertJsonToCsv_ValidFiles_ShouldReturnPersonCount() throws Exception {
        String jsonFilePath = "input.json";
        String csvFilePath = "output.csv";
        
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0),
            new Person(2L, "Jane", "Smith", "jane.smith@example.com", 28, "Marketing", 65000.0)
        );
        
        when(jsonReaderService.isValidJsonFile(jsonFilePath)).thenReturn(true);
        when(csvWriterService.isPathWritable(csvFilePath)).thenReturn(true);
        when(jsonReaderService.readPersonsFromJson(jsonFilePath)).thenReturn(persons);

        int result = converterService.convertJsonToCsv(jsonFilePath, csvFilePath);

        assertEquals(2, result);
        verify(jsonReaderService).readPersonsFromJson(jsonFilePath);
        verify(csvWriterService).writePersonsToCsv(persons, csvFilePath);
    }

    /**
     * Test JSON to CSV conversion with custom delimiter.
     * Verifies that the converter service properly passes custom formatting options
     * (delimiter, quote character, escape character) to the CSV writer service.
     * This ensures flexible CSV output formatting based on specific requirements.
     * 
     * @throws Exception if conversion process fails
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testConvertJsonToCsv_WithCustomDelimiter_ShouldUseCustomFormat() throws Exception {
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

        int result = converterService.convertJsonToCsv(jsonFilePath, csvFilePath, delimiter, quoteChar, escapeChar);

        assertEquals(1, result);
        verify(jsonReaderService).readPersonsFromJson(jsonFilePath);
        verify(csvWriterService).writePersonsToCsv(persons, csvFilePath, delimiter, quoteChar, escapeChar);
    }

    /**
     * Test conversion with empty JSON file should return 0.
     * Verifies that the converter service properly handles empty JSON files
     * by returning zero as the count of processed persons and avoiding
     * unnecessary CSV writing operations for empty datasets.
     * 
     * @throws Exception if conversion process fails
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testConvertJsonToCsv_EmptyJsonFile_ShouldReturnZero() throws Exception {
        String jsonFilePath = "empty.json";
        String csvFilePath = "output.csv";
        
        List<Person> emptyList = Arrays.asList();
        
        when(jsonReaderService.isValidJsonFile(jsonFilePath)).thenReturn(true);
        when(csvWriterService.isPathWritable(csvFilePath)).thenReturn(true);
        when(jsonReaderService.readPersonsFromJson(jsonFilePath)).thenReturn(emptyList);

        int result = converterService.convertJsonToCsv(jsonFilePath, csvFilePath);

        assertEquals(0, result);
        verify(jsonReaderService).readPersonsFromJson(jsonFilePath);
        verify(csvWriterService, never()).writePersonsToCsv(any(), any());
    }

    /**
     * Test conversion with null JSON file path should throw exception.
     * Verifies that the converter service properly validates input parameters
     * and throws JsonProcessingException when provided with null JSON file path.
     * This ensures defensive programming and clear error handling.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testConvertJsonToCsv_NullJsonFilePath_ShouldThrowException() {
        String csvFilePath = "output.csv";

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertJsonToCsv(null, csvFilePath));
        
        assertTrue(exception.getMessage().contains("JSON file path cannot be null or empty"));
    }

    /**
     * Test conversion with null CSV file path should throw exception.
     * Verifies that the converter service properly validates output parameters
     * and throws CsvWritingException when provided with null CSV file path.
     * This ensures comprehensive input validation for all required parameters.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testConvertJsonToCsv_NullCsvFilePath_ShouldThrowException() {
        String jsonFilePath = "input.json";

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertJsonToCsv(jsonFilePath, null));
        
        assertTrue(exception.getMessage().contains("CSV file path cannot be null or empty"));
    }

    /**
     * Test conversion with unreadable JSON file should throw exception.
     * Verifies that the converter service properly validates JSON file accessibility
     * and throws JsonProcessingException when the input file is not readable.
     * This ensures early detection of file access issues before processing begins.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testConvertJsonToCsv_UnreadableJsonFile_ShouldThrowException() {
        String jsonFilePath = "unreadable.json";
        String csvFilePath = "output.csv";
        
        when(jsonReaderService.isValidJsonFile(jsonFilePath)).thenReturn(false);

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertJsonToCsv(jsonFilePath, csvFilePath));
        
        assertTrue(exception.getMessage().contains("JSON file is not readable or does not exist"));
    }

    /**
     * Test conversion with unwritable CSV path should throw exception.
     * Verifies that the converter service properly validates CSV file writability
     * and throws JsonProcessingException when the output path is not writable.
     * This ensures early detection of output file access issues before processing.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testConvertJsonToCsv_UnwritableCsvPath_ShouldThrowException() {
        String jsonFilePath = "input.json";
        String csvFilePath = "unwritable.csv";
        
        when(jsonReaderService.isValidJsonFile(jsonFilePath)).thenReturn(true);
        when(csvWriterService.isPathWritable(csvFilePath)).thenReturn(false);

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertJsonToCsv(jsonFilePath, csvFilePath));
        
        assertTrue(exception.getMessage().contains("CSV file path is not writable"));
    }

    /**
     * Test conversion when JSON reading fails should propagate exception.
     * Verifies that the converter service properly propagates JsonProcessingExceptions
     * thrown by the JSON reader service, maintaining the error context and message
     * for proper error handling in the calling code.
     * 
     * @throws Exception if mock setup fails
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testConvertJsonToCsv_JsonReadingFails_ShouldPropagateException() throws Exception {
        String jsonFilePath = "input.json";
        String csvFilePath = "output.csv";
        
        when(jsonReaderService.isValidJsonFile(jsonFilePath)).thenReturn(true);
        when(csvWriterService.isPathWritable(csvFilePath)).thenReturn(true);
        when(jsonReaderService.readPersonsFromJson(jsonFilePath))
            .thenThrow(new JsonProcessingException("JSON parsing failed"));

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertJsonToCsv(jsonFilePath, csvFilePath));
        
        assertEquals("JSON parsing failed", exception.getMessage());
    }

    /**
     * Test conversion when CSV writing fails should propagate exception.
     * Verifies that the converter service properly propagates CsvWritingExceptions
     * thrown by the CSV writer service, maintaining the error context and message
     * for proper error handling in the calling code.
     * 
     * @throws Exception if mock setup fails
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testConvertJsonToCsv_CsvWritingFails_ShouldPropagateException() throws Exception {
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

        CsvWritingException exception = assertThrows(CsvWritingException.class, 
            () -> converterService.convertJsonToCsv(jsonFilePath, csvFilePath));
        
        assertEquals("CSV writing failed", exception.getMessage());
    }

    /**
     * Test multiple JSON files to single CSV conversion.
     * Verifies that the converter service can properly combine data from multiple
     * JSON files into a single CSV output file. Tests the batch processing functionality
     * and ensures all Person objects from all input files are correctly merged.
     * 
     * @throws Exception if conversion process fails
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testConvertMultipleJsonToCsv_ValidFiles_ShouldCombineAllPersons() throws Exception {
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

        int result = converterService.convertMultipleJsonToCsv(jsonFilePaths, csvFilePath);

        assertEquals(2, result);
        verify(jsonReaderService).readPersonsFromJson("file1.json");
        verify(jsonReaderService).readPersonsFromJson("file2.json");
        verify(csvWriterService).writePersonsToCsv(persons1, csvFilePath);
        verify(csvWriterService).appendPersonsToCsv(persons2, csvFilePath);
    }

    /**
     * Test multiple JSON files conversion with null array should throw exception.
     * Verifies that the converter service properly validates input parameters
     * and throws JsonProcessingException when provided with null file paths array.
     * This ensures defensive programming for batch conversion operations.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testConvertMultipleJsonToCsv_NullArray_ShouldThrowException() {
        String csvFilePath = "output.csv";

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertMultipleJsonToCsv(null, csvFilePath));
        
        assertTrue(exception.getMessage().contains("JSON file paths array cannot be null or empty"));
    }

    /**
     * Test multiple JSON files conversion with empty array should throw exception.
     * Verifies that the converter service properly validates input parameters
     * and throws JsonProcessingException when provided with empty file paths array.
     * This ensures meaningful batch operations with at least one input file.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testConvertMultipleJsonToCsv_EmptyArray_ShouldThrowException() {
        String[] emptyArray = {};
        String csvFilePath = "output.csv";

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.convertMultipleJsonToCsv(emptyArray, csvFilePath));
        
        assertTrue(exception.getMessage().contains("JSON file paths array cannot be null or empty"));
    }

    /**
     * Test getting conversion statistics.
     * Verifies that the converter service can generate comprehensive statistics
     * about JSON data including total count, unique departments, average age,
     * and average salary. This provides valuable insights for data analysis.
     * 
     * @throws Exception if statistics generation fails
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testGetConversionStatistics_ValidFile_ShouldReturnStatistics() throws Exception {
        String jsonFilePath = "input.json";
        
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0),
            new Person(2L, "Jane", "Smith", "jane.smith@example.com", 25, "Marketing", 65000.0),
            new Person(3L, "Bob", "Wilson", "bob.wilson@example.com", 35, "Engineering", 80000.0)
        );
        
        when(jsonReaderService.readPersonsFromJson(jsonFilePath)).thenReturn(persons);

        String statistics = converterService.getConversionStatistics(jsonFilePath);

        assertNotNull(statistics);
        assertTrue(statistics.contains("Total Persons: 3"));
        assertTrue(statistics.contains("Unique Departments: 2"));
        assertTrue(statistics.contains("Average Age: 30.00"));
        assertTrue(statistics.contains("Average Salary: 73333.33"));
    }

    /**
     * Test getting conversion statistics when JSON reading fails should propagate exception.
     * Verifies that the converter service properly propagates JsonProcessingExceptions
     * thrown during JSON reading when generating statistics. This ensures consistent
     * error handling across all service operations.
     * 
     * @throws Exception if mock setup fails
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testGetConversionStatistics_JsonReadingFails_ShouldPropagateException() throws Exception {
        String jsonFilePath = "invalid.json";
        
        when(jsonReaderService.readPersonsFromJson(jsonFilePath))
            .thenThrow(new JsonProcessingException("JSON parsing failed"));

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> converterService.getConversionStatistics(jsonFilePath));
        
        assertEquals("JSON parsing failed", exception.getMessage());
    }
}
