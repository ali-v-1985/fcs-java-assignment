package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  @Test
  public void testListProducts() {
    final String path = "product";

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  public void testCreateGetUpdateAndDeleteProduct() {
    var name = uniqueName("PRODUCT_CRUD");
    var updatedName = uniqueName("PRODUCT_UPDATED");

    var productId =
        given()
            .contentType("application/json")
            .body(
                """
                {
                  "name": "%s",
                  "description": "flat-pack shelf",
                  "price": 19.95,
                  "stock": 8
                }
                """
                    .formatted(name))
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .body("name", equalTo(name))
            .body("description", equalTo("flat-pack shelf"))
            .body("stock", equalTo(8))
            .extract()
            .path("id");

    given()
        .when()
        .get("/product/{id}", productId)
        .then()
        .statusCode(200)
        .body("id", equalTo(productId))
        .body("name", equalTo(name));

    given()
        .contentType("application/json")
        .body(
            """
            {
              "name": "%s",
              "description": "updated product",
              "price": 24.50,
              "stock": 11
            }
            """
                .formatted(updatedName))
        .when()
        .put("/product/{id}", productId)
        .then()
        .statusCode(200)
        .body("id", equalTo(productId))
        .body("name", equalTo(updatedName))
        .body("description", equalTo("updated product"))
        .body("stock", equalTo(11));

    given().when().delete("/product/{id}", productId).then().statusCode(204);

    given()
        .when()
        .get("/product")
        .then()
        .statusCode(200)
        .body(not(containsString(updatedName)));
  }

  @Test
  public void testGetUnknownProductShouldReturnNotFound() {
    given()
        .when()
        .get("/product/999999")
        .then()
        .statusCode(404)
        .body("code", equalTo(404))
        .body("error", containsString("Product with id of 999999 does not exist."));
  }

  @Test
  public void testCreateProductWithIdShouldReturnUnprocessableEntity() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "id": 99,
              "name": "INVALID_PRODUCT",
              "stock": 1
            }
            """)
        .when()
        .post("/product")
        .then()
        .statusCode(422)
        .body("code", equalTo(422))
        .body("error", containsString("Id was invalidly set on request."));
  }

  @Test
  public void testUpdateProductWithoutNameShouldReturnUnprocessableEntity() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "description": "missing name",
              "stock": 1
            }
            """)
        .when()
        .put("/product/1")
        .then()
        .statusCode(422)
        .body("code", equalTo(422))
        .body("error", containsString("Product Name was not set on request."));
  }

  @Test
  public void testUpdateUnknownProductShouldReturnNotFound() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "name": "UNKNOWN_PRODUCT",
              "stock": 1
            }
            """)
        .when()
        .put("/product/999999")
        .then()
        .statusCode(404)
        .body("code", equalTo(404))
        .body("error", containsString("Product with id of 999999 does not exist."));
  }

  @Test
  public void testDeleteUnknownProductShouldReturnNotFound() {
    given()
        .when()
        .delete("/product/999999")
        .then()
        .statusCode(404)
        .body("code", equalTo(404))
        .body("error", containsString("Product with id of 999999 does not exist."));
  }

  private static String uniqueName(String prefix) {
    return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
  }
}
