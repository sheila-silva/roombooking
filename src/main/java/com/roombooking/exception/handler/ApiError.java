package com.roombooking.exception.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private final int    status;
    private final String error;
    private final String message;
    private final LocalDateTime timestamp;

    /** Present only for validation errors – one entry per failing field. */
    private final List<FieldError> fields;

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;
    }
}
