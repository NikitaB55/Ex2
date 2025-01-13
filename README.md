# Spreadsheet Project

This project is a simple spreadsheet application implemented in Java, which allows users to define cells with numeric, text, or formula-based content. It supports mathematical expression evaluation and dependencies between cells.

---

## Features

- **Basic Cell Content**: Cells can contain numbers, text, or formulas (e.g., `=A1+B2`).
- **Formula Evaluation**: Supports mathematical operations such as addition, subtraction, multiplication, division, and parentheses.
- **Error Handling**:
  - Detects invalid formulas.
  - Identifies circular dependencies between cells.
- **File Operations**:
  - Save the spreadsheet to a CSV file.
  - Load the spreadsheet from a CSV file.
- **Dependencies**: Automatically updates dependent cells when a referenced cell's value changes.

---

## Classes

### 1. `SCell`
- Represents an individual cell in the spreadsheet.
- Handles content and evaluation logic for numbers, text, and formulas.
- Provides:
  - Methods for formula parsing and evaluation.
  - Error detection for invalid references and cycles.

### 2. `Ex2Sheet`
- Represents the spreadsheet itself.
- Manages a grid of `SCell` objects.
- Provides:
  - Methods for setting and retrieving cell data.
  - Evaluation of the entire sheet or individual cells.
  - File save/load functionality.

### 3. `Ex2Utils`
- Provides utility constants and helper methods for error codes and sheet dimensions.

---

## Project Setup

### Prerequisites
- **Java JDK**: Version 8 or higher.
- **IntelliJ IDEA/Eclipse** (optional, for development).

### Dependencies
- **Exp4j Library**: Used for evaluating mathematical expressions.

#### Adding Exp4j to the Project:
- **Maven**:
  ```xml
  <dependency>
      <groupId>net.objecthunter</groupId>
      <artifactId>exp4j</artifactId>
      <version>0.4.8</version>
  </dependency>
  ```
- **Gradle**:
  ```groovy
  implementation 'net.objecthunter:exp4j:0.4.8'
  ```
- **Manual JAR Addition**:
  1. Download the JAR from [Maven Central](https://search.maven.org/artifact/net.objecthunter/exp4j/0.4.8/jar).
  2. Add it to your project dependencies (refer to your IDE's documentation).

---

## Usage

### Initialization
```java
Ex2Sheet sheet = new Ex2Sheet(5, 5); // Create a 5x5 spreadsheet
```

### Setting Cell Data
```java
sheet.set(0, 0, "42");       // Set A1 to 42
sheet.set(1, 1, "=A0+10");   // Set B2 to a formula referencing A1
```

### Retrieving Cell Values
```java
String value = sheet.value(0, 0); // Retrieve value from A1
System.out.println(value);       // Outputs: 42.0
```

### Saving and Loading
```java
sheet.save("spreadsheet.csv");       // Save the sheet to a file
sheet.load("spreadsheet.csv");       // Load the sheet from a file
```

---

## Testing

### Unit Tests
Unit tests are included for the `SCell` and `Ex2Sheet` classes. These tests ensure proper functionality for:
- Initialization.
- Formula evaluation.
- Error handling.
- File operations.

#### Running Tests
- Use **JUnit 5** to run the test cases.
- Example:
  ```bash
  mvn test
  ```

---

## Example

Here is an example of how the spreadsheet works:

```java
public class Main {
    public static void main(String[] args) {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);

        // Set some values
        sheet.set(0, 0, "10");        // A1 = 10
        sheet.set(0, 1, "5");         // A2 = 5
        sheet.set(0, 2, "=A0+A1");    // A3 = A0 + A1

        // Retrieve and print values
        System.out.println(sheet.value(0, 2)); // Outputs: 15.0

        // Save the sheet
        try {
            sheet.save("example.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


![image](https://github.com/user-attachments/assets/8b37b717-a927-4fd0-ab57-206c9c23514a)



