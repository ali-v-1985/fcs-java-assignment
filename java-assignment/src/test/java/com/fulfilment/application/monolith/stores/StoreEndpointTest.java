package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.util.ArrayList;
import java.util.List;
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
        .statusCode(422);

    verify(legacyStoreManagerGateway, never()).createStoreOnLegacySystem(any(Store.class));
  }
}
