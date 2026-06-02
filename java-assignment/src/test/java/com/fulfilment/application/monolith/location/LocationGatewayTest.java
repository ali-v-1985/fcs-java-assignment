package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class LocationGatewayTest {

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    // given
    var locationGateway = new LocationGateway();

    // when
    var location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  public void testWhenResolveUnknownLocationShouldThrowException() {
    // given
    var locationGateway = new LocationGateway();

    // when / then
    assertThrows(
        InvalidLocationIdentifierException.class,
        () -> locationGateway.resolveByIdentifier("UNKNOWN-001"));
  }

  @Test
  public void testWhenResolveNullLocationShouldThrowException() {
    // given
    var locationGateway = new LocationGateway();

    // when / then
    assertThrows(
        InvalidLocationIdentifierException.class, () -> locationGateway.resolveByIdentifier(null));
  }

  @Test
  public void testWhenResolveBlankLocationShouldThrowException() {
    // given
    var locationGateway = new LocationGateway();

    // when / then
    assertThrows(
        InvalidLocationIdentifierException.class, () -> locationGateway.resolveByIdentifier(" "));
  }
}
