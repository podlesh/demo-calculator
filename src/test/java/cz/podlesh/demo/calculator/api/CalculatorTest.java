package cz.podlesh.demo.calculator.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    void testPlus() throws JsonProcessingException {
        FullOperation request = new FullOperation("+", new BigDecimal[]{new BigDecimal(1), new BigDecimal(2)});
        String json = jackson.writeValueAsString(request);
        System.out.println(json);
        FullOperationResult result = client.toBlocking()
                .retrieve(HttpRequest.POST("/", request),
                        Argument.of(FullOperationResult.class), Argument.of(ObjectNode.class)
                );
        assertNull(result.error);
        assertEquals(new BigDecimal(3), result.result);
    }

}
