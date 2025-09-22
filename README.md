# SciDataFlow JSON-CSV Integration

A robust Spring Boot application that provides comprehensive JSON to CSV conversion functionality with enterprise-grade error handling and testing.

## 📋 Project Overview

This project implements a complete solution for converting JSON data to CSV format using popular Java libraries. It follows Spring Boot best practices and includes comprehensive testing, documentation, and error handling.

### 🎯 Purpose

- **JSON Processing**: Read and parse JSON files containing person data
- **CSV Generation**: Write data to CSV files with configurable formatting
- **Data Transformation**: Convert between JSON and CSV formats seamlessly
- **Error Handling**: Robust exception handling for common scenarios
- **Testing**: Comprehensive unit tests ensuring reliability

### 🏗️ Architecture

The application follows a layered architecture with clear separation of concerns:

```
├── Model Layer (Person.java) - Data transfer objects
├── Service Layer - Business logic and orchestration
│   ├── JsonReaderService - JSON file reading and parsing
│   ├── CsvWriterService - CSV file writing and formatting
│   └── JsonToCsvConverterService - Integration and orchestration
├── Exception Layer - Custom exception handling
└── Test Layer - Comprehensive unit tests
```

## 🚀 Features

### ✨ JSON Processing
- **File Validation**: Comprehensive file existence and readability checks
- **JSON Parsing**: Robust JSON parsing using Jackson library
- **Multiple Formats**: Support for both single objects and arrays
- **Error Handling**: Detailed error messages for parsing issues

### 📊 CSV Writing  
- **Configurable Format**: Custom delimiters, quote characters, and escape characters
- **Flexible Writing**: Write new files or append to existing ones
- **Directory Creation**: Automatic creation of parent directories
- **Null Value Handling**: Graceful handling of missing or null data

### 🔄 Integration Features
- **Single File Conversion**: Convert one JSON file to CSV
- **Batch Processing**: Convert multiple JSON files to a single CSV
- **Statistics**: Get conversion statistics and data insights
- **Custom Formatting**: Support for different CSV formats and delimiters

## 📦 Dependencies

### Core Libraries
- **Spring Boot 3.5.6**: Application framework and dependency injection
- **Jackson 2.x**: JSON processing and parsing
- **OpenCSV 5.8**: CSV reading and writing
- **Lombok**: Code generation and boilerplate reduction

### Testing Libraries
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework for isolated testing
- **Spring Boot Test**: Integration testing support

## 🛠️ Setup Instructions

### Prerequisites
- **Java 17** or higher
- **Maven 3.9.5** or higher
- **Git** for version control

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/MelsLores/scidataflow-json-csv-integration.git
   cd scidataflow-json-csv-integration
   ```

2. **Build the Project**
   ```bash
   # Using Maven Wrapper (recommended)
   ./mvnw clean compile
   
   # Or using installed Maven
   mvn clean compile
   ```

3. **Run Tests**
   ```bash
   # Run all tests
   ./mvnw test
   
   # Run specific test class
   ./mvnw test -Dtest=JsonReaderServiceTest
   ```

4. **Package the Application**
   ```bash
   ./mvnw clean package
   ```

5. **Run the Application**
   ```bash
   # Method 1: Using Maven
   ./mvnw spring-boot:run
   
   # Method 2: Using JAR file
   java -jar target/scidataflow-json-csv-integration-0.0.1-SNAPSHOT.jar
   ```

## 📚 Usage Examples

### Basic JSON to CSV Conversion

```java
@Autowired
private JsonToCsvConverterService converterService;

public void convertData() {
    try {
        int convertedCount = converterService.convertJsonToCsv(
            "input/persons.json", 
            "output/persons.csv"
        );
        System.out.println("Converted " + convertedCount + " persons");
    } catch (JsonProcessingException | CsvWritingException e) {
        System.err.println("Conversion failed: " + e.getMessage());
    }
}
```

### Custom CSV Format

```java
public void convertWithCustomFormat() {
    try {
        int count = converterService.convertJsonToCsv(
            "input/persons.json", 
            "output/persons.csv",
            ';',    // Delimiter
            '\'',   // Quote character  
            '\\'    // Escape character
        );
        System.out.println("Converted with custom format: " + count);
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
}
```

### Multiple Files to Single CSV

```java
public void convertMultipleFiles() {
    try {
        String[] jsonFiles = {
            "input/persons1.json", 
            "input/persons2.json", 
            "input/persons3.json"
        };
        
        int totalCount = converterService.convertMultipleJsonToCsv(
            jsonFiles, 
            "output/combined.csv"
        );
        
        System.out.println("Total persons converted: " + totalCount);
    } catch (Exception e) {
        System.err.println("Batch conversion failed: " + e.getMessage());
    }
}
```

### Get Conversion Statistics

```java
public void showStatistics() {
    try {
        String stats = converterService.getConversionStatistics("input/persons.json");
        System.out.println(stats);
        // Output:
        // File: input/persons.json
        // Total Persons: 8
        // Unique Departments: 4
        // Average Age: 30.50
        // Average Salary: 70125.00
    } catch (JsonProcessingException e) {
        System.err.println("Could not get statistics: " + e.getMessage());
    }
}
```

## 📁 Sample Data

The project includes sample JSON data in `src/main/resources/sample-data/`:

### persons.json
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe", 
    "email": "john.doe@example.com",
    "age": 30,
    "department": "Engineering",
    "salary": 75000.00
  }
  // ... more persons
]
```

### single-person.json
```json
{
  "id": 9,
  "firstName": "Alice",
  "lastName": "Thompson",
  "email": "alice.thompson@example.com", 
  "age": 27,
  "department": "Design",
  "salary": 62000.00
}
```

## 🧪 Testing

The project includes comprehensive unit tests covering all major functionality:

### Test Structure
```
src/test/java/
├── JsonReaderServiceTest.java - JSON reading functionality
├── CsvWriterServiceTest.java - CSV writing functionality  
└── JsonToCsvConverterServiceTest.java - Integration testing
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report

# Run specific test category
./mvnw test -Dgroups="integration"
```

### Test Coverage
- **JSON Reading**: File validation, parsing, error scenarios
- **CSV Writing**: Format options, error handling, directory creation
- **Integration**: End-to-end conversion, batch processing, statistics

## 🔧 Configuration

### Application Properties

```properties
# Logging configuration
logging.level.com.example.scidataflow_json_csv_integration=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# Application settings
app.default-csv-delimiter=,
app.default-quote-char="
app.max-file-size=10MB
```

### Custom Configuration

```java
@Configuration
public class CsvConfig {
    
    @Value("${app.default-csv-delimiter:,}")
    private char defaultDelimiter;
    
    @Bean
    public CsvWriterConfig csvWriterConfig() {
        return CsvWriterConfig.builder()
            .delimiter(defaultDelimiter)
            .quoteChar('"')
            .escapeChar('\\')
            .build();
    }
}
```

## 🚨 Error Handling

The application includes robust error handling with custom exceptions:

### Exception Types
- **JsonProcessingException**: JSON file reading and parsing errors
- **CsvWritingException**: CSV file writing and formatting errors

### Common Error Scenarios
- File not found or not readable
- Invalid JSON format or structure  
- CSV writing permissions or disk space issues
- Network or I/O related failures

### Error Response Example
```
JsonProcessingException: Failed to read or parse JSON file: input/invalid.json
Error: Unexpected character ('}' (code 125)): was expecting double-quote to start field name at [Source: (File); line: 3, column: 5]
```

## 📊 Performance Considerations

### Memory Usage
- Streaming JSON parser for large files
- Configurable batch sizes for CSV writing
- Memory-efficient data structures

### File Size Limits
- Default maximum file size: 10MB
- Configurable via application properties
- Automatic cleanup of temporary files

### Optimization Tips
- Use streaming APIs for large datasets
- Process files in batches when possible
- Monitor memory usage in production

## 🔒 Security Considerations

### File Access
- Validate file paths to prevent directory traversal
- Check file permissions before reading/writing
- Sanitize file names and paths

### Data Validation  
- Validate JSON structure and content
- Sanitize data before CSV output
- Handle malformed or malicious input gracefully

## 🤝 Contributing

### Development Guidelines
1. Follow Spring Boot best practices
2. Write comprehensive tests for new features
3. Update documentation for API changes
4. Use meaningful commit messages

### Code Style
- Use Java 17+ features appropriately
- Follow Google Java Style Guide
- Include JavaDoc for public methods
- Maintain 80%+ test coverage

### Pull Request Process
1. Create feature branch from `main`
2. Implement changes with tests
3. Update README.md if needed
4. Submit PR with detailed description

## 📋 Digital NAO Team Access

### Repository Information
- **Repository**: `https://github.com/MelsLores/scidataflow-json-csv-integration`
- **Branch**: `sprint1` (primary development branch)
- **Access Level**: Public repository with team collaboration permissions

### Review Process
1. **Code Review**: All changes require peer review
2. **Testing**: Automated tests must pass before merge
3. **Documentation**: Updates to README.md for significant changes
4. **Deployment**: CI/CD pipeline for automated testing and deployment

### Team Permissions
- **Read Access**: All Digital NAO team members
- **Write Access**: Core development team
- **Admin Access**: Project maintainers and technical leads

## 📝 Additional Information

### Project Structure
```
scidataflow-json-csv-integration/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/scidataflow_json_csv_integration/
│   │   │       ├── model/          # Data models
│   │   │       ├── service/        # Business logic
│   │   │       ├── exception/      # Custom exceptions
│   │   │       └── ScidataflowJsonCsvIntegrationApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── sample-data/        # Test JSON files
│   └── test/
│       └── java/                   # Unit tests
├── target/                         # Build output
├── pom.xml                        # Maven configuration
└── README.md                      # This file
```

### Supported Data Types
- **Person Model**: ID, names, email, age, department, salary
- **Extensible**: Easy to add new data models
- **Flexible**: Supports partial data and null values

### Future Enhancements
- [ ] Web API endpoints for file upload/conversion
- [ ] Support for additional data formats (XML, YAML)
- [ ] Real-time conversion monitoring and progress tracking
- [ ] Cloud storage integration (AWS S3, Google Cloud)
- [ ] Data validation and transformation rules engine

---

## 📞 Support

For questions, issues, or contributions:
- **Issues**: Create GitHub issues for bugs or feature requests
- **Discussions**: Use GitHub Discussions for questions and ideas
- **Email**: Contact the Digital NAO team for urgent matters

---

**Project Version**: 1.0.0  
**Last Updated**: September 21, 2025  
**License**: MIT License  
**Author**: Digital NAO Team