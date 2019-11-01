package cz.podlesh.demo.calculator.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.podlesh.demo.calculator.op.CalculatorType;
import cz.podlesh.demo.calculator.op.PrimeFactorization;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Calculator: REST-like API that provides basic arithmetic operations.
 */
@Controller("/calculator/scientific")
public class SciCalculator  extends AbstractCalculator {

    @Override
    protected CalculatorType getCalculatorType() {
        return CalculatorType.SCIENTIFIC;
    }

    @Post("/factor")
    public FactorizationResult factorization(FullOperation request) {
        List<BigDecimal> arguments = request.getArguments();
        if (arguments.isEmpty()) {
            throw new IllegalArgumentException("no argument to factorize given");
        }
        if (arguments.size()>1) {
            throw new IllegalArgumentException("only one number can be factorized");
        }
        request.operator = "factor";
        long n;
        try {
            n = arguments.get(0).longValueExact();
        } catch (ArithmeticException e) {
            return new FactorizationResult(request, "only integer number can be factorized to primes", null);
        }
        if (n < 1) {
            return new FactorizationResult(request, "only positive number can be factorized to primes", null);
        }
        try {
            List<Long> primes = PrimeFactorization.DEFAULT.apply(n);
            return new FactorizationResult(request, null, primes.stream().mapToLong(Long::longValue).toArray());
        } catch (ArithmeticException e) {
            return new FactorizationResult(request, e.getMessage(), null);
        }
    }

}
