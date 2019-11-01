package cz.podlesh.demo.calculator.op;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PrimeFactorizationTest {

    @ParameterizedTest
    @MethodSource("testCases")
    void testFactorization(long n, List<Number> expectedResult) {
        assertEquals(
                expectedResult.stream().map(Number::longValue).collect(Collectors.toList()),
                PrimeFactorization.DEFAULT.apply(n)
        );
    }

    public static Object[][] testCases() {
        return new Object[][]{
                {1, singletonList(1)},
                {2, singletonList(2)},
                {3, singletonList(3)},
                {19, singletonList(19)},
                {27, Arrays.asList(3, 3, 3)},
                {54, Arrays.asList(2, 3, 3, 3)},
                {2 * 5 * 5 * 7 * 19, Arrays.asList(2, 5, 5, 7, 19)},
                {1_000_000, Arrays.asList(2, 2, 2, 2, 2, 2, 5, 5, 5, 5, 5, 5)},
                {1_000_000_000, Arrays.asList(2, 2, 2, 2, 2, 2, 2, 2, 2, 5, 5, 5, 5, 5, 5, 5, 5, 5)},
                {1_306_020_009, Arrays.asList(3, 7, 62191429)},
                {221_306_020_009L, Arrays.asList(17, 3659, 3557803)},
        };
    }

}