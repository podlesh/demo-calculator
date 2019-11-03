package cz.podlesh.demo.calculator.api;

import cz.podlesh.demo.calculator.KnownOperators;
import cz.podlesh.demo.calculator.op.CalculatorType;
import cz.podlesh.demo.calculator.op.Operator;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Base superclass for both basic and scientific calculator.
 */
public abstract class AbstractCalculator {

    @Inject
    protected KnownOperators operators;

    protected MathContext getMathContext(Integer precision) {
        if (precision == null || precision.intValue() == 0)
            return null;
        if (precision < 0) {
            throw new IllegalArgumentException("negative precision");
        }
        return new MathContext(precision);
    }

    /**
     * Method used to filter available operators.
     *
     * @return calculator type, used by {@link Operator#isAvailableIn(CalculatorType)} to filter operators
     */
    protected abstract CalculatorType getCalculatorType();

    @Get
    public OperatorList listSupportedOperations() {
        return new OperatorList(
                operators.getOperators().stream()
                        .filter(op -> op.isAvailableIn(getCalculatorType()))
                        .map(op -> new OperatorList.JsonOperator(op.getSymbolicName(), op.getName().toLowerCase()))
                        .toArray(OperatorList.JsonOperator[]::new)
        );
    }

    @Post("/{opName}")
    public FullOperationResult request(@PathVariable String opName, FullOperation operation,
                                       @QueryValue("precision") @Nullable Integer precision) {
        Operator operator;
        try {
            operator = operators.findOperator(opName);
        } catch (KnownOperators.InvalidOperatorException e) {
            throw new InvalidOperatorInPathException(e.getMessage(), opName);
        }
        if (!operator.isAvailableIn(getCalculatorType())) {
            throw new InvalidOperatorInPathException("operator " + operation.operator + " is not available at this endpoint");
        }
        operation.operator = opName;
        return doRequest(operation, operator, precision);
    }

    @Post("/")
    public FullOperationResult request(FullOperation operation, @QueryValue("precision") @Nullable Integer precision) {
        Operator operator = operators.findOperator(operation.operator);
        if (!operator.isAvailableIn(getCalculatorType())) {
            throw new KnownOperators.InvalidOperatorException("operator " + operation.operator + " is not available at this endpoint");
        }
        return doRequest(operation, operator, precision);
    }

    private FullOperationResult doRequest(FullOperation operation, Operator operator, @Nullable Integer precision) {
        try {
            BigDecimal result = operator.apply(operation.getArguments(), getMathContext(precision));
            return new FullOperationResult(operation, null, result);
        } catch (ArithmeticException e) {
            //note: this is HTTP success!
            return new FullOperationResult(operation, e.getMessage(), null);
        }
    }

    //--------------------------------------------------------------------------------------------------------------

    private static class InvalidOperatorInPathException extends KnownOperators.InvalidOperatorException {
        public InvalidOperatorInPathException(String s, String name) {
            super(s, name);
        }

        public InvalidOperatorInPathException(String name) {
            super(name);
        }
    }

    /**
     * Invalid operator requested - convert this to 400 Bad Request or 404 Not Found.
     *
     * @param request http request
     * @param e       exception from {@link KnownOperators}
     * @return error response
     */
    @Error
    public HttpResponse<JsonError> jsonError(HttpRequest request, KnownOperators.InvalidOperatorException e) {
        JsonError error = new JsonError(e.getMessage());
        error.link(Link.SELF, Link.of(request.getUri()));
        if (e instanceof InvalidOperatorInPathException) {
            return HttpResponse.notFound(error);
        }
        return HttpResponse.badRequest(error);
    }

    /**
     * Invalid arguments to the operator.
     *
     * @param request http request
     * @param e       exception from {@link KnownOperators}
     * @return error response
     */
    @Error
    public HttpResponse<JsonError> jsonError(HttpRequest request, IllegalArgumentException e) {
        JsonError error = new JsonError(e.getMessage());
        error.link(Link.SELF, Link.of(request.getUri()));
        return HttpResponse.badRequest(error);
    }

}
