package cz.podlesh.demo.calculator.op;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Iterator;
import java.util.List;

/**
 * All supported basic operators.
 */
public enum BinaryOperator implements Operator {

    PLUS("+", true, BigDecimal::add),
    MINUS("-", false, BigDecimal::subtract),
    MUL("*", true, BigDecimal::multiply),
    DIV("/", false, BigDecimal::divide),
    ;

    private final String symbolicName;
    /**
     * When <code>true</code>, the operator is commutative and associative --> it can be applied on unlimited number of arguments.
     */
    private final boolean anyLength;
    /**
     * The operator itself.
     */
    private final BigDecimalBinaryOp op;


    BinaryOperator(String symbolicName, boolean anyLength, BigDecimalBinaryOp op) {
        this.symbolicName = symbolicName;
        this.anyLength = anyLength;
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
        return 2;
    }

    @Override
    public int getMaxArgumentsCount() {
        return anyLength ? 2 : Integer.MAX_VALUE;
    }

    @Nonnull
    @Override
    public BigDecimal apply(@Nonnull List<BigDecimal> arguments, @Nullable MathContext mathContext) throws ArithmeticException, IllegalArgumentException {
        if (arguments.size() < 2) {
            throw new IllegalArgumentException("invalid argument list: " + this + " needs at least 2 arguments");
        }
        if (!anyLength && arguments.size() > 2) {
            throw new IllegalArgumentException("invalid argument list: " + this + " needs exactly 2 arguments");
        }
        final Iterator<BigDecimal> it = arguments.iterator();
        BigDecimal result = it.next();
        while (it.hasNext()) {
            result = op.apply(result, it.next(), mathContext);
        }
        return result;
    }

}
