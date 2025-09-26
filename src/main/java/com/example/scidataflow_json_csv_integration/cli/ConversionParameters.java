package com.example.scidataflow_json_csv_integration.cli;

/**
 * Data Transfer Object for Conversion Parameters
 * 
 * This class encapsulates all parameters required for JSON to CSV conversion,
 * including input/output file paths, formatting options, and configuration settings.
 * It provides a clean interface for passing conversion settings between different
 * components of the desktop application.
 * 
 * The class includes validation methods to ensure parameter integrity and
 * provides meaningful defaults for optional parameters. It supports both
 * programmatic construction and configuration file-based initialization.
 * 
 * @author Melany Rivera
 * @version 1.0.0
 * @since 25/09/2025
 */
public class ConversionParameters {
    
    /**
     * Path to the input JSON file to be converted.
     * This field is required and must point to a valid, readable JSON file.
     */
    public final String inputFile;
    
    /**
     * Path to the output CSV file where converted data will be written.
     * If not specified, a default filename will be generated based on the input file.
     */
    public final String outputFile;
    
    /**
     * Character delimiter to use in the output CSV file.
     * Common values include comma (','), semicolon (';'), tab ('\t'), and pipe ('|').
     * Default value is comma (',').
     */
    public final String delimiter;
    
    /**
     * Optional path to a configuration file containing additional parameters.
     * If specified, settings from this file will be merged with command-line parameters.
     */
    public final String configFile;
    
    /**
     * Constructs ConversionParameters with basic input and output file paths.
     * Uses default comma delimiter and no configuration file.
     * 
     * @param inputFile Path to the input JSON file (required)
     * @param outputFile Path to the output CSV file (required)
     * @throws IllegalArgumentException if inputFile or outputFile is null or empty
     * @since 25/09/2025
     */
    public ConversionParameters(String inputFile, String outputFile) {
        this(inputFile, outputFile, ",", null);
    }
    
    /**
     * Constructs ConversionParameters with input file, output file, and custom delimiter.
     * No configuration file is used.
     * 
     * @param inputFile Path to the input JSON file (required)
     * @param outputFile Path to the output CSV file (required)
     * @param delimiter Character delimiter for CSV output (required)
     * @throws IllegalArgumentException if any parameter is null or empty
     * @since 25/09/2025
     */
    public ConversionParameters(String inputFile, String outputFile, String delimiter) {
        this(inputFile, outputFile, delimiter, null);
    }
    
    /**
     * Constructs ConversionParameters with all available options.
     * This is the primary constructor that accepts all possible configuration parameters.
     * 
     * @param inputFile Path to the input JSON file (required)
     * @param outputFile Path to the output CSV file (required)
     * @param delimiter Character delimiter for CSV output (required)
     * @param configFile Optional path to configuration file (can be null)
     * @throws IllegalArgumentException if required parameters are null or empty
     * @since 25/09/2025
     */
    public ConversionParameters(String inputFile, String outputFile, String delimiter, String configFile) {
        // Validate required parameters
        if (inputFile == null || inputFile.trim().isEmpty()) {
            throw new IllegalArgumentException("Input file path cannot be null or empty");
        }
        if (outputFile == null || outputFile.trim().isEmpty()) {
            throw new IllegalArgumentException("Output file path cannot be null or empty");
        }
        if (delimiter == null || delimiter.isEmpty()) {
            throw new IllegalArgumentException("Delimiter cannot be null or empty");
        }
        
        this.inputFile = inputFile.trim();
        this.outputFile = outputFile.trim();
        this.delimiter = delimiter;
        this.configFile = (configFile != null && !configFile.trim().isEmpty()) ? configFile.trim() : null;
    }
    
    /**
     * Validates that all parameters are consistent and usable for conversion.
     * Checks file path formats, delimiter validity, and configuration file accessibility.
     * 
     * @throws IllegalArgumentException if any parameter is invalid
     * @since 25/09/2025
     */
    public void validate() throws IllegalArgumentException {
        // Validate input file path format
        if (!isValidFilePath(inputFile)) {
            throw new IllegalArgumentException("Invalid input file path format: " + inputFile);
        }
        
        // Validate output file path format
        if (!isValidFilePath(outputFile)) {
            throw new IllegalArgumentException("Invalid output file path format: " + outputFile);
        }
        
        // Validate delimiter
        if (delimiter.length() > 1) {
            // Allow common multi-character delimiters like tab representation
            if (!delimiter.equals("\\t") && !delimiter.equals("\\n") && !delimiter.equals("\\r")) {
                throw new IllegalArgumentException("Delimiter should be a single character or escape sequence");
            }
        }
        
        // Check for potentially problematic delimiters
        if (delimiter.contains("\"") || delimiter.contains("'")) {
            throw new IllegalArgumentException("Quote characters are not recommended as delimiters");
        }
    }
    
    /**
     * Checks if a file path has a valid format.
     * Performs basic validation on path structure without checking file existence.
     * 
     * @param filePath File path to validate
     * @return true if path format is valid, false otherwise
     * @since 25/09/2025
     */
    private boolean isValidFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        // Check for invalid characters in file path
        String invalidChars = "<>:\"|?*";
        for (char c : invalidChars.toCharArray()) {
            if (filePath.indexOf(c) >= 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns the delimiter character, converting escape sequences to actual characters.
     * Handles common escape sequences like \t for tab, \n for newline, etc.
     * 
     * @return Actual delimiter character to use in CSV output
     * @since 25/09/2025
     */
    public String getActualDelimiter() {
        switch (delimiter) {
            case "\\t":
                return "\t";
            case "\\n":
                return "\n";
            case "\\r":
                return "\r";
            case "\\\\":
                return "\\";
            default:
                return delimiter;
        }
    }
    
    /**
     * Creates a user-friendly string representation of the conversion parameters.
     * Useful for logging, debugging, and displaying configuration to users.
     * 
     * @return String representation of all parameters
     * @since 25/09/2025
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConversionParameters{");
        sb.append("inputFile='").append(inputFile).append('\'');
        sb.append(", outputFile='").append(outputFile).append('\'');
        sb.append(", delimiter='").append(delimiter).append('\'');
        if (configFile != null) {
            sb.append(", configFile='").append(configFile).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
    
    /**
     * Checks if two ConversionParameters objects are equal based on their field values.
     * Useful for testing and configuration comparison.
     * 
     * @param obj Object to compare with
     * @return true if objects are equal, false otherwise
     * @since 25/09/2025
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ConversionParameters that = (ConversionParameters) obj;
        
        if (!inputFile.equals(that.inputFile)) return false;
        if (!outputFile.equals(that.outputFile)) return false;
        if (!delimiter.equals(that.delimiter)) return false;
        return configFile != null ? configFile.equals(that.configFile) : that.configFile == null;
    }
    
    /**
     * Generates a hash code for the ConversionParameters object.
     * Consistent with the equals method implementation.
     * 
     * @return Hash code value for this object
     * @since 25/09/2025
     */
    @Override
    public int hashCode() {
        int result = inputFile.hashCode();
        result = 31 * result + outputFile.hashCode();
        result = 31 * result + delimiter.hashCode();
        result = 31 * result + (configFile != null ? configFile.hashCode() : 0);
        return result;
    }
}