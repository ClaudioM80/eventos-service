package com.duoc.eventos_service.exception;

public class S3AccessDeniedException extends RuntimeException {
    public S3AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}