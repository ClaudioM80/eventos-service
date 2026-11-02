package com.duoc.eventos_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFile(InvalidFileException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(S3BucketNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBucketNotFound(S3BucketNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(S3ObjectNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleObjectNotFound(S3ObjectNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(S3AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(S3AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler({S3UploadException.class, S3OperationException.class})
    public ResponseEntity<Map<String, String>> handleS3GeneralException(RuntimeException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<Map<String, String>> buildResponse(HttpStatus status, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return new ResponseEntity<>(response, status);
    }
}