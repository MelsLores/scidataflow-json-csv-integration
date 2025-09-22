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
 * @author Digital NAO Team
 * @version 1.0
 * @since 2025-09-21
 */
class CsvWriterServiceTest {

    private CsvWriterService csvWriterService;

    @TempDir
    Path tempDir;

    /**
     * Set up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        csvWriterService = new CsvWriterService();
    }

    /**
     * Test writing persons to CSV file with default configuration.
     */
    @Test
    void testWritePersonsToCsv_ValidPersons_ShouldCreateCsvFile() throws Exception {
        // Arrange
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0),
            new Person(2L, "Jane", "Smith", "jane.smith@example.com", 28, "Marketing", 65000.0)
        );
        
        Path csvFile = tempDir.resolve("output.csv");

        // Act
        csvWriterService.writePersonsToCsv(persons, csvFile.toString());

        // Assert
        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertNotNull(content);
        
        String[] lines = content.split("\n");
        assertEquals(3, lines.length); // Header + 2 data rows
        
        // Check header
        assertEquals("\"ID\",\"First Name\",\"Last Name\",\"Email\",\"Age\",\"Department\",\"Salary\"", lines[0]);
        
        // Check first data row
        assertEquals("\"1\",\"John\",\"Doe\",\"john.doe@example.com\",\"30\",\"Engineering\",\"75000.0\"", lines[1]);
        
        // Check second data row
        assertEquals("\"2\",\"Jane\",\"Smith\",\"jane.smith@example.com\",\"28\",\"Marketing\",\"65000.0\"", lines[2]);
    }

    /**
     * Test writing persons to CSV file with custom delimiter.
     */
    @Test
    void testWritePersonsToCsv_CustomDelimiter_ShouldCreateCsvFileWithCustomFormat() throws Exception {
        // Arrange
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );
        
        Path csvFile = tempDir.resolve("custom.csv");

        // Act
        csvWriterService.writePersonsToCsv(persons, csvFile.toString(), ';', '\'', '\\');

        // Assert
        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertNotNull(content);
        
        // Should contain semicolon delimiter
        assertTrue(content.contains(";"));
        assertTrue(content.contains("'ID'"));
    }

    /**
     * Test writing a single person to CSV file.
     */
    @Test
    void testWritePersonToCsv_ValidPerson_ShouldCreateCsvFile() throws Exception {
        // Arrange
        Person person = new Person(1L, "Alice", "Johnson", "alice.johnson@example.com", 25, "Design", 60000.0);
        Path csvFile = tempDir.resolve("single.csv");

        // Act
        csvWriterService.writePersonToCsv(person, csvFile.toString());

        // Assert
        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertNotNull(content);
        
        String[] lines = content.split("\n");
        assertEquals(2, lines.length); // Header + 1 data row
        
        // Check header
        assertEquals("\"ID\",\"First Name\",\"Last Name\",\"Email\",\"Age\",\"Department\",\"Salary\"", lines[0]);
        
        // Check data row
        assertEquals("\"1\",\"Alice\",\"Johnson\",\"alice.johnson@example.com\",\"25\",\"Design\",\"60000.0\"", lines[1]);
    }

    /**
     * Test appending persons to existing CSV file.
     */
    @Test
    void testAppendPersonsToCsv_ExistingFile_ShouldAppendData() throws Exception {
        // Arrange
        List<Person> initialPersons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );
        
        List<Person> additionalPersons = Arrays.asList(
            new Person(2L, "Jane", "Smith", "jane.smith@example.com", 28, "Marketing", 65000.0)
        );
        
        Path csvFile = tempDir.resolve("append.csv");

        // Act
        csvWriterService.writePersonsToCsv(initialPersons, csvFile.toString());
        csvWriterService.appendPersonsToCsv(additionalPersons, csvFile.toString());

        // Assert
        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertNotNull(content);
        
        String[] lines = content.split("\n");
        assertEquals(3, lines.length); // Header + 2 data rows
        
        // Check that both persons are present
        assertTrue(content.contains("John"));
        assertTrue(content.contains("Jane"));
    }

    /**
     * Test appending persons to non-existing file should create file with header.
     */
    @Test
    void testAppendPersonsToCsv_NonExistingFile_ShouldCreateFileWithHeader() throws Exception {
        // Arrange
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );
        
        Path csvFile = tempDir.resolve("new_append.csv");

        // Act
        csvWriterService.appendPersonsToCsv(persons, csvFile.toString());

        // Assert
        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertNotNull(content);
        
        String[] lines = content.split("\n");
        assertEquals(2, lines.length); // Header + 1 data row
        
        // Check header is present
        assertEquals("\"ID\",\"First Name\",\"Last Name\",\"Email\",\"Age\",\"Department\",\"Salary\"", lines[0]);
    }

    /**
     * Test writing with null persons list should throw CsvWritingException.
     */
    @Test
    void testWritePersonsToCsv_NullPersonsList_ShouldThrowException() {
        // Arrange
        Path csvFile = tempDir.resolve("output.csv");

        // Act & Assert
        CsvWritingException exception = assertThrows(CsvWritingException.class, 
            () -> csvWriterService.writePersonsToCsv(null, csvFile.toString()));
        
        assertTrue(exception.getMessage().contains("Persons list cannot be null"));
    }

    /**
     * Test writing with empty persons list should throw CsvWritingException.
     */
    @Test
    void testWritePersonsToCsv_EmptyPersonsList_ShouldThrowException() {
        // Arrange
        List<Person> emptyList = Arrays.asList();
        Path csvFile = tempDir.resolve("output.csv");

        // Act & Assert
        CsvWritingException exception = assertThrows(CsvWritingException.class, 
            () -> csvWriterService.writePersonsToCsv(emptyList, csvFile.toString()));
        
        assertTrue(exception.getMessage().contains("Persons list cannot be empty"));
    }

    /**
     * Test writing with null file path should throw CsvWritingException.
     */
    @Test
    void testWritePersonsToCsv_NullFilePath_ShouldThrowException() {
        // Arrange
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );

        // Act & Assert
        CsvWritingException exception = assertThrows(CsvWritingException.class, 
            () -> csvWriterService.writePersonsToCsv(persons, null));
        
        assertTrue(exception.getMessage().contains("File path cannot be null or empty"));
    }

    /**
     * Test writing with empty file path should throw CsvWritingException.
     */
    @Test
    void testWritePersonsToCsv_EmptyFilePath_ShouldThrowException() {
        // Arrange
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );

        // Act & Assert
        CsvWritingException exception = assertThrows(CsvWritingException.class, 
            () -> csvWriterService.writePersonsToCsv(persons, ""));
        
        assertTrue(exception.getMessage().contains("File path cannot be null or empty"));
    }

    /**
     * Test writing null person should throw CsvWritingException.
     */
    @Test
    void testWritePersonToCsv_NullPerson_ShouldThrowException() {
        // Arrange
        Path csvFile = tempDir.resolve("output.csv");

        // Act & Assert
        CsvWritingException exception = assertThrows(CsvWritingException.class, 
            () -> csvWriterService.writePersonToCsv(null, csvFile.toString()));
        
        assertTrue(exception.getMessage().contains("Person object cannot be null"));
    }

    /**
     * Test isPathWritable method with valid path.
     */
    @Test
    void testIsPathWritable_ValidPath_ShouldReturnTrue() {
        // Arrange
        Path csvFile = tempDir.resolve("writable.csv");

        // Act & Assert
        assertTrue(csvWriterService.isPathWritable(csvFile.toString()));
    }

    /**
     * Test isPathWritable method with null path.
     */
    @Test
    void testIsPathWritable_NullPath_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(csvWriterService.isPathWritable(null));
    }

    /**
     * Test isPathWritable method with empty path.
     */
    @Test
    void testIsPathWritable_EmptyPath_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(csvWriterService.isPathWritable(""));
    }

    /**
     * Test writing persons with null values should handle gracefully.
     */
    @Test
    void testWritePersonsToCsv_PersonsWithNullValues_ShouldHandleGracefully() throws Exception {
        // Arrange
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", null, null, null, null, null),
            new Person(null, null, "Smith", "jane@example.com", 28, "Marketing", 65000.0)
        );
        
        Path csvFile = tempDir.resolve("null_values.csv");

        // Act
        csvWriterService.writePersonsToCsv(persons, csvFile.toString());

        // Assert
        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertNotNull(content);
        
        String[] lines = content.split("\n");
        assertEquals(3, lines.length); // Header + 2 data rows
        
        // Should contain empty strings for null values
        assertTrue(lines[1].contains("\"1\",\"John\",\"\",\"\",\"\",\"\",\"\""));
        assertTrue(lines[2].contains("\"\",\"\",\"Smith\""));
    }

    /**
     * Test creating directory structure automatically.
     */
    @Test
    void testWritePersonsToCsv_NonExistentDirectory_ShouldCreateDirectory() throws Exception {
        // Arrange
        List<Person> persons = Arrays.asList(
            new Person(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0)
        );
        
        Path nestedDir = tempDir.resolve("nested").resolve("directory");
        Path csvFile = nestedDir.resolve("output.csv");

        // Act
        csvWriterService.writePersonsToCsv(persons, csvFile.toString());

        // Assert
        assertTrue(Files.exists(csvFile));
        assertTrue(Files.exists(nestedDir));
    }
}