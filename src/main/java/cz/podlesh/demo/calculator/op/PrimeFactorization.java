package cz.podlesh.demo.calculator.op;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Prime factorization; limited to avoid too long duration.
 */
public class PrimeFactorization implements LongFunction<List<Long>> {

    public static final PrimeFactorization DEFAULT = new PrimeFactorization(1000L * Integer.MAX_VALUE);

    private final long maxN;

    public PrimeFactorization(long maxN) {
        this.maxN = maxN;
    }

    public long getMaxN() {
        return maxN;
    }

    /**
     * Compute all prime factors of given number n.
     *
     * @param n number to factor
     * @return all prime factors; list of size 1 means that the argument is prime number
     * @throws IllegalArgumentException negative number or zero
     * @throws ArithmeticException      too big parameter
     */
    public List<Long> apply(long n) throws IllegalArgumentException, ArithmeticException {
        if (n < 1) {
            throw new IllegalArgumentException("only positive numbers can be prime-factored");
        }
        if (n > getMaxN()) {
            throw new ArithmeticException("prime factorization of " + n + " refused, too big value");
        }
        if (n < 4) {
            return Collections.singletonList(n);
        }
        //TODO: use better algorithm!
        long maxPossibleFactor = (long) Math.ceil(Math.sqrt(n));
        List<Long> factors = new ArrayList<>();
        long remains = n;
        //first consume all "2" - they are more effectively implemented
        while ((remains % 2) == 0) {
            factors.add(2L);
            remains /= 2;
        }
        long factor = 3;
        while (factor <= maxPossibleFactor && remains > 1) {
            if ((remains % factor) == 0) {
                factors.add(factor);
                remains = remains / factor;
            } else {
                factor += 2;
            }
        }
        if (remains > 1) {
            factors.add(remains);
        }
        return factors;
    }

}
