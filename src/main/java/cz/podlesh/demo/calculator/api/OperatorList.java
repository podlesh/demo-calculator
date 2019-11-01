package cz.podlesh.demo.calculator.api;

/**
 * JSON representation of all known operators.
 */
public class OperatorList {

    protected final JsonOperator[] operators;

    public JsonOperator[] getOperators() {
        return operators;
    }

    public OperatorList(JsonOperator[] operators) {
        this.operators = operators;
    }

    public static class JsonOperator {
        protected String symbol, name;

        public JsonOperator(String symbol, String name) {
            this.symbol = symbol;
            this.name = name;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getName() {
            return name;
        }
    }

}
