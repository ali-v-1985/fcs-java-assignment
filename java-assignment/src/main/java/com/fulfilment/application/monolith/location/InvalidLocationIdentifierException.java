package com.fulfilment.application.monolith.location;

public class InvalidLocationIdentifierException extends RuntimeException {

  public InvalidLocationIdentifierException(String identifier) {
    super("Invalid location identifier: " + identifier);
  }
}
