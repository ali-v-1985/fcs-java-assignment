package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FulfilmentAssignmentRepository implements PanacheRepository<FulfilmentAssignment> {

  public List<FulfilmentAssignment> listAllAssignments() {
    return list("order by store.id, product.id, warehouse.id");
  }

  public Product findProduct(Long productId) {
    return getEntityManager().find(Product.class, productId);
  }

  public Store findStore(Long storeId) {
    return getEntityManager().find(Store.class, storeId);
  }

  public DbWarehouse findActiveWarehouse(Long warehouseId) {
    var warehouse = getEntityManager().find(DbWarehouse.class, warehouseId);
    if (warehouse == null || warehouse.archivedAt != null) {
      return null;
    }

    return warehouse;
  }

  public boolean exists(Long productId, Long storeId, Long warehouseId) {
    return count(
            "product.id = ?1 and store.id = ?2 and warehouse.id = ?3",
            productId,
            storeId,
            warehouseId)
        > 0;
  }

  public long countWarehousesForProductAndStore(Long productId, Long storeId) {
    return getEntityManager()
        .createQuery(
            """
            select count(distinct assignment.warehouse.id)
            from FulfilmentAssignment assignment
            where assignment.product.id = :productId and assignment.store.id = :storeId
            """,
            Long.class)
        .setParameter("productId", productId)
        .setParameter("storeId", storeId)
        .getSingleResult();
  }

  public long countWarehousesForStore(Long storeId) {
    return getEntityManager()
        .createQuery(
            """
            select count(distinct assignment.warehouse.id)
            from FulfilmentAssignment assignment
            where assignment.store.id = :storeId
            """,
            Long.class)
        .setParameter("storeId", storeId)
        .getSingleResult();
  }

  public boolean isWarehouseAssignedToStore(Long storeId, Long warehouseId) {
    return count("store.id = ?1 and warehouse.id = ?2", storeId, warehouseId) > 0;
  }

  public long countProductTypesForWarehouse(Long warehouseId) {
    return getEntityManager()
        .createQuery(
            """
            select count(distinct assignment.product.id)
            from FulfilmentAssignment assignment
            where assignment.warehouse.id = :warehouseId
            """,
            Long.class)
        .setParameter("warehouseId", warehouseId)
        .getSingleResult();
  }

  public boolean isProductAssignedToWarehouse(Long productId, Long warehouseId) {
    return count("product.id = ?1 and warehouse.id = ?2", productId, warehouseId) > 0;
  }
}
