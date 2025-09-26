package com.example.scidataflow_json_csv_integration.cli;

import com.example.scidataflow_json_csv_integration.service.DataTransformService;
import com.example.scidataflow_json_csv_integration.service.JsonReaderService;
import com.example.scidataflow_json_csv_integration.service.CsvWriterService;
import com.example.scidataflow_json_csv_integration.model.Person;
import com.example.scidataflow_json_csv_integration.exception.JsonProcessingException;
import com.example.scidataflow_json_csv_integration.exception.CsvWritingException;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

/**
 * JSON to CSV Desktop Application - Command Line Interface
 * 
 * This is a standalone desktop Java application that converts JSON files into CSV files
 * using intelligent data transformation and sorting capabilities. The application integrates
 * all functionalities developed in previous sprints including JSON reading, data transformation,
 * intelligent sorting, and CSV writing.
 * 
 * Key Features:
 * - Command-line argument processing for input/output files and configuration
 * - Interactive mode for user-friendly operation
 * - Intelligent data transformation with type detection and sorting
 * - Support for multiple data types (Person, Publication, Medical, Product, etc.)
 * - Configurable CSV delimiters and output formatting
 * - Comprehensive error handling and logging
 * - Configuration file support for default parameters
 * 
 * Usage Examples:
 * java -jar app.jar input.json output.csv
 * java -jar app.jar --input=data.json --output=result.csv --delimiter=;
 * java -jar app.jar --config=config.properties
 * java -jar app.jar --interactive
 * 
 * @author Melany Rivera
 * @version 1.0.0
 * @since 25/09/2025
 */
public class JsonToCsvDesktopApplication {
    
    private final JsonReaderService jsonReaderService;
    private final CsvWriterService csvWriterService;
    private final DataTransformService dataTransformService;
    private final CommandLineParser commandLineParser;
    private final ConfigurationManager configurationManager;
    
    private static final String DEFAULT_DELIMITER = ",";
    private static final String DEFAULT_CONFIG_FILE = "config.properties";
    private static final String APPLICATION_NAME = "JSON to CSV Desktop Converter";
    private static final String VERSION = "1.0.0";
    
    /**
     * Constructor that initializes all required services for the desktop application.
     * Sets up the service dependencies needed for JSON reading, data transformation,
     * and CSV writing operations.
     * 
     * @since 25/09/2025
     */
    public JsonToCsvDesktopApplication() {
        this.dataTransformService = new DataTransformService();
        this.jsonReaderService = new JsonReaderService(dataTransformService);
        this.csvWriterService = new CsvWriterService();
        this.commandLineParser = new CommandLineParser();
        this.configurationManager = new ConfigurationManager();
    }
    
    /**
     * Main entry point for the desktop application.
     * Processes command line arguments and executes the appropriate conversion mode.
     * 
     * Supported argument formats:
     * - Direct files: java -jar app.jar input.json output.csv
     * - Named parameters: --input=file.json --output=result.csv --delimiter=;
     * - Configuration file: --config=myconfig.properties
     * - Interactive mode: --interactive or no arguments
     * 
     * @param args Command line arguments for input/output files and configuration
     * @since 25/09/2025
     */
    public static void main(String[] args) {
        JsonToCsvDesktopApplication app = new JsonToCsvDesktopApplication();
        try {
            app.run(args);
        } catch (Exception e) {
            System.err.println("Application failed: " + e.getMessage());
            if (args.length > 0 && (args[0].equals("--debug") || args[0].equals("-d"))) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
    
    /**
     * Executes the main application logic based on provided command line arguments.
     * Determines the execution mode (command-line, configuration file, or interactive)
     * and processes the JSON to CSV conversion accordingly.
     * 
     * @param args Command line arguments array
     * @throws Exception if conversion process fails
     * @since 25/09/2025
     */
    public void run(String[] args) throws Exception {
        printWelcomeMessage();
        
        ConversionParameters params;
        
        if (args.length == 0) {
            // Interactive mode when no arguments provided
            params = runInteractiveMode();
        } else if (args.length == 2 && !args[0].startsWith("--")) {
            // Direct file arguments mode: input.json output.csv
            params = new ConversionParameters(args[0], args[1], DEFAULT_DELIMITER);
        } else {
            // Named parameter mode: --input=file.json --output=result.csv
            params = commandLineParser.parseArguments(args);
        }
        
        if (params.configFile != null) {
            // Load additional parameters from configuration file
            Properties configProps = configurationManager.loadConfiguration(params.configFile);
            params = configurationManager.mergeWithConfig(params, configProps);
        }
        
        executeConversion(params);
    }
    
    /**
     * Displays welcome message with application information and version.
     * Provides user with basic information about the application capabilities.
     * 
     * @since 25/09/2025
     */
    private void printWelcomeMessage() {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                " + APPLICATION_NAME + "                ║");
        System.out.println("║                         Version " + VERSION + "                          ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.println("║ Features:                                                        ║");
        System.out.println("║ • Intelligent data type detection and sorting                   ║");
        System.out.println("║ • Support for multiple data formats (Person, Publication, etc.)║");
        System.out.println("║ • Configurable CSV delimiters and formatting                   ║");
        System.out.println("║ • Command-line and interactive operation modes                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * Runs the application in interactive mode, prompting user for input parameters.
     * Provides a user-friendly interface for specifying conversion parameters
     * when command line arguments are not provided.
     * 
     * @return ConversionParameters object with user-specified settings
     * @throws IOException if there are issues reading user input
     * @since 25/09/2025
     */
    private ConversionParameters runInteractiveMode() throws IOException {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("🔄 Interactive Mode - Please provide the following information:");
            System.out.println();
            
            // Get input file
            String inputFile;
            while (true) {
                System.out.print("📂 Enter JSON input file path: ");
                inputFile = scanner.nextLine().trim();
                if (inputFile.isEmpty()) {
                    System.out.println("⚠️  Input file path cannot be empty. Please try again.");
                    continue;
                }
                
                Path inputPath = Paths.get(inputFile);
                if (!Files.exists(inputPath)) {
                    System.out.println("⚠️  File not found: " + inputFile);
                    System.out.print("   Would you like to try again? (y/n): ");
                    if (!scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                        throw new IOException("Input file not found: " + inputFile);
                    }
                    continue;
                }
                break;
            }
            
            // Get output file
            System.out.print("💾 Enter CSV output file path: ");
            String outputFile = scanner.nextLine().trim();
            if (outputFile.isEmpty()) {
                outputFile = inputFile.replaceAll("\\.[^.]+$", ".csv");
                System.out.println("📝 Using default output file: " + outputFile);
            }
            
            // Get delimiter
            System.out.print("🔸 Enter CSV delimiter (default: comma ','): ");
            String delimiter = scanner.nextLine().trim();
            if (delimiter.isEmpty()) {
                delimiter = DEFAULT_DELIMITER;
            }
            
            // Ask about configuration file
            System.out.print("⚙️  Load additional settings from config file? (y/n): ");
            String configFile = null;
            if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                System.out.print("📋 Enter configuration file path (default: " + DEFAULT_CONFIG_FILE + "): ");
                configFile = scanner.nextLine().trim();
                if (configFile.isEmpty()) {
                    configFile = DEFAULT_CONFIG_FILE;
                }
                
                if (!Files.exists(Paths.get(configFile))) {
                    System.out.println("⚠️  Configuration file not found, proceeding without it.");
                    configFile = null;
                }
            }
            
            System.out.println();
            return new ConversionParameters(inputFile, outputFile, delimiter, configFile);
        }
    }
    
    /**
     * Executes the JSON to CSV conversion process using the specified parameters.
     * Performs the complete conversion workflow including JSON reading, data transformation,
     * intelligent sorting, and CSV writing with comprehensive error handling.
     * 
     * @param params ConversionParameters object containing input/output files and settings
     * @throws Exception if any step of the conversion process fails
     * @since 25/09/2025
     */
    private void executeConversion(ConversionParameters params) throws Exception {
        System.out.println("🚀 Starting JSON to CSV conversion...");
        System.out.println("   📂 Input:  " + params.inputFile);
        System.out.println("   💾 Output: " + params.outputFile);
        System.out.println("   🔸 Delimiter: '" + params.delimiter + "'");
        System.out.println();
        
        try {
            // Step 1: Read and parse JSON file with intelligent transformation
            System.out.println("📖 Step 1: Reading and parsing JSON file...");
            List<Person> persons = jsonReaderService.readPersonsFromJson(params.inputFile);
            System.out.println("   ✅ Successfully parsed " + persons.size() + " records");
            
            // Step 2: Display transformation statistics
            displayTransformationStatistics(persons);
            
            // Step 3: Write to CSV file with specified delimiter
            System.out.println("💾 Step 3: Writing data to CSV file...");
            
            // Use the appropriate method based on delimiter
            if (DEFAULT_DELIMITER.equals(params.delimiter)) {
                csvWriterService.writePersonsToCsv(persons, params.outputFile);
            } else {
                char delimiter = params.getActualDelimiter().charAt(0);
                csvWriterService.writePersonsToCsv(persons, params.outputFile, delimiter, '"', '\\');
            }
            System.out.println("   ✅ Successfully wrote CSV file");
            
            // Step 4: Display completion summary
            displayCompletionSummary(params, persons.size());
            
        } catch (JsonProcessingException e) {
            throw new Exception("JSON processing failed: " + e.getMessage(), e);
        } catch (CsvWritingException e) {
            throw new Exception("CSV writing failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("Conversion failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Displays detailed statistics about the data transformation process.
     * Shows information about data types detected, quality levels, and sorting applied.
     * 
     * @param persons List of transformed Person objects to analyze
     * @since 25/09/2025
     */
    private void displayTransformationStatistics(List<Person> persons) {
        System.out.println("🔍 Step 2: Analyzing transformed data...");
        
        // Count records by data completeness
        long completeRecords = persons.stream()
            .filter(p -> p.getFirstName() != null && p.getLastName() != null && p.getEmail() != null)
            .count();
        
        long partialRecords = persons.size() - completeRecords;
        
        System.out.println("   📊 Data Quality Analysis:");
        System.out.println("      • Complete records: " + completeRecords);
        System.out.println("      • Partial records:  " + partialRecords);
        System.out.println("      • Total records:    " + persons.size());
        
        // Show first few records as preview
        System.out.println("   👀 Sample records (first 3):");
        persons.stream()
            .limit(3)
            .forEach(p -> System.out.println("      • " + 
                (p.getFirstName() != null ? p.getFirstName() : "[No Name]") + " " +
                (p.getLastName() != null ? p.getLastName() : "[No Surname]") + 
                " (" + (p.getEmail() != null ? p.getEmail() : "no-email") + ")"));
        
        System.out.println("   ✅ Data transformation and intelligent sorting completed");
    }
    
    /**
     * Displays a completion summary with conversion results and file information.
     * Provides final statistics and next steps for the user.
     * 
     * @param params ConversionParameters used for the conversion
     * @param recordCount Number of records successfully converted
     * @since 25/09/2025
     */
    private void displayCompletionSummary(ConversionParameters params, int recordCount) {
        System.out.println();
        System.out.println("🎉 Conversion completed successfully!");
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        CONVERSION SUMMARY                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.println("║ Records processed: " + String.format("%-44d", recordCount) + " ║");
        System.out.println("║ Input file:        " + String.format("%-44s", params.inputFile.length() > 44 ? 
            "..." + params.inputFile.substring(params.inputFile.length() - 41) : params.inputFile) + " ║");
        System.out.println("║ Output file:       " + String.format("%-44s", params.outputFile.length() > 44 ? 
            "..." + params.outputFile.substring(params.outputFile.length() - 41) : params.outputFile) + " ║");
        System.out.println("║ Delimiter used:    '" + String.format("%-43s", params.delimiter) + " ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("💡 Next steps:");
        System.out.println("   • Open the CSV file in your preferred spreadsheet application");
        System.out.println("   • Verify the data has been converted correctly");
        System.out.println("   • Note that data has been intelligently sorted by type and quality");
    }
}