package org.lambda.performance.lambda;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class LambdaHandlerTest {
    @Test
    void testSimpleLambdaSuccess() throws Exception {
        // you test your lambdas by invoking on http://localhost:8081
        // this works in dev mode too
        given()
                .contentType("application/json")
                .accept("application/json")
                .body("{}")
                .when()
                .post()
                .then()
                .statusCode(200)
                .body(containsString("Hello, World!"));
    }

}
