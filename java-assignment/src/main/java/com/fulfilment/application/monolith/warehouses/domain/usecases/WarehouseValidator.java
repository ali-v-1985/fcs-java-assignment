package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@ApplicationScoped
public class WarehouseValidator {

  private final LocationResolver locationResolver;
  private final WarehouseStore warehouseStore;

  public WarehouseValidator(LocationResolver locationResolver, WarehouseStore warehouseStore) {
    this.locationResolver = locationResolver;
    this.warehouseStore = warehouseStore;
  }

  public void validateNewWarehouse(Warehouse warehouse) {
    validateRequiredFields(warehouse);
    validateUniqueBusinessUnitCode(warehouse.businessUnitCode);
    validateLocationConstraints(warehouse, null);
  }

  public void validateReplacementWarehouse(Warehouse newWarehouse, Warehouse warehouseBeingReplaced) {
    validateRequiredFields(newWarehouse);

    if (!Objects.equals(newWarehouse.stock, warehouseBeingReplaced.stock)) {
      throw new WarehouseValidationException("Replacement warehouse stock must match current stock.");
    }

    if (newWarehouse.capacity < warehouseBeingReplaced.stock) {
      throw new WarehouseValidationException(
          "Replacement warehouse capacity must accommodate current stock.");
    }

    validateLocationConstraints(newWarehouse, warehouseBeingReplaced.id);
  }

  private void validateRequiredFields(Warehouse warehouse) {
    if (StringUtils.isBlank(warehouse.businessUnitCode)) {
      throw new WarehouseValidationException("Warehouse business unit code is required.");
    }

    if (StringUtils.isBlank(warehouse.location)) {
      throw new WarehouseValidationException("Warehouse location is required.");
    }

    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new WarehouseValidationException("Warehouse capacity must be greater than zero.");
    }

    if (warehouse.stock == null || warehouse.stock < 0) {
      throw new WarehouseValidationException("Warehouse stock must not be negative.");
    }

    if (warehouse.stock > warehouse.capacity) {
      throw new WarehouseValidationException("Warehouse capacity must accommodate warehouse stock.");
    }
  }

  private void validateUniqueBusinessUnitCode(String businessUnitCode) {
    if (warehouseStore.findActiveByBusinessUnitCode(businessUnitCode) != null) {
      throw new WarehouseValidationException("Warehouse business unit code already exists.");
    }
  }

  private void validateLocationConstraints(Warehouse warehouse, Long warehouseIdToExclude) {
    var location = locationResolver.resolveByIdentifier(warehouse.location);
    var activeWarehouses =
        warehouseStore.findActiveByLocation(warehouse.location).stream()
            .filter(existingWarehouse -> !Objects.equals(existingWarehouse.id, warehouseIdToExclude))
            .toList();

    if (activeWarehouses.size() + 1 > location.maxNumberOfWarehouses) {
      throw new WarehouseValidationException("Location has reached maximum number of warehouses.");
    }

    var existingCapacity =
        activeWarehouses.stream()
            .map(existingWarehouse -> existingWarehouse.capacity)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();

    if (existingCapacity + warehouse.capacity > location.maxCapacity) {
      throw new WarehouseValidationException("Location maximum warehouse capacity would be exceeded.");
    }
  }
}
