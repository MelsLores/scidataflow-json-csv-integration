package com.example.scidataflow_json_csv_integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa a una persona con información básica.
 * Esta clase sirve como objeto de transferencia de datos para operaciones de conversión JSON-CSV.
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