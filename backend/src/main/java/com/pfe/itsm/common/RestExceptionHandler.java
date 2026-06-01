package com.pfe.itsm.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(exception.getMessage()));
    }

    @ExceptionHandler({BusinessException.class, IllegalStateException.class})
    public ResponseEntity<ApiError> handleBusiness(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of(exception.getMessage()));
    }
}

