package com.duoc.eventos_service.exception;

public class S3BucketNotFoundException extends RuntimeException {
    public S3BucketNotFoundException(String bucketName, Throwable cause) {
        super("No se encontró el bucket: " + bucketName, cause);
    }
}