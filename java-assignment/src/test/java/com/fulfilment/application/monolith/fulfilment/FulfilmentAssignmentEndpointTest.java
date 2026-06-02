package com.fulfilment.application.monolith.fulfilment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfilmentAssignmentEndpointTest {

  @Test
  public void testCreateFulfilmentAssignmentShouldPersist() {
    var productId = createProduct("BONUS_CREATE_PRODUCT");
    var storeId = createStore("BONUS_CREATE_STORE");

    given()
        .contentType("application/json")
        .body(assignmentRequest(productId, storeId, 1L))
        .when()
        .post("/fulfilment-assignments")
        .then()
        .statusCode(201)
        .body("productId", equalTo(productId.intValue()))
        .body("productName", equalTo("BONUS_CREATE_PRODUCT"))
        .body("storeId", equalTo(storeId.intValue()))
        .body("storeName", equalTo("BONUS_CREATE_STORE"))
        .body("warehouseId", equalTo(1))
        .body("warehouseBusinessUnitCode", equalTo("MWH.001"));

    given()
        .when()
        .get("/fulfilment-assignments")
        .then()
        .statusCode(200)
        .body(containsString("BONUS_CREATE_PRODUCT"), containsString("BONUS_CREATE_STORE"));
  }

  @Test
  public void testCreateDuplicateFulfilmentAssignmentShouldFail() {
    var productId = createProduct("BONUS_DUPLICATE_PRODUCT");
    var storeId = createStore("BONUS_DUPLICATE_STORE");

    createFulfilmentAssignment(productId, storeId, 1L);

    given()
        .contentType("application/json")
        .body(assignmentRequest(productId, storeId, 1L))
        .when()
        .post("/fulfilment-assignments")
        .then()
        .statusCode(409)
        .body("error", containsString("already exists"));
  }

  @Test
  public void testProductCanBeFulfilledByMaximumTwoWarehousesPerStore() {
    var productId = createProduct("BONUS_PRODUCT_STORE_LIMIT_PRODUCT");
    var storeId = createStore("BONUS_PRODUCT_STORE_LIMIT_STORE");

    createFulfilmentAssignment(productId, storeId, 1L);
    createFulfilmentAssignment(productId, storeId, 2L);

    given()
        .contentType("application/json")
        .body(assignmentRequest(productId, storeId, 3L))
        .when()
        .post("/fulfilment-assignments")
        .then()
        .statusCode(409)
        .body("error", containsString("maximum of 2 warehouses per store"));
  }

  @Test
  public void testStoreCanBeFulfilledByMaximumThreeWarehouses() {
    var storeId = createStore("BONUS_STORE_LIMIT_STORE");
    var fourthWarehouseId = createWarehouse("MWH.BONUS_STORE_LIMIT");

    createFulfilmentAssignment(createProduct("BONUS_STORE_LIMIT_PRODUCT_1"), storeId, 1L);
    createFulfilmentAssignment(createProduct("BONUS_STORE_LIMIT_PRODUCT_2"), storeId, 2L);
    createFulfilmentAssignment(createProduct("BONUS_STORE_LIMIT_PRODUCT_3"), storeId, 3L);

    given()
        .contentType("application/json")
        .body(
            assignmentRequest(
                createProduct("BONUS_STORE_LIMIT_PRODUCT_4"), storeId, fourthWarehouseId))
        .when()
        .post("/fulfilment-assignments")
        .then()
        .statusCode(409)
        .body("error", containsString("maximum of 3 warehouses"));
  }

  @Test
  public void testWarehouseCanStoreMaximumFiveProductTypes() {
    var storeId = createStore("BONUS_WAREHOUSE_LIMIT_STORE");
    var warehouseId = createWarehouse("MWH.BONUS_WAREHOUSE_LIMIT");

    createFulfilmentAssignment(
        createProduct("BONUS_WAREHOUSE_LIMIT_PRODUCT_1"), storeId, warehouseId);
    createFulfilmentAssignment(
        createProduct("BONUS_WAREHOUSE_LIMIT_PRODUCT_2"), storeId, warehouseId);
    createFulfilmentAssignment(
        createProduct("BONUS_WAREHOUSE_LIMIT_PRODUCT_3"), storeId, warehouseId);
    createFulfilmentAssignment(
        createProduct("BONUS_WAREHOUSE_LIMIT_PRODUCT_4"), storeId, warehouseId);
    createFulfilmentAssignment(
        createProduct("BONUS_WAREHOUSE_LIMIT_PRODUCT_5"), storeId, warehouseId);

    given()
        .contentType("application/json")
        .body(
            assignmentRequest(
                createProduct("BONUS_WAREHOUSE_LIMIT_PRODUCT_6"), storeId, warehouseId))
        .when()
        .post("/fulfilment-assignments")
        .then()
        .statusCode(409)
        .body("error", containsString("maximum of 5 product types"));
  }

  @Test
  public void testCreateFulfilmentAssignmentWithUnknownProductShouldFail() {
    var storeId = createStore("BONUS_UNKNOWN_PRODUCT_STORE");

    given()
        .contentType("application/json")
        .body(assignmentRequest(9999L, storeId, 1L))
        .when()
        .post("/fulfilment-assignments")
        .then()
        .statusCode(404)
        .body("error", containsString("Product with id 9999 does not exist"));
  }

  private Long createProduct(String name) {
    return Long.valueOf(
        given()
            .contentType("application/json")
            .body(
                """
                {
                  "name": "%s",
                  "stock": 100
                }
                """
                    .formatted(name))
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .extract()
            .path("id")
            .toString());
  }

  private Long createStore(String name) {
    return Long.valueOf(
        given()
            .contentType("application/json")
            .body(
                """
                {
                  "name": "%s",
                  "quantityProductsInStock": 100
                }
                """
                    .formatted(name))
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .path("id")
            .toString());
  }

  private Long createWarehouse(String businessUnitCode) {
    return Long.valueOf(
        given()
            .contentType("application/json")
            .body(
                """
                {
                  "businessUnitCode": "%s",
                  "location": "AMSTERDAM-001",
                  "capacity": 10,
                  "stock": 0
                }
                """
                    .formatted(businessUnitCode))
            .when()
            .post("/warehouse")
            .then()
            .statusCode(201)
            .extract()
            .path("id")
            .toString());
  }

  private void createFulfilmentAssignment(Long productId, Long storeId, Long warehouseId) {
    given()
        .contentType("application/json")
        .body(assignmentRequest(productId, storeId, warehouseId))
        .when()
        .post("/fulfilment-assignments")
        .then()
        .statusCode(201);
  }

  private String assignmentRequest(Long productId, Long storeId, Long warehouseId) {
    return """
        {
          "productId": %d,
          "storeId": %d,
          "warehouseId": %d
        }
        """
        .formatted(productId, storeId, warehouseId);
  }
}
