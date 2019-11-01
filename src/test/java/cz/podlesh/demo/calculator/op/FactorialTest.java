package cz.podlesh.demo.calculator.op;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
class FactorialTest {

    @Test
    void testBadInput() {
        assertThrows(IllegalArgumentException.class, () -> Factorial.DEFAULT.apply(-1));
        assertThrows(IllegalArgumentException.class, () -> Factorial.DEFAULT.apply(0));
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testFactorial(int n, BigInteger expectedResult) {
        assertEquals(expectedResult, Factorial.DEFAULT.apply(n));
    }

    public static Object[][] testCases() {
        return new Object[][]{
                {1, new BigInteger("1")},
                {2, new BigInteger("2")},
                {3, new BigInteger("6")},
                {4, new BigInteger("24")},
                {20, new BigInteger("2432902008176640000")},
                {100, new BigInteger("93326215443944152681699238856266700490715968264381621468592963895217599993229915608941463976156518286253697920827223758251185210916864000000000000000000000000")},
        };
    }

    @Test
    void maxDigits() {
        Factorial factorial = Factorial.maxDigits(158);
        assertEquals(100, factorial.getMaxN());
    }
}