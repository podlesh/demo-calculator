package cz.podlesh.demo.calculator.op;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.Arrays;

import static cz.podlesh.demo.calculator.op.BinaryOperator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test division operation from {@link BinaryOperator}.
 * Other operators are quite straightforward and covered by the REST API test (see coverage), but this one is tricky.
 */
class DivisionOperatorTest {

    private static BigDecimal bd(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        return new BigDecimal(o.toString());
    }

    /**
     * Full precision cases: take two numbers, multiply them, then make sure that the result can be divided by either one
     * (and the result is the other one).
     */
    @ParameterizedTest
    @MethodSource("fullPrecisionCases")
    void testFullPrecisionCases(Object mul1, Object mul2) {
        BigDecimal m1 = bd(mul1);
        BigDecimal m2 = bd(mul2);
        BigDecimal t = MUL.apply(Arrays.asList(m1, m2), null);
        assertEquals(m1, DIV.apply(Arrays.asList(t, m2), null));
        assertEquals(m2, DIV.apply(Arrays.asList(t, m1), null));
    }

    private static Object[][] fullPrecisionCases() {
        return new Object[][]{
                {1, 1},
                {10, 10},
                {1243, 33432342},
                {"12432222212123123123", 33432342},
                {"-12432222212123123123", 33432342},
                {"-12432222212123123123", -33432342},
        };
    }

    /**
     * Limited precision cases: parameters are directly the arguments of the division.
     * Result is provided with bigger precision than used by the implmentation
     */
    @ParameterizedTest
    @MethodSource("fullRoundedCases")
    void testRoundedCases(Object arg1, Object arg2, String expectedResult) {
        BigDecimal t = bd(arg1);
        BigDecimal m1 = bd(arg2);
        BigDecimal m2 = DIV.apply(Arrays.asList(t, m1), null);
        String result = m2.toString();
        //trim to the same length -1  (the last digit might be different)
        String trimmedExpectedResult = expectedResult.substring(0, result.length() - 1);
        String trimmedResult = result.substring(0, result.length() - 1);
        assertEquals(trimmedExpectedResult, trimmedResult);
    }

    private static Object[][] fullRoundedCases() {
        return new Object[][]{
                {10, 12, "0.8333333333333333333333333333333333333333"},
                {1243, 334, "3.7215568862275449101796407185628742514970"},
                {-1243, 33, "-37.6666666666666666666666666666666666666666"},
                {-1243, -33, "37.6666666666666666666666666666666666666666"},
                {"-124322222188342212312312388", -3, "41440740729447404104104129.3333333333333333333333333333333333333333"},
        };
    }

}