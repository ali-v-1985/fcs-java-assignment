package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator warehouseValidator;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator warehouseValidator) {
    this.warehouseStore = warehouseStore;
    this.warehouseValidator = warehouseValidator;
  }

  @Override
  @Transactional
  public Warehouse replace(String businessUnitCode, Warehouse newWarehouse) {
    if (StringUtils.isBlank(businessUnitCode)) {
      throw new WarehouseValidationException("Warehouse business unit code is required.");
    }

    var warehouseBeingReplaced = warehouseStore.findActiveByBusinessUnitCode(businessUnitCode);
    if (warehouseBeingReplaced == null) {
      throw new WarehouseNotFoundException(
          "Warehouse with business unit code " + businessUnitCode + " does not exist.");
    }

    if (StringUtils.isNotBlank(newWarehouse.businessUnitCode)
        && !businessUnitCode.equals(newWarehouse.businessUnitCode)) {
      throw new WarehouseValidationException(
          "Replacement warehouse business unit code must match path parameter.");
    }

    newWarehouse.businessUnitCode = businessUnitCode;
    warehouseValidator.validateReplacementWarehouse(newWarehouse, warehouseBeingReplaced);

    warehouseBeingReplaced.archivedAt = LocalDateTime.now();
    warehouseStore.update(warehouseBeingReplaced);

    newWarehouse.id = null;
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    return warehouseStore.create(newWarehouse);
  }
}
