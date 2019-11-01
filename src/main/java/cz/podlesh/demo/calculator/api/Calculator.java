package cz.podlesh.demo.calculator.api;

import cz.podlesh.demo.calculator.KnownOperators;
import cz.podlesh.demo.calculator.op.Operator;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Calculator: REST-like API that provides basic arithmetic operations.
 */
@Controller("/calculator")
public class Calculator {

    @Inject
    protected KnownOperators operators;

    protected KnownOperators getOperators() {
        return operators;
    }

    protected MathContext getMathContext() {
        return null;
    }


    @Get("/")
    public OperatorList listSupportedOperations() {
        return new OperatorList(
                getOperators().getOperators().stream()
                        .map(op -> new OperatorList.JsonOperator(op.getSymbolicName(), op.getName().toLowerCase()))
                        .toArray(OperatorList.JsonOperator[]::new)
        );
    }

    @Post("/{operator}")
    public FullOperationResult request(@PathVariable String operator, FullOperation operation) {
        operation.operator = operator;
        return request(operation);
    }

    @Post("/")
    public FullOperationResult request(FullOperation operation) {
        Operator operator = getOperators().findOperator(operation.operator);
        try {
            BigDecimal result = operator.apply(operation.getArguments(), getMathContext());
            return new FullOperationResult(operation, null, result);
        } catch (ArithmeticException e) {
            //note: this is HTTP success!
            return new FullOperationResult(operation, e.getMessage(), null);
        }
    }

    /**
     * Invalid operator requested - convert this to 400 Bad Request.
     * @param request http request
     * @param e exception from {@link KnownOperators}
     * @return error response
     */
    @Error
    public HttpResponse<JsonError> jsonError(HttpRequest request, KnownOperators.InvalidOperatorException e) {
        JsonError error = new JsonError(e.getMessage());
        error.link(Link.SELF, Link.of(request.getUri()));
        return HttpResponse.badRequest(error);
    }

    /**
     * Invalid arguments to the operator.
     * @param request http request
     * @param e exception from {@link KnownOperators}
     * @return error response
     */
    @Error
    public HttpResponse<JsonError> jsonError(HttpRequest request, IllegalArgumentException e) {
        JsonError error = new JsonError(e.getMessage());
        error.link(Link.SELF, Link.of(request.getUri()));
        return HttpResponse.badRequest(error);
    }

}
