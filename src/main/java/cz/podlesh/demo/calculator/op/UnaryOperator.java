package cz.podlesh.demo.calculator.op;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.function.BiFunction;

/**
 * All supported basic operators.
 */
public enum UnaryOperator implements Operator {

    NEGATE("+/-", BigDecimal::negate),
    ABS("|x|", BigDecimal::abs),
    SQUARE("x^2", (v, mc) -> v.multiply(v, mc)),
    FACT("x!", (v, mc) -> {
        if (v.signum() <= 0) {
            throw new ArithmeticException("factorial is defined only for positive numbers");
        }
        final int n;
        try {
            n = v.intValueExact();
        } catch (Exception ignored) {
            throw new ArithmeticException("factorial is defined only for integers");
        }
        return new BigDecimal(Factorial.DEFAULT.apply(n));
    }),
    ;

    private final String symbolicName;
    /**
     * The operator itself.
     */
    private final BiFunction<BigDecimal, MathContext, BigDecimal> op;

    UnaryOperator(String symbolicName, BiFunction<BigDecimal, MathContext, BigDecimal> op) {
        this.symbolicName = symbolicName;
        this.op = op;
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    @Override
    public String getName() {
        return name();
    }

    /**
     * Binary operators always need two arguments.
     *
     * @return
     */
    @Override
    public int getMinArgumentsCount() {
        return 1;
    }

    @Override
    public int getMaxArgumentsCount() {
        return 1;
    }

    @Nonnull
    @Override
    public BigDecimal apply(@Nonnull List<BigDecimal> arguments, @Nullable MathContext mathContext) throws ArithmeticException, IllegalArgumentException {
        if (arguments.size() != 1) {
            throw new IllegalArgumentException("invalid argument list: " + this + " is an unary operator");
        }
        //ensure that we have precision specified
        mathContext = fixMathContext(mathContext);
        //and apply the operation
        return op.apply(arguments.get(0), mathContext);
    }

}
