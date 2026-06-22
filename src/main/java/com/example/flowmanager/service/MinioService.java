package com.example.flowmanager.service;

public interface MinioService {

    byte[] downloadFile(String path);

    void uploadFile(String path, byte[] data);
}
