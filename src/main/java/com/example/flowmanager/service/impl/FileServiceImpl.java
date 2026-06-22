package com.example.flowmanager.service.impl;

import com.example.flowmanager.dto.ConversionResultEvent;
import com.example.flowmanager.dto.FileStatusResponse;
import com.example.flowmanager.dto.FileUploadResponse;
import com.example.flowmanager.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl  implements FileService {
    @Override
    public FileUploadResponse upload(MultipartFile file) {
        return null;
    }

    @Override
    public FileStatusResponse getStatus(UUID id) {
        return null;
    }

    @Override
    public byte[] getFile(UUID id) {
        return new byte[0];
    }

    @Override
    public void handleConversionResult(ConversionResultEvent event) {

    }
}
