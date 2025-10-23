package com.skat.backend.api.exception;

import com.skat.backend.application.dto.ErrorResponseTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseTO> handleNotFoundException(NotFoundException ex) {
        ErrorResponseTO error = new ErrorResponseTO(
            "not_found",
            ex.getMessage(),
            ex.getField()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseTO> handleConflictException(ConflictException ex) {
        ErrorResponseTO error = new ErrorResponseTO(
            "conflict",
            ex.getMessage(),
            ex.getField()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseTO> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String field = fieldError != null ? fieldError.getField() : null;
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation failed";
        
        ErrorResponseTO error = new ErrorResponseTO(
            "bad_request",
            message,
            field
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseTO> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value for parameter '%s'", ex.getName());
        ErrorResponseTO error = new ErrorResponseTO(
            "bad_request",
            message,
            ex.getName()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponseTO error = new ErrorResponseTO(
            "bad_request",
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
