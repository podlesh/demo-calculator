package cz.podlesh.demo.calculator.op;

import java.math.BigInteger;
import java.util.function.IntFunction;

/**
 * Utility class: implement factorial computation, with some error checking.
 */
public class Factorial implements IntFunction<BigInteger> {

    /**
     * Some arbitrary limit: 1001 digits maximum -> 450!.
     */
    public static final Factorial DEFAULT = new Factorial(450); //maxDigits(1000);


    private final int maxN;

    public Factorial(int maxN) {
        this.maxN = maxN;
    }

    public int getMaxN() {
        return maxN;
    }

    /**
     * Compute factorial for given n
     *
     * @param n the value; must be positive integer
     * @return factorial
     * @throws IllegalArgumentException value lower than 1
     * @throws ArithmeticException      value bigger than maximum supported n
     */
    @Override
    public BigInteger apply(int n) throws IllegalArgumentException, ArithmeticException {
        if (n < 1) {
            throw new IllegalArgumentException("factorial is not defined for " + n);
        }
        if (n > getMaxN()) {
            throw new ArithmeticException("cannot compute factorial of " + n + ", too big value");
        }
        switch (n) {
            case 1:
                return BigInteger.ONE;
            case 2:
                return new BigInteger("2");
        }
        BigInteger val = new BigInteger("2");
        BigInteger cnt = new BigInteger("3");
        for (int i = 3; i <= n; i++) {
            val = val.multiply(cnt);
            cnt = cnt.add(BigInteger.ONE);
        }
        return val;
    }


    /**
     * Create instance that produces only values up to specified number of (decimal) digits.
     *
     * @param numDigits maximum number of digits; this is used to compute maximum input number by using Sterling's approximation
     * @return instance with appropriate limit
     */
    public static Factorial maxDigits(int numDigits) {
        double digits = 1;
        for (int i = 2; i < Short.MAX_VALUE; i++) {
            double newDigits = digits + Math.log10(i);
            if (newDigits > numDigits) {
                return new Factorial(i);
            }
            digits = newDigits;
        }
        throw new IllegalArgumentException("invalid number of digits: " + numDigits);
    }


}
