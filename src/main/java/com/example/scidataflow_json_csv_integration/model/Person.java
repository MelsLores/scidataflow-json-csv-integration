package com.example.scidataflow_json_csv_integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents a person with basic information.
 * This class serves as a data transfer object for JSON-CSV conversion operations.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    
    /**
     * Unique identifier for the person
     */
    @JsonProperty("id")
    private Long id;
    
    /**
     * First name of the person
     */
    @JsonProperty("firstName")
    private String firstName;
    
    /**
     * Last name of the person
     */
    @JsonProperty("lastName")
    private String lastName;
    
    /**
     * Email address of the person
     */
    @JsonProperty("email")
    private String email;
    
    /**
     * Age of the person
     */
    @JsonProperty("age")
    private Integer age;
    
    /**
     * Department where the person works
     */
    @JsonProperty("department")
    private String department;
    
    /**
     * Annual salary of the person
     */
    @JsonProperty("salary")
    private Double salary;
}