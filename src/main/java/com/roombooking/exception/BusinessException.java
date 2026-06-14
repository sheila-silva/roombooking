package com.roombooking.exception;

import lombok.Getter;

/**
 * Thrown when a domain rule is violated (maps to HTTP 400).
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String field;

    public BusinessException(String field, String message) {
        super(message);
        this.field = field;
    }
}
