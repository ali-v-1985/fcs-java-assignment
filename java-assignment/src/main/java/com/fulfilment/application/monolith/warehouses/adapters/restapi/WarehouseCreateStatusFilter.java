package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;
import org.jboss.resteasy.reactive.server.SimpleResourceInfo;

@Provider
@ApplicationScoped
public class WarehouseCreateStatusFilter {

  public static final String CREATE_ANEW_WAREHOUSE_UNIT = "createANewWarehouseUnit";
  public static final String REPLACE_THE_CURRENT_ACTIVE_WAREHOUSE = "replaceTheCurrentActiveWarehouse";

  @ServerResponseFilter
  public void setCreatedStatusForWarehouseCreation(
      SimpleResourceInfo resourceInfo, ContainerResponseContext responseContext) {
    // Treat both create and replacement endpoints as resource-creation operations
    var method = resourceInfo.getMethodName();
    if ((CREATE_ANEW_WAREHOUSE_UNIT.equals(method)
            || REPLACE_THE_CURRENT_ACTIVE_WAREHOUSE.equals(method))
        && responseContext.getStatus() == Response.Status.OK.getStatusCode()) {
      responseContext.setStatus(Response.Status.CREATED.getStatusCode());
    }
  }
}
