package com.example.scidataflow_json_csv_integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the JSON-CSV conversion system.
 * 
 * This Spring Boot application provides a complete service for converting
 * JSON files to CSV format, including support for multiple formats,
 * data validation, and a web interface for user interaction.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
@SpringBootApplication
public class ScidataflowJsonCsvIntegrationApplication {

	/**
	 * Main method that starts the Spring Boot application.
	 * 
	 * @param args command line arguments passed to the application
	 * @author Melany Rivera
	 * @since 21/09/2025
	 */
	public static void main(String[] args) {
		SpringApplication.run(ScidataflowJsonCsvIntegrationApplication.class, args);
	}

}
