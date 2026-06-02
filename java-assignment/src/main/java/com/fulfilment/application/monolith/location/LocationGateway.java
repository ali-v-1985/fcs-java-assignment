package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class LocationGateway implements LocationResolver {

  private static final Map<String, Location> LOCATIONS_BY_IDENTIFIER =
      Map.of(
          "ZWOLLE-001", new Location("ZWOLLE-001", 1, 40),
          "ZWOLLE-002", new Location("ZWOLLE-002", 2, 50),
          "AMSTERDAM-001", new Location("AMSTERDAM-001", 5, 100),
          "AMSTERDAM-002", new Location("AMSTERDAM-002", 3, 75),
          "TILBURG-001", new Location("TILBURG-001", 1, 40),
          "HELMOND-001", new Location("HELMOND-001", 1, 45),
          "EINDHOVEN-001", new Location("EINDHOVEN-001", 2, 70),
          "VETSBY-001", new Location("VETSBY-001", 1, 90));

  @Override
  public Location resolveByIdentifier(String identifier) {
    if (StringUtils.isBlank(identifier)) {
      throw new InvalidLocationIdentifierException(identifier);
    }

    var location = LOCATIONS_BY_IDENTIFIER.get(identifier);
    if (location == null) {
      throw new InvalidLocationIdentifierException(identifier);
    }

    return location;
  }
}
