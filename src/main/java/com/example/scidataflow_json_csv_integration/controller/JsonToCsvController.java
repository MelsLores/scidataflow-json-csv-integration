package com.example.scidataflow_json_csv_integration.controller;

import com.example.scidataflow_json_csv_integration.service.JsonToCsvConverterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para la conversión de archivos JSON a CSV.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
@RestController
@RequestMapping("/api/v1/converter")
public class JsonToCsvController {

    private final JsonToCsvConverterService converterService;

    /**
     * Constructor que inyecta el servicio de conversión.
     *
     * @param converterService Servicio de conversión JSON a CSV
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public JsonToCsvController(JsonToCsvConverterService converterService) {
        this.converterService = converterService;
    }

    /**
     * Endpoint para verificar el estado de la API.
     *
     * @return Respuesta con el estado de la API
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
     * Convierte un archivo JSON a CSV.
     *
     * @param request Objeto con los parámetros de conversión
     * @return Respuesta con el resultado de la conversión
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

        // Constructores
        public ConversionRequest() {}

        public ConversionRequest(String jsonFilePath, String csvFilePath) {
            this.jsonFilePath = jsonFilePath;
            this.csvFilePath = csvFilePath;
        }

        // Getters y Setters
        public String getJsonFilePath() { return jsonFilePath; }
        public void setJsonFilePath(String jsonFilePath) { this.jsonFilePath = jsonFilePath; }
        
        public String getCsvFilePath() { return csvFilePath; }
        public void setCsvFilePath(String csvFilePath) { this.csvFilePath = csvFilePath; }
    }
}