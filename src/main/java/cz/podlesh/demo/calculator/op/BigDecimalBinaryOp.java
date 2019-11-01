package cz.podlesh.demo.calculator.op;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Functional interface for basic arithmetic operations of {@link java.math.BigDecimal}.
 */
@FunctionalInterface
public interface BigDecimalBinaryOp {

    BigDecimal apply(BigDecimal a1, BigDecimal a2, MathContext mathContext);

}
