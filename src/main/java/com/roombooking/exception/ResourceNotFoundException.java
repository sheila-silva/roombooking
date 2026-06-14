package com.roombooking.exception;

/**
 * Thrown when a requested resource does not exist (maps to HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}
