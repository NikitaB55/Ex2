package Code;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashMap;
import java.util.Map;

public class SCell implements Cell {
    private String content;
    private int type;
    private int order;
    private boolean visited = false;
    private final Map<String, Double> variables;

    // Constructor
    public SCell(String content) {
        this.variables = new HashMap<>();
        setData(content);
    }

    // Getter for visited state
    public boolean isVisit() {
        return visited;
    }

    // Setter for visited state
    public void setVisit(boolean visited) {
        this.visited = visited;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return getData();
    }

    @Override
    public void setData(String content) {
        this.content = content;

        if (content.startsWith("=")) {
            this.type = Ex2Utils.FORM;
        } else {
            try {
                Double.parseDouble(content);
                this.type = Ex2Utils.NUMBER;
            } catch (NumberFormatException e) {
                this.type = Ex2Utils.TEXT;
            }
        }
    }

    @Override
    public String getData() {
        return content;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    // Evaluates the value of the cell based on its type
    public double evaluate(Ex2Sheet sheet, SCell currentCell) {
        switch (type) {
            case Ex2Utils.NUMBER:
                return Double.parseDouble(content);

            case Ex2Utils.FORM:
            case Ex2Utils.ERR_FORM_FORMAT:
            case Ex2Utils.ERR_CYCLE_FORM:
                return evaluateFormula(sheet, currentCell);

            default:
                currentCell.setType(Ex2Utils.ERR_FORM_FORMAT);
                return Ex2Utils.ERR_FORM_FORMAT;
        }
    }

    // Helper method to evaluate formulas
    private double evaluateFormula(Ex2Sheet sheet, SCell currentCell) {
        try {
            String formula = content.substring(1).toUpperCase(); // Remove '=' and convert to uppercase
            variables.clear();

            for (String variable : formula.split("[^A-Za-z0-9]")) {
                if (variable.matches("[A-Za-z]+[0-9]+")) { // Valid cell reference
                    int[] coords = sheet.cellCoordinates(variable);

                    if (!sheet.isIn(coords[0], coords[1])) {
                        currentCell.setType(Ex2Utils.ERR_FORM_FORMAT);
                        throw new IllegalArgumentException("Invalid reference: " + variable);
                    }

                    String refValue = sheet.eval(coords[0], coords[1]);
                    SCell referencedCell = sheet.get(coords[0], coords[1]);

                    if (referencedCell.getType() == Ex2Utils.ERR_CYCLE_FORM) {
                        currentCell.setType(Ex2Utils.ERR_CYCLE_FORM);
                        return Ex2Utils.ERR_CYCLE_FORM;
                    }

                    currentCell.setType(Ex2Utils.FORM);
                    variables.put(variable, Double.parseDouble(refValue));
                }
            }

            // Build and evaluate the expression
            Expression expression = new ExpressionBuilder(formula)
                    .variables(variables.keySet())
                    .build();

            variables.forEach(expression::setVariable);
            return expression.evaluate();
        } catch (Exception e) {
            currentCell.setType(Ex2Utils.ERR_FORM_FORMAT);
            return Ex2Utils.ERR_FORM_FORMAT;
        }
    }
}
