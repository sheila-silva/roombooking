package com.roombooking.exception;

/**
 * Thrown when a new booking overlaps with an existing one (maps to HTTP 409).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
