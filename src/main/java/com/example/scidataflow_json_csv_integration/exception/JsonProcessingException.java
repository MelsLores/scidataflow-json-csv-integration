package com.example.scidataflow_json_csv_integration.exception;

/**
 * Clase de excepción personalizada para manejar errores de procesamiento JSON.
 * Esta excepción se lanza cuando hay problemas leyendo o analizando archivos JSON.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
public class JsonProcessingException extends Exception {
    
    /**
     * Construye una nueva JsonProcessingException con el mensaje de detalle especificado.
     * 
     * @param message el mensaje de detalle que explica la causa de la excepción
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public JsonProcessingException(String message) {
        super(message);
    }
    
    /**
     * Construye una nueva JsonProcessingException con el mensaje de detalle y causa especificados.
     * 
     * @param message el mensaje de detalle que explica la causa de la excepción
     * @param cause la causa de la excepción
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public JsonProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}