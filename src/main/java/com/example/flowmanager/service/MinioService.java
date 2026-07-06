package com.example.flowmanager.service;

public interface MinioService {
    byte[] downloadFile(String path);
    String uploadFile(String fileName, byte[] content);
}
