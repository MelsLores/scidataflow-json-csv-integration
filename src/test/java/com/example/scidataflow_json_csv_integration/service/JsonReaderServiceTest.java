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
 * These tests validate the JSON reading functionality in isolation to ensure
 * correct operation before integration into the general workflow.
 * 
 * @author Digital NAO Team
 * @version 1.0
 * @since 2025-09-21
 */
class JsonReaderServiceTest {

    private JsonReaderService jsonReaderService;

    @TempDir
    Path tempDir;

    /**
     * Set up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        jsonReaderService = new JsonReaderService();
    }

    /**
     * Test reading a valid JSON file with multiple persons.
     */
    @Test
    void testReadPersonsFromJson_ValidFile_ShouldReturnPersonsList() throws Exception {
        // Arrange
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

        // Act
        List<Person> persons = jsonReaderService.readPersonsFromJson(jsonFile.toString());

        // Assert
        assertNotNull(persons);
        assertEquals(2, persons.size());
        
        Person firstPerson = persons.get(0);
        assertEquals(1L, firstPerson.getId());
        assertEquals("John", firstPerson.getFirstName());
        assertEquals("Doe", firstPerson.getLastName());
        assertEquals("john.doe@example.com", firstPerson.getEmail());
        assertEquals(30, firstPerson.getAge());
        assertEquals("Engineering", firstPerson.getDepartment());
        assertEquals(75000.00, firstPerson.getSalary());
        
        Person secondPerson = persons.get(1);
        assertEquals(2L, secondPerson.getId());
        assertEquals("Jane", secondPerson.getFirstName());
        assertEquals("Smith", secondPerson.getLastName());
    }

    /**
     * Test reading a valid JSON file with a single person.
     */
    @Test
    void testReadPersonFromJson_ValidFile_ShouldReturnPerson() throws Exception {
        // Arrange
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

        // Act
        Person person = jsonReaderService.readPersonFromJson(jsonFile.toString());

        // Assert
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
     */
    @Test
    void testReadPersonsFromJson_FileNotFound_ShouldThrowException() {
        // Arrange
        String nonExistentFile = tempDir.resolve("nonexistent.json").toString();

        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> jsonReaderService.readPersonsFromJson(nonExistentFile));
        
        assertTrue(exception.getMessage().contains("File not found"));
    }

    /**
     * Test reading a file with invalid JSON should throw JsonProcessingException.
     */
    @Test
    void testReadPersonsFromJson_InvalidJson_ShouldThrowException() throws IOException {
        // Arrange
        String invalidJsonContent = "{ invalid json content }";
        Path jsonFile = tempDir.resolve("invalid.json");
        Files.writeString(jsonFile, invalidJsonContent);

        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> jsonReaderService.readPersonsFromJson(jsonFile.toString()));
        
        assertTrue(exception.getMessage().contains("Failed to read or parse JSON file"));
    }

    /**
     * Test reading an empty file should throw JsonProcessingException.
     */
    @Test
    void testReadPersonsFromJson_EmptyFile_ShouldThrowException() throws IOException {
        // Arrange
        Path emptyFile = tempDir.resolve("empty.json");
        Files.createFile(emptyFile);

        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> jsonReaderService.readPersonsFromJson(emptyFile.toString()));
        
        assertTrue(exception.getMessage().contains("File is empty"));
    }

    /**
     * Test reading with null file path should throw JsonProcessingException.
     */
    @Test
    void testReadPersonsFromJson_NullFilePath_ShouldThrowException() {
        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> jsonReaderService.readPersonsFromJson(null));
        
        assertTrue(exception.getMessage().contains("File path cannot be null or empty"));
    }

    /**
     * Test reading with empty file path should throw JsonProcessingException.
     */
    @Test
    void testReadPersonsFromJson_EmptyFilePath_ShouldThrowException() {
        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> jsonReaderService.readPersonsFromJson(""));
        
        assertTrue(exception.getMessage().contains("File path cannot be null or empty"));
    }

    /**
     * Test reading a directory instead of a file should throw JsonProcessingException.
     */
    @Test
    void testReadPersonsFromJson_DirectoryPath_ShouldThrowException() {
        // Arrange
        String directoryPath = tempDir.toString();

        // Act & Assert
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, 
            () -> jsonReaderService.readPersonsFromJson(directoryPath));
        
        assertTrue(exception.getMessage().contains("Path is not a regular file"));
    }

    /**
     * Test isFileReadable method with valid file.
     */
    @Test
    void testIsFileReadable_ValidFile_ShouldReturnTrue() throws IOException {
        // Arrange
        Path jsonFile = tempDir.resolve("readable.json");
        Files.writeString(jsonFile, "{}");

        // Act & Assert
        assertTrue(jsonReaderService.isValidJsonFile(jsonFile.toString()));
    }

    /**
     * Test isFileReadable method with non-existent file.
     */
    @Test
    void testIsFileReadable_NonExistentFile_ShouldReturnFalse() {
        // Arrange
        String nonExistentFile = tempDir.resolve("nonexistent.json").toString();

        // Act & Assert
        assertFalse(jsonReaderService.isValidJsonFile(nonExistentFile));
    }

    /**
     * Test isFileReadable method with null file path.
     */
    @Test
    void testIsFileReadable_NullFilePath_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(jsonReaderService.isValidJsonFile(null));
    }

    /**
     * Test isFileReadable method with empty file path.
     */
    @Test
    void testIsFileReadable_EmptyFilePath_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(jsonReaderService.isValidJsonFile(""));
    }

    /**
     * Test reading JSON with missing fields should still work with null values.
     */
    @Test
    void testReadPersonsFromJson_MissingFields_ShouldHandleNullValues() throws Exception {
        // Arrange
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

        // Act
        List<Person> persons = jsonReaderService.readPersonsFromJson(jsonFile.toString());

        // Assert
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