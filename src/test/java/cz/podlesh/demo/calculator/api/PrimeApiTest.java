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
    void testFactorization(Number n, Collection<Number> expectedResult, HttpStatus expectedStatus, String error) {
        //create object manually to avoid BigDecimal parsing in request
        ObjectNode request = jackson.createObjectNode();
        request.putArray("arguments").add(n.toString());

        FactorizationResult result;
        try {
            result = sciCalc.toBlocking()
                    .retrieve(HttpRequest.POST("/factor", request),
                            Argument.of(FactorizationResult.class), Argument.of(ObjectNode.class)
                    );
            assertEquals(expectedStatus, OK);
        } catch (HttpClientResponseException e) {
            if (expectedStatus == e.getStatus() && expectedStatus != OK) {
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
        assertEquals(error, result.error);
    }

    public static Object[][] testCases() {
        return new Object[][]{
                {1, singletonList(1), OK, null},
                {1.0, singletonList(1), OK, null},
                {2, singletonList(2), OK, null},
                {3, singletonList(3), OK, null},
                {19, singletonList(19), OK, null},
                {27, Arrays.asList(3, 3, 3), OK, null},
                {54, Arrays.asList(2, 3, 3, 3), OK, null},
                {2 * 5 * 5 * 7 * 19, Arrays.asList(2, 5, 5, 7, 19), OK, null},
                {1_000_000, Arrays.asList(2, 2, 2, 2, 2, 2, 5, 5, 5, 5, 5, 5), OK, null},
                {1_000_000_000, Arrays.asList(2, 2, 2, 2, 2, 2, 2, 2, 2, 5, 5, 5, 5, 5, 5, 5, 5, 5), OK, null},
                {1_306_020_009, Arrays.asList(3, 7, 62191429), OK, null},
                {221_306_020_009L, Arrays.asList(17, 3659, 3557803), OK, null},

                //and some invalid results
                {0, null, OK, "only positive number can be factorized to primes"},
                {1.2, null, OK, "only integer number can be factorized to primes"},
                {Long.MAX_VALUE, null, OK, "prime factorization of " + Long.MAX_VALUE + " refused, too big value"},
        };
    }

}
