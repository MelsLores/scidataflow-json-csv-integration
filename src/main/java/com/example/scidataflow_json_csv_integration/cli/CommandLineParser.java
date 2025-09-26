package com.example.scidataflow_json_csv_integration.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Command Line Parser for JSON to CSV Desktop Application
 * 
 * This utility class handles parsing and validation of command line arguments
 * for the desktop application. It supports both positional arguments and
 * named parameter formats, providing flexible input methods for users.
 * 
 * Supported Argument Formats:
 * - Named parameters: --input=file.json --output=result.csv --delimiter=;
 * - Short flags: -i file.json -o result.csv -d ;
 * - Configuration file: --config=config.properties
 * - Help and version: --help, --version
 * - Debug mode: --debug
 * 
 * Usage Examples:
 * --input=/path/to/data.json --output=/path/to/result.csv
 * --config=myconfig.properties --delimiter=|
 * -i data.json -o result.csv -d ";"
 * 
 * @author Melany Rivera
 * @version 1.0.0
 * @since 25/09/2025
 */
public class CommandLineParser {
    
    private static final String DEFAULT_DELIMITER = ",";
    
    /**
     * Parses command line arguments and returns a ConversionParameters object.
     * Handles both named parameters (--param=value) and short flags (-p value).
     * Validates required parameters and provides helpful error messages.
     * 
     * @param args Array of command line arguments to parse
     * @return ConversionParameters object with parsed values
     * @throws IllegalArgumentException if required parameters are missing or invalid
     * @since 25/09/2025
     */
    public ConversionParameters parseArguments(String[] args) throws IllegalArgumentException {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No arguments provided");
        }
        
        // Check for help or version requests
        if (containsHelpRequest(args)) {
            displayHelp();
            System.exit(0);
        }
        
        if (containsVersionRequest(args)) {
            displayVersion();
            System.exit(0);
        }
        
        Map<String, String> parsedArgs = parseArgumentsToMap(args);
        
        // Extract parameters with validation
        String inputFile = getRequiredParameter(parsedArgs, "input", "i");
        String outputFile = getOptionalParameter(parsedArgs, "output", "o");
        String delimiter = getOptionalParameter(parsedArgs, "delimiter", "d", DEFAULT_DELIMITER);
        String configFile = getOptionalParameter(parsedArgs, "config", "c");
        
        // Generate default output filename if not provided
        if (outputFile == null || outputFile.isEmpty()) {
            outputFile = generateDefaultOutputFilename(inputFile);
        }
        
        return new ConversionParameters(inputFile, outputFile, delimiter, configFile);
    }
    
    /**
     * Parses command line arguments into a map of key-value pairs.
     * Supports both long form (--param=value) and short form (-p value) arguments.
     * 
     * @param args Array of command line arguments
     * @return Map containing parsed argument key-value pairs
     * @since 25/09/2025
     */
    private Map<String, String> parseArgumentsToMap(String[] args) {
        Map<String, String> parsedArgs = new HashMap<>();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.startsWith("--")) {
                // Long form parameter: --param=value
                if (arg.contains("=")) {
                    String[] parts = arg.substring(2).split("=", 2);
                    parsedArgs.put(parts[0], parts[1]);
                } else {
                    // Long form flag: --param value
                    String key = arg.substring(2);
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        parsedArgs.put(key, args[++i]);
                    } else {
                        parsedArgs.put(key, "true");
                    }
                }
            } else if (arg.startsWith("-") && arg.length() > 1) {
                // Short form parameter: -p value
                String key = arg.substring(1);
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    parsedArgs.put(key, args[++i]);
                } else {
                    parsedArgs.put(key, "true");
                }
            }
        }
        
        return parsedArgs;
    }
    
    /**
     * Retrieves a required parameter from the parsed arguments map.
     * Checks both long and short parameter names and throws exception if not found.
     * 
     * @param parsedArgs Map of parsed command line arguments
     * @param longName Long form parameter name (e.g., "input")
     * @param shortName Short form parameter name (e.g., "i")
     * @return Parameter value
     * @throws IllegalArgumentException if required parameter is missing
     * @since 25/09/2025
     */
    private String getRequiredParameter(Map<String, String> parsedArgs, String longName, String shortName) {
        String value = parsedArgs.get(longName);
        if (value == null) {
            value = parsedArgs.get(shortName);
        }
        
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Required parameter missing: --" + longName + " (or -" + shortName + ")");
        }
        
        return value.trim();
    }
    
    /**
     * Retrieves an optional parameter from the parsed arguments map.
     * Returns the default value if the parameter is not found.
     * 
     * @param parsedArgs Map of parsed command line arguments
     * @param longName Long form parameter name
     * @param shortName Short form parameter name
     * @param defaultValue Default value to return if parameter not found
     * @return Parameter value or default value
     * @since 25/09/2025
     */
    private String getOptionalParameter(Map<String, String> parsedArgs, String longName, String shortName, String defaultValue) {
        String value = parsedArgs.get(longName);
        if (value == null) {
            value = parsedArgs.get(shortName);
        }
        return value != null ? value.trim() : defaultValue;
    }
    
    /**
     * Retrieves an optional parameter from the parsed arguments map.
     * Returns null if the parameter is not found.
     * 
     * @param parsedArgs Map of parsed command line arguments
     * @param longName Long form parameter name
     * @param shortName Short form parameter name
     * @return Parameter value or null
     * @since 25/09/2025
     */
    private String getOptionalParameter(Map<String, String> parsedArgs, String longName, String shortName) {
        return getOptionalParameter(parsedArgs, longName, shortName, null);
    }
    
    /**
     * Generates a default output filename based on the input filename.
     * Replaces the input file extension with .csv.
     * 
     * @param inputFile Input file path
     * @return Generated output filename with .csv extension
     * @since 25/09/2025
     */
    private String generateDefaultOutputFilename(String inputFile) {
        if (inputFile == null) {
            return "output.csv";
        }
        
        return inputFile.replaceAll("\\.[^.]+$", ".csv");
    }
    
    /**
     * Checks if the arguments contain a help request.
     * Recognizes various help flags: --help, -h, -?
     * 
     * @param args Command line arguments array
     * @return true if help is requested, false otherwise
     * @since 25/09/2025
     */
    private boolean containsHelpRequest(String[] args) {
        return Arrays.stream(args).anyMatch(arg -> 
            arg.equals("--help") || arg.equals("-h") || arg.equals("-?"));
    }
    
    /**
     * Checks if the arguments contain a version request.
     * Recognizes version flags: --version, -v
     * 
     * @param args Command line arguments array
     * @return true if version is requested, false otherwise
     * @since 25/09/2025
     */
    private boolean containsVersionRequest(String[] args) {
        return Arrays.stream(args).anyMatch(arg -> 
            arg.equals("--version") || arg.equals("-v"));
    }
    
    /**
     * Displays comprehensive help information for the application.
     * Shows usage examples, parameter descriptions, and configuration options.
     * 
     * @since 25/09/2025
     */
    private void displayHelp() {
        System.out.println("JSON to CSV Desktop Converter - Help");
        System.out.println("=====================================");
        System.out.println();
        System.out.println("USAGE:");
        System.out.println("  java -jar json-csv-converter.jar [OPTIONS]");
        System.out.println("  java -jar json-csv-converter.jar input.json output.csv");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  --input, -i <file>     Input JSON file path (required)");
        System.out.println("  --output, -o <file>    Output CSV file path (optional)");
        System.out.println("  --delimiter, -d <char> CSV delimiter character (default: ',')");
        System.out.println("  --config, -c <file>    Configuration file path (optional)");
        System.out.println("  --help, -h, -?         Show this help message");
        System.out.println("  --version, -v          Show version information");
        System.out.println("  --debug                Enable debug mode");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  java -jar json-csv-converter.jar data.json");
        System.out.println("  java -jar json-csv-converter.jar --input=data.json --output=result.csv");
        System.out.println("  java -jar json-csv-converter.jar -i data.json -o result.csv -d \";\"");
        System.out.println("  java -jar json-csv-converter.jar --config=myconfig.properties");
        System.out.println();
        System.out.println("FEATURES:");
        System.out.println("  • Intelligent data type detection and sorting");
        System.out.println("  • Support for multiple data formats");
        System.out.println("  • Configurable CSV delimiters");
        System.out.println("  • Interactive mode when no arguments provided");
        System.out.println();
        System.out.println("For more information, visit: https://github.com/MelsLores/scidataflow-json-csv-integration");
    }
    
    /**
     * Displays version information for the application.
     * Shows current version, build information, and system details.
     * 
     * @since 25/09/2025
     */
    private void displayVersion() {
        System.out.println("JSON to CSV Desktop Converter");
        System.out.println("Version: 1.0.0");
        System.out.println("Build Date: September 25, 2025");
        System.out.println("Author: Melany Rivera");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
    }
}