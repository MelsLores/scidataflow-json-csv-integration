package com.example.scidataflow_json_csv_integration.service;

import com.example.scidataflow_json_csv_integration.exception.CsvWritingException;
import com.example.scidataflow_json_csv_integration.model.Person;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Servicio para escribir archivos CSV desde datos de Person.
 * Proporciona funcionalidad para generar contenido CSV con diferentes delimitadores
 * y opciones de configuración, incluyendo manejo de errores integral.
 * 
 * @author Melany Rivera
 * @since 21/09/2025
 */
@Slf4j
@Service
public class CsvWriterService {

    /**
     * Default CSV delimiter
     */
    private static final char DEFAULT_DELIMITER = ',';
    
    /**
     * Default CSV quote character
     */
    private static final char DEFAULT_QUOTE_CHAR = '"';
    
    /**
     * Default CSV escape character
     */
    private static final char DEFAULT_ESCAPE_CHAR = '\\';

    /**
     * Escribe una lista de objetos Person a un archivo CSV usando configuración por defecto.
     * Crea un archivo CSV con delimitadores de coma y formato estándar.
     * 
     * @param persons la lista de objetos Person a escribir en CSV
     * @param filePath la ruta donde se creará el archivo CSV
     * @throws CsvWritingException si el archivo no puede ser escrito o creado
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public void writePersonsToCsv(List<Person> persons, String filePath) throws CsvWritingException {
        writePersonsToCsv(persons, filePath, DEFAULT_DELIMITER, DEFAULT_QUOTE_CHAR, DEFAULT_ESCAPE_CHAR);
    }

    /**
     * Escribe una lista de objetos Person a un archivo CSV con configuración personalizada.
     * Permite personalización del formato CSV incluyendo delimitador, carácter de comillas y carácter de escape.
     * 
     * @param persons la lista de objetos Person a escribir en CSV
     * @param filePath la ruta donde se creará el archivo CSV
     * @param delimiter el carácter a usar como delimitador de campos
     * @param quoteChar el carácter a usar para entrecomillar campos
     * @param escapeChar el carácter a usar para escapar caracteres especiales
     * @throws CsvWritingException si el archivo no puede ser escrito o creado
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public void writePersonsToCsv(List<Person> persons, String filePath, char delimiter, char quoteChar, char escapeChar) 
            throws CsvWritingException {
        
        log.info("Starting to write {} persons to CSV file: {}", persons != null ? persons.size() : 0, filePath);
        
        // Validate input parameters
        validateInput(persons, filePath);
        
        CSVWriter csvWriter = null;
        FileWriter fileWriter = null;
        
        try {
            // Ensure parent directory exists
            ensureDirectoryExists(filePath);
            
            // Create file writer and CSV writer
            fileWriter = new FileWriter(filePath);
            csvWriter = new CSVWriter(fileWriter, delimiter, quoteChar, escapeChar, CSVWriter.DEFAULT_LINE_END);
            
            // Write CSV header
            writeHeader(csvWriter);
            
            // Write person data
            writePersonData(csvWriter, persons);
            
            log.info("Successfully wrote {} persons to CSV file: {}", persons.size(), filePath);
            
        } catch (IOException e) {
            String errorMessage = "Failed to write CSV file: " + filePath + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new CsvWritingException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Unexpected error while writing CSV file: " + filePath + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new CsvWritingException(errorMessage, e);
        } finally {
            // Close resources safely
            closeResources(csvWriter, fileWriter);
        }
    }

    /**
     * Escribe un objeto Person individual a un archivo CSV.
     * Es útil cuando necesita escribir registros de personas individuales.
     * 
     * @param person el objeto Person a escribir en CSV
     * @param filePath la ruta donde se creará el archivo CSV
     * @throws CsvWritingException si el archivo no puede ser escrito o creado
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public void writePersonToCsv(Person person, String filePath) throws CsvWritingException {
        if (person == null) {
            throw new CsvWritingException("Person object cannot be null");
        }
        
        writePersonsToCsv(List.of(person), filePath);
    }

    /**
     * Agrega una lista de objetos Person a un archivo CSV existente.
     * Añade nuevos registros a un archivo CSV existente sin sobrescribir los datos existentes.
     * 
     * @param persons la lista de objetos Person a agregar al CSV
     * @param filePath la ruta del archivo CSV existente
     * @throws CsvWritingException si el archivo no puede ser escrito o accedido
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public void appendPersonsToCsv(List<Person> persons, String filePath) throws CsvWritingException {
        log.info("Starting to append {} persons to CSV file: {}", persons != null ? persons.size() : 0, filePath);
        
        // Validate input parameters
        validateInput(persons, filePath);
        
        CSVWriter csvWriter = null;
        FileWriter fileWriter = null;
        
        try {
            // Ensure parent directory exists
            ensureDirectoryExists(filePath);
            
            // Check if file exists to determine if header is needed
            boolean fileExists = Files.exists(Paths.get(filePath));
            
            // Create file writer in append mode and CSV writer
            fileWriter = new FileWriter(filePath, true);
            csvWriter = new CSVWriter(fileWriter, DEFAULT_DELIMITER, DEFAULT_QUOTE_CHAR, DEFAULT_ESCAPE_CHAR, CSVWriter.DEFAULT_LINE_END);
            
            // Write header only if file doesn't exist
            if (!fileExists) {
                writeHeader(csvWriter);
            }
            
            // Write person data
            writePersonData(csvWriter, persons);
            
            log.info("Successfully appended {} persons to CSV file: {}", persons.size(), filePath);
            
        } catch (IOException e) {
            String errorMessage = "Failed to append to CSV file: " + filePath + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new CsvWritingException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Unexpected error while appending to CSV file: " + filePath + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new CsvWritingException(errorMessage, e);
        } finally {
            // Close resources safely
            closeResources(csvWriter, fileWriter);
        }
    }

    /**
     * Valida los parámetros de entrada para las operaciones de escritura CSV.
     * 
     * @param persons la lista de objetos Person a validar
     * @param filePath la ruta del archivo a validar
     * @throws CsvWritingException si la validación falla
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private void validateInput(List<Person> persons, String filePath) throws CsvWritingException {
        if (persons == null) {
            throw new CsvWritingException("Persons list cannot be null");
        }
        
        if (persons.isEmpty()) {
            throw new CsvWritingException("Persons list cannot be empty");
        }
        
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new CsvWritingException("File path cannot be null or empty");
        }
        
        // Validate that the file path has a .csv extension
        if (!filePath.toLowerCase().endsWith(".csv")) {
            log.warn("File path does not have .csv extension: {}", filePath);
        }
    }

    /**
     * Asegura que el directorio padre de la ruta del archivo exista.
     * 
     * @param filePath la ruta del archivo cuyo directorio padre debe ser creado
     * @throws CsvWritingException si falla la creación del directorio
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private void ensureDirectoryExists(String filePath) throws CsvWritingException {
        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();
            
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                log.debug("Created parent directories for file: {}", filePath);
            }
        } catch (IOException e) {
            throw new CsvWritingException("Failed to create parent directories for file: " + filePath, e);
        }
    }

    /**
     * Escribe la fila de encabezado del CSV.
     * 
     * @param csvWriter la instancia de CSVWriter para escribir
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private void writeHeader(CSVWriter csvWriter) {
        String[] header = {"ID", "First Name", "Last Name", "Email", "Age", "Department", "Salary"};
        csvWriter.writeNext(header);
        log.debug("CSV header written successfully");
    }

    /**
     * Escribe los datos de personas al archivo CSV.
     * 
     * @param csvWriter la instancia de CSVWriter para escribir
     * @param persons la lista de objetos Person a escribir
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private void writePersonData(CSVWriter csvWriter, List<Person> persons) {
        for (Person person : persons) {
            String[] row = convertPersonToStringArray(person);
            csvWriter.writeNext(row);
        }
        log.debug("Person data written successfully");
    }

    /**
     * Convierte un objeto Person a un arreglo de strings para escritura CSV.
     * 
     * @param person el objeto Person a convertir
     * @return un arreglo de strings conteniendo los datos de la persona
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private String[] convertPersonToStringArray(Person person) {
        return new String[] {
            person.getId() != null ? person.getId().toString() : "",
            person.getFirstName() != null ? person.getFirstName() : "",
            person.getLastName() != null ? person.getLastName() : "",
            person.getEmail() != null ? person.getEmail() : "",
            person.getAge() != null ? person.getAge().toString() : "",
            person.getDepartment() != null ? person.getDepartment() : "",
            person.getSalary() != null ? person.getSalary().toString() : ""
        };
    }

    /**
     * Cierra de forma segura los recursos de CSV writer y file writer.
     * 
     * @param csvWriter el CSVWriter a cerrar
     * @param fileWriter el FileWriter a cerrar
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    private void closeResources(CSVWriter csvWriter, FileWriter fileWriter) {
        try {
            if (csvWriter != null) {
                csvWriter.close();
            }
        } catch (IOException e) {
            log.error("Error closing CSV writer", e);
        }
        
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (IOException e) {
            log.error("Error closing file writer", e);
        }
    }

    /**
     * Verifica si una ruta de archivo es válida para escritura.
     * Este método utilitario puede usarse para validar rutas de archivo antes de intentar escribir.
     * 
     * @param filePath la ruta del archivo a validar
     * @return true si la ruta es válida para escritura, false de lo contrario
     * 
     * @author Melany Rivera
     * @since 21/09/2025
     */
    public boolean isPathWritable(String filePath) {
        try {
            if (filePath == null || filePath.trim().isEmpty()) {
                return false;
            }
            
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();
            
            // Check if parent directory exists or can be created
            if (parentDir != null) {
                return Files.isWritable(parentDir) || !Files.exists(parentDir);
            }
            
            return true;
        } catch (Exception e) {
            log.warn("Error checking path writability for path: {}", filePath, e);
            return false;
        }
    }
}