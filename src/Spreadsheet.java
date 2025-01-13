public class Spreadsheet {
    private Cell[][] cells;

    public Spreadsheet(int x, int y) {
        cells = new Cell[y][x];
        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                cells[i][j] = new Cell("");
            }
        }
    }

    public Cell get(int x, int y) {
        return cells[y][x];
    }

    public void set(int x, int y, Cell c) {
        cells[y][x] = c;
    }

    public int width() {
        return cells[0].length;
    }

    public int height() {
        return cells.length;
    }

    public int xCell(String c) {
        int column = 0;
        for (int i = 0; i < c.length() && Character.isLetter(c.charAt(i)); i++) {
            column = column * 26 + (c.charAt(i) - 'A' + 1);
        }
        return column - 1;
    }

    public int yCell(String c) {
        StringBuilder numberPart = new StringBuilder();
        for (int i = 0; i < c.length(); i++) {
            if (Character.isDigit(c.charAt(i))) {
                numberPart.append(c.charAt(i));
            }
        }
        return Integer.parseInt(numberPart.toString()) - 1;
    }

    public String eval(int x, int y) {
        Cell cell = get(x, y);
        String content = cell.getContent();
        if (cell.isNumber(content)) {
            return content;
        } else if (cell.isText(content)) {
            return content;
        } else if (cell.isForm(content)) {
            Double result = cell.computeForm(content);
            return result == null ? "ERROR" : result.toString();
        }
        return "";
    }

    public String[][] evalAll() {
        String[][] result = new String[height()][width()];
        for (int i = 0; i < height(); i++) {
            for (int j = 0; j < width(); j++) {
                result[i][j] = eval(j, i);
            }
        }
        return result;
    }

    public int[][] depth() {
        int[][] result = new int[height()][width()];
        for (int i = 0; i < height(); i++) {
            for (int j = 0; j < width(); j++) {
                result[i][j] = computeDepth(j, i);
            }
        }
        return result;
    }

    private int computeDepth(int x, int y) {
        Cell cell = get(x, y);
        String content = cell.getContent();
        if (cell.isNumber(content) || cell.isText(content)) {
            return 0;
        } else if (cell.isForm(content)) {
            // Simplified dependency handling for now
            return 1; // Assuming no complex dependencies yet
        }
        return -1; // ERROR or unknown type
    }
}