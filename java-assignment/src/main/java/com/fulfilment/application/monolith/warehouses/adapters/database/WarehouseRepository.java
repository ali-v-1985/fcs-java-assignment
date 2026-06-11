package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.list("archivedAt is null").stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
  }

  @Override
  public Warehouse create(Warehouse warehouse) {
    var dbWarehouse = toDbWarehouse(warehouse);
    persist(dbWarehouse);
    return dbWarehouse.toWarehouse();
  }

  @Override
  public void update(Warehouse warehouse) {
    // Validate ID presence to avoid silently no-op updates which mask caller bugs
    if (warehouse == null || warehouse.id == null) {
      throw new com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException(
          "Warehouse id must be present for update.");
    }

    var dbWarehouse = findById(warehouse.id);
    if (dbWarehouse == null) {
      throw new com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException(
          "Warehouse with id of " + warehouse.id + " does not exist.");
    }

    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
  }

  @Override
  public Warehouse findActiveById(Long id) {
    return find("id = ?1 and archivedAt is null", id).firstResultOptional()
        .map(DbWarehouse::toWarehouse)
        .orElse(null);
  }

  @Override
  public Warehouse findActiveByBusinessUnitCode(String businessUnitCode) {
    return find("businessUnitCode = ?1 and archivedAt is null", businessUnitCode)
        .firstResultOptional()
        .map(DbWarehouse::toWarehouse)
        .orElse(null);
  }

  @Override
  public List<Warehouse> findActiveByLocation(String location) {
    return list("location = ?1 and archivedAt is null", location).stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
  }

  private DbWarehouse toDbWarehouse(Warehouse warehouse) {
    var dbWarehouse = new DbWarehouse();
    dbWarehouse.id = warehouse.id;
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    return dbWarehouse;
  }
}
