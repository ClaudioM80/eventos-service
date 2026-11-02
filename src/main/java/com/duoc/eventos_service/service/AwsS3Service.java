package com.duoc.eventos_service.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// IMPORTS CORREGIDOS AQUÍ:
import com.duoc.eventos_service.dto.S3ObjectDto;
import com.duoc.eventos_service.exception.InvalidFileException;
import com.duoc.eventos_service.exception.S3AccessDeniedException;
import com.duoc.eventos_service.exception.S3BucketNotFoundException;
import com.duoc.eventos_service.exception.S3ObjectNotFoundException;
import com.duoc.eventos_service.exception.S3OperationException;
import com.duoc.eventos_service.exception.S3UploadException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private final S3Client s3Client;

    /**
     * Lista todos los objetos en un bucket de S3
     */
    public List<S3ObjectDto> listObjects(String bucket) {

        try {
            log.info("Listando objetos del bucket: {}", bucket);

            ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucket).build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            log.info("Se encontraron {} objetos en el bucket {}", response.contents().size(), bucket);

            return response.contents().stream()
                    .map(obj -> new S3ObjectDto(obj.key(), obj.size(),
                            obj.lastModified() != null ? obj.lastModified().toString() : null))
                    .collect(Collectors.toList());

        } catch (NoSuchBucketException e) {
            throw new S3BucketNotFoundException(bucket, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                throw new S3AccessDeniedException("listar objetos del bucket: " + bucket, e);
            }
            throw new S3OperationException("Error al listar objetos del bucket: " + bucket, e);
        }
    }

    /**
     * Descarga un objeto de S3 como array de bytes
     */
    public byte[] downloadAsBytes(String bucket, String key) {

        try {
            log.info("Descargando objeto: {} del bucket: {}", key, bucket);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();

            ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(getObjectRequest);

            log.info("Objeto descargado exitosamente: {}", key);

            return responseBytes.asByteArray();

        } catch (NoSuchBucketException e) {
            throw new S3BucketNotFoundException(bucket, e);
        } catch (NoSuchKeyException e) {
            throw new S3ObjectNotFoundException(key, bucket, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                throw new S3AccessDeniedException("descargar el objeto: " + key, e);
            }
            throw new S3OperationException("Error al descargar el objeto: " + key, e);
        }
    }

    /**
     * Sube un archivo a S3
     */
    public void upload(String bucket, String key, MultipartFile file) {

        // Validaciones del archivo
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("El archivo está vacío o es nulo");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new InvalidFileException("El nombre del archivo no es válido");
        }

        if (file.getSize() == 0) {
            throw new InvalidFileException("El archivo no puede tener tamaño 0");
        }

        try {
            log.info("Subiendo archivo: {} al bucket: {}, tamaño: {} bytes", key, bucket, file.getSize());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(key)
                    .contentType(file.getContentType()).contentLength(file.getSize()).build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Archivo subido exitosamente: {}", key);

        } catch (NoSuchBucketException e) {
            throw new S3BucketNotFoundException(bucket, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                throw new S3AccessDeniedException("subir archivo al bucket: " + bucket, e);
            }
            throw new S3UploadException("Error al subir el archivo a S3: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new S3UploadException("Error al leer el archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Mueve un objeto dentro del mismo bucket (copiar + borrar)
     */
    public void moveObject(String bucket, String sourceKey, String destKey) {

        try {
            log.info("Moviendo objeto de {} a {} en el bucket: {}", sourceKey, destKey, bucket);

            CopyObjectRequest copyRequest = CopyObjectRequest.builder().sourceBucket(bucket).sourceKey(sourceKey)
                    .destinationBucket(bucket).destinationKey(destKey).build();

            s3Client.copyObject(copyRequest);

            log.info("Objeto copiado exitosamente, procediendo a eliminar el origen");

            deleteObject(bucket, sourceKey);

            log.info("Objeto movido exitosamente de {} a {}", sourceKey, destKey);

        } catch (NoSuchBucketException e) {
            throw new S3BucketNotFoundException(bucket, e);
        } catch (NoSuchKeyException e) {
            throw new S3ObjectNotFoundException(sourceKey, bucket, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                throw new S3AccessDeniedException("mover objeto en el bucket: " + bucket, e);
            }
            throw new S3OperationException("Error al mover el objeto de " + sourceKey + " a " + destKey, e);
        }
    }

    /**
     * Elimina un objeto de S3
     */
    public void deleteObject(String bucket, String key) {

        try {
            log.info("Eliminando objeto: {} del bucket: {}", key, bucket);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder().bucket(bucket).key(key).build();

            s3Client.deleteObject(deleteRequest);

            log.info("Objeto eliminado exitosamente: {}", key);

        } catch (NoSuchBucketException e) {
            throw new S3BucketNotFoundException(bucket, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                throw new S3AccessDeniedException("eliminar objeto del bucket: " + bucket, e);
            }
            throw new S3OperationException("Error al eliminar el objeto: " + key, e);
        }
    }
}