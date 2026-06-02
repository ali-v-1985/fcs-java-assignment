package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreEndpointTest {

  @InjectMock LegacyStoreManagerGateway legacyStoreManagerGateway;
  @Inject TransactionSynchronizationRegistry transactionSynchronizationRegistry;

  private List<Integer> transactionStatuses;

  @BeforeEach
  public void setUp() {
    reset(legacyStoreManagerGateway);
    transactionStatuses = new ArrayList<>();
  }

  @Test
  public void testCreateStoreShouldNotifyLegacySystemAfterTransactionCommit() {
    doAnswer(
            invocation -> {
              transactionStatuses.add(transactionSynchronizationRegistry.getTransactionStatus());
              return null;
            })
        .when(legacyStoreManagerGateway)
        .createStoreOnLegacySystem(any(Store.class));

    given()
        .contentType("application/json")
        .body(
            """
            {
              "name": "TASK2_CREATE",
              "quantityProductsInStock": 7
            }
            """)
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .body("name", equalTo("TASK2_CREATE"))
        .body("quantityProductsInStock", equalTo(7));

    assertEquals(List.of(Status.STATUS_NO_TRANSACTION), transactionStatuses);
  }

  @Test
  public void testUpdateStoreShouldNotifyLegacySystemAfterTransactionCommit() {
    doAnswer(
            invocation -> {
              transactionStatuses.add(transactionSynchronizationRegistry.getTransactionStatus());
              return null;
            })
        .when(legacyStoreManagerGateway)
        .updateStoreOnLegacySystem(any(Store.class));

    given()
        .contentType("application/json")
        .body(
            """
            {
              "name": "TASK2_UPDATE",
              "quantityProductsInStock": 12
            }
            """)
        .when()
        .put("/store/1")
        .then()
        .statusCode(200)
        .body("name", equalTo("TASK2_UPDATE"))
        .body("quantityProductsInStock", equalTo(12));

    assertEquals(List.of(Status.STATUS_NO_TRANSACTION), transactionStatuses);
  }

  @Test
  public void testInvalidCreateStoreShouldNotNotifyLegacySystem() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "id": 999,
              "name": "TASK2_INVALID",
              "quantityProductsInStock": 1
            }
            """)
        .when()
        .post("/store")
        .then()
        .statusCode(422)
        .body("code", equalTo(422))
        .body("error", containsString("Id was invalidly set on request."));

    verify(legacyStoreManagerGateway, never()).createStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void testListAndGetSingleStore() {
    var storeName = uniqueName("STORE_GET");
    var storeId = createStore(storeName, 9);

    given().when().get("/store").then().statusCode(200).body(containsString(storeName));

    given()
        .when()
        .get("/store/{id}", storeId)
        .then()
        .statusCode(200)
        .body("id", equalTo(storeId))
        .body("name", equalTo(storeName))
        .body("quantityProductsInStock", equalTo(9));
  }

  @Test
  public void testGetUnknownStoreShouldReturnNotFound() {
    given()
        .when()
        .get("/store/999999")
        .then()
        .statusCode(404)
        .body("code", equalTo(404))
        .body("error", containsString("Store with id of 999999 does not exist."));
  }

  @Test
  public void testPatchStoreShouldNotifyLegacySystemAfterTransactionCommit() {
    var storeId = createStore(uniqueName("STORE_PATCH"), 4);
    reset(legacyStoreManagerGateway);

    doAnswer(
            invocation -> {
              transactionStatuses.add(transactionSynchronizationRegistry.getTransactionStatus());
              return null;
            })
        .when(legacyStoreManagerGateway)
        .updateStoreOnLegacySystem(any(Store.class));

    given()
        .contentType("application/json")
        .body(
            """
            {
              "name": "TASK2_PATCH",
              "quantityProductsInStock": 15
            }
            """)
        .when()
        .patch("/store/{id}", storeId)
        .then()
        .statusCode(200)
        .body("id", equalTo(storeId))
        .body("name", equalTo("TASK2_PATCH"))
        .body("quantityProductsInStock", equalTo(15));

    assertEquals(List.of(Status.STATUS_NO_TRANSACTION), transactionStatuses);
  }

  @Test
  public void testUpdateStoreWithoutNameShouldReturnUnprocessableEntityAndSkipLegacySystem() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "quantityProductsInStock": 2
            }
            """)
        .when()
        .put("/store/1")
        .then()
        .statusCode(422)
        .body("code", equalTo(422))
        .body("error", containsString("Store Name was not set on request."));

    verify(legacyStoreManagerGateway, never()).updateStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void testPatchStoreWithoutNameShouldReturnUnprocessableEntityAndSkipLegacySystem() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "quantityProductsInStock": 2
            }
            """)
        .when()
        .patch("/store/1")
        .then()
        .statusCode(422)
        .body("code", equalTo(422))
        .body("error", containsString("Store Name was not set on request."));

    verify(legacyStoreManagerGateway, never()).updateStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void testUpdateUnknownStoreShouldReturnNotFoundAndSkipLegacySystem() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "name": "UNKNOWN_STORE",
              "quantityProductsInStock": 2
            }
            """)
        .when()
        .put("/store/999999")
        .then()
        .statusCode(404)
        .body("code", equalTo(404))
        .body("error", containsString("Store with id of 999999 does not exist."));

    verify(legacyStoreManagerGateway, never()).updateStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void testPatchUnknownStoreShouldReturnNotFoundAndSkipLegacySystem() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "name": "UNKNOWN_STORE",
              "quantityProductsInStock": 2
            }
            """)
        .when()
        .patch("/store/999999")
        .then()
        .statusCode(404)
        .body("code", equalTo(404))
        .body("error", containsString("Store with id of 999999 does not exist."));

    verify(legacyStoreManagerGateway, never()).updateStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void testDeleteStore() {
    var storeName = uniqueName("STORE_DELETE");
    var storeId = createStore(storeName, 1);

    given().when().delete("/store/{id}", storeId).then().statusCode(204);

    given()
        .when()
        .get("/store")
        .then()
        .statusCode(200)
        .body(org.hamcrest.core.IsNot.not(containsString(storeName)));
  }

  @Test
  public void testDeleteUnknownStoreShouldReturnNotFound() {
    given()
        .when()
        .delete("/store/999999")
        .then()
        .statusCode(404)
        .body("code", equalTo(404))
        .body("error", containsString("Store with id of 999999 does not exist."));
  }

  @Test
  public void testCreateStoreWhenLegacySystemFailsShouldReturnServerErrorAfterStoreCommit() {
    var storeName = uniqueName("STORE_LEGACY_FAILURE");

    doThrow(new IllegalStateException("Legacy store manager unavailable"))
        .when(legacyStoreManagerGateway)
        .createStoreOnLegacySystem(any(Store.class));

    given()
        .contentType("application/json")
        .body(
            """
            {
              "name": "%s",
              "quantityProductsInStock": 6
            }
            """
                .formatted(storeName))
        .when()
        .post("/store")
        .then()
        .statusCode(500)
        .body("code", equalTo(500))
        .body("error", containsString("Legacy store manager unavailable"));

    reset(legacyStoreManagerGateway);

    given().when().get("/store").then().statusCode(200).body(containsString(storeName));
  }

  @Test
  public void testStoreErrorMapperShouldMapWebApplicationException() {
    var errorMapper = new StoreResource.ErrorMapper();
    errorMapper.objectMapper = new ObjectMapper();

    var response = errorMapper.toResponse(new WebApplicationException("invalid store request", 400));
    var entity = (ObjectNode) response.getEntity();

    assertEquals(400, response.getStatus());
    assertEquals(400, entity.get("code").asInt());
    assertEquals(WebApplicationException.class.getName(), entity.get("exceptionType").asText());
    assertEquals("invalid store request", entity.get("error").asText());
  }

  @Test
  public void testStoreErrorMapperShouldMapUnexpectedExceptionWithoutMessage() {
    var errorMapper = new StoreResource.ErrorMapper();
    errorMapper.objectMapper = new ObjectMapper();

    var response = errorMapper.toResponse(new RuntimeException());
    var entity = (ObjectNode) response.getEntity();

    assertEquals(500, response.getStatus());
    assertEquals(500, entity.get("code").asInt());
    assertEquals(RuntimeException.class.getName(), entity.get("exceptionType").asText());
    assertFalse(entity.has("error"));
  }

  private static Object createStore(String name, int quantityProductsInStock) {
    return given()
        .contentType("application/json")
        .body(
            """
            {
              "name": "%s",
              "quantityProductsInStock": %d
            }
            """
                .formatted(name, quantityProductsInStock))
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .body("name", equalTo(name))
        .body("quantityProductsInStock", equalTo(quantityProductsInStock))
        .extract()
        .path("id");
  }

  private static String uniqueName(String prefix) {
    return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
  }
}
