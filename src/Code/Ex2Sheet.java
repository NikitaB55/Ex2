package Code;

import java.io.*;
import java.util.*;

public class Ex2Sheet implements Sheet {
    private SCell[][] table; // 2D array to represent the cells in the sheet
    private Map<String, Set<String>> dependencies = new HashMap<>(); // Stores dependencies for each cell

    // Constructor with specified dimensions
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y]; // Initialize the table with dimensions x by y
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell(""); // Initialize each cell with empty data
            }
        }
        eval(); // Evaluate all cells after initialization
    }

    // Default constructor using constants from Ex2Utils
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    // Helper method to generate a unique key for each cell (x, y)
    private String cellKey(int x, int y) {
        return x + "," + y;
    }

    // Get the value of a cell at position (x, y)
    @Override
    public String value(int x, int y) {
        if (isIn(x, y)) { // Check if coordinates are valid
            String cellValue = eval(x, y); // Evaluate the value of the cell
            if (table[x][y].getType() == Ex2Utils.ERR_FORM_FORMAT) {
                return Ex2Utils.ERR_FORM; // Return error message if there's a format error
            }
            if (table[x][y].getType() == Ex2Utils.ERR_CYCLE_FORM) {
                return Ex2Utils.ERR_CYCLE; // Return cycle error message
            }
            if (cellValue.isEmpty()) {
                return ""; // Return empty string if there's no value
            }
            return cellValue; // Return the evaluated value
        }
        return Ex2Utils.EMPTY_CELL; // Return empty cell message if coordinates are out of bounds
    }

    // Get the SCell object at position (x, y)
    @Override
    public SCell get(int x, int y) {
        return isIn(x, y) ? table[x][y] : null; // Return the cell or null if out of bounds
    }

    // Get the SCell object using string coordinates
    @Override
    public SCell get(String cords) {
        int[] coord = cellCoordinates(cords); // Convert string coordinates to integer coordinates
        return get(coord[0], coord[1]);
    }

    // Get the width of the sheet (number of rows)
    @Override
    public int width() {
        return table.length; // Number of rows in the table
    }

    // Get the height of the sheet (number of columns)
    @Override
    public int height() {
        return table[0].length; // Number of columns in the first row
    }

    // Set the data for a cell at position (x, y)
    @Override
    public void set(int x, int y, String s) {
        if (isIn(x, y)) { // Check if coordinates are valid
            table[x][y].setData(s); // Set the cell's data
            eval(); // Reevaluate the sheet after setting the value
        }
    }

    // Evaluate the entire sheet
    @Override
    public void eval() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                eval(i, j); // Evaluate each cell individually
            }
        }
    }

    // Evaluate a specific cell at position (x, y)
    @Override
    public String eval(int x, int y) {
        if (!isIn(x, y)) return Ex2Utils.EMPTY_CELL; // Return empty cell if out of bounds

        SCell cell = table[x][y];
        if (cell.isVisit()) {
            cell.setType(Ex2Utils.ERR_CYCLE_FORM); // Mark as cycle error if the cell has been visited already
            return Ex2Utils.ERR_CYCLE; // Return cycle error message
        }

        if (cell.getType() == Ex2Utils.TEXT) {
            return cell.getData(); // Return the cell's text value
        }
        if (cell.getType() == Ex2Utils.NUMBER) {
            return String.valueOf(Double.parseDouble(cell.getData())); // Return the numeric value of the cell
        }

        try {
            cell.setVisit(true); // Mark the cell as visited to prevent cycles
            double result = cell.evaluate(this, cell); // Evaluate the cell's formula
            cell.setVisit(false); // Unmark the cell as visited after evaluation
            return String.valueOf(result); // Return the result of the evaluation
        } catch (Exception e) {
            cell.setType(Ex2Utils.ERR_FORM_FORMAT); // Mark as invalid format if there's an exception
            return Ex2Utils.ERR_FORM; // Return error message for format error
        }
    }

    // Check if the coordinates (xx, yy) are within the sheet's bounds
    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height(); // Check if coordinates are valid
    }

    // Calculate the depth matrix for evaluating formulas and dependencies
    @Override
    public int[][] depth() {
        int[][] depthMatrix = new int[width()][height()]; // Initialize depth matrix
        boolean[][] visited = new boolean[width()][height()]; // Initialize visited matrix

        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                if (!visited[x][y]) {
                    if (!updateDepth(x, y, visited, new HashSet<>(), depthMatrix)) {
                        depthMatrix[x][y] = Ex2Utils.ERR_CYCLE_FORM; // Mark as cycle if update fails
                    }
                }
            }
        }

        return depthMatrix; // Return the calculated depth matrix
    }

    // Helper method to update the depth of a cell
    private boolean updateDepth(int x, int y, boolean[][] visited, Set<String> stack, int[][] depthMatrix) {
        if (!isIn(x, y)) return true; // Return true if the cell is out of bounds

        String cellId = cellKey(x, y);
        if (stack.contains(cellId)) {
            table[x][y].setType(Ex2Utils.ERR_CYCLE_FORM); // Mark as cycle error if the cell is in the stack
            return false;
        }

        if (visited[x][y]) return true; // Return true if the cell has already been visited

        visited[x][y] = true; // Mark the cell as visited
        stack.add(cellId); // Add the cell to the stack to detect cycles

        String data = table[x][y].getData();
        if (data.startsWith("=")) { // If the cell contains a formula
            for (String ref : data.substring(1).split("[^A-Za-z0-9]")) {
                if (ref.matches("[A-Za-z]+[0-9]+")) { // If the reference is a valid cell
                    int[] coords = cellCoordinates(ref); // Convert the reference to coordinates
                    if (!isIn(coords[0], coords[1])) {
                        table[x][y].setType(Ex2Utils.ERR_FORM_FORMAT); // Mark invalid reference
                        return false; // Stop processing further
                    }
                    if (!updateDepth(coords[0], coords[1], visited, stack, depthMatrix)) {
                        return false;
                    }
                    depthMatrix[x][y] = Math.max(depthMatrix[x][y], depthMatrix[coords[0]][coords[1]] + 1); // Update depth
                }
            }
        }

        stack.remove(cellId); // Remove the cell from the stack after processing
        return true;
    }

    // Load sheet data from a file
    @Override
    public void load(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String[] dimensions = reader.readLine().split(","); // Read dimensions of the sheet
            int newWidth = Integer.parseInt(dimensions[0]);
            int newHeight = Integer.parseInt(dimensions[1]);

            if (newWidth <= 0 || newHeight <= 0) {
                throw new IOException("Invalid sheet dimensions in file."); // Handle invalid dimensions
            }

            table = new SCell[newWidth][newHeight]; // Initialize the table with new dimensions
            for (int i = 0; i < newWidth; i++) {
                for (int j = 0; j < newHeight; j++) {
                    table[i][j] = new SCell(""); // Initialize each cell with empty data
                }
            }

            int rowIndex = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Skip empty lines

                String[] row = line.split(","); // Split the line into row values
                if (row.length != newHeight) {
                    String[] paddedRow = new String[newHeight];
                    System.arraycopy(row, 0, paddedRow, 0, row.length); // Pad row if necessary
                    Arrays.fill(paddedRow, row.length, newHeight, "");
                    row = paddedRow;
                }

                for (int j = 0; j < newHeight; j++) {
                    table[rowIndex][j].setData(row[j]); // Set data for each cell in the row
                }
                rowIndex++;
            }

            if (rowIndex != newWidth) {
                throw new IOException("Data row count mismatch. Expected " + newWidth + ", but got " + rowIndex + "."); // Handle row count mismatch
            }

            eval(); // Evaluate the sheet after loading data
        } catch (IOException | NumberFormatException e) {
            throw new IOException("Error loading the sheet: " + e.getMessage(), e); // Handle errors during loading
        }
    }

    // Save the sheet data to a file
    @Override
    public void save(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(width() + "," + height()); // Write sheet dimensions
            writer.newLine();

            for (int i = 0; i < width(); i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < height(); j++) {
                    row.append(table[i][j].getData()); // Append cell data to the row
                    if (j < height() - 1) {
                        row.append(","); // Add a separator if it's not the last cell
                    }
                }
                writer.write(row.toString()); // Write the row to the file
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IOException("Error saving the sheet to file: " + e.getMessage(), e); // Handle errors during saving
        }
    }

    // Convert string coordinates (e.g., "A1") to integer indices
    public int[] cellCoordinates(String cords) {
        String column = cords.replaceAll("[^A-Za-z]", ""); // Extract column letters
        String row = cords.replaceAll("[^0-9]", ""); // Extract row number
        int colIndex = column.charAt(0) - 'A'; // Convert column letter to index
        int rowIndex = Integer.parseInt(row); // Convert row string to integer
        return new int[]{colIndex, rowIndex};
    }
}
