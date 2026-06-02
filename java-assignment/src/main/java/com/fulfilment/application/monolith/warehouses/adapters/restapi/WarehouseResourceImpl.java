package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.location.InvalidLocationIdentifierException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.RetrieveWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject CreateWarehouseOperation createWarehouseOperation;
  @Inject RetrieveWarehouseOperation retrieveWarehouseOperation;
  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject ObjectMapper objectMapper;

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class.getName());

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return retrieveWarehouseOperation.listAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    return toWarehouseResponse(createWarehouseOperation.create(toDomainWarehouse(data)));
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    return toWarehouseResponse(retrieveWarehouseOperation.getById(parseId(id)));
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    archiveWarehouseOperation.archive(parseId(id));
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    return toWarehouseResponse(
        replaceWarehouseOperation.replace(businessUnitCode, toDomainWarehouse(data)));
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setId(warehouse.id == null ? null : warehouse.id.toString());
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainWarehouse(
      Warehouse request) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.id = StringUtils.isBlank(request.getId()) ? null : parseId(request.getId());
    warehouse.businessUnitCode = request.getBusinessUnitCode();
    warehouse.location = request.getLocation();
    warehouse.capacity = request.getCapacity();
    warehouse.stock = request.getStock();
    return warehouse;
  }

  private Long parseId(String id) {
    if (!StringUtils.isNumeric(id)) {
      throw new WarehouseValidationException("Warehouse id must be numeric.");
    }

    return Long.valueOf(id);
  }

  @ServerExceptionMapper
  public Response mapWarehouseValidationException(WarehouseValidationException exception) {
    return errorResponse(Response.Status.BAD_REQUEST, exception);
  }

  @ServerExceptionMapper
  public Response mapInvalidLocationIdentifierException(InvalidLocationIdentifierException exception) {
    return errorResponse(Response.Status.BAD_REQUEST, exception);
  }

  @ServerExceptionMapper
  public Response mapWarehouseNotFoundException(WarehouseNotFoundException exception) {
    return errorResponse(Response.Status.NOT_FOUND, exception);
  }

  @ServerExceptionMapper
  public Response mapWebApplicationException(WebApplicationException exception) {
    return errorResponse(exception.getResponse().getStatus(), exception);
  }

  private Response errorResponse(Response.Status status, Exception exception) {
    return errorResponse(status.getStatusCode(), exception);
  }

  private Response errorResponse(int code, Exception exception) {
    LOGGER.debugf(exception, "Warehouse request failed with status %d", code);

    ObjectNode exceptionJson = objectMapper.createObjectNode();
    exceptionJson.put("exceptionType", exception.getClass().getName());
    exceptionJson.put("code", code);

    if (exception.getMessage() != null) {
      exceptionJson.put("error", exception.getMessage());
    }

    return Response.status(code).entity(exceptionJson).build();
  }
}
