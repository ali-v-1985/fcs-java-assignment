package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.RetrieveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class RetrieveWarehouseUseCase implements RetrieveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public RetrieveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public List<Warehouse> listAll() {
    return warehouseStore.getAll();
  }

  @Override
  public Warehouse getById(Long id) {
    var warehouse = warehouseStore.findActiveById(id);
    if (warehouse == null) {
      throw new WarehouseNotFoundException("Warehouse with id " + id + " does not exist.");
    }

    return warehouse;
  }
}
