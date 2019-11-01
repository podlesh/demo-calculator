package cz.podlesh.demo.calculator.op;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

/**
 * Generic interface of generic operator.
 */
public interface Operator {

    /**
     * Default precision for division. Used only when the result has some fraction part
     */
    MathContext DEFAULT_DIV_PRECISION = MathContext.DECIMAL64;

    /**
     * Canonical symbolic name: this is the one used in JSON responses and logs.
     * Note that this is usually symbol and therefore <b>NOT</b> used as enum name!
     */
    String getSymbolicName();

    /**
     * Canonical "ascii" name: used in URL paths (lowercase) and java code (uppercase).
     * Usually uppercase.
     */
    String getName();

    /**
     * Detect category of the operator: is it available in the calculator of given type?
     */
    default boolean isAvailableIn(CalculatorType type) {
        return true;
    }

    /**
     * Number of minimum required arguments.
     */
    int getMinArgumentsCount();

    /**
     * Maximum number of accepted arguments; {@link Integer#MAX_VALUE} if unlimited.
     */
    int getMaxArgumentsCount();

    /**
     * Apply the operator on given arguments (operands).
     *
     * @param arguments   operands; size of this list must be in the {@link #getMinArgumentsCount()} - {@link #getMaxArgumentsCount()} range, otherwise {@link IllegalArgumentException}  is thrown
     * @param mathContext math context used for computation: defines precision limit and rounding;
     *                    <code>null</code> = use default one (depends on the operand); see {@link #fixMathContext(MathContext)}
     * @return result of the operation
     * @throws ArithmeticException      operation does not have defined result (division by zero) or it's infinite or it cannot be completed in reasonable time
     * @throws IllegalArgumentException invalid arguments: list is too small, to big or it contains nulls
     */
    @Nonnull
    BigDecimal apply(@Nonnull List<BigDecimal> arguments, @Nullable MathContext mathContext)
            throws ArithmeticException, IllegalArgumentException;

    /**
     * Fix match context: use the supplied one, or provide default one.
     * By default, {@link MathContext#UNLIMITED} is provided as a default; this is, however, not acceptable for division.
     *
     * @param mathContext supplied math context, might be <code>null</code>
     * @return real math context used for the computation, never <code>null</code>
     */
    @Nonnull
    default MathContext fixMathContext(@Nullable MathContext mathContext) {
        return mathContext == null ? MathContext.UNLIMITED : mathContext;
    }

}
