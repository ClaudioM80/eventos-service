package com.duoc.eventos_service.exception;

public class S3OperationException extends RuntimeException {
    public S3OperationException(String message, Throwable cause) {
        super(message, cause);
    }
}