package com.shotaroi.integrationhub.common.errors;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetails> handleMissingHeader(MissingRequestHeaderException ex,
                                                             HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ProblemDetails(
                        "https://api.integrationhub.example/errors/missing-header",
                        "Missing required header",
                        400,
                        "Required header '" + ex.getHeaderName() + "' is missing",
                        request.getRequestURI(),
                        MDC.get("correlationId"),
                        Instant.now(),
                        null
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetails> handleIllegalArgument(IllegalArgumentException ex,
                                                                 HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ProblemDetails(
                        "https://api.integrationhub.example/errors/validation",
                        "Validation failed",
                        400,
                        ex.getMessage(),
                        request.getRequestURI(),
                        MDC.get("correlationId"),
                        Instant.now(),
                        null
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.toList());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ProblemDetails(
                        "https://api.integrationhub.example/errors/validation",
                        "Validation failed",
                        400,
                        "Invalid request",
                        request.getRequestURI(),
                        MDC.get("correlationId"),
                        Instant.now(),
                        errors
                ));
    }
}
