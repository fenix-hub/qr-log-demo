package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ExampleResourceTest {

    @Test
    @SuppressWarnings("unchecked")
    void testQREndpoint() {
        HashMap<String, String> responseBody = (HashMap<String, String>) given()
                .when().get("/qr")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract()
                .body().as(HashMap.class);
    }

    @Test
    void testChannelsEndpoint() {
        given()
            .when().get("/tokens")
            .then()
            .statusCode(200);
    }

}