package com.example.scidataflow_json_csv_integration.service;

import com.example.scidataflow_json_csv_integration.model.Person;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service dedicated to universal data transformation operations.
 * Handles automatic data transformation between different formats and structures,
 * including intelligent field mapping, data validation, format conversions, and automatic sorting.
 * This service is completely generic and can handle any type of source data automatically
 * without any predefined mappings or specific data types.
 * 
 * @author Melany Rivera
 * @since 25/09/2025
 */
@Slf4j
@Service
public class DataTransformService {

    /**
     * Transforms a list of JSON objects into Person objects with automatic sorting.
     * This is the core universal transformation method that applies intelligent mapping,
     * data validation, and automatic field ordering to ensure consistent Person object creation
     * from any type of source data.
     * 
     * @param jsonObjects the raw JSON objects to transform
     * @param sourceType the type of source data (for logging purposes only)
     * @return list of transformed and sorted Person objects
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    public List<Person> transformToPersons(List<Object> jsonObjects, String sourceType) {
        log.info("Starting universal data transformation for {} objects", 
                jsonObjects != null ? jsonObjects.size() : 0);
        
        if (jsonObjects == null || jsonObjects.isEmpty()) {
            log.warn("No objects provided for transformation");
            return new ArrayList<>();
        }
        
        List<Person> transformedPersons = new ArrayList<>();
        
        // Sort input data first if it's disordered
        List<Object> sortedJsonObjects = sortInputData(jsonObjects);
        
        for (int i = 0; i < sortedJsonObjects.size(); i++) {
            Object jsonObject = sortedJsonObjects.get(i);
            Person transformedPerson = null;
            
            // Apply universal intelligent mapping for any data type
            transformedPerson = applyUniversalMapping(jsonObject, i + 1);
            
            if (transformedPerson != null) {
                transformedPerson = validateAndCleanPersonData(transformedPerson);
                transformedPersons.add(transformedPerson);
            }
        }
        
        // Sort the final result by natural order
        transformedPersons = sortTransformedPersons(transformedPersons);
        
        log.info("Successfully transformed and sorted {} objects to Person format", transformedPersons.size());
        return transformedPersons;
    }
    
    /**
     * Sorts input data automatically to handle disordered JSON objects.
     * Uses intelligent sorting based on data type, field concordance, and semantic similarity.
     * Groups similar data types together and orders by relevance and completeness.
     * 
     * @param inputObjects the raw input objects to sort
     * @return sorted list of objects grouped by type and concordance
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private List<Object> sortInputData(List<Object> inputObjects) {
        if (inputObjects == null || inputObjects.size() <= 1) {
            return inputObjects;
        }
        
        log.debug("Sorting {} input objects by type, concordance, and data quality", inputObjects.size());
        
        // Group objects by data type and semantic similarity
        Map<String, List<Object>> groupedObjects = groupObjectsByTypeAndConcordance(inputObjects);
        
        // Sort groups by priority (Person-like data first, then Publications, then others)
        List<String> groupPriority = Arrays.asList("PERSON", "PUBLICATION", "MEDICAL", "PRODUCT", "STUDENT", "MIXED", "STRING", "UNKNOWN");
        
        List<Object> sortedObjects = new ArrayList<>();
        
        for (String groupType : groupPriority) {
            if (groupedObjects.containsKey(groupType)) {
                List<Object> group = groupedObjects.get(groupType);
                
                // Sort within each group by completeness and field quality
                group.sort((obj1, obj2) -> {
                    int completeness1 = calculateDataCompleteness(obj1);
                    int completeness2 = calculateDataCompleteness(obj2);
                    
                    // If completeness is similar, sort by field concordance
                    if (Math.abs(completeness1 - completeness2) <= 10) {
                        int concordance1 = calculateFieldConcordance(obj1, groupType);
                        int concordance2 = calculateFieldConcordance(obj2, groupType);
                        return Integer.compare(concordance2, concordance1); // Descending
                    }
                    
                    return Integer.compare(completeness2, completeness1); // Descending
                });
                
                sortedObjects.addAll(group);
                log.debug("Added {} objects from group {} to sorted list", group.size(), groupType);
            }
        }
        
        return sortedObjects;
    }
    
    /**
     * Calculates data completeness score for an object.
     * Higher scores indicate more complete data.
     * 
     * @param obj the object to analyze
     * @return completeness score (0-100)
     */
    private int calculateDataCompleteness(Object obj) {
        if (obj == null) return 0;
        
        int nonNullFields = 0;
        int totalFields = 0;
        
        try {
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                totalFields = map.size();
                for (Object value : map.values()) {
                    if (value != null && !value.toString().trim().isEmpty()) {
                        nonNullFields++;
                    }
                }
            } else if (obj instanceof String) {
                return obj.toString().trim().isEmpty() ? 0 : 50;
            } else {
                // Use reflection for generic objects
                java.lang.reflect.Field[] fields = obj.getClass().getDeclaredFields();
                totalFields = fields.length;
                for (java.lang.reflect.Field field : fields) {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value != null && !value.toString().trim().isEmpty()) {
                        nonNullFields++;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not calculate completeness for object: {}", e.getMessage());
            return 25; // Default medium score
        }
        
        return totalFields > 0 ? (nonNullFields * 100) / totalFields : 0;
    }
    
    /**
     * Groups objects by their data type and semantic concordance.
     * Analyzes field patterns to identify similar data structures.
     * 
     * @param objects the objects to group
     * @return map of group type to list of objects
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private Map<String, List<Object>> groupObjectsByTypeAndConcordance(List<Object> objects) {
        Map<String, List<Object>> groups = new LinkedHashMap<>();
        
        for (Object obj : objects) {
            String objectType = determineObjectType(obj);
            groups.computeIfAbsent(objectType, k -> new ArrayList<>()).add(obj);
        }
        
        log.debug("Grouped {} objects into {} type categories", objects.size(), groups.size());
        return groups;
    }
    
    /**
     * Determines the semantic type of an object based on its field patterns.
     * 
     * @param obj the object to analyze
     * @return the determined object type
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private String determineObjectType(Object obj) {
        if (obj == null) return "UNKNOWN";
        
        if (obj instanceof String) return "STRING";
        
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            Set<String> fieldNames = map.keySet().stream()
                .map(key -> key.toString().toLowerCase())
                .collect(Collectors.toSet());
            
            // Check for person-like patterns
            long personFields = fieldNames.stream()
                .filter(field -> field.matches(".*(name|nombre|nom|namen|first.*name|last.*name|surname|apellido|email|correo|age|edad|department|departamento|salary|salario).*"))
                .count();
                
            // Check for publication-like patterns
            long publicationFields = fieldNames.stream()
                .filter(field -> field.matches(".*(title|titulo|titre|titel|journal|revista|author|autor|publication|publicacion|doi|issn|year|año).*"))
                .count();
                
            // Check for medical-like patterns
            long medicalFields = fieldNames.stream()
                .filter(field -> field.matches(".*(patient|paciente|diagnosis|diagnostico|treatment|tratamiento|doctor|medico|hospital|clinic).*"))
                .count();
                
            // Check for product-like patterns
            long productFields = fieldNames.stream()
                .filter(field -> field.matches(".*(product|producto|price|precio|category|categoria|inventory|inventario|stock|description).*"))
                .count();
                
            // Check for student-like patterns
            long studentFields = fieldNames.stream()
                .filter(field -> field.matches(".*(student|estudiante|grade|nota|course|curso|university|universidad|career|carrera).*"))
                .count();
            
            // Determine the most likely type
            if (personFields >= 2) return "PERSON";
            if (publicationFields >= 2) return "PUBLICATION";
            if (medicalFields >= 2) return "MEDICAL";
            if (productFields >= 2) return "PRODUCT";
            if (studentFields >= 2) return "STUDENT";
            
            return "MIXED";
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Calculates field concordance score based on how well fields match the expected type.
     * 
     * @param obj the object to analyze
     * @param expectedType the expected group type
     * @return concordance score (0-100)
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private int calculateFieldConcordance(Object obj, String expectedType) {
        if (obj == null || !(obj instanceof Map)) return 0;
        
        Map<?, ?> map = (Map<?, ?>) obj;
        Set<String> fieldNames = map.keySet().stream()
            .map(key -> key.toString().toLowerCase())
            .collect(Collectors.toSet());
        
        int matchingFields = 0;
        int totalFields = fieldNames.size();
        
        switch (expectedType) {
            case "PERSON":
                matchingFields = (int) fieldNames.stream()
                    .filter(field -> field.matches(".*(name|nombre|nom|namen|first.*name|last.*name|surname|apellido|email|correo|age|edad|department|departamento|salary|salario).*"))
                    .count();
                break;
            case "PUBLICATION":
                matchingFields = (int) fieldNames.stream()
                    .filter(field -> field.matches(".*(title|titulo|titre|titel|journal|revista|author|autor|publication|publicacion|doi|issn|year|año).*"))
                    .count();
                break;
            case "MEDICAL":
                matchingFields = (int) fieldNames.stream()
                    .filter(field -> field.matches(".*(patient|paciente|diagnosis|diagnostico|treatment|tratamiento|doctor|medico|hospital|clinic).*"))
                    .count();
                break;
            case "PRODUCT":
                matchingFields = (int) fieldNames.stream()
                    .filter(field -> field.matches(".*(product|producto|price|precio|category|categoria|inventory|inventario|stock|description).*"))
                    .count();
                break;
            case "STUDENT":
                matchingFields = (int) fieldNames.stream()
                    .filter(field -> field.matches(".*(student|estudiante|grade|nota|course|curso|university|universidad|career|carrera).*"))
                    .count();
                break;
            default:
                return 25; // Default score for mixed/unknown types
        }
        
        return totalFields > 0 ? (matchingFields * 100) / totalFields : 0;
    }
    
    /**
     * Applies universal intelligent mapping for any data type to Person format.
     * Uses advanced heuristics and multi-language field recognition without any predefined mappings.
     * 
     * @param unknownObject the object to transform
     * @param index the index for ID assignment
     * @return transformed Person object or null if transformation fails
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private Person applyUniversalMapping(Object unknownObject, int index) {
        log.debug("Applying universal mapping for object at index {}", index);
        
        if (unknownObject == null) {
            return null;
        }
        
        Person person = new Person();
        person.setId((long) index);
        
        try {
            // If it's already a Person object, cast it directly
            if (unknownObject instanceof Person) {
                Person originalPerson = (Person) unknownObject;
                person.setFirstName(originalPerson.getFirstName());
                person.setLastName(originalPerson.getLastName());
                person.setEmail(originalPerson.getEmail());
                person.setAge(originalPerson.getAge());
                person.setDepartment(originalPerson.getDepartment());
                person.setSalary(originalPerson.getSalary());
                log.debug("Direct Person mapping applied for index {}", index);
                return person;
            }
            
            // For Map-like objects (most common from JSON parsing)
            if (unknownObject instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) unknownObject;
                return mapFromAnyMapObject(map, index);
            }
            
            // For String objects, apply intelligent string parsing
            if (unknownObject instanceof String) {
                String stringValue = (String) unknownObject;
                return parseFromStringData(stringValue, index);
            }
            
            // For any other generic objects, use universal reflection
            return mapFromAnyGenericObject(unknownObject, index);
            
        } catch (Exception e) {
            log.warn("Failed to apply universal mapping for object at index {}: {}", index, e.getMessage());
            // Return a minimal person object with just ID
            return person;
        }
    }
    
    /**
     * Sorts transformed Person objects using intelligent semantic ordering.
     * Groups by data completeness, semantic similarity, and natural alphabetical order.
     * Handles disordered data by applying multiple sorting criteria with prioritization.
     * 
     * @param persons the list of Person objects to sort
     * @return sorted list of Person objects grouped by relevance and concordance
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private List<Person> sortTransformedPersons(List<Person> persons) {
        if (persons == null || persons.size() <= 1) {
            return persons;
        }
        
        log.debug("Sorting {} transformed Person objects using intelligent semantic ordering", persons.size());
        
        // Group persons by data quality and type
        Map<String, List<Person>> groupedPersons = groupPersonsByDataQuality(persons);
        
        List<Person> sortedPersons = new ArrayList<>();
        
        // Process groups in order of priority: Complete -> Partial -> Minimal -> Empty
        for (String qualityGroup : Arrays.asList("COMPLETE", "PARTIAL", "MINIMAL", "EMPTY")) {
            if (groupedPersons.containsKey(qualityGroup)) {
                List<Person> group = groupedPersons.get(qualityGroup);
                
                // Sort within each group by semantic relevance
                group.sort((p1, p2) -> {
                    // Primary: Sort by data type concordance (professional data first)
                    int concordance1 = calculatePersonConcordance(p1);
                    int concordance2 = calculatePersonConcordance(p2);
                    
                    if (Math.abs(concordance1 - concordance2) > 10) {
                        return Integer.compare(concordance2, concordance1); // Descending
                    }
                    
                    // Secondary: Sort by completeness within similar concordance
                    int completeness1 = calculatePersonCompleteness(p1);
                    int completeness2 = calculatePersonCompleteness(p2);
                    
                    if (Math.abs(completeness1 - completeness2) > 5) {
                        return Integer.compare(completeness2, completeness1); // Descending
                    }
                    
                    // Tertiary: Alphabetical by last name, then first name
                    if (p1.getLastName() != null && p2.getLastName() != null) {
                        int lastNameComparison = p1.getLastName().compareToIgnoreCase(p2.getLastName());
                        if (lastNameComparison != 0) return lastNameComparison;
                    }
                    
                    if (p1.getFirstName() != null && p2.getFirstName() != null) {
                        int firstNameComparison = p1.getFirstName().compareToIgnoreCase(p2.getFirstName());
                        if (firstNameComparison != 0) return firstNameComparison;
                    }
                    
                    // Final: Sort by ID
                    return Long.compare(p1.getId(), p2.getId());
                });
                
                sortedPersons.addAll(group);
                log.debug("Added {} persons from {} quality group", group.size(), qualityGroup);
            }
        }
        
        return sortedPersons;
    }
    
    /**
     * Groups Person objects by their data quality level.
     * 
     * @param persons the persons to group
     * @return map of quality level to list of persons
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private Map<String, List<Person>> groupPersonsByDataQuality(List<Person> persons) {
        Map<String, List<Person>> groups = new LinkedHashMap<>();
        
        for (Person person : persons) {
            String qualityLevel = determinePersonQualityLevel(person);
            groups.computeIfAbsent(qualityLevel, k -> new ArrayList<>()).add(person);
        }
        
        return groups;
    }
    
    /**
     * Determines the data quality level of a Person object.
     * 
     * @param person the person to analyze
     * @return quality level: COMPLETE, PARTIAL, MINIMAL, or EMPTY
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private String determinePersonQualityLevel(Person person) {
        if (person == null) return "EMPTY";
        
        int filledFields = 0;
        if (person.getFirstName() != null && !person.getFirstName().trim().isEmpty()) filledFields++;
        if (person.getLastName() != null && !person.getLastName().trim().isEmpty()) filledFields++;
        if (person.getEmail() != null && !person.getEmail().trim().isEmpty()) filledFields++;
        if (person.getAge() != null) filledFields++;
        if (person.getDepartment() != null && !person.getDepartment().trim().isEmpty()) filledFields++;
        if (person.getSalary() != null) filledFields++;
        
        if (filledFields >= 5) return "COMPLETE";
        if (filledFields >= 3) return "PARTIAL";
        if (filledFields >= 1) return "MINIMAL";
        return "EMPTY";
    }
    
    /**
     * Calculates concordance score for how well a Person object matches professional/person-like data.
     * 
     * @param person the person to analyze
     * @return concordance score (0-100)
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private int calculatePersonConcordance(Person person) {
        if (person == null) return 0;
        
        int concordanceScore = 0;
        
        // Professional name patterns (higher scores for typical person names)
        if (person.getFirstName() != null && person.getFirstName().matches("^[A-Za-zÀ-ÿ\\s]{2,30}$")) {
            concordanceScore += 20;
        }
        
        if (person.getLastName() != null && person.getLastName().matches("^[A-Za-zÀ-ÿ\\s]{2,30}$")) {
            concordanceScore += 20;
        }
        
        // Valid email pattern
        if (person.getEmail() != null && person.getEmail().matches("^[^@]+@[^@]+\\.[^@]+$")) {
            concordanceScore += 25;
        }
        
        // Reasonable age range
        if (person.getAge() != null && person.getAge() >= 16 && person.getAge() <= 80) {
            concordanceScore += 15;
        }
        
        // Professional department
        if (person.getDepartment() != null && person.getDepartment().length() > 2) {
            concordanceScore += 10;
        }
        
        // Reasonable salary range
        if (person.getSalary() != null && person.getSalary() > 0 && person.getSalary() <= 1000000) {
            concordanceScore += 10;
        }
        
        return concordanceScore;
    }
    
    /**
     * Calculates completeness score for a Person object.
     * 
     * @param person the person to analyze
     * @return completeness score (0-100)
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private int calculatePersonCompleteness(Person person) {
        if (person == null) return 0;
        
        int totalFields = 6; // firstName, lastName, email, age, department, salary
        int filledFields = 0;
        
        if (person.getFirstName() != null && !person.getFirstName().trim().isEmpty()) filledFields++;
        if (person.getLastName() != null && !person.getLastName().trim().isEmpty()) filledFields++;
        if (person.getEmail() != null && !person.getEmail().trim().isEmpty()) filledFields++;
        if (person.getAge() != null) filledFields++;
        if (person.getDepartment() != null && !person.getDepartment().trim().isEmpty()) filledFields++;
        if (person.getSalary() != null) filledFields++;
        
        return (filledFields * 100) / totalFields;
    }
    
    /**
     * Maps data from any Map object to Person format using universal field recognition.
     * Uses advanced multi-language field pattern matching without predefined mappings.
     * 
     * @param map the Map object containing data
     * @param index the index for ID assignment
     * @return mapped Person object
     */
    private Person mapFromAnyMapObject(Map<?, ?> map, int index) {
        Person person = new Person();
        person.setId((long) index);
        
        log.debug("Mapping from Map object with {} fields", map.size());
        
        // Advanced field recognition patterns (supports many languages and formats)
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString().toLowerCase().trim();
            String value = entry.getValue() != null ? entry.getValue().toString().trim() : null;
            
            if (value == null || value.isEmpty()) continue;
            
            // Universal field mapping using pattern matching
            if (isFirstNameField(key)) {
                person.setFirstName(value);
            } else if (isLastNameField(key)) {
                person.setLastName(value);
            } else if (isEmailField(key)) {
                person.setEmail(value);
            } else if (isAgeField(key)) {
                person.setAge(extractNumericValue(value));
            } else if (isDepartmentField(key)) {
                person.setDepartment(value);
            } else if (isSalaryField(key)) {
                person.setSalary(extractCurrencyValue(value));
            } else if (isNestedObject(entry.getValue())) {
                // Handle nested objects recursively
                handleNestedMapping(entry.getValue(), person);
            } else if (isArrayField(entry.getValue())) {
                // Handle arrays (like publications, authors, etc.)
                handleArrayMapping(entry.getValue(), person, key);
            } else if (isPublicationTitleField(key)) {
                // Map publication title to first name
                person.setFirstName(value);
            } else if (isJournalField(key)) {
                // Map journal to last name
                person.setLastName(value);
            } else if (isAuthorField(key)) {
                // Handle single author field
                person.setFirstName(value);
            } else if (person.getFirstName() == null && isNameLikeValue(value)) {
                // If no name found yet and this looks like a name, use it
                person.setFirstName(value);
            }
        }
        
        log.debug("Successfully mapped Map object to Person");
        return person;
    }
    
    /**
     * Parses Person data from string format using intelligent text analysis.
     * 
     * @param stringValue the string to parse
     * @param index the index for ID assignment
     * @return parsed Person object
     */
    private Person parseFromStringData(String stringValue, int index) {
        Person person = new Person();
        person.setId((long) index);
        
        if (stringValue == null || stringValue.trim().isEmpty()) {
            return person;
        }
        
        String cleaned = stringValue.trim();
        log.debug("Parsing string data: {}", cleaned);
        
        // Extract email if present
        String email = extractEmailFromString(cleaned);
        if (email != null) {
            person.setEmail(email);
            cleaned = cleaned.replace(email, "").trim();
        }
        
        // Extract age/numbers
        Integer age = extractAgeFromString(cleaned);
        if (age != null) {
            person.setAge(age);
        }
        
        // Extract salary/currency
        Double salary = extractSalaryFromString(cleaned);
        if (salary != null) {
            person.setSalary(salary);
        }
        
        // What remains should be name-related
        String[] parts = cleaned.split("\\s*[-,]\\s*");
        if (parts.length >= 2) {
            person.setFirstName(parts[0].trim());
            person.setLastName(parts[1].trim());
        } else if (parts.length == 1 && !parts[0].isEmpty()) {
            person.setFirstName(parts[0].trim());
        }
        
        return person;
    }
    
    /**
     * Maps data from any generic object using universal reflection.
     * 
     * @param obj the generic object
     * @param index the index for ID assignment  
     * @return mapped Person object
     */
    private Person mapFromAnyGenericObject(Object obj, int index) {
        Person person = new Person();
        person.setId((long) index);
        
        try {
            Class<?> clazz = obj.getClass();
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            
            log.debug("Mapping generic object with {} fields using reflection", fields.length);
            
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName().toLowerCase().trim();
                Object fieldValue = field.get(obj);
                
                if (fieldValue == null) continue;
                
                String value = fieldValue.toString().trim();
                if (value.isEmpty()) continue;
                
                // Universal field recognition
                if (isFirstNameField(fieldName)) {
                    person.setFirstName(value);
                } else if (isLastNameField(fieldName)) {
                    person.setLastName(value);
                } else if (isEmailField(fieldName)) {
                    person.setEmail(value);
                } else if (isAgeField(fieldName)) {
                    person.setAge(extractNumericValue(value));
                } else if (isDepartmentField(fieldName)) {
                    person.setDepartment(value);
                } else if (isSalaryField(fieldName)) {
                    person.setSalary(extractCurrencyValue(value));
                } else if (person.getFirstName() == null && isNameLikeValue(value)) {
                    person.setFirstName(value);
                }
            }
            
            log.debug("Successfully mapped generic object using reflection");
            
        } catch (Exception e) {
            log.warn("Failed to map object using universal reflection: {}", e.getMessage());
        }
        
        return person;
    }
    
    // ====================== UNIVERSAL FIELD RECOGNITION METHODS ======================
    
    /**
     * Checks if a field name represents a first name using universal patterns.
     */
    private boolean isFirstNameField(String fieldName) {
        if (fieldName == null) return false;
        String lower = fieldName.toLowerCase().trim();
        return lower.matches(".*(firstname|first_name|fname|given_name|prenom|vorname|student_first_name|patient_first_name|product_name|client_name|title|titulo|titre|titel|generated_by).*") ||
               lower.equals("name") || lower.equals("nombre") || lower.equals("nom");
    }
    
    /**
     * Checks if a field name represents a last name using universal patterns.
     */
    private boolean isLastNameField(String fieldName) {
        if (fieldName == null) return false;
        String lower = fieldName.toLowerCase().trim();
        return lower.matches(".*(lastname|last_name|lname|surname|family_name|apellido|nom_famille|nachname|student_last_name|patient_last_name|journal|revista|revue|zeitschrift).*");
    }
    
    /**
     * Checks if a field name represents an email using universal patterns.
     */
    private boolean isEmailField(String fieldName) {
        if (fieldName == null) return false;
        String lower = fieldName.toLowerCase().trim();
        return lower.matches(".*(email|mail|correo|courriel|e_mail|university_email|contact_email|email_address).*");
    }
    
    /**
     * Checks if a field name represents age using universal patterns.
     */
    private boolean isAgeField(String fieldName) {
        if (fieldName == null) return false;
        String lower = fieldName.toLowerCase().trim();
        return lower.matches(".*(age|years|edad|anos|annee|alter|student_age|patient_age|age_years).*");
    }
    
    /**
     * Checks if a field name represents a department using universal patterns.
     */
    private boolean isDepartmentField(String fieldName) {
        if (fieldName == null) return false;
        String lower = fieldName.toLowerCase().trim();
        return lower.matches(".*(department|dept|area|division|departamento|departement|abteilung|major|specialite|studiengang|category|tipo).*");
    }
    
    /**
     * Checks if a field name represents salary/price using universal patterns.
     */
    private boolean isSalaryField(String fieldName) {
        if (fieldName == null) return false;
        String lower = fieldName.toLowerCase().trim();
        return lower.matches(".*(salary|salario|wage|income|sueldo|salaire|gehalt|price|precio|prix|cost).*");
    }
    
    /**
     * Checks if a field name represents a publication title.
     */
    private boolean isPublicationTitleField(String fieldName) {
        if (fieldName == null) return false;
        String lower = fieldName.toLowerCase().trim();
        return lower.matches(".*(title|titulo|titre|titel|publication_title|article_title).*");
    }
    
    /**
     * Checks if a field name represents a journal.
     */
    private boolean isJournalField(String fieldName) {
        if (fieldName == null) return false;
        String lower = fieldName.toLowerCase().trim();
        return lower.matches(".*(journal|revista|revue|zeitschrift|publication|venue).*");
    }
    
    /**
     * Checks if a field name represents an author field.
     */
    private boolean isAuthorField(String fieldName) {
        if (fieldName == null) return false;
        String lower = fieldName.toLowerCase().trim();
        return lower.matches(".*(author|autor|auteur|verfasser|authors|writers|generated_by).*");
    }
    
    /**
     * Checks if a value is an array.
     */
    private boolean isArrayField(Object value) {
        return value instanceof java.util.List || value instanceof Object[] || 
               (value != null && value.getClass().isArray());
    }
    
    /**
     * Checks if a value is a nested object.
     */
    private boolean isNestedObject(Object value) {
        return value instanceof Map || (value != null && !isPrimitiveType(value.getClass()));
    }
    
    /**
     * Checks if a class is a primitive type or wrapper.
     */
    private boolean isPrimitiveType(Class<?> clazz) {
        return clazz.isPrimitive() || 
               clazz == String.class || 
               clazz == Integer.class || 
               clazz == Long.class || 
               clazz == Double.class || 
               clazz == Boolean.class ||
               Number.class.isAssignableFrom(clazz);
    }
    
    /**
     * Checks if a value looks like a person name.
     */
    private boolean isNameLikeValue(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        String trimmed = value.trim();
        // Basic name heuristics: contains only letters, spaces, apostrophes, hyphens
        return trimmed.matches("^[a-zA-ZÀ-ÿ\\s'.-]+$") && 
               trimmed.length() >= 2 && 
               trimmed.length() <= 50 &&
               !trimmed.matches(".*\\d.*"); // No numbers
    }
    
    /**
     * Handles nested object mapping recursively.
     */
    private void handleNestedMapping(Object nestedObject, Person person) {
        try {
            if (nestedObject instanceof Map) {
                Map<?, ?> nestedMap = (Map<?, ?>) nestedObject;
                Person nestedPerson = mapFromAnyMapObject(nestedMap, person.getId().intValue());
                // Merge non-null values
                mergePersonData(person, nestedPerson);
            }
        } catch (Exception e) {
            log.debug("Could not handle nested mapping: {}", e.getMessage());
        }
    }
    
    /**
     * Merges data from source person into target person (only non-null values).
     */
    private void mergePersonData(Person target, Person source) {
        if (source == null) return;
        
        if (target.getFirstName() == null && source.getFirstName() != null) {
            target.setFirstName(source.getFirstName());
        }
        if (target.getLastName() == null && source.getLastName() != null) {
            target.setLastName(source.getLastName());
        }
        if (target.getEmail() == null && source.getEmail() != null) {
            target.setEmail(source.getEmail());
        }
        if (target.getAge() == null && source.getAge() != null) {
            target.setAge(source.getAge());
        }
        if (target.getDepartment() == null && source.getDepartment() != null) {
            target.setDepartment(source.getDepartment());
        }
        if (target.getSalary() == null && source.getSalary() != null) {
            target.setSalary(source.getSalary());
        }
    }
    
    /**
     * Handles array mapping for complex data structures like publications, authors, etc.
     */
    private void handleArrayMapping(Object arrayObject, Person person, String fieldKey) {
        try {
            java.util.List<?> array = null;
            
            // Convert different array types to List
            if (arrayObject instanceof java.util.List) {
                array = (java.util.List<?>) arrayObject;
            } else if (arrayObject instanceof Object[]) {
                array = java.util.Arrays.asList((Object[]) arrayObject);
            } else if (arrayObject != null && arrayObject.getClass().isArray()) {
                // Handle primitive arrays
                java.util.ArrayList<Object> arrayList = new java.util.ArrayList<>();
                int length = java.lang.reflect.Array.getLength(arrayObject);
                for (int i = 0; i < length; i++) {
                    arrayList.add(java.lang.reflect.Array.get(arrayObject, i));
                }
                array = arrayList;
            }
            
            if (array == null || array.isEmpty()) return;
            
            // Handle different types of arrays based on field key
            if (fieldKey.toLowerCase().contains("publication")) {
                handlePublicationsArray(array, person);
            } else if (fieldKey.toLowerCase().contains("author")) {
                handleAuthorsArray(array, person);
            } else {
                // Generic array handling - take first meaningful element
                for (Object item : array) {
                    if (item instanceof Map) {
                        Person mappedPerson = mapFromAnyMapObject((Map<?, ?>) item, person.getId().intValue());
                        mergePersonData(person, mappedPerson);
                        break; // Only take first item for simplicity
                    } else if (item instanceof String && person.getFirstName() == null) {
                        person.setFirstName(item.toString());
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            log.debug("Could not handle array mapping for field {}: {}", fieldKey, e.getMessage());
        }
    }
    
    /**
     * Handles publications array specifically.
     */
    private void handlePublicationsArray(java.util.List<?> publications, Person person) {
        if (publications.isEmpty()) return;
        
        // Take the first publication and extract relevant data
        Object firstPublication = publications.get(0);
        if (firstPublication instanceof Map) {
            Map<?, ?> pub = (Map<?, ?>) firstPublication;
            
            // Extract title as first name
            for (Map.Entry<?, ?> entry : pub.entrySet()) {
                String key = entry.getKey().toString().toLowerCase();
                String value = entry.getValue() != null ? entry.getValue().toString() : null;
                
                if (value == null) continue;
                
                if (key.contains("title") && person.getFirstName() == null) {
                    person.setFirstName(value);
                } else if (key.contains("journal") && person.getLastName() == null) {
                    person.setLastName(value);
                } else if (key.contains("author") && person.getFirstName() == null) {
                    // Handle authors array within publication
                    if (entry.getValue() instanceof java.util.List) {
                        java.util.List<?> authors = (java.util.List<?>) entry.getValue();
                        if (!authors.isEmpty()) {
                            person.setFirstName(authors.get(0).toString());
                        }
                    } else {
                        person.setFirstName(value);
                    }
                }
            }
        }
    }
    
    /**
     * Handles authors array specifically.
     */
    private void handleAuthorsArray(java.util.List<?> authors, Person person) {
        if (authors.isEmpty() || person.getFirstName() != null) return;
        
        // Take first author as name
        Object firstAuthor = authors.get(0);
        if (firstAuthor instanceof String) {
            String authorName = firstAuthor.toString();
            // Try to split author name
            String[] parts = authorName.split("\\s+", 2);
            if (parts.length >= 2) {
                person.setFirstName(parts[0]);
                person.setLastName(parts[1]);
            } else {
                person.setFirstName(authorName);
            }
        }
    }
    
    // ====================== UNIVERSAL VALUE EXTRACTION METHODS ======================
    
    /**
     * Extracts numeric value from any string format.
     */
    private Integer extractNumericValue(String value) {
        if (value == null) return null;
        try {
            // Extract first number found in string
            String numbers = value.replaceAll("[^0-9]", "");
            if (!numbers.isEmpty()) {
                int parsed = Integer.parseInt(numbers);
                return (parsed >= 0 && parsed <= 150) ? parsed : null; // Reasonable age range
            }
        } catch (NumberFormatException e) {
            log.debug("Could not extract numeric value from: {}", value);
        }
        return null;
    }
    
    /**
     * Extracts currency/salary value from any string format.
     */
    private Double extractCurrencyValue(String value) {
        if (value == null) return null;
        try {
            // Remove currency symbols and parse decimal number
            String cleaned = value.replaceAll("[^0-9.]", "");
            if (!cleaned.isEmpty()) {
                return Double.parseDouble(cleaned);
            }
        } catch (NumberFormatException e) {
            log.debug("Could not extract currency value from: {}", value);
        }
        return null;
    }
    
    /**
     * Extracts email from string using regex.
     */
    private String extractEmailFromString(String text) {
        if (text == null) return null;
        
        String emailPattern = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(emailPattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        return matcher.find() ? matcher.group() : null;
    }
    
    /**
     * Extracts age from string using intelligent parsing.
     */
    private Integer extractAgeFromString(String text) {
        if (text == null) return null;
        
        // Look for age patterns like "25 years", "30 años", "35 ans"
        String agePattern = "\\b(\\d{1,3})\\s*(years?|años?|ans?|jahre?)\\b";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(agePattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            try {
                int age = Integer.parseInt(matcher.group(1));
                return (age >= 0 && age <= 150) ? age : null;
            } catch (NumberFormatException e) {
                // Continue to fallback method
            }
        }
        
        // Fallback: extract any number that could be an age
        return extractNumericValue(text);
    }
    
    /**
     * Extracts salary from string using currency pattern recognition.
     */
    private Double extractSalaryFromString(String text) {
        if (text == null) return null;
        
        // Look for currency patterns
        String currencyPattern = "\\b(\\$|€|£|USD|EUR)\\s*(\\d{1,8}(?:[.,]\\d{2})?)\\b";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(currencyPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            try {
                String amountStr = matcher.group(2).replace(",", ".");
                return Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                // Continue to fallback method
            }
        }
        
        // Fallback: extract any decimal number
        return extractCurrencyValue(text);
    }
    
    /**
     * Validates and cleans Person data after transformation.
     * Ensures data integrity and applies business rules.
     * 
     * @param person the Person object to validate and clean
     * @return cleaned and validated Person object
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private Person validateAndCleanPersonData(Person person) {
        if (person == null) {
            return null;
        }
        
        // Apply data validation and cleaning rules
        
        // Clean and validate name fields
        if (person.getFirstName() != null) {
            person.setFirstName(person.getFirstName().trim());
            if (person.getFirstName().length() > 50) {
                person.setFirstName(person.getFirstName().substring(0, 50));
                log.debug("Truncated firstName for person ID {}", person.getId());
            }
        }
        
        if (person.getLastName() != null) {
            person.setLastName(person.getLastName().trim());
            if (person.getLastName().length() > 50) {
                person.setLastName(person.getLastName().substring(0, 50));
                log.debug("Truncated lastName for person ID {}", person.getId());
            }
        }
        
        // Validate email format
        if (person.getEmail() != null) {
            person.setEmail(person.getEmail().trim().toLowerCase());
            if (!isValidEmail(person.getEmail())) {
                log.warn("Invalid email format for person ID {}: {}", person.getId(), person.getEmail());
            }
        }
        
        // Validate age range
        if (person.getAge() != null && (person.getAge() < 0 || person.getAge() > 150)) {
            log.warn("Invalid age for person ID {}: {}", person.getId(), person.getAge());
            person.setAge(null); // Set to null for invalid ages
        }
        
        // Validate salary
        if (person.getSalary() != null && person.getSalary() < 0) {
            log.warn("Negative salary for person ID {}: {}", person.getId(), person.getSalary());
            person.setSalary(0.0); // Set to 0 for negative salaries
        }
        
        return person;
    }
    
    /**
     * Validates email format using a basic regex pattern.
     * 
     * @param email the email string to validate
     * @return true if the email format is valid, false otherwise
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Basic email validation pattern
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }
    
    /**
     * Transforms Person objects to CSV-ready format.
     * Ensures all Person fields are properly formatted for CSV output.
     * 
     * @param persons the list of Person objects to transform
     * @return list of CSV-ready Person objects
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    public List<Person> transformForCsvOutput(List<Person> persons) {
        log.info("Transforming {} persons for CSV output", persons != null ? persons.size() : 0);
        
        if (persons == null || persons.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Person> csvReadyPersons = new ArrayList<>();
        
        for (Person person : persons) {
            Person csvPerson = preparPersonForCsv(person);
            if (csvPerson != null) {
                csvReadyPersons.add(csvPerson);
            }
        }
        
        log.info("Prepared {} persons for CSV output", csvReadyPersons.size());
        return csvReadyPersons;
    }
    
    /**
     * Prepares a single Person object for CSV output.
     * Handles special characters and formatting requirements.
     * 
     * @param person the Person object to prepare
     * @return CSV-ready Person object
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private Person preparPersonForCsv(Person person) {
        if (person == null) {
            return null;
        }
        
        // Create a copy to avoid modifying the original
        Person csvPerson = new Person();
        csvPerson.setId(person.getId());
        
        // Handle potential CSV special characters in string fields
        csvPerson.setFirstName(sanitizeForCsv(person.getFirstName()));
        csvPerson.setLastName(sanitizeForCsv(person.getLastName()));
        csvPerson.setEmail(sanitizeForCsv(person.getEmail()));
        csvPerson.setDepartment(sanitizeForCsv(person.getDepartment()));
        
        // Numeric fields don't need special CSV handling
        csvPerson.setAge(person.getAge());
        csvPerson.setSalary(person.getSalary());
        
        return csvPerson;
    }
    
    /**
     * Sanitizes string values for CSV output.
     * Handles special characters that might cause issues in CSV format.
     * 
     * @param value the string value to sanitize
     * @return sanitized string safe for CSV output
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    private String sanitizeForCsv(String value) {
        if (value == null) {
            return null;
        }
        
        // Remove or replace characters that might cause CSV parsing issues
        String sanitized = value.trim();
        
        // Replace line breaks with spaces
        sanitized = sanitized.replaceAll("[\r\n]+", " ");
        
        // Note: OpenCSV library handles quote escaping automatically,
        // so we don't need to manually escape quotes here
        
        return sanitized;
    }
    
    /**
     * Gets transformation statistics for monitoring and debugging.
     * 
     * @param originalCount the number of original objects
     * @param transformedCount the number of successfully transformed objects
     * @param sourceType the type of source data
     * @return formatted statistics string
     * 
     * @author Melany Rivera
     * @since 25/09/2025
     */
    public String getTransformationStatistics(int originalCount, int transformedCount, String sourceType) {
        double successRate = originalCount > 0 ? (double) transformedCount / originalCount * 100 : 0;
        
        return String.format(
            "Data Transformation Summary:%n" +
            "Source Type: %s%n" +
            "Original Objects: %d%n" +
            "Transformed Successfully: %d%n" +
            "Success Rate: %.2f%%%n" +
            "Failed Transformations: %d",
            sourceType,
            originalCount,
            transformedCount,
            successRate,
            originalCount - transformedCount
        );
    }
}