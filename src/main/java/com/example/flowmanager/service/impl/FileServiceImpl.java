package com.example.flowmanager.service.impl;

import com.example.flowmanager.dao.FileRecordRepository;
import com.example.flowmanager.dao.OutboxEventRepository;
import com.example.flowmanager.dto.*;
import com.example.flowmanager.entity.FileRecord;
import com.example.flowmanager.entity.outbox.OutboxEvent;
import com.example.flowmanager.enums.FileStatus;
import com.example.flowmanager.enums.OutboxStatus;
import com.example.flowmanager.exception.FileNotFoundException;
import com.example.flowmanager.exception.FileStorageException;
import com.example.flowmanager.service.FileService;
import com.example.flowmanager.service.MinioService;
import com.example.flowmanager.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;
    private final SubscriptionService subscriptionService;

    @Transactional
    @Override
    public FileUploadResponse upload(MultipartFile file, String login) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        SubscriptionDto subscription = subscriptionService.getSubscription(login);
        long maxSize = "PAID".equals(subscription.type())
                ? Long.MAX_VALUE
                : 100L * 1024 * 1024; // ← 100 мб

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

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
        record.setUpdatedAt(LocalDateTime.now());

        FileRecord savedRecord = fileRecordRepository.save(record);

        try {
            ConversionRequestEvent event = new ConversionRequestEvent(savedRecord.getId(), path, format);
            String payload = objectMapper.writeValueAsString(event);

            log.info("📤 Sending payload to Kafka: {}", payload);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateId(savedRecord.getId());
            outboxEvent.setEventType("CONVERSION_REQUEST");
            outboxEvent.setPayload(payload);
            outboxEvent.setStatus(OutboxStatus.PENDING);
            outboxEvent.setCreatedAt(LocalDateTime.now());
            outboxEvent.setRetryCount(0);

            outboxEventRepository.save(outboxEvent);
            log.info("Outbox event saved for file: {}", savedRecord.getId());

        } catch (Exception e) {
            log.error("Failed to save outbox event", e);
            throw new FileStorageException("Failed to queue file", e);
        }

        return new FileUploadResponse(savedRecord.getId(), savedRecord.getStatus());
    }


    private String extractFormat(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new FileStorageException("Cannot determine file format", null);
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    @Override
    @Transactional(readOnly = true)
    public FileStatusResponse getStatus(UUID id) {
        try {
            FileRecord record = fileRecordRepository.findById(id)
                    .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));

            return new FileStatusResponse(
                    record.getId(),
                    record.getStatus(),
                    record.getFormat(),
                    record.getCreatedAt()
            );
        } catch (Exception e) {
            throw new FileNotFoundException("Failed to get file status for id: " + id, e);
        }
    }

    @Override
    public byte[] getFile(UUID id) {
        try {
            FileRecord record = fileRecordRepository.findById(id)
                    .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));

            String originalPath = record.getOriginalPath();

            return minioService.downloadFile(originalPath);
        } catch (Exception e) {
            throw new FileNotFoundException("Failed to get file status for id: " + id, e);
        }
    }

    @Override
    public void handleConversionResult(ConversionResultEvent event) {
        // 1. Проверяем fileId
        if (event.fileId() == null) {
            log.error("❌ ConversionResultEvent with null fileId: {}", event);
            return; // или throw, но лучше просто логировать
        }

        // 2. Ищем файл
        FileRecord record = fileRecordRepository.findById(event.fileId())
                .orElseThrow(() -> new FileNotFoundException("File not found: " + event.fileId()));

        // 3. Обновляем статус
        if (event.status() == FileStatus.SUCCESS) {
            record.setStatus(FileStatus.SUCCESS);
            record.setConvertedPath(event.resultPath());
        } else {
            record.setStatus(FileStatus.ERROR);
        }
        record.setUpdatedAt(LocalDateTime.now());

        // 4. Сохраняем
        fileRecordRepository.save(record);
    }
}
