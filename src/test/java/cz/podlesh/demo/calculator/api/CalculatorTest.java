package cz.podlesh.demo.calculator.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.podlesh.demo.calculator.KnownOperators;
import cz.podlesh.demo.calculator.op.BinaryOperator;
import cz.podlesh.demo.calculator.op.Operator;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.micronaut.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the calculator API.
 */
@MicronautTest
public class CalculatorTest {

    @SuppressWarnings("unused")
    @Inject
    EmbeddedServer server;

    @Inject
    ObjectMapper jackson;
    @Inject
    KnownOperators operators;

    @Inject
    @Client("/calculator/scientific")
    HttpClient sciCalc;
    @Inject
    @Client("/calculator/basic")
    HttpClient basicCalc;

    @Test
    void testListOperands() {
        ObjectNode result = sciCalc.toBlocking().retrieve(HttpRequest.GET("/"), ObjectNode.class);
        System.out.println(result);
        ArrayNode list = (ArrayNode) result.get("operators");
        Assertions.assertNotNull(list);
        Set<String> symbols = new TreeSet<>();
        for (JsonNode node : list) {
            assertEquals(node.getNodeType(), JsonNodeType.OBJECT);
            ObjectNode opNode = (ObjectNode) node;
            Assertions.assertNotNull(opNode.get("symbol"));
            Assertions.assertNotNull(opNode.get("name"));
            symbols.add(opNode.get("symbol").asText());
        }
        assertTrue(symbols.containsAll(
                Stream.of(BinaryOperator.values()).map(Operator::getSymbolicName).collect(Collectors.toSet()))
        );
    }

    @Test
    void testUnsupportedOperand() {
        FullOperation request = new FullOperation("?", null);
        HttpResponse<ObjectNode> result = null;
        try {
            result = sciCalc.toBlocking()
                    .exchange(HttpRequest.POST("/", request),
                            Argument.of(ObjectNode.class), Argument.of(ObjectNode.class)
                    );
        } catch (HttpClientResponseException e) {
            //noinspection unchecked
            result = (HttpResponse<ObjectNode>) e.getResponse();
        }
        assertEquals(result.getStatus(), HttpStatus.BAD_REQUEST);
        Optional<ObjectNode> body = result.getBody(ObjectNode.class);
        assertTrue(body.isPresent());
        assertEquals("unsupported operator: ?", body.get().get("message").asText());
    }

    @Test
    void testPlus() {
        FullOperation request = new FullOperation("+", new BigDecimal[]{new BigDecimal(1), new BigDecimal(2)});
        FullOperationResult result = sciCalc.toBlocking()
                .retrieve(HttpRequest.POST("/", request),
                        Argument.of(FullOperationResult.class), Argument.of(ObjectNode.class)
                );
        assertNull(result.error);
        assertEquals(new BigDecimal(3), result.result);
    }

    @Test
    void testDivideByZero() {
        FullOperationResult result = testOperation(invokePost("/", false), new String[]{"1", "0"}, OK, null);
        assertNotNull(result);
        assertNull(result.result);
        assertEquals("Division by zero", result.error);
    }

    private Function<ObjectNode, FullOperationResult> invokePost(String op, boolean sci) {
        return (request) -> {
            request.put("operator", op);
            return (sci ? sciCalc : basicCalc).toBlocking()
                    .retrieve(HttpRequest.POST("/", request),
                            Argument.of(FullOperationResult.class), Argument.of(ObjectNode.class)
                    );
        };
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testOperationBasic(String op, boolean sciOnly, String[] args, HttpStatus expectedStatus, String expectedResult) {
        if (sciOnly)
            testOperation(invokePost(op, false), args, BAD_REQUEST, null);
        else
            testOperation(invokePost(op, false), args, expectedStatus, expectedResult);
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testOperationSci(String op, boolean sciOnly, String[] args, HttpStatus expectedStatus, String expectedResult) {
        testOperation(invokePost(op, true), args, expectedStatus, expectedResult);
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testOperandInPathBasic(String op, boolean sciOnly, String[] args, HttpStatus expectedStatus, String expectedResult) {
        if (sciOnly) {
            expectedStatus = NOT_FOUND;
            expectedResult = null;
        }
        testOperandInPath(op, sciOnly, args, expectedStatus, expectedResult, basicCalc);
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testOperandInPathSci(String op, boolean sciOnly, String[] args, HttpStatus expectedStatus, String expectedResult) {
        testOperandInPath(op, sciOnly, args, expectedStatus, expectedResult, sciCalc);
    }

    private void testOperandInPath(String op, boolean sciOnly, String[] args, HttpStatus expectedStatus, String expectedResult, HttpClient client) {
        Operator operator = operators.findOperator(op);
        String pathName = operator.getName();

        Function<ObjectNode, FullOperationResult> invoke = (request) -> client.toBlocking()
                .retrieve(HttpRequest.POST("/" + pathName, request),
                        Argument.of(FullOperationResult.class), Argument.of(ObjectNode.class)
                );
        testOperation(invoke, args, expectedStatus, expectedResult);
    }

    private FullOperationResult testOperation(Function<ObjectNode, FullOperationResult> invoke, String[] args, HttpStatus expectedStatus, String expectedResult) {
        //create object manually to avoid BigDecimal parsing in request
        ObjectNode request = jackson.createObjectNode();
        if (args != null) {
            ArrayNode arguments = request.putArray("arguments");
            Arrays.stream(args).forEach(arguments::add);
        }

        FullOperationResult result;
        try {
            result = invoke.apply(request);
            assertEquals(expectedStatus, OK, result.toString());
        } catch (HttpClientResponseException e) {
            if (expectedStatus == e.getStatus() && expectedStatus != OK) {
                //OK
                return null;
            }
            //this is NOT the expected outcome
            throw e;
        }
        System.out.println(result);
        if (expectedResult == null) {
            assertNotNull(result.error);
            assertNull(result.result);
        } else {
            assertNull(result.error);
            assertEquals(expectedResult, result.result.toString());
        }
        return result;
    }

    public static Object[][] testCases() {
        return new Object[][]{
                {"+", false, new String[]{"1", "2.4", "77"}, OK, "80.4"},
                {"-", false, new String[]{"10", "12"}, OK, "-2"},
                {"*", false, new String[]{"10", "12"}, OK, "120"},
                {"/", false, new String[]{"12", "10"}, OK, "1.2"},
                //some bad requests
                {"/", false, new String[]{"12", "10", "5"}, BAD_REQUEST, null},
                {"/", false, new String[]{"12"}, BAD_REQUEST, null},
                {"+", false, new String[]{"12"}, BAD_REQUEST, null},
                {"+", false, new String[]{}, BAD_REQUEST, null},
                {"+", false, null, BAD_REQUEST, null},
                //division by zero
                {"/", false, new String[]{"12", "0"}, OK, null},
                //test some very big numbers
                {"+", false, new String[]{"10000000000000000000000000000000000000000001", "3"}, OK, "10000000000000000000000000000000000000000004"},
                {"*", false, new String[]{"10000000000000000000000000000000000000000001", "3"}, OK, "30000000000000000000000000000000000000000003"},
                //unary operands
                {"+/-", false, new String[]{"12", "10"}, BAD_REQUEST, null},
                {"+/-", false, new String[]{"12.33"}, OK, "-12.33"},
                {"NEGATE", false, new String[]{"12.33"}, OK, "-12.33"},
                {"|x|", false, new String[]{"12.33"}, OK, "12.33"},
                {"|x|", false, new String[]{"-12.33"}, OK, "12.33"},
                {"abs", false, new String[]{"-12.33"}, OK, "12.33"},
                {"square", true, new String[]{"-12.33"}, OK, "152.0289"},
                {"x!", true, new String[]{"20"}, OK, "2432902008176640000"},
                {"fact", true, new String[]{"4"}, OK, "24"},
                {"x!", true, new String[]{"-1"}, OK, null},

                //division is tricky; for infinite expansion, limited precision is auto-selected
                {"/", false, new String[]{"12", "10"}, OK, "1.2"},
                {"/", false, new String[]{"30000000000000000000000000000000000000000003", "3"}, OK, "10000000000000000000000000000000000000000001"},
                {"/", false, new String[]{"10", "12"}, OK, "0.8333333333333333"},
                {"/", false, new String[]{"10000000000000000000000000000000000000000001", "3"}, OK, "3333333333333333333333333333333333333333333.6666666666666667"},
        };
    }

}
