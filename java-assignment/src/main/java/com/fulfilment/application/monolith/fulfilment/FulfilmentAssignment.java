package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "fulfilment_assignment",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_fulfilment_assignment_product_store_warehouse",
            columnNames = {"product_id", "store_id", "warehouse_id"}))
@Cacheable
public class FulfilmentAssignment {

  @Id @GeneratedValue public Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  public Product product;

  @ManyToOne(optional = false)
  @JoinColumn(name = "store_id", nullable = false)
  public Store store;

  @ManyToOne(optional = false)
  @JoinColumn(name = "warehouse_id", nullable = false)
  public DbWarehouse warehouse;
}
