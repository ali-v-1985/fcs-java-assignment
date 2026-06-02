package com.fulfilment.application.monolith.fulfilment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@ApplicationScoped
public class FulfilmentAssignmentService {

  private static final int MAX_WAREHOUSES_PER_PRODUCT_AND_STORE = 2;
  private static final int MAX_WAREHOUSES_PER_STORE = 3;
  private static final int MAX_PRODUCT_TYPES_PER_WAREHOUSE = 5;

  private final FulfilmentAssignmentRepository repository;

  public FulfilmentAssignmentService(FulfilmentAssignmentRepository repository) {
    this.repository = repository;
  }

  public List<FulfilmentAssignmentResponse> listAll() {
    return repository.listAllAssignments().stream().map(FulfilmentAssignmentResponse::from).toList();
  }

  @Transactional
  public FulfilmentAssignmentResponse create(FulfilmentAssignmentRequest request) {
    validateRequiredIds(request);

    var product = repository.findProduct(request.productId);
    if (product == null) {
      throw new WebApplicationException(
          "Product with id " + request.productId + " does not exist.", 404);
    }

    var store = repository.findStore(request.storeId);
    if (store == null) {
      throw new WebApplicationException(
          "Store with id " + request.storeId + " does not exist.", 404);
    }

    var warehouse = repository.findActiveWarehouse(request.warehouseId);
    if (warehouse == null) {
      throw new WebApplicationException(
          "Active warehouse with id " + request.warehouseId + " does not exist.", 404);
    }

    validateAssignmentDoesNotExist(request);
    validateProductStoreWarehouseLimit(request);
    validateStoreWarehouseLimit(request);
    validateWarehouseProductTypeLimit(request);

    var assignment = new FulfilmentAssignment();
    assignment.product = product;
    assignment.store = store;
    assignment.warehouse = warehouse;

    repository.persist(assignment);
    return FulfilmentAssignmentResponse.from(assignment);
  }

  private void validateRequiredIds(FulfilmentAssignmentRequest request) {
    if (request == null
        || request.productId == null
        || request.storeId == null
        || request.warehouseId == null) {
      throw new WebApplicationException(
          "Product, store and warehouse identifiers are required.", 400);
    }
  }

  private void validateAssignmentDoesNotExist(FulfilmentAssignmentRequest request) {
    if (repository.exists(request.productId, request.storeId, request.warehouseId)) {
      throw new WebApplicationException("Fulfilment assignment already exists.", 409);
    }
  }

  private void validateProductStoreWarehouseLimit(FulfilmentAssignmentRequest request) {
    if (repository.countWarehousesForProductAndStore(request.productId, request.storeId)
        >= MAX_WAREHOUSES_PER_PRODUCT_AND_STORE) {
      throw new WebApplicationException(
          "Product can be fulfilled by a maximum of 2 warehouses per store.", 409);
    }
  }

  private void validateStoreWarehouseLimit(FulfilmentAssignmentRequest request) {
    var warehouseAlreadyAssignedToStore =
        repository.isWarehouseAssignedToStore(request.storeId, request.warehouseId);
    if (!warehouseAlreadyAssignedToStore
        && repository.countWarehousesForStore(request.storeId) >= MAX_WAREHOUSES_PER_STORE) {
      throw new WebApplicationException("Store can be fulfilled by a maximum of 3 warehouses.", 409);
    }
  }

  private void validateWarehouseProductTypeLimit(FulfilmentAssignmentRequest request) {
    var productAlreadyAssignedToWarehouse =
        repository.isProductAssignedToWarehouse(request.productId, request.warehouseId);
    if (!productAlreadyAssignedToWarehouse
        && repository.countProductTypesForWarehouse(request.warehouseId)
            >= MAX_PRODUCT_TYPES_PER_WAREHOUSE) {
      throw new WebApplicationException("Warehouse can store a maximum of 5 product types.", 409);
    }
  }
}
