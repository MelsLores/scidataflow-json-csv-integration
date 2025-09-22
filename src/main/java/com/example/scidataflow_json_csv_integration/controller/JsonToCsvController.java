package com.example.scidataflow_json_csv_integration.controller;

import com.example.scidataflow_json_csv_integration.service.JsonToCsvConverterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for JSON to CSV file conversion.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
@RestController
@RequestMapping("/api/v1/converter")
public class JsonToCsvController {

    private final JsonToCsvConverterService converterService;

    /**
     * Constructor that injects the conversion service.
     *
     * @param converterService JSON to CSV conversion service
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public JsonToCsvController(JsonToCsvConverterService converterService) {
        this.converterService = converterService;
    }

    /**
     * Endpoint to check the API status.
     *
     * @return Response with the API status
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "JSON to CSV Converter");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * Converts a JSON file to CSV.
     *
     * @param request Object with conversion parameters
     * @return Response with the conversion result
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @PostMapping("/convert")
    public ResponseEntity<Map<String, Object>> convertJsonToCsv(@RequestBody ConversionRequest request) {
        try {
            converterService.convertJsonToCsv(request.getJsonFilePath(), request.getCsvFilePath());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Conversion completed successfully");
            response.put("inputFile", request.getJsonFilePath());
            response.put("outputFile", request.getCsvFilePath());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Conversion failed: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Obtiene información básica del servicio.
     *
     * @return Respuesta con información del servicio
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "JSON to CSV Converter Service");
        response.put("version", "1.0.0");
        response.put("author", "MelsLores");
        response.put("description", "Servicio para convertir archivos JSON a formato CSV");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /health", "Verificar estado del servicio");
        endpoints.put("POST /convert", "Convertir JSON a CSV");
        endpoints.put("GET /info", "Información del servicio");
        
        response.put("endpoints", endpoints);
        return ResponseEntity.ok(response);
    }

    /**
     * Clase interna para la solicitud de conversión básica.
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public static class ConversionRequest {
        private String jsonFilePath;
        private String csvFilePath;

        /**
         * Constructor por defecto para ConversionRequest.
         * @author Melany Rivera
         * @since 21/09/2025
         */
        public ConversionRequest() {}

        /**
         * Constructor con parámetros para ConversionRequest.
         * @param jsonFilePath Ruta del archivo JSON de entrada
         * @param csvFilePath Ruta del archivo CSV de salida
         * @author Melany Rivera
         * @since 21/09/2025
         */
        public ConversionRequest(String jsonFilePath, String csvFilePath) {
            this.jsonFilePath = jsonFilePath;
            this.csvFilePath = csvFilePath;
        }

        /**
         * Obtiene la ruta del archivo JSON.
         * @return La ruta del archivo JSON
         * @author Melany Rivera
         * @since 21/09/2025
         */
        public String getJsonFilePath() { return jsonFilePath; }
        
        /**
         * Establece la ruta del archivo JSON.
         * @param jsonFilePath La nueva ruta del archivo JSON
         * @author Melany Rivera
         * @since 21/09/2025
         */
        public void setJsonFilePath(String jsonFilePath) { this.jsonFilePath = jsonFilePath; }
        
        /**
         * Obtiene la ruta del archivo CSV.
         * @return La ruta del archivo CSV
         * @author Melany Rivera
         * @since 21/09/2025
         */
        public String getCsvFilePath() { return csvFilePath; }
        
        /**
         * Establece la ruta del archivo CSV.
         * @param csvFilePath La nueva ruta del archivo CSV
         * @author Melany Rivera
         * @since 21/09/2025
         */
        public void setCsvFilePath(String csvFilePath) { this.csvFilePath = csvFilePath; }
    }
}