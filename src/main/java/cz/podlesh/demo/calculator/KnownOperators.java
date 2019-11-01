package cz.podlesh.demo.calculator;

import cz.podlesh.demo.calculator.op.BinaryOperator;
import cz.podlesh.demo.calculator.op.Operator;
import cz.podlesh.demo.calculator.op.UnaryOperator;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 * Micronaut bean that provides map of all know operators.
 */
@Singleton
public class KnownOperators {

    private final List<Operator> operators;
    private final Map<String, Operator> operatorsByName;

    public KnownOperators() {
        operatorsByName = new TreeMap<>();
        operators = new ArrayList<>();
        for (Operator[] values : Arrays.asList(
                BinaryOperator.values(),
                UnaryOperator.values()
        )) {
            for (Operator op : values) {
                add(op);
            }
        }
    }

    protected void add(Operator op) {
        operators.add(op);
        operatorsByName.put(op.getName().toLowerCase(), op);
        operatorsByName.put(op.getSymbolicName().toLowerCase(), op);
    }

    public List<Operator> getOperators() {
        return Collections.unmodifiableList(operators);
    }

    public Map<String, Operator> getOperatorsByName() {
        return Collections.unmodifiableMap(operatorsByName);
    }

    /**
     * Find operator by name; both symbolic and full names are accepted, case is ignored.
     * TODO: implement some prefix search for shorter/longer variants?
     * <p/>
     * This method always either succeeds and returns the operator, or throws an exception.
     *
     * @param name operator name
     * @return operator of this name, never <code>null</code>
     * @throws InvalidOperatorException unknown or missing operator name
     */
    @Nonnull
    public Operator findOperator(String name) throws InvalidOperatorException {
        if (name == null)
            throw new InvalidOperatorException("operator is missing", name);
        Operator operator = operatorsByName.get(name.toLowerCase());
        if (operator == null) {
            throw new InvalidOperatorException(name);
        }
        return operator;
    }

    public static class InvalidOperatorException extends NoSuchElementException {
        private final String name;

        public InvalidOperatorException(String s, String name) {
            super(s);
            this.name = name;
        }

        public InvalidOperatorException(String name) {
            super("unsupported operator: " + name);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
