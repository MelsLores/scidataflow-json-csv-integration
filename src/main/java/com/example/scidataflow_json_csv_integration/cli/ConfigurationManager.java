package com.example.scidataflow_json_csv_integration.cli;

import java.io.*;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Configuration Manager for JSON to CSV Desktop Application
 * 
 * This class manages loading, parsing, and merging configuration settings from
 * properties files. It provides flexible configuration management capabilities
 * for the desktop application, allowing users to store frequently used settings
 * in configuration files rather than specifying them via command line arguments.
 * 
 * Configuration File Format:
 * The configuration file should be in standard Java properties format:
 * 
 * # JSON to CSV Converter Configuration
 * input.file=/path/to/default/input.json
 * output.file=/path/to/default/output.csv
 * csv.delimiter=,
 * csv.include.header=true
 * conversion.sort.enabled=true
 * conversion.validation.enabled=true
 * 
 * Features:
 * - Properties file loading with error handling
 * - Parameter merging with command-line argument precedence
 * - Default configuration creation
 * - Configuration validation
 * - Support for relative and absolute file paths
 * 
 * @author Melany Rivera
 * @version 1.0.0
 * @since 25/09/2025
 */
public class ConfigurationManager {
    
    // Configuration property keys
    private static final String PROP_INPUT_FILE = "input.file";
    private static final String PROP_OUTPUT_FILE = "output.file";
    private static final String PROP_DELIMITER = "csv.delimiter";
    private static final String PROP_INCLUDE_HEADER = "csv.include.header";
    private static final String PROP_SORT_ENABLED = "conversion.sort.enabled";
    private static final String PROP_VALIDATION_ENABLED = "conversion.validation.enabled";
    
    // Default configuration values
    private static final String DEFAULT_DELIMITER = ",";
    private static final String DEFAULT_INCLUDE_HEADER = "true";
    private static final String DEFAULT_SORT_ENABLED = "true";
    private static final String DEFAULT_VALIDATION_ENABLED = "true";
    
    /**
     * Loads configuration properties from the specified file.
     * Handles file not found scenarios gracefully by returning empty properties.
     * Validates the loaded properties for correctness and completeness.
     * 
     * @param configFilePath Path to the configuration properties file
     * @return Properties object containing loaded configuration settings
     * @throws IOException if the configuration file exists but cannot be read
     * @since 25/09/2025
     */
    public Properties loadConfiguration(String configFilePath) throws IOException {
        Properties properties = new Properties();
        
        if (configFilePath == null || configFilePath.trim().isEmpty()) {
            return properties; // Return empty properties if no config file specified
        }
        
        File configFile = new File(configFilePath.trim());
        
        if (!configFile.exists()) {
            System.out.println("⚠️  Configuration file not found: " + configFilePath);
            System.out.println("   Proceeding with default settings.");
            return properties;
        }
        
        if (!configFile.canRead()) {
            throw new IOException("Configuration file is not readable: " + configFilePath);
        }
        
        try (InputStream inputStream = new FileInputStream(configFile)) {
            properties.load(inputStream);
            System.out.println("✅ Configuration loaded from: " + configFilePath);
            
            // Validate loaded properties
            validateConfiguration(properties);
            
            return properties;
        } catch (IOException e) {
            throw new IOException("Failed to load configuration from: " + configFilePath, e);
        }
    }
    
    /**
     * Merges configuration file properties with command-line parameters.
     * Command-line parameters take precedence over configuration file settings.
     * Only merges non-null, non-empty values to preserve explicit user choices.
     * 
     * @param cmdLineParams ConversionParameters from command line parsing
     * @param configProperties Properties loaded from configuration file
     * @return ConversionParameters with merged settings
     * @since 25/09/2025
     */
    public ConversionParameters mergeWithConfig(ConversionParameters cmdLineParams, Properties configProperties) {
        if (configProperties == null || configProperties.isEmpty()) {
            return cmdLineParams;
        }
        
        // Use command line values if present, otherwise use config file values
        String inputFile = getValueOrDefault(
            cmdLineParams.inputFile,
            configProperties.getProperty(PROP_INPUT_FILE),
            null
        );
        
        String outputFile = getValueOrDefault(
            cmdLineParams.outputFile,
            configProperties.getProperty(PROP_OUTPUT_FILE),
            null
        );
        
        String delimiter = getValueOrDefault(
            cmdLineParams.delimiter,
            configProperties.getProperty(PROP_DELIMITER),
            DEFAULT_DELIMITER
        );
        
        // Config file path remains the same
        String configFile = cmdLineParams.configFile;
        
        System.out.println("🔧 Configuration merged successfully:");
        System.out.println("   📂 Input: " + inputFile);
        System.out.println("   💾 Output: " + outputFile);
        System.out.println("   🔸 Delimiter: '" + delimiter + "'");
        
        return new ConversionParameters(inputFile, outputFile, delimiter, configFile);
    }
    
    /**
     * Creates a default configuration file with common settings and documentation.
     * Useful for first-time users or when creating template configurations.
     * 
     * @param configFilePath Path where the default configuration file will be created
     * @throws IOException if the configuration file cannot be created
     * @since 25/09/2025
     */
    public void createDefaultConfiguration(String configFilePath) throws IOException {
        Properties defaultProps = createDefaultProperties();
        
        try (OutputStream outputStream = new FileOutputStream(configFilePath)) {
            defaultProps.store(outputStream, generateConfigurationComment());
            System.out.println("✅ Default configuration created: " + configFilePath);
        } catch (IOException e) {
            throw new IOException("Failed to create default configuration file: " + configFilePath, e);
        }
    }
    
    /**
     * Validates configuration properties for correctness and completeness.
     * Checks for required properties, validates file paths, and verifies delimiter settings.
     * 
     * @param properties Properties object to validate
     * @throws IllegalArgumentException if configuration contains invalid values
     * @since 25/09/2025
     */
    private void validateConfiguration(Properties properties) throws IllegalArgumentException {
        // Validate delimiter if specified
        String delimiter = properties.getProperty(PROP_DELIMITER);
        if (delimiter != null && delimiter.length() > 1) {
            if (!delimiter.equals("\\t") && !delimiter.equals("\\n")) {
                System.out.println("⚠️  Warning: Multi-character delimiter detected: '" + delimiter + "'");
            }
        }
        
        // Validate boolean properties
        validateBooleanProperty(properties, PROP_INCLUDE_HEADER, "csv.include.header");
        validateBooleanProperty(properties, PROP_SORT_ENABLED, "conversion.sort.enabled");
        validateBooleanProperty(properties, PROP_VALIDATION_ENABLED, "conversion.validation.enabled");
        
        // Validate file paths exist if specified
        String inputFile = properties.getProperty(PROP_INPUT_FILE);
        if (inputFile != null && !inputFile.trim().isEmpty()) {
            if (!Files.exists(Paths.get(inputFile.trim()))) {
                System.out.println("⚠️  Warning: Input file specified in config does not exist: " + inputFile);
            }
        }
    }
    
    /**
     * Validates that a boolean property has a valid value.
     * Accepts: true, false, yes, no, 1, 0 (case-insensitive).
     * 
     * @param properties Properties object containing the property
     * @param propertyKey Key of the property to validate
     * @param displayName Human-readable name for error messages
     * @throws IllegalArgumentException if property value is not a valid boolean
     * @since 25/09/2025
     */
    private void validateBooleanProperty(Properties properties, String propertyKey, String displayName) {
        String value = properties.getProperty(propertyKey);
        if (value != null && !value.trim().isEmpty()) {
            String normalizedValue = value.trim().toLowerCase();
            if (!normalizedValue.equals("true") && !normalizedValue.equals("false") &&
                !normalizedValue.equals("yes") && !normalizedValue.equals("no") &&
                !normalizedValue.equals("1") && !normalizedValue.equals("0")) {
                
                throw new IllegalArgumentException("Invalid boolean value for " + displayName + ": " + value);
            }
        }
    }
    
    /**
     * Creates default properties with standard configuration values.
     * Includes all supported configuration options with sensible defaults.
     * 
     * @return Properties object with default configuration values
     * @since 25/09/2025
     */
    private Properties createDefaultProperties() {
        Properties defaultProps = new Properties();
        
        // File paths (left empty for user to fill in)
        defaultProps.setProperty(PROP_INPUT_FILE, "");
        defaultProps.setProperty(PROP_OUTPUT_FILE, "");
        
        // CSV formatting options
        defaultProps.setProperty(PROP_DELIMITER, DEFAULT_DELIMITER);
        defaultProps.setProperty(PROP_INCLUDE_HEADER, DEFAULT_INCLUDE_HEADER);
        
        // Conversion behavior options
        defaultProps.setProperty(PROP_SORT_ENABLED, DEFAULT_SORT_ENABLED);
        defaultProps.setProperty(PROP_VALIDATION_ENABLED, DEFAULT_VALIDATION_ENABLED);
        
        return defaultProps;
    }
    
    /**
     * Generates a comprehensive comment header for configuration files.
     * Provides documentation and usage examples for configuration properties.
     * 
     * @return Multi-line comment string for configuration file header
     * @since 25/09/2025
     */
    private String generateConfigurationComment() {
        return "JSON to CSV Desktop Converter Configuration File\n" +
               "Generated on: " + new java.util.Date() + "\n" +
               "\n" +
               "This configuration file allows you to set default values for the JSON to CSV converter.\n" +
               "Command-line arguments will override settings specified in this file.\n" +
               "\n" +
               "File Paths:\n" +
               "  input.file  - Default path to JSON input file\n" +
               "  output.file - Default path to CSV output file\n" +
               "\n" +
               "CSV Formatting:\n" +
               "  csv.delimiter          - Character to use as field separator (default: ,)\n" +
               "  csv.include.header     - Include column headers in output (true/false)\n" +
               "\n" +
               "Conversion Options:\n" +
               "  conversion.sort.enabled       - Enable intelligent data sorting (true/false)\n" +
               "  conversion.validation.enabled - Enable data validation (true/false)\n" +
               "\n" +
               "Examples:\n" +
               "  csv.delimiter=;\n" +
               "  csv.delimiter=\\t  (for tab-separated values)\n" +
               "  input.file=C:/data/input.json\n" +
               "  output.file=C:/results/output.csv";
    }
    
    /**
     * Utility method to select a value based on priority: cmdLine > config > default.
     * Returns the first non-null, non-empty value in the priority order.
     * 
     * @param cmdLineValue Value from command line arguments
     * @param configValue Value from configuration file
     * @param defaultValue Default value to use if others are not available
     * @return Selected value based on priority
     * @since 25/09/2025
     */
    private String getValueOrDefault(String cmdLineValue, String configValue, String defaultValue) {
        if (cmdLineValue != null && !cmdLineValue.trim().isEmpty()) {
            return cmdLineValue;
        }
        
        if (configValue != null && !configValue.trim().isEmpty()) {
            return configValue.trim();
        }
        
        return defaultValue;
    }
}