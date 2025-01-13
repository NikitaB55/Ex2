import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Cell {
    private String content;

    public Cell(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isNumber(String text) {
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
        return text.startsWith("=") && isValidFormula(text.substring(1));
    }

    private boolean isValidFormula(String formula) {
        if (!formula.matches("[0-9+\\-*/().A-Za-z]+$")) return false;
        // Check for balanced parentheses
        int balance = 0;
        for (char c : formula.toCharArray()) {
            if (c == '(') balance++;
            if (c == ')') balance--;
            if (balance < 0) return false; // Closing parenthesis before opening
        }
        return balance == 0;
    }

    public Double computeForm(String form, Spreadsheet sheet) {
        if (!isForm(form)) return null;
        String formula = form.substring(1); // Remove '='
        try {
            return evalFormula(formula, sheet);
        } catch (Exception e) {
            return null; // Invalid formula
        }
    }

    private Double evalFormula(String formula, Spreadsheet sheet) {
        // Replace cell references with actual values
        String updatedFormula = formula;
        for (int i = 0; i < formula.length(); i++) {
            if (Character.isLetter(formula.charAt(i))) {
                // Extract the cell reference (e.g., "A1", "B2")
                StringBuilder cellRef = new StringBuilder();
                while (i < formula.length() && (Character.isLetter(formula.charAt(i)) || Character.isDigit(formula.charAt(i)))) {
                    cellRef.append(formula.charAt(i));
                    i++;
                }
                String cellReference = cellRef.toString();
                int x = sheet.xCell(cellReference.substring(0, cellReference.length() - 1)); // Extract column part
                int y = sheet.yCell(cellReference.substring(cellReference.length() - 1)); // Extract row part
                String cellValue = sheet.eval(x, y);
                updatedFormula = updatedFormula.replace(cellReference, cellValue);
            }
        }

        try {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            return Double.parseDouble(engine.eval(updatedFormula).toString());
        } catch (Exception e) {
            return null; // Invalid formula
        }
    }
}
