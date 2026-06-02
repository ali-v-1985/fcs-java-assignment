package com.fulfilment.application.monolith.fulfilment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Path("fulfilment-assignments")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfilmentAssignmentResource {

  @Inject FulfilmentAssignmentService service;

  private static final Logger LOGGER =
      Logger.getLogger(FulfilmentAssignmentResource.class.getName());

  @GET
  public List<FulfilmentAssignmentResponse> listAll() {
    return service.listAll();
  }

  @POST
  public Response create(FulfilmentAssignmentRequest request) {
    return Response.status(Response.Status.CREATED).entity(service.create(request)).build();
  }

  @ServerExceptionMapper
  public Response mapWebApplicationException(WebApplicationException exception) {
    var code = exception.getResponse().getStatus();
    LOGGER.debugf(exception, "Fulfilment assignment request failed with status %d", code);
    return Response.status(code)
        .entity(new ErrorResponse(exception.getClass().getName(), code, exception.getMessage()))
        .build();
  }

  public record ErrorResponse(String exceptionType, int code, String error) {}
}
