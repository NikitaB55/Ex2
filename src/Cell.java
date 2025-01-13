// Cell.java
public class Cell {
    private String content; // The content of the cell

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
        // Basic validation: Ensure formula only contains numbers, operators, and parentheses
        return formula.matches("[0-9+\-*/().]+$");
    }

    public Double computeForm(String form) {
        if (!isForm(form)) return null;
        String formula = form.substring(1); // Remove '='
        try {
            return evalFormula(formula);
        } catch (Exception e) {
            return null; // Invalid formula
        }
    }

    private Double evalFormula(String formula) {
        // Evaluate the formula using JavaScript engine
        try {
            javax.script.ScriptEngineManager mgr = new javax.script.ScriptEngineManager();
            javax.script.ScriptEngine engine = mgr.getEngineByName("JavaScript");
            return Double.parseDouble(engine.eval(formula).toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid formula: " + formula);
        }
    }
}

