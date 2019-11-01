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
    @Client("/calculator")
    HttpClient client;

    @Test
    void testListOperands() {
        ObjectNode result = client.toBlocking().retrieve(HttpRequest.GET("/"), ObjectNode.class);
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
            result = client.toBlocking()
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
        FullOperationResult result = client.toBlocking()
                .retrieve(HttpRequest.POST("/", request),
                        Argument.of(FullOperationResult.class), Argument.of(ObjectNode.class)
                );
        assertNull(result.error);
        assertEquals(new BigDecimal(3), result.result);
    }

    @Test
    void testDivideByZero() {
        FullOperationResult result = testOperation(invokePost("/"), new String[]{"1", "0"}, OK, null);
        assertNotNull(result);
        assertNull(result.result);
        assertEquals("Division by zero", result.error);
    }

    private Function<ObjectNode, FullOperationResult> invokePost(String op) {
        return (request) -> {
            request.put("operator", op);
            return client.toBlocking()
                    .retrieve(HttpRequest.POST("/", request),
                            Argument.of(FullOperationResult.class), Argument.of(ObjectNode.class)
                    );
        };
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testOperation(String op, String[] args, HttpStatus expectedStatus, String expectedResult) {
        testOperation(invokePost(op), args, expectedStatus, expectedResult);
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testOperandInPath(String op, String[] args, HttpStatus expectedStatus, String expectedResult) {
        String pathName;
        try {
            Operator operator = operators.findOperator(op);
            pathName = operator.getName();
        } catch (KnownOperators.InvalidOperatorException e) {
            //ok, ignore this test case - it is already covered by the basic test
            return;
        }

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
        } catch (HttpClientResponseException e) {
            if (expectedStatus == e.getStatus()) {
                //OK
                return null;
            }
            //this is NOT the expected outcome
            throw e;
        }
        if (expectedResult == null) {
            assertNotNull(result.error);
            assertNull(result.result);
        }
        else {
            assertEquals(expectedResult, result.result.toString());
            assertNull(result.error);
        }
        return result;
    }

    public static Object[][] testCases() {
        return new Object[][]{
                {"+", new String[]{"1", "2.4", "77"}, OK, "80.4"},
                {"-", new String[]{"10", "12"}, OK, "-2"},
                {"*", new String[]{"10", "12"}, OK, "120"},
                {"/", new String[]{"12", "10"}, OK, "1.2"},
                //some bad requests
                {"/", new String[]{"12", "10", "5"}, BAD_REQUEST, null},
                {"/", new String[]{"12"}, BAD_REQUEST, null},
                {"+", new String[]{"12"}, BAD_REQUEST, null},
                {"+", new String[]{}, BAD_REQUEST, null},
                {"+", null, BAD_REQUEST, null},
                //division by zero
                {"/", new String[]{"12", "0"}, OK, null},
                //test some very big numbers
                {"+", new String[]{"10000000000000000000000000000000000000000001", "3"}, OK, "10000000000000000000000000000000000000000004"},
                {"*", new String[]{"10000000000000000000000000000000000000000001", "3"}, OK, "30000000000000000000000000000000000000000003"},
                //unary operands
                {"+/-", new String[]{"12", "10"}, BAD_REQUEST, null},
                {"+/-", new String[]{"12.33"}, OK, "-12.33"},
                {"NEGATE", new String[]{"12.33"}, OK, "-12.33"},
                {"|x|", new String[]{"12.33"}, OK, "12.33"},
                {"|x|", new String[]{"-12.33"}, OK, "12.33"},
                {"abs", new String[]{"-12.33"}, OK, "12.33"},
                {"square", new String[]{"-12.33"}, OK, "152.0289"},
        };
    }

}
