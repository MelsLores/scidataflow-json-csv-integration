package com.example.scidataflow_json_csv_integration.service;

import com.example.scidataflow_json_csv_integration.exception.CsvWritingException;
import com.example.scidataflow_json_csv_integration.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CsvWriterService.
 * These tests validate the CSV writing functionality in isolation to ensure
 * correct operation before integration into the general workflow.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
class CsvWriterServiceTest {

    private CsvWriterService csvWriterService;

    @TempDir
    Path tempDir;

    /**
     * Set up the test environment before each test.
     * Initializes a new CsvWriterService instance for each test method.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @BeforeEach
    void setUp() {
        csvWriterService = new CsvWriterService();
    }

    /**
     * Test writing persons to CSV file with default configuration.
     * Verifies that the service correctly creates a CSV file with proper header
     * and data rows when given a list of valid Person objects.
     * 
     * @throws Exception if an error occurs during file operations
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testWritePersonsToCsv_ValidPersons_ShouldCreateCsvFile() throws Exception {
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0),
            new Person(2L, "Jane", "Smith", "jane.smith@example.com", 28, "Marketing", 65000.0)
        );
        
        Path csvFile = tempDir.resolve("output.csv");

        csvWriterService.writePersonsToCsv(persons, csvFile.toString());

        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertNotNull(content);
        
        String[] lines = content.split("\n");
        assertEquals(3, lines.length);
        
        assertEquals("\"ID\",\"First Name\",\"Last Name\",\"Email\",\"Age\",\"Department\",\"Salary\"", lines[0]);
        
        assertEquals("\"1\",\"John\",\"Doe\",\"john.doe@example.com\",\"30\",\"Engineering\",\"75000.0\"", lines[1]);
        
        assertEquals("\"2\",\"Jane\",\"Smith\",\"jane.smith@example.com\",\"28\",\"Marketing\",\"65000.0\"", lines[2]);
    }

    /**
     * Test writing persons to CSV file with custom delimiter.
     * Verifies that the service correctly uses custom delimiters when creating
     * CSV files, maintaining proper formatting with the specified delimiter.
     * 
     * @throws Exception if an error occurs during file operations
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testWritePersonsToCsv_CustomDelimiter_ShouldCreateCsvFileWithCustomFormat() throws Exception {
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );
        
        Path csvFile = tempDir.resolve("custom.csv");

        csvWriterService.writePersonsToCsv(persons, csvFile.toString(), ';', '\'', '\\');

        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertNotNull(content);
        
        assertTrue(content.contains(";"));
        assertTrue(content.contains("'ID'"));
    }

    /**
     * Test writing a single person to CSV file.
     * Verifies that the service can write a single Person object to CSV
     * and creates a properly formatted file with header and data row.
     * 
     * @throws Exception if an error occurs during file operations
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testWritePersonToCsv_ValidPerson_ShouldCreateCsvFile() throws Exception {
        Person person = new Person(1L, "Alice", "Johnson", "alice.johnson@example.com", 25, "Design", 60000.0);
        Path csvFile = tempDir.resolve("single.csv");

        csvWriterService.writePersonToCsv(person, csvFile.toString());

        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertNotNull(content);
        
        String[] lines = content.split("\n");
        assertEquals(2, lines.length);
        
        assertEquals("\"ID\",\"First Name\",\"Last Name\",\"Email\",\"Age\",\"Department\",\"Salary\"", lines[0]);
        
        assertEquals("\"1\",\"Alice\",\"Johnson\",\"alice.johnson@example.com\",\"25\",\"Design\",\"60000.0\"", lines[1]);
    }

    /**
     * Test appending persons to existing CSV file.
     * Verifies that the service correctly appends new data to an existing CSV file
     * without duplicating headers and maintaining proper file format.
     * 
     * @throws Exception if an error occurs during file operations
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testAppendPersonsToCsv_ExistingFile_ShouldAppendData() throws Exception {
        List<Person> initialPersons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );
        
        List<Person> additionalPersons = Arrays.asList(
            new Person(2L, "Jane", "Smith", "jane.smith@example.com", 28, "Marketing", 65000.0)
        );
        
        Path csvFile = tempDir.resolve("append.csv");

        csvWriterService.writePersonsToCsv(initialPersons, csvFile.toString());
        csvWriterService.appendPersonsToCsv(additionalPersons, csvFile.toString());

        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertNotNull(content);
        
        String[] lines = content.split("\n");
        assertEquals(3, lines.length);
        
        assertTrue(content.contains("John"));
        assertTrue(content.contains("Jane"));
    }

    /**
     * Test appending persons to non-existing file should create file with header.
     * Verifies that when appending to a non-existing file, the service creates
     * a new file with proper header and data rows.
     * 
     * @throws Exception if an error occurs during file operations
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testAppendPersonsToCsv_NonExistingFile_ShouldCreateFileWithHeader() throws Exception {
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );
        
        Path csvFile = tempDir.resolve("new_append.csv");

        csvWriterService.appendPersonsToCsv(persons, csvFile.toString());

        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertNotNull(content);
        
        String[] lines = content.split("\n");
        assertEquals(2, lines.length); // Header + 1 data row
        
        assertEquals("\"ID\",\"First Name\",\"Last Name\",\"Email\",\"Age\",\"Department\",\"Salary\"", lines[0]);
    }

    /**
     * Test writing with null persons list should throw CsvWritingException.
     * Verifies that the service correctly validates input and throws appropriate
     * exception when provided with null persons list.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testWritePersonsToCsv_NullPersonsList_ShouldThrowException() {
        Path csvFile = tempDir.resolve("output.csv");

        CsvWritingException exception = assertThrows(CsvWritingException.class, 
            () -> csvWriterService.writePersonsToCsv(null, csvFile.toString()));
        
        assertTrue(exception.getMessage().contains("Persons list cannot be null"));
    }

    /**
     * Test writing with empty persons list should throw CsvWritingException.
     * Verifies that the service validates input parameters and throws appropriate
     * exception when provided with an empty persons list.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testWritePersonsToCsv_EmptyPersonsList_ShouldThrowException() {
        List<Person> emptyList = Arrays.asList();
        Path csvFile = tempDir.resolve("output.csv");

        CsvWritingException exception = assertThrows(CsvWritingException.class, 
            () -> csvWriterService.writePersonsToCsv(emptyList, csvFile.toString()));
        
        assertTrue(exception.getMessage().contains("Persons list cannot be empty"));
    }

    /**
     * Test writing with null file path should throw CsvWritingException.
     * Verifies that the service validates the file path parameter and throws
     * appropriate exception when provided with null file path.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testWritePersonsToCsv_NullFilePath_ShouldThrowException() {
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );

        CsvWritingException exception = assertThrows(CsvWritingException.class, 
            () -> csvWriterService.writePersonsToCsv(persons, null));
        
        assertTrue(exception.getMessage().contains("File path cannot be null or empty"));
    }

    /**
     * Test writing with empty file path should throw CsvWritingException.
     * Verifies that the service validates the file path parameter and throws
     * appropriate exception when provided with empty file path string.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testWritePersonsToCsv_EmptyFilePath_ShouldThrowException() {
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );

        CsvWritingException exception = assertThrows(CsvWritingException.class, 
            () -> csvWriterService.writePersonsToCsv(persons, ""));
        
        assertTrue(exception.getMessage().contains("File path cannot be null or empty"));
    }

    /**
     * Test writing null person should throw CsvWritingException.
     * Verifies that the service validates the Person parameter and throws
     * appropriate exception when provided with null Person object.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testWritePersonToCsv_NullPerson_ShouldThrowException() {
        Path csvFile = tempDir.resolve("output.csv");

        CsvWritingException exception = assertThrows(CsvWritingException.class, 
            () -> csvWriterService.writePersonToCsv(null, csvFile.toString()));
        
        assertTrue(exception.getMessage().contains("Person object cannot be null"));
    }

    /**
     * Test isPathWritable method with valid path.
     * Verifies that the service correctly identifies writable paths
     * and returns true for valid file paths.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testIsPathWritable_ValidPath_ShouldReturnTrue() {
        Path csvFile = tempDir.resolve("writable.csv");

        assertTrue(csvWriterService.isPathWritable(csvFile.toString()));
    }

    /**
     * Test isPathWritable method with null path.
     * Verifies that the service correctly handles null path parameter
     * and returns false as expected.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testIsPathWritable_NullPath_ShouldReturnFalse() {
        assertFalse(csvWriterService.isPathWritable(null));
    }

    /**
     * Test isPathWritable method with empty path.
     * Verifies that the service correctly handles empty path parameter
     * and returns false as expected for invalid paths.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testIsPathWritable_EmptyPath_ShouldReturnFalse() {
        assertFalse(csvWriterService.isPathWritable(""));
    }

    /**
     * Test writing persons with null values should handle gracefully.
     * Verifies that the CSV writer service can properly process Person objects
     * containing null values and represents them as empty strings in the output CSV file.
     * This test ensures data integrity when handling incomplete or missing data.
     * 
     * @throws Exception if CSV writing operation fails
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testWritePersonsToCsv_PersonsWithNullValues_ShouldHandleGracefully() throws Exception {
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", null, null, null, null, null),
            new Person(null, null, "Smith", "jane@example.com", 28, "Marketing", 65000.0)
        );
        
        Path csvFile = tempDir.resolve("null_values.csv");

        csvWriterService.writePersonsToCsv(persons, csvFile.toString());

        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertNotNull(content);
        
        String[] lines = content.split("\n");
        assertEquals(3, lines.length); // Header + 2 data rows
        
        assertTrue(lines[1].contains("\"1\",\"John\",\"\",\"\",\"\",\"\",\"\""));
        assertTrue(lines[2].contains("\"\",\"\",\"Smith\""));
    }

    /**
     * Test creating directory structure automatically.
     * Verifies that the CSV writer service automatically creates nested directory
     * structures when the target path contains directories that don't exist.
     * This test ensures the service handles file system operations robustly.
     * 
     * @throws Exception if CSV writing operation or file system operations fail
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testWritePersonsToCsv_NonExistentDirectory_ShouldCreateDirectory() throws Exception {
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );
        
        Path nestedDir = tempDir.resolve("nested").resolve("directory");
        Path csvFile = nestedDir.resolve("output.csv");

        csvWriterService.writePersonsToCsv(persons, csvFile.toString());

        assertTrue(Files.exists(csvFile));
        assertTrue(Files.exists(nestedDir));
    }
}
