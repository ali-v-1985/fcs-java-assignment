package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseEndpointTest {

  @Test
  public void testListWarehousesShouldReturnOnlyActiveWarehouses() {
    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  public void testCreateWarehouseShouldPersistAndReturnWarehouse() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "businessUnitCode": "MWH.CREATE",
              "location": "HELMOND-001",
              "capacity": 40,
              "stock": 5
            }
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(201)
        .body("businessUnitCode", equalTo("MWH.CREATE"))
        .body("location", equalTo("HELMOND-001"))
        .body("capacity", equalTo(40))
        .body("stock", equalTo(5));
  }

  @Test
  public void testCreateWarehouseWithExistingBusinessUnitCodeShouldFail() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "businessUnitCode": "MWH.001",
              "location": "ZWOLLE-002",
              "capacity": 20,
              "stock": 5
            }
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400)
        .body("error", containsString("business unit code already exists"));
  }

  @Test
  public void testCreateWarehouseWithInvalidLocationShouldFail() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "businessUnitCode": "MWH.BAD_LOCATION",
              "location": "UNKNOWN-001",
              "capacity": 20,
              "stock": 5
            }
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400)
        .body("error", containsString("Invalid location identifier"));
  }

  @Test
  public void testCreateWarehouseWhenLocationReachedMaximumWarehouseCountShouldFail() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "businessUnitCode": "MWH.MAX_COUNT",
              "location": "ZWOLLE-001",
              "capacity": 10,
              "stock": 2
            }
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400)
        .body("error", containsString("maximum number of warehouses"));
  }

  @Test
  public void testCreateWarehouseWhenLocationCapacityWouldBeExceededShouldFail() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "businessUnitCode": "MWH.MAX_CAPACITY",
              "location": "AMSTERDAM-001",
              "capacity": 60,
              "stock": 5
            }
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400)
        .body("error", containsString("maximum warehouse capacity would be exceeded"));
  }

  @Test
  public void testCreateWarehouseWithStockGreaterThanCapacityShouldFail() {
    given()
        .contentType("application/json")
        .body(
            """
            {
              "businessUnitCode": "MWH.STOCK_OVER_CAPACITY",
              "location": "ZWOLLE-002",
              "capacity": 10,
              "stock": 11
            }
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400)
        .body("error", containsString("capacity must accommodate warehouse stock"));
  }

  @Test
  public void testGetWarehouseByIdShouldReturnActiveWarehouse() {
    given()
        .when()
        .get("/warehouse/1")
        .then()
        .statusCode(200)
        .body("id", equalTo("1"))
        .body("businessUnitCode", equalTo("MWH.001"));
  }

  @Test
  public void testGetUnknownWarehouseByIdShouldReturnNotFound() {
    given().when().get("/warehouse/9999").then().statusCode(404);
  }

  @Test
  public void testArchiveWarehouseShouldHideItFromReads() {
    var warehouseId =
        given()
            .contentType("application/json")
            .body(
                """
                {
                  "businessUnitCode": "MWH.ARCHIVE",
                  "location": "EINDHOVEN-001",
                  "capacity": 20,
                  "stock": 2
                }
                """)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(201)
            .extract()
            .path("id")
            .toString();

    given().when().delete("/warehouse/" + warehouseId).then().statusCode(204);

    given().when().get("/warehouse/" + warehouseId).then().statusCode(404);

    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body(not(containsString("MWH.ARCHIVE")));
  }

  @Test
  public void testReplaceWarehouseShouldArchiveCurrentAndCreateReplacement() {
    var warehouseId =
        createWarehouse(
            """
            {
              "businessUnitCode": "MWH.REPLACE",
              "location": "AMSTERDAM-002",
              "capacity": 30,
              "stock": 5
            }
            """);

    given()
        .contentType("application/json")
        .body(
            """
            {
              "businessUnitCode": "MWH.REPLACE",
              "location": "AMSTERDAM-002",
              "capacity": 40,
              "stock": 5
            }
            """)
        .when()
        .post("/warehouse/MWH.REPLACE/replacement")
        .then()
        .statusCode(201)
        .body("businessUnitCode", equalTo("MWH.REPLACE"))
        .body("location", equalTo("AMSTERDAM-002"))
        .body("capacity", equalTo(40))
        .body("stock", equalTo(5));

    given().when().get("/warehouse/" + warehouseId).then().statusCode(404);
  }

  @Test
  public void testReplaceWarehouseWithDifferentStockShouldFail() {
    createWarehouse(
        """
        {
          "businessUnitCode": "MWH.REPLACE_STOCK",
          "location": "VETSBY-001",
          "capacity": 30,
          "stock": 7
        }
        """);

    given()
        .contentType("application/json")
        .body(
            """
            {
              "businessUnitCode": "MWH.REPLACE_STOCK",
              "location": "VETSBY-001",
              "capacity": 30,
              "stock": 8
            }
            """)
        .when()
        .post("/warehouse/MWH.REPLACE_STOCK/replacement")
        .then()
        .statusCode(400)
        .body("error", containsString("stock must match"));
  }

  @Test
  public void testReplaceWarehouseWithDifferentBusinessUnitCodeShouldFail() {
    createWarehouse(
        """
        {
          "businessUnitCode": "MWH.REPLACE_BUC",
          "location": "ZWOLLE-002",
          "capacity": 20,
          "stock": 3
        }
        """);

    given()
        .contentType("application/json")
        .body(
            """
            {
              "businessUnitCode": "MWH.REPLACE_BUC_DIFFERENT",
              "location": "ZWOLLE-002",
              "capacity": 20,
              "stock": 3
            }
            """)
        .when()
        .post("/warehouse/MWH.REPLACE_BUC/replacement")
        .then()
        .statusCode(400)
        .body("error", containsString("business unit code must match path parameter"));
  }

  private String createWarehouse(String requestBody) {
    return given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(201)
        .extract()
        .path("id")
        .toString();
  }
}
