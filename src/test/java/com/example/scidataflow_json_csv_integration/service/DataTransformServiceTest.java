package com.example.scidataflow_json_csv_integration.service;

import com.example.scidataflow_json_csv_integration.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DataTransformService.
 * Comprehensive test suite that validates data transformation functionality
 * including object transformation, validation, and CSV preparation.
 * 
 * @author Melany Rivera
 * @since 25/09/2025
 */
@ExtendWith(MockitoExtension.class)
class DataTransformServiceTest {

    @InjectMocks
    private DataTransformService dataTransformService;

    private List<Person> testPersons;

    /**
     * Sets up test data before each test method.
     * Creates sample Person objects for testing transformation operations.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @BeforeEach
    void setUp() {
        testPersons = Arrays.asList(
            createTestPerson(1L, "John", "Doe", "john.doe@example.com", 30, "Engineering", 75000.0),
            createTestPerson(2L, "Jane", "Smith", "jane.smith@example.com", 28, "Marketing", 65000.0),
            createTestPerson(3L, "Bob", "Johnson", "bob.johnson@example.com", 35, "Finance", 80000.0)
        );
    }

    /**
     * Test transforming objects with null input should return empty list.
     * Verifies that the service handles null input gracefully without throwing exceptions.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @Test
    void testTransformToPersons_NullInput_ShouldReturnEmptyList() {
        List<Person> result = dataTransformService.transformToPersons(null, "person");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test transforming empty list should return empty list.
     * Verifies that the service handles empty input collections properly.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @Test
    void testTransformToPersons_EmptyList_ShouldReturnEmptyList() {
        List<Object> emptyList = new ArrayList<>();
        List<Person> result = dataTransformService.transformToPersons(emptyList, "person");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test transforming valid objects should return transformed persons.
     * Verifies that the service successfully transforms objects into Person format.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @Test
    void testTransformToPersons_ValidObjects_ShouldReturnTransformedPersons() {
        List<Object> testObjects = Arrays.asList(
            createTestPerson(1L, "Test", "User", "test@example.com", 25, "IT", 50000.0),
            "secondObject",
            "thirdObject"
        );
        
        List<Person> result = dataTransformService.transformToPersons(testObjects, "person");
        
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify that we have a person with the expected data (order may vary due to auto-sorting)
        boolean foundExpectedPerson = result.stream()
            .anyMatch(person -> "Test".equals(person.getFirstName()) 
                     && "User".equals(person.getLastName()) 
                     && "test@example.com".equals(person.getEmail()));
        assertTrue(foundExpectedPerson, "Expected person with Test/User/test@example.com should be found");
        
        // Verify all persons have valid IDs
        result.forEach(person -> assertNotNull(person.getId()));
    }

    /**
     * Test transforming for CSV output should prepare persons correctly.
     * Verifies that Person objects are properly formatted for CSV generation.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @Test
    void testTransformForCsvOutput_ValidPersons_ShouldReturnCsvReadyPersons() {
        List<Person> result = dataTransformService.transformForCsvOutput(testPersons);
        
        assertNotNull(result);
        assertEquals(testPersons.size(), result.size());
        
        // Verify CSV formatting is applied
        for (Person person : result) {
            assertNotNull(person);
            assertNotNull(person.getId());
        }
    }

    /**
     * Test transforming null persons for CSV should return empty list.
     * Verifies graceful handling of null input for CSV transformation.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @Test
    void testTransformForCsvOutput_NullInput_ShouldReturnEmptyList() {
        List<Person> result = dataTransformService.transformForCsvOutput(null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test transforming empty persons list for CSV should return empty list.
     * Verifies handling of empty collections for CSV transformation.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @Test
    void testTransformForCsvOutput_EmptyList_ShouldReturnEmptyList() {
        List<Person> emptyList = new ArrayList<>();
        List<Person> result = dataTransformService.transformForCsvOutput(emptyList);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test transformation statistics generation should return formatted statistics.
     * Verifies that statistical information is correctly calculated and formatted.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @Test
    void testGetTransformationStatistics_ValidInput_ShouldReturnFormattedStats() {
        String stats = dataTransformService.getTransformationStatistics(100, 95, "JSON");
        
        assertNotNull(stats);
        assertTrue(stats.contains("Source Type: JSON"));
        assertTrue(stats.contains("Original Objects: 100"));
        assertTrue(stats.contains("Transformed Successfully: 95"));
        assertTrue(stats.contains("Success Rate: 95.00%"));
        assertTrue(stats.contains("Failed Transformations: 5"));
    }

    /**
     * Test transformation statistics with zero input should handle correctly.
     * Verifies proper handling of edge cases in statistics calculation.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @Test
    void testGetTransformationStatistics_ZeroInput_ShouldHandleGracefully() {
        String stats = dataTransformService.getTransformationStatistics(0, 0, "Test");
        
        assertNotNull(stats);
        assertTrue(stats.contains("Source Type: Test"));
        assertTrue(stats.contains("Original Objects: 0"));
        assertTrue(stats.contains("Transformed Successfully: 0"));
        assertTrue(stats.contains("Success Rate: 0.00%"));
    }

    /**
     * Test transformation statistics with partial success should calculate correctly.
     * Verifies accuracy of success rate calculations with partial transformations.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @Test
    void testGetTransformationStatistics_PartialSuccess_ShouldCalculateCorrectly() {
        String stats = dataTransformService.getTransformationStatistics(10, 7, "Publication");
        
        assertNotNull(stats);
        assertTrue(stats.contains("Success Rate: 70.00%"));
        assertTrue(stats.contains("Failed Transformations: 3"));
    }

    /**
     * Test CSV transformation with persons containing special characters.
     * Verifies that special characters in person data are handled properly for CSV output.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @Test
    void testTransformForCsvOutput_PersonsWithSpecialCharacters_ShouldSanitize() {
        List<Person> personsWithSpecialChars = Arrays.asList(
            createTestPerson(1L, "John,Test", "Doe\"Quote", "test@example.com", 30, "Department\nNewline", 50000.0)
        );
        
        List<Person> result = dataTransformService.transformForCsvOutput(personsWithSpecialChars);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Person csvPerson = result.get(0);
        // Verify that newlines are replaced with spaces
        assertFalse(csvPerson.getDepartment().contains("\n"));
    }

    /**
     * Test transformation with different source types should handle appropriately.
     * Verifies that the service correctly processes different data source types.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @Test
    void testTransformToPersons_DifferentSourceTypes_ShouldHandleAppropriately() {
        List<Object> testObjects = Arrays.asList("testObject");
        
        // Test different source types
        String[] sourceTypes = {"person", "publication", "scientific", "unknown"};
        
        for (String sourceType : sourceTypes) {
            List<Person> result = dataTransformService.transformToPersons(testObjects, sourceType);
            assertNotNull(result, "Result should not be null for source type: " + sourceType);
            assertEquals(1, result.size(), "Should transform one object for source type: " + sourceType);
        }
    }

    /**
     * Test CSV transformation preserves essential person data.
     * Verifies that important person information is maintained during CSV transformation.
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    @Test
    void testTransformForCsvOutput_PreservesEssentialData_ShouldMaintainPersonData() {
        Person originalPerson = createTestPerson(1L, "John", "Doe", "john@example.com", 30, "Engineering", 75000.0);
        List<Person> persons = Arrays.asList(originalPerson);
        
        List<Person> result = dataTransformService.transformForCsvOutput(persons);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Person csvPerson = result.get(0);
        assertEquals(originalPerson.getId(), csvPerson.getId());
        assertEquals(originalPerson.getFirstName(), csvPerson.getFirstName());
        assertEquals(originalPerson.getLastName(), csvPerson.getLastName());
        assertEquals(originalPerson.getEmail(), csvPerson.getEmail());
        assertEquals(originalPerson.getAge(), csvPerson.getAge());
        assertEquals(originalPerson.getDepartment(), csvPerson.getDepartment());
        assertEquals(originalPerson.getSalary(), csvPerson.getSalary());
    }

    /**
     * Helper method to create test Person objects.
     * Provides consistent test data creation for all test methods.
     * 
     * @param id the person ID
     * @param firstName the first name
     * @param lastName the last name
     * @param email the email address
     * @param age the age
     * @param department the department
     * @param salary the salary
     * @return configured Person object for testing
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private Person createTestPerson(Long id, String firstName, String lastName, String email, 
                                   Integer age, String department, Double salary) {
        Person person = new Person();
        person.setId(id);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmail(email);
        person.setAge(age);
        person.setDepartment(department);
        person.setSalary(salary);
        return person;
    }
}