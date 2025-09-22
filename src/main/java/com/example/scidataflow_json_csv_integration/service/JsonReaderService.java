package com.example.scidataflow_json_csv_integration.service;

import com.example.scidataflow_json_csv_integration.exception.JsonProcessingException;
import com.example.scidataflow_json_csv_integration.model.Person;
import com.example.scidataflow_json_csv_integration.model.Publication;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JsonReaderService {

    private final ObjectMapper objectMapper;

    public JsonReaderService() {
        this.objectMapper = new ObjectMapper();
    }

    public List<Person> readPersonsFromJson(String filePath) throws JsonProcessingException {
        log.info("Starting to read JSON file: {}", filePath);
        
        try {
            if (filePath == null || filePath.trim().isEmpty()) {
                throw new JsonProcessingException("File path cannot be null or empty");
            }
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new JsonProcessingException("File does not exist: " + filePath);
            }
            
            if (!Files.isReadable(path)) {
                throw new JsonProcessingException("File is not readable: " + filePath);
            }
            
            JsonNode rootNode = objectMapper.readTree(new File(filePath));
            
            List<Person> persons = tryParseAsPersonArray(rootNode);
            if (persons != null && !persons.isEmpty()) {
                log.info("Successfully parsed {} persons from array structure", persons.size());
                return persons;
            }
            
            Person singlePerson = tryParseAsSinglePerson(rootNode);
            if (singlePerson != null) {
                log.info("Successfully parsed single person from JSON");
                return Arrays.asList(singlePerson);
            }
            
            persons = tryParseAsScientometricsReport(rootNode);
            if (persons != null && !persons.isEmpty()) {
                log.info("Successfully converted {} publications to person format", persons.size());
                return persons;
            }
            
            throw new JsonProcessingException("Unable to parse JSON file - unknown structure: " + filePath);
            
        } catch (IOException e) {
            String errorMessage = "Failed to read JSON file: " + filePath + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new JsonProcessingException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Unexpected error while processing JSON file: " + filePath + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new JsonProcessingException(errorMessage, e);
        }
    }

    public Person readPersonFromJson(String filePath) throws JsonProcessingException {
        List<Person> persons = readPersonsFromJson(filePath);
        if (persons.isEmpty()) {
            throw new JsonProcessingException("No person found in JSON file: " + filePath);
        }
        return persons.get(0); // Return the first person
    }

    private List<Person> tryParseAsPersonArray(JsonNode rootNode) {
        try {
            if (rootNode.isArray()) {
                log.debug("Attempting to parse as Person array");
                return objectMapper.convertValue(rootNode, new TypeReference<List<Person>>() {});
            } else if (rootNode.has("persons") && rootNode.get("persons").isArray()) {
                log.debug("Attempting to parse nested persons array");
                JsonNode personsNode = rootNode.get("persons");
                return objectMapper.convertValue(personsNode, new TypeReference<List<Person>>() {});
            }
        } catch (Exception e) {
            log.debug("Failed to parse as Person array: {}", e.getMessage());
        }
        return null;
    }

    private Person tryParseAsSinglePerson(JsonNode rootNode) {
        try {
            if (rootNode.isObject() && !rootNode.isArray()) {
                log.debug("Attempting to parse as single Person object");
                
                if (rootNode.has("firstName") || rootNode.has("lastName") || 
                    rootNode.has("email") || rootNode.has("age")) {
                    return objectMapper.convertValue(rootNode, Person.class);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse as single Person: {}", e.getMessage());
        }
        return null;
    }

    private List<Person> tryParseAsScientometricsReport(JsonNode rootNode) {
        try {
            List<Publication> publications = null;
            
            if (rootNode.isArray()) {
                log.debug("Attempting to parse as Publication array");
                publications = objectMapper.convertValue(rootNode, new TypeReference<List<Publication>>() {});
            }
            else if (rootNode.has("publications") && rootNode.get("publications").isArray()) {
                log.debug("Attempting to parse nested publications array");
                JsonNode publicationsNode = rootNode.get("publications");
                publications = objectMapper.convertValue(publicationsNode, new TypeReference<List<Publication>>() {});
            }
            else if (rootNode.isObject() && (rootNode.has("title") || rootNode.has("authors") || rootNode.has("journal"))) {
                log.debug("Attempting to parse as single Publication object");
                Publication singlePub = objectMapper.convertValue(rootNode, Publication.class);
                publications = Arrays.asList(singlePub);
            }
            
            if (publications != null && !publications.isEmpty()) {
                return convertPublicationsToPersons(publications);
            }
            
        } catch (Exception e) {
            log.debug("Failed to parse as scientometrics report: {}", e.getMessage());
        }
        return null;
    }

    private List<Person> convertPublicationsToPersons(List<Publication> publications) {
        log.debug("Converting {} publications to Person objects", publications.size());
        
        List<Person> persons = new ArrayList<>();
        
        for (int i = 0; i < publications.size(); i++) {
            Publication pub = publications.get(i);
            Person person = new Person();
            
            person.setId((long) (i + 1));
            
            String title = pub.getTitle();
            if (title != null && title.length() > 50) {
                title = title.substring(0, 47) + "...";
            }
            person.setFirstName(title);
            
            person.setLastName(pub.getJournal());
            
            String email = "publication" + (i + 1) + "@journal.com";
            person.setEmail(email);
            
            if (pub.getYear() != null) {
                person.setAge(pub.getYear());
            } else {
                person.setAge(2023);
            }
            
            if (pub.getAuthors() != null && !pub.getAuthors().isEmpty()) {
                String department = pub.getAuthors().get(0);
                if (pub.getAuthors().size() > 1) {
                    department += " et al.";
                }
                person.setDepartment(department);
            } else {
                person.setDepartment("Unknown Author");
            }
            
            if (pub.getCitations() != null) {
                person.setSalary(pub.getCitations().doubleValue() * 100);
            } else {
                person.setSalary(0.0);
            }
            
            persons.add(person);
        }
        
        log.debug("Successfully converted {} publications to persons", persons.size());
        return persons;
    }

    public boolean isValidJsonFile(String filePath) {
        try {
            if (filePath == null || filePath.trim().isEmpty()) {
                return false;
            }
            
            Path path = Paths.get(filePath);
            return Files.exists(path) && Files.isReadable(path) && Files.isRegularFile(path);
        } catch (Exception e) {
            log.warn("Error validating JSON file path: {}", filePath, e);
            return false;
        }
    }
}
