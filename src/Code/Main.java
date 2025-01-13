package Code;

import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Represents a cell with mathematical, text, or formula content
class CellMath {
    private final String content;

    public CellMath(String content) {
        this.content = content;
    }

    public boolean isNumber(String text) {
        if (text == null || text.isEmpty()) return false;
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isText(String text) {
        return !isNumber(text) && !isForm(text);
    }

    public boolean isForm(String text) {
        return text != null && text.startsWith("=") && isValidFormula(text.substring(1));
    }

    private boolean isValidFormula(String text) {
        if (!areParenthesesBalanced(text)) return false;

        String mathPattern = "[0-9+\\-*/().]*";
        String cellPattern = "[A-Z]+[0-9]+";

        if (text.matches(cellPattern)) return true;

        if (text.matches(mathPattern)) {
            try {
                new ExpressionBuilder(text).build();
                return true;
            } catch (Exception e) {
                return false; // Invalid math expression
            }
        }

        return false;
    }

    private boolean areParenthesesBalanced(String text) {
        int balance = 0;
        for (char ch : text.toCharArray()) {
            if (ch == '(') balance++;
            else if (ch == ')') balance--;
            if (balance < 0) return false; // Closing parenthesis before opening
        }
        return balance == 0; // All parentheses are balanced
    }

    public Double computeForm(String form, Spreadsheet sheet) {
        if (!isForm(form)) throw new IllegalArgumentException("Invalid formula");

        String expression = form.substring(1); // Remove '='
        Matcher matcher = Pattern.compile("[A-Z][0-9]+").matcher(expression);

        while (matcher.find()) {
            String ref = matcher.group();
            int refX = sheet.xCell(ref);
            int refY = sheet.yCell(ref);

            if (refX == -1 || refY == -1) throw new RuntimeException("Invalid cell reference: " + ref);

            String refValue = sheet.eval(refX, refY);
            if ("ERR".equals(refValue)) throw new RuntimeException("Error in referenced cell");

            expression = expression.replace(ref, refValue);
        }

        try {
            return new ExpressionBuilder(expression).build().evaluate();
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating formula: " + form, e);
        }
    }

    public String getContent() {
        return content;
    }
}

// Represents a spreadsheet with cells
class Spreadsheet {
    private final CellMath[][] cells;

    public Spreadsheet(int width, int height) {
        cells = new CellMath[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new CellMath("");
            }
        }
    }

    public CellMath get(int x, int y) {
        return cells[x][y];
    }

    public void set(int x, int y, CellMath cell) {
        cells[x][y] = cell;
    }

    public int width() {
        return cells.length;
    }

    public int height() {
        return cells[0].length;
    }

    public int xCell(String ref) {
        char col = ref.charAt(0);
        return (col >= 'A' && col <= 'Z') ? col - 'A' : -1;
    }

    public int yCell(String ref) {
        try {
            return Integer.parseInt(ref.replaceAll("[A-Z]", "")) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String eval(int x, int y) {
        CellMath cell = get(x, y);
        if (cell == null) return "null";

        String content = cell.getContent();
        if (content.isEmpty()) return " ";
        if (cell.isNumber(content)) return content;
        if (cell.isText(content)) return content;
        if (cell.isForm(content)) {
            if (hasCycle(x, y, new boolean[width()][height()])) return "ERR";
            try {
                return String.valueOf(cell.computeForm(content, this));
            } catch (Exception e) {
                return "ERR";
            }
        }
        return "ERR";
    }

    public String[][] evalAll() {
        int width = width(), height = height();
        String[][] result = new String[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                result[x][y] = eval(x, y);
            }
        }

        return result;
    }

    private boolean hasCycle(int x, int y, boolean[][] visited) {
        if (visited[x][y]) return true;

        CellMath cell = get(x, y);
        if (cell == null || cell.isNumber(cell.getContent()) || cell.isText(cell.getContent())) return false;

        if (cell.isForm(cell.getContent())) {
            visited[x][y] = true;
            Matcher matcher = Pattern.compile("[A-Z][0-9]+").matcher(cell.getContent().substring(1));

            while (matcher.find()) {
                int refX = xCell(matcher.group());
                int refY = yCell(matcher.group());

                if (refX == -1 || refY == -1 || hasCycle(refX, refY, visited)) return true;
            }

            visited[x][y] = false;
        }
        return false;
    }

    public void printSheet() {
        String[][] values = evalAll();

        System.out.print("\t"); // Column headers
        for (int col = 0; col < values.length; col++) {
            System.out.print((char) ('A' + col) + "\t");
        }
        System.out.println();

        for (int row = 0; row < values[0].length; row++) {
            System.out.print((row + 1) + "\t"); // Row numbers
            for (int col = 0; col < values.length; col++) {
                String value = values[col][row];
                System.out.print((value == null || value.isEmpty() ? " " : value) + "\t");
            }
            System.out.println();
        }
    }
}

// Main Class
public class Main {
    public static void main(String[] args) {
        Spreadsheet sheet = new Spreadsheet(9, 9);
        sheet.set(0, 0, new CellMath("5"));
        sheet.set(0, 5, new CellMath("=A4"));
        sheet.set(0, 3, new CellMath("=A6"));
        sheet.set(1, 0, new CellMath("hello"));
        sheet.set(0, 1, new CellMath("=(3*7)/2"));
        sheet.set(1, 2, new CellMath("=A1+10"));
        sheet.set(2, 3, new CellMath("=B1"));
        sheet.printSheet();
    }
}
