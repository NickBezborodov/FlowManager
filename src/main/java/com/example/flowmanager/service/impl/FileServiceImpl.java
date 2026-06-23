package com.example.flowmanager.service.impl;

import com.example.flowmanager.dao.FileRecordRepository;
import com.example.flowmanager.dto.ConversionRequestEvent;
import com.example.flowmanager.dto.ConversionResultEvent;
import com.example.flowmanager.dto.FileStatusResponse;
import com.example.flowmanager.dto.FileUploadResponse;
import com.example.flowmanager.entity.FileRecord;
import com.example.flowmanager.enums.FileStatus;
import com.example.flowmanager.exception.FileStorageException;
import com.example.flowmanager.kafka.producer.ConversionRequestProducer;
import com.example.flowmanager.service.FileService;
import com.example.flowmanager.service.MinioService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private final FileRecordRepository fileRecordRepository;
    private final MinioService minioService;
    private final ConversionRequestProducer producer;

    @Transactional
    @Override
    public FileUploadResponse upload(MultipartFile file) {
        String format = extractFormat(file.getOriginalFilename());

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new FileStorageException("Failed to read file", e);
        }
        String path = minioService.uploadFile(file.getOriginalFilename(), content);

        FileRecord record = new FileRecord();
        record.setOriginalPath(path);
        record.setConvertedPath(null);
        record.setFormat(format);
        record.setStatus(FileStatus.PROCESSING);
        record.setCreatedAt(LocalDateTime.now());
        record.setSize(file.getSize());

        FileRecord savedRecord = fileRecordRepository.save(record);

        try {
            producer.sendMessage(new ConversionRequestEvent(savedRecord.getId(), path, format));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event", e);
            throw new FileStorageException("Failed to queue file", e);
        }
        return new FileUploadResponse(record.getId(), record.getStatus());
    }

    private String extractFormat(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new FileStorageException("Cannot determine file format", null);
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
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
