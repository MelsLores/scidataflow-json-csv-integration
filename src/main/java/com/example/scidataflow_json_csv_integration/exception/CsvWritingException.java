package com.example.scidataflow_json_csv_integration.exception;

/**
 * Clase de excepción personalizada para manejar errores de escritura CSV.
 * Esta excepción se lanza cuando hay problemas escribiendo datos a archivos CSV.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
public class CsvWritingException extends Exception {
    
    /**
     * Construye una nueva CsvWritingException con el mensaje de detalle especificado.
     * 
     * @param message el mensaje de detalle que explica la causa de la excepción
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public CsvWritingException(String message) {
        super(message);
    }
    
    /**
     * Construye una nueva CsvWritingException con el mensaje de detalle y causa especificados.
     * 
     * @param message el mensaje de detalle que explica la causa de la excepción
     * @param cause la causa de la excepción
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public CsvWritingException(String message, Throwable cause) {
        super(message, cause);
    }
}