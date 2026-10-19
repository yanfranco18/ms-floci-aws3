package com.example.application.shared.storage;

import com.example.application.shared.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    @ConfigProperty(name = "app.storage.s3.bucket")
    String bucket;

    @ConfigProperty(name = "app.storage.s3.endpoint")
    String endpoint;

    @PostConstruct
    void init() {
        try {
            ensureBucketExists();
        } catch (Exception e) {
            log.warn("No se pudo verificar/crear el bucket inicial S3 '{}': {}", bucket, e.getMessage());
        }
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            log.info("Bucket S3 '{}' no existe en Floci, creándolo...", bucket);
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("Bucket S3 '{}' creado exitosamente.", bucket);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.info("Bucket S3 '{}' retornó 404 en Floci, creándolo...", bucket);
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                log.info("Bucket S3 '{}' creado exitosamente.", bucket);
            } else {
                log.warn("Verificación de bucket arrojó código {}: {}", e.statusCode(), e.getMessage());
            }
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType, long size) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .contentLength(size)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, size));
            log.info("Archivo '{}' subido correctamente al bucket S3 '{}'", fileName, bucket);
            return fileName;
        } catch (Exception e) {
            log.error("Error al subir archivo '{}' al bucket '{}': {}", fileName, bucket, e.getMessage(), e);
            throw new BusinessException("Error al almacenar la imagen en S3: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        String cleanEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return cleanEndpoint + "/" + bucket + "/" + fileName;
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build());
            log.info("Archivo '{}' eliminado del bucket S3 '{}'", fileName, bucket);
        } catch (Exception e) {
            log.warn("No se pudo eliminar el archivo '{}' del bucket '{}': {}", fileName, bucket, e.getMessage());
        }
    }
}
