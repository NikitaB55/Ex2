public class Main {
    public static void main(String[] args) {
        // Create a Spreadsheet instance
        Spreadsheet sheet = new Spreadsheet(5, 5);

        // Set some cells with different types of content
        sheet.set(0, 0, new Cell("42"));       // Number
        sheet.set(1, 0, new Cell("Hello"));    // Text
        sheet.set(2, 0, new Cell("=1+2*3"));   // Formula
        sheet.set(3, 0, new Cell("=10/(2+3)")); // Formula
        sheet.set(4, 0, new Cell("=A1*2"));    // Reference to another cell

        // Evaluate individual cells
        System.out.println("Cell Evaluations:");
        System.out.println("Cell A1: " + sheet.eval(0, 0)); // Should print 42
        System.out.println("Cell B1: " + sheet.eval(1, 0)); // Should print Hello
        System.out.println("Cell C1: " + sheet.eval(2, 0)); // Should print 7.0
        System.out.println("Cell D1: " + sheet.eval(3, 0)); // Should print 2.0
        System.out.println("Cell E1: " + sheet.eval(4, 0)); // Should print 84.0

        // Evaluate all cells
        System.out.println("\nSpreadsheet Evaluation:");
        String[][] evaluatedSheet = sheet.evalAll();
        for (int i = 0; i < sheet.height(); i++) {
            for (int j = 0; j < sheet.width(); j++) {
                System.out.print(evaluatedSheet[i][j] + "\t");
            }
            System.out.println();
        }

        // Compute depth of all cells
        System.out.println("\nSpreadsheet Depth:");
        int[][] depthSheet = sheet.depth();
        for (int i = 0; i < sheet.height(); i++) {
            for (int j = 0; j < sheet.width(); j++) {
                System.out.print(depthSheet[i][j] + "\t");
            }
            System.out.println();
        }
    }
}
