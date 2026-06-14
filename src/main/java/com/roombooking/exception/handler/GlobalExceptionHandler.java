package com.roombooking.exception.handler;

import com.roombooking.exception.BusinessException;
import com.roombooking.exception.ConflictException;
import com.roombooking.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 400 – Bean Validation (@Valid) ──────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {

        List<ApiError.FieldError> fields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> ApiError.FieldError.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();

        ApiError body = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("One or more fields are invalid.")
                .timestamp(LocalDateTime.now())
                .fields(fields)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ── 400 – Domain / Business rule ────────────────────────────────────────

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {

        ApiError body = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Business Rule Violation")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .fields(List.of(ApiError.FieldError.builder()
                        .field(ex.getField())
                        .message(ex.getMessage())
                        .build()))
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ── 404 – Resource not found ─────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {

        ApiError body = ApiError.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ── 409 – Booking conflict ───────────────────────────────────────────────

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {

        ApiError body = ApiError.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Booking Conflict")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ── 500 – Unexpected ─────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {

        ApiError body = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
