package com.wildrep.accountantapp.config;

import com.wildrep.accountantapp.exceptions.ComparisonNotFoundException;
import com.wildrep.accountantapp.exceptions.NoComparisonsLeftException;
import com.wildrep.accountantapp.exceptions.UserDoesNotExistException;
import com.wildrep.accountantapp.model.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserDoesNotExistException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserDoesNotExistException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse("User not found", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoComparisonsLeftException.class)
    public ResponseEntity<ErrorResponse> handleNoComparisonsLeft(NoComparisonsLeftException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse("No comparisons left", ex.getMessage());
        return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.PAYMENT_REQUIRED);
    }

    @ExceptionHandler(ComparisonNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleComparisonNotFound(ComparisonNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse("Comparison not found", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleGenericException(RuntimeException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse("An unexpected error occurred", "Please contact support.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
