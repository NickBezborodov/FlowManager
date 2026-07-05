package com.example.flowmanager.service;

import com.example.flowmanager.dto.ConversionResultEvent;
import com.example.flowmanager.dto.FileStatusResponse;
import com.example.flowmanager.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileService {
    FileUploadResponse upload(MultipartFile file, String login);
    FileStatusResponse getStatus(UUID id);
    byte[] getFile(UUID id);
    void handleConversionResult(ConversionResultEvent event);
}
