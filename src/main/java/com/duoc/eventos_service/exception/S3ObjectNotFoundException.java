package com.duoc.eventos_service.exception;

public class S3ObjectNotFoundException extends RuntimeException {
    public S3ObjectNotFoundException(String key, String bucket, Throwable cause) {
        super("No se encontró el objeto '" + key + "' en el bucket: " + bucket, cause);
    }
}