package com.workflow.workflow_saas.user.handler;

import com.workflow.workflow_saas.tenant.exception.TenantNotFoundException;
import com.workflow.workflow_saas.user.exception.EmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(
            int status,
            String error,
            String message,
            Instant timestamp
    ) {
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExists(EmailAlreadyExistsException exception){
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(),
                                      HttpStatus.CONFLICT.getReasonPhrase(),
                                      exception.getMessage(),
                                      Instant.now());
        return  ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ApiError>handleTenantNotFoundException(TenantNotFoundException exception){
            ApiError error = new ApiError(HttpStatus.NOT_FOUND.value(),
                                          HttpStatus.NOT_FOUND.getReasonPhrase(),
                                          exception.getMessage(),
                                          Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
