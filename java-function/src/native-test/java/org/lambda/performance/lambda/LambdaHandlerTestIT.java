package org.lambda.performance.lambda;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusIntegrationTest
public class LambdaHandlerTestIT extends LambdaHandlerTest {

    @Test
    void testSimpleLambdaSuccess() throws Exception {
        given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\"request\": \"{ \\\"fistMatrix\\\": [ [1, 2, 3], [4, 5, 6] ], \\\"secondMatrix\\\": [ [7, 8], [9, 10], [11, 12] ] }\"}")
                .when()
                .post()
                .then()
                .statusCode(200)
                .body(containsString("Hello, World!"));
    }
}