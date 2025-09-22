package com.example.scidataflow_json_csv_integration.controller;

import com.example.scidataflow_json_csv_integration.service.JsonToCsvConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador web para la interfaz de conversor JSON a CSV.
 * 
 * Este controlador proporciona endpoints web para que los usuarios interactúen
 * con la funcionalidad de conversión JSON-CSV a través de una interfaz de navegador.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
@Controller
public class WebController {

    @Autowired
    private JsonToCsvConverterService converterService;

    /**
     * Muestra la página principal del conversor.
     * 
     * @param model Spring Model para renderizado de plantillas
     * @return el nombre de la plantilla converter
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "JSON to CSV Converter");
        model.addAttribute("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return "converter";
    }

    /**
     * Muestra la página about con información del sistema.
     * 
     * @param model Spring Model para renderizado de plantillas
     * @return el nombre de la plantilla about
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About JSON-CSV Converter");
        model.addAttribute("version", "1.0.0");
        model.addAttribute("description", "Advanced JSON to CSV conversion system with configurable options");
        return "about";
    }

    /**
     * Maneja la carga de archivos JSON y los convierte a CSV.
     * 
     * @param file el archivo JSON subido
     * @param delimiter el delimitador CSV a usar
     * @param redirectAttributes para mensajes flash
     * @return redirección a la página de resultados
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @PostMapping("/convert")
    public String convertFile(@RequestParam("jsonFile") MultipartFile file,
                            @RequestParam(value = "delimiter", defaultValue = ",") String delimiter,
                            RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a JSON file to upload");
            return "redirect:/";
        }

        try {
            // Create uploads and downloads directories
            createDirectoriesIfNotExist();
            
            // Save uploaded file
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String originalFilename = file.getOriginalFilename();
            String jsonFileName = "uploads/" + timestamp + "_" + originalFilename;
            String csvFileName = "downloads/" + timestamp + "_" + getNameWithoutExtension(originalFilename) + ".csv";
            
            Path jsonPath = Paths.get(jsonFileName);
            Files.write(jsonPath, file.getBytes());
            
            // Convert JSON to CSV - using the correct method signature
            char delimiterChar = delimiter.length() > 0 ? delimiter.charAt(0) : ',';
            converterService.convertJsonToCsv(jsonFileName, csvFileName, delimiterChar, '"', '\\');
            
            // Add success information
            redirectAttributes.addFlashAttribute("success", "File converted successfully!");
            redirectAttributes.addFlashAttribute("originalFile", originalFilename);
            redirectAttributes.addFlashAttribute("csvFile", csvFileName);
            redirectAttributes.addFlashAttribute("fileSize", Files.size(Paths.get(csvFileName)));
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "File processing error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Conversion error: " + e.getMessage());
        }

        return "redirect:/results";
    }

    /**
     * Muestra la página de resultados de conversión.
     * 
     * @param model Spring Model para renderizado de plantillas
     * @return el nombre de la plantilla results
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @GetMapping("/results")
    public String results(Model model) {
        model.addAttribute("title", "Conversion Results");
        return "results";
    }

    /**
     * Descarga el archivo CSV convertido.
     * 
     * @param filename el nombre del archivo CSV a descargar
     * @return el archivo como una respuesta descargable
     * @throws IOException si ocurre un error al acceder al archivo
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    @GetMapping("/download/{filename:.+}")
    @ResponseBody
    public org.springframework.core.io.Resource downloadFile(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get("downloads", filename);
        org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(filePath);
        
        if (resource.exists()) {
            return resource;
        } else {
            throw new RuntimeException("File not found: " + filename);
        }
    }

    /**
     * Crea los directorios necesarios si no existen.
     * 
     * @throws IOException si ocurre un error al crear los directorios
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private void createDirectoriesIfNotExist() throws IOException {
        Files.createDirectories(Paths.get("uploads"));
        Files.createDirectories(Paths.get("downloads"));
    }

    /**
     * Gets filename without extension.
     * 
     * @param filename the original filename
     * @return filename without extension
     */
    private String getNameWithoutExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(0, filename.lastIndexOf('.'));
        }
        return filename;
    }
}