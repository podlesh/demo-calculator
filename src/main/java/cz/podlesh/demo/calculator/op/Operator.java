package cz.podlesh.demo.calculator.op;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

/**
 *
 */
public interface Operator {

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
     * @param mathContext math context used for computation: defines precision limit and rounding
     * @return result of the operation
     * @throws ArithmeticException      operation does not have defined result (division by zero) or it's infinite or it cannot be completed in reasonable time
     * @throws IllegalArgumentException invalid arguments: list is too small, to big or it contains nulls
     */
    @Nonnull
    BigDecimal apply(@Nonnull List<BigDecimal> arguments, @Nonnull MathContext mathContext)
            throws ArithmeticException, IllegalArgumentException;

}
