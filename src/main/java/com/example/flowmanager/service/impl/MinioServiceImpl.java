package com.example.flowmanager.service.impl;

import com.example.flowmanager.exception.FileStorageException;
import com.example.flowmanager.service.MinioService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Override
    public byte[] downloadFile(String path) {
        try {
            GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());
            return response.readAllBytes();
        } catch (Exception e) {
            throw new FileStorageException("Failed to download file", e);
        }
    }

    @Override
    public String uploadFile(String fileName, byte[] content) {
        String path = UUID.randomUUID() + "_" + fileName;
        try{
            ByteArrayInputStream stream = new ByteArrayInputStream(content);
            minioClient.putObject(PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(stream, content.length, -1)
                    .build());
            return path;
        } catch (Exception e) {
            throw new FileStorageException("Failed to upload file", e);
        }
    }
}
