package com.example.scidataflow_json_csv_integration.exception;

/**
 * Custom exception class for handling CSV writing errors.
 * This exception is thrown when there are problems writing data to CSV files.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
public class CsvWritingException extends Exception {
    
    /**
     * Constructs a new CsvWritingException with the specified detail message.
     * 
     * @param message the detail message that explains the cause of the exception
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public CsvWritingException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new CsvWritingException with the specified detail message and cause.
     * 
     * @param message the detail message that explains the cause of the exception
     * @param cause the cause of the exception
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public CsvWritingException(String message, Throwable cause) {
        super(message, cause);
    }
}