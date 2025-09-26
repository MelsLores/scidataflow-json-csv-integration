package com.example.scidataflow_json_csv_integration.service;

import com.example.scidataflow_json_csv_integration.exception.JsonProcessingException;
import com.example.scidataflow_json_csv_integration.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonReaderService.
 * Comprehensive test suite that validates JSON reading functionality including
 * file processing, error handling, validation, and edge case scenarios.
 * Tests ensure the service correctly parses JSON files into Person objects
 * and handles various error conditions appropriately.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
class JsonReaderServiceTest {

    private JsonReaderService jsonReaderService;
    private DataTransformService dataTransformService;

    @TempDir
    Path tempDir;

    /**
     * Set up the test environment before each test.
     * Initializes a fresh JsonReaderService instance to ensure
     * test isolation and consistent state for each test execution.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @BeforeEach
    void setUp() {
        dataTransformService = new DataTransformService();
        jsonReaderService = new JsonReaderService(dataTransformService);
    }

    /**
     * Test reading a valid JSON file with multiple persons.
     * Verifies that the service can correctly parse a JSON file containing
     * an array of Person objects and convert them to a List of Person instances.
     * Validates all properties of the parsed objects match the source JSON data.
     * 
     * @throws Exception if JSON parsing or file operations fail
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testReadPersonsFromJson_ValidFile_ShouldReturnPersonsList() throws Exception {
        String jsonContent = """
            [
              {
                "id": 1,
                "firstName": "John",
                "lastName": "Doe",
                "email": "john.doe@example.com",
                "age": 30,
                "department": "Engineering",
                "salary": 75000.00
              },
              {
                "id": 2,
                "firstName": "Jane",
                "lastName": "Smith",
                "email": "jane.smith@example.com",
                "age": 28,
                "department": "Marketing",
                "salary": 65000.00
              }
            ]
            """;
        
        Path jsonFile = tempDir.resolve("test.json");
        Files.writeString(jsonFile, jsonContent);

        List<Person> persons = jsonReaderService.readPersonsFromJson(jsonFile.toString());

        assertNotNull(persons);
        assertEquals(2, persons.size());
        
        // After intelligent transformation, persons are sorted by lastName
        // First person should be "John Doe" (Doe comes before Smith alphabetically)
        Person firstPerson = persons.get(0);
        assertEquals(1L, firstPerson.getId());
        assertEquals("John", firstPerson.getFirstName());
        assertEquals("Doe", firstPerson.getLastName());
        assertEquals("john.doe@example.com", firstPerson.getEmail());
        assertEquals(30, firstPerson.getAge());
        assertEquals("Engineering", firstPerson.getDepartment());
        assertEquals(75000.00, firstPerson.getSalary());
        
        // Second person should be "Jane Smith"
        Person secondPerson = persons.get(1);
        assertEquals(2L, secondPerson.getId());
        assertEquals("Jane", secondPerson.getFirstName());
        assertEquals("Smith", secondPerson.getLastName());
    }

    /**
     * Test reading a valid JSON file with a single person.
     * Verifies that the service can correctly parse a JSON file containing
     * a single Person object and convert it to a Person instance.
     * Validates all properties of the parsed object match the source JSON data.
     * 
     * @throws Exception if JSON parsing or file operations fail
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testReadPersonFromJson_ValidFile_ShouldReturnPerson() throws Exception {
        String jsonContent = """
            {
              "id": 1,
              "firstName": "Alice",
              "lastName": "Johnson",
              "email": "alice.johnson@example.com",
              "age": 25,
              "department": "Design",
              "salary": 60000.00
            }
            """;
        
        Path jsonFile = tempDir.resolve("single.json");
        Files.writeString(jsonFile, jsonContent);

        Person person = jsonReaderService.readPersonFromJson(jsonFile.toString());

        assertNotNull(person);
        assertEquals(1L, person.getId());
        assertEquals("Alice", person.getFirstName());
        assertEquals("Johnson", person.getLastName());
        assertEquals("alice.johnson@example.com", person.getEmail());
        assertEquals(25, person.getAge());
        assertEquals("Design", person.getDepartment());
        assertEquals(60000.00, person.getSalary());
    }

    /**
     * Test reading a non-existent file should throw JsonProcessingException.
     * Verifies that the service properly handles file not found scenarios
     * by throwing an appropriate JsonProcessingException with descriptive message.
     * This ensures graceful error handling for invalid file paths.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testReadPersonsFromJson_FileNotFound_ShouldThrowException() {
        String nonExistentFile = tempDir.resolve("nonexistent.json").toString();

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> jsonReaderService.readPersonsFromJson(nonExistentFile));
        
        assertTrue(exception.getMessage().contains("File does not exist"));
    }

    /**
     * Test reading a file with invalid JSON should throw JsonProcessingException.
     * Verifies that the service properly handles malformed JSON content
     * by throwing a JsonProcessingException. This ensures robust error handling
     * when processing files with syntax errors or invalid JSON structure.
     * 
     * @throws IOException if file operations fail
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testReadPersonsFromJson_InvalidJson_ShouldThrowException() throws IOException {
        String invalidJsonContent = "{ invalid json content }";
        Path jsonFile = tempDir.resolve("invalid.json");
        Files.writeString(jsonFile, invalidJsonContent);

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> jsonReaderService.readPersonsFromJson(jsonFile.toString()));
        
        assertTrue(exception.getMessage().contains("Failed to read JSON file"));
    }

    /**
     * Test reading an empty file should throw JsonProcessingException.
     * Verifies that the service properly handles empty files by throwing
     * a JsonProcessingException. This ensures consistent error handling
     * when attempting to process files with no content.
     * 
     * @throws IOException if file operations fail
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testReadPersonsFromJson_EmptyFile_ShouldThrowException() throws IOException {
        Path emptyFile = tempDir.resolve("empty.json");
        Files.createFile(emptyFile);

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> jsonReaderService.readPersonsFromJson(emptyFile.toString()));
        
        assertTrue(exception.getMessage().contains("File is empty"));
    }

    /**
     * Test reading with null file path should throw JsonProcessingException.
     * Verifies that the service properly validates input parameters and
     * throws JsonProcessingException when provided with null file path.
     * This ensures defensive programming and prevents null pointer exceptions.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testReadPersonsFromJson_NullFilePath_ShouldThrowException() {
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> jsonReaderService.readPersonsFromJson(null));
        
        assertTrue(exception.getMessage().contains("File path cannot be null or empty"));
    }

    /**
     * Test reading with empty file path should throw JsonProcessingException.
     * Verifies that the service properly validates input parameters and
     * throws JsonProcessingException when provided with empty file path.
     * This ensures robust input validation and prevents processing invalid paths.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testReadPersonsFromJson_EmptyFilePath_ShouldThrowException() {
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> jsonReaderService.readPersonsFromJson(""));
        
        assertTrue(exception.getMessage().contains("File path cannot be null or empty"));
    }

    /**
     * Test reading a directory instead of a file should throw JsonProcessingException.
     * Verifies that the service properly handles directory paths by throwing
     * a JsonProcessingException. This ensures the service only processes valid files
     * and provides clear error messages for invalid path types.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testReadPersonsFromJson_DirectoryPath_ShouldThrowException() {
        String directoryPath = tempDir.toString();

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> jsonReaderService.readPersonsFromJson(directoryPath));
        
        assertTrue(exception.getMessage().contains("Path is a directory, not a file"));
    }

    /**
     * Test isFileReadable method with valid file.
     * Verifies that the utility method correctly identifies readable files
     * and returns true for valid, accessible file paths. This tests the
     * file validation functionality used throughout the service.
     * 
     * @throws IOException if file creation fails
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testIsFileReadable_ValidFile_ShouldReturnTrue() throws IOException {
        Path jsonFile = tempDir.resolve("readable.json");
        Files.writeString(jsonFile, "{}");

        assertTrue(jsonReaderService.isValidJsonFile(jsonFile.toString()));
    }

    /**
     * Test isFileReadable method with non-existent file.
     * Verifies that the utility method correctly identifies non-existent files
     * and returns false. This ensures proper file validation before attempting
     * to read JSON content from files that don't exist.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testIsFileReadable_NonExistentFile_ShouldReturnFalse() {
        String nonExistentFile = tempDir.resolve("nonexistent.json").toString();

        assertFalse(jsonReaderService.isValidJsonFile(nonExistentFile));
    }

    /**
     * Test isFileReadable method with null file path.
     * Verifies that the utility method correctly handles null input parameters
     * and returns false. This ensures defensive programming and prevents
     * null pointer exceptions during file validation.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testIsFileReadable_NullFilePath_ShouldReturnFalse() {
        assertFalse(jsonReaderService.isValidJsonFile(null));
    }

    /**
     * Test isFileReadable method with empty file path.
     * Verifies that the utility method correctly handles empty string parameters
     * and returns false. This ensures robust input validation for edge cases
     * where empty strings are provided instead of valid file paths.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testIsFileReadable_EmptyFilePath_ShouldReturnFalse() {
        assertFalse(jsonReaderService.isValidJsonFile(""));
    }

    /**
     * Test reading JSON with missing fields should still work with null values.
     * Verifies that the service can gracefully handle incomplete JSON data
     * by properly mapping missing fields to null values in Person objects.
     * This ensures robust data processing for real-world scenarios with incomplete data.
     * 
     * @throws Exception if JSON parsing or file operations fail
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @Test
    void testReadPersonsFromJson_MissingFields_ShouldHandleNullValues() throws Exception {
        String jsonContent = """
            [
              {
                "id": 1,
                "firstName": "John"
              }
            ]
            """;
        
        Path jsonFile = tempDir.resolve("partial.json");
        Files.writeString(jsonFile, jsonContent);

        List<Person> persons = jsonReaderService.readPersonsFromJson(jsonFile.toString());

        assertNotNull(persons);
        assertEquals(1, persons.size());
        
        Person person = persons.get(0);
        assertEquals(1L, person.getId());
        assertEquals("John", person.getFirstName());
        assertNull(person.getLastName());
        assertNull(person.getEmail());
        assertNull(person.getAge());
        assertNull(person.getDepartment());
        assertNull(person.getSalary());
    }
}
