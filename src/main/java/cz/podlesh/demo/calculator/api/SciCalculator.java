package cz.podlesh.demo.calculator.api;

import cz.podlesh.demo.calculator.op.CalculatorType;
import io.micronaut.http.annotation.Controller;

/**
 * Calculator: REST-like API that provides basic arithmetic operations.
 */
@Controller("/calculator/scientific")
public class SciCalculator  extends AbstractCalculator {

    @Override
    protected CalculatorType getCalculatorType() {
        return CalculatorType.SCIENTIFIC;
    }

    

}
