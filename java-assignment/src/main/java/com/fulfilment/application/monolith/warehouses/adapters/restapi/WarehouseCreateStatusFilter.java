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

  @ServerResponseFilter
  public void setCreatedStatusForWarehouseCreation(
      SimpleResourceInfo resourceInfo, ContainerResponseContext responseContext) {
    if ("createANewWarehouseUnit".equals(resourceInfo.getMethodName())
        && responseContext.getStatus() == Response.Status.OK.getStatusCode()) {
      responseContext.setStatus(Response.Status.CREATED.getStatusCode());
    }
  }
}
