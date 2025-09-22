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
 * Web controller for the JSON to CSV converter interface.
 * 
 * This controller provides web endpoints for users to interact
 * with JSON-CSV conversion functionality through a browser interface.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
@Controller
public class WebController {

    @Autowired
    private JsonToCsvConverterService converterService;

    /**
     * Shows the converter main page.
     * 
     * @param model Spring Model for template rendering
     * @return the converter template name
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
     * Shows the about page with system information.
     * 
     * @param model Spring Model for template rendering
     * @return the about template name
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
     * Handles JSON file upload and converts them to CSV.
     * 
     * @param file the uploaded JSON file
     * @param delimiter the CSV delimiter to use
     * @param redirectAttributes for flash messages
     * @return redirection to the results page
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
     * Shows the conversion results page.
     * 
     * @param model Spring Model for template rendering
     * @return the results template name
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
     * Creates necessary directories if they do not exist.
     * 
     * @throws IOException if an error occurs while creating directories
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