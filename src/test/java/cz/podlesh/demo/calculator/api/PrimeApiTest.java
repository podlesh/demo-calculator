package cz.podlesh.demo.calculator.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;

import static io.micronaut.http.HttpStatus.OK;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the special API for prime number factorization
 */
@MicronautTest
public class PrimeApiTest {

    @SuppressWarnings("unused")
    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/calculator/scientific")
    HttpClient sciCalc;
    @Inject
    ObjectMapper jackson;


    @ParameterizedTest
    @MethodSource("testCases")
    void testFactorization(long n, Collection<Number> expectedResult, HttpStatus expectedStatus) {
        //create object manually to avoid BigDecimal parsing in request
        ObjectNode request = jackson.createObjectNode();
        request.putArray("arguments").add(n);

        FactorizationResult result;
        try {
            result = sciCalc.toBlocking()
                    .retrieve(HttpRequest.POST("/factor", request),
                            Argument.of(FactorizationResult.class), Argument.of(ObjectNode.class)
                    );
            assertEquals(expectedStatus, OK);
        } catch (HttpClientResponseException e) {
            if (expectedStatus == e.getStatus()) {
                //OK
                return;
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
            long[] expectedArr = expectedResult.stream().mapToLong(Number::longValue).toArray();
            assertArrayEquals(expectedArr, result.result);
        }
    }

    public static Object[][] testCases() {
        return new Object[][]{
                {1, singletonList(1), OK},
                {2, singletonList(2), OK},
                {3, singletonList(3), OK},
                {19, singletonList(19), OK},
                {27, Arrays.asList(3, 3, 3), OK},
                {54, Arrays.asList(2, 3, 3, 3), OK},
                {2 * 5 * 5 * 7 * 19, Arrays.asList(2, 5, 5, 7, 19), OK},
                {1_000_000, Arrays.asList(2, 2, 2, 2, 2, 2, 5, 5, 5, 5, 5, 5), OK},
                {1_000_000_000, Arrays.asList(2, 2, 2, 2, 2, 2, 2, 2, 2, 5, 5, 5, 5, 5, 5, 5, 5, 5), OK},
                {1_306_020_009, Arrays.asList(3, 7, 62191429), OK},
                {221_306_020_009L, Arrays.asList(17, 3659, 3557803), OK},
        };
    }

}
