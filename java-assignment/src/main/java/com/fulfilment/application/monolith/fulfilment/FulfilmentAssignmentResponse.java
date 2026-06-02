package com.fulfilment.application.monolith.fulfilment;

public record FulfilmentAssignmentResponse(
    Long id,
    Long productId,
    String productName,
    Long storeId,
    String storeName,
    Long warehouseId,
    String warehouseBusinessUnitCode) {

  public static FulfilmentAssignmentResponse from(FulfilmentAssignment assignment) {
    return new FulfilmentAssignmentResponse(
        assignment.id,
        assignment.product.id,
        assignment.product.name,
        assignment.store.id,
        assignment.store.name,
        assignment.warehouse.id,
        assignment.warehouse.businessUnitCode);
  }
}
