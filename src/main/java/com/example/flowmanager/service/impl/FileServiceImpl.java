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

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRecordRepository fileRecordRepository;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;
    private final SubscriptionService subscriptionService;

    @Override
    @Transactional
    public FileUploadResponse upload(MultipartFile file, String login) {
        log.info("🚀 НАЧАЛО ЗАГРУЗКИ для пользователя: '{}'", login);

        if (file == null || file.isEmpty()) {
            log.error("❌ Файл пустой или null");
            throw new IllegalArgumentException("File is empty");
        }
        log.info("✅ Файл получен: {}, размер: {} байт", file.getOriginalFilename(), file.getSize());

        SubscriptionDto subscription;
        try {
            log.info("📡 Запрос подписки для логина: '{}'", login);
            subscription = subscriptionService.getSubscription(login);
            log.info("✅ Подписка получена: type={}, expirationDate={}",
                    subscription.type(), subscription.expirationDate());
        } catch (Exception e) {
            log.error("❌ Ошибка при получении подписки: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось проверить подписку для пользователя: " + login, e);
        }

        long maxSize = "PAID".equals(subscription.type())
                ? Long.MAX_VALUE
                : 100L * 1024 * 1024;
        log.info("📏 Максимальный размер: {} байт", maxSize);

        if (file.getSize() > maxSize) {
            log.error("❌ Файл слишком большой: {} байт, лимит: {} байт", file.getSize(), maxSize);
            throw new IllegalArgumentException(
                    String.format("File size %d bytes exceeds limit %d bytes", file.getSize(), maxSize)
            );
        }
        log.info("✅ Размер файла в пределах лимита");

        String format = extractFormat(file.getOriginalFilename());
        log.info("📄 Формат файла: {}", format);

        byte[] content;
        try {
            content = file.getBytes();
            log.info("📦 Содержимое файла прочитано, размер: {} байт", content.length);
        } catch (IOException e) {
            log.error("❌ Ошибка чтения файла", e);
            throw new FileStorageException("Failed to read file", e);
        }

        String path;
        try {
            path = minioService.uploadFile(file.getOriginalFilename(), content);
            log.info("📁 Файл загружен в MinIO: {}", path);
        } catch (Exception e) {
            log.error("❌ Ошибка загрузки в MinIO", e);
            throw new FileStorageException("Failed to upload file to MinIO", e);
        }

        FileRecord record = new FileRecord();
        record.setOriginalPath(path);
        record.setConvertedPath(null);
        record.setFormat(format);
        record.setStatus(FileStatus.PROCESSING);
        record.setCreatedAt(LocalDateTime.now());
        record.setSize(file.getSize());
        record.setUpdatedAt(LocalDateTime.now());

        FileRecord savedRecord;
        try {
            savedRecord = fileRecordRepository.save(record);
            log.info("💾 Запись сохранена в БД: id={}", savedRecord.getId());
        } catch (Exception e) {
            log.error("❌ Ошибка сохранения в БД", e);
            throw new FileStorageException("Failed to save file record", e);
        }

        try {
            ConversionRequestEvent event = new ConversionRequestEvent(savedRecord.getId(), path, format);
            String payload = objectMapper.writeValueAsString(event);
            log.info("📤 Событие для Kafka: {}", payload);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateId(savedRecord.getId());
            outboxEvent.setEventType("CONVERSION_REQUEST");
            outboxEvent.setPayload(payload);
            outboxEvent.setStatus(OutboxStatus.PENDING);
            outboxEvent.setCreatedAt(LocalDateTime.now());
            outboxEvent.setRetryCount(0);

            outboxEventRepository.save(outboxEvent);
            log.info("📨 Outbox событие сохранено: {}", savedRecord.getId());

        } catch (Exception e) {
            log.error("❌ Ошибка сохранения Outbox события", e);
            throw new FileStorageException("Failed to queue file for conversion", e);
        }

        log.info("✅ ЗАГРУЗКА ЗАВЕРШЕНА УСПЕШНО для пользователя: {}", login);
        return new FileUploadResponse(savedRecord.getId(), savedRecord.getStatus());
    }

    private String extractFormat(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            log.error("❌ Не удалось определить формат файла: {}", fileName);
            throw new FileStorageException("Cannot determine file format", null);
        }
        String format = fileName.substring(fileName.lastIndexOf('.') + 1);
        log.info("📄 Формат файла: {}", format);
        return format;
    }

    @Override
    @Transactional(readOnly = true)
    public FileStatusResponse getStatus(UUID id) {
        log.info("🔍 Запрос статуса для файла: {}", id);
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
            log.error("❌ Ошибка при получении статуса", e);
            throw e;
        }
    }

    @Override
    public byte[] getFile(UUID id) {
        log.info("📥 Запрос файла: {}", id);
        try {
            FileRecord record = fileRecordRepository.findById(id)
                    .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));
            byte[] data = minioService.downloadFile(record.getOriginalPath());
            log.info("✅ Файл загружен из MinIO, размер: {} байт", data.length);
            return data;
        } catch (Exception e) {
            log.error("❌ Ошибка при загрузке файла", e);
            throw new FileNotFoundException("Failed to get file status for id: " + id, e);
        }
    }

    @Override
    public void handleConversionResult(ConversionResultEvent event) {
        log.info("📩 Получен результат конвертации: {}", event);
        if (event.fileId() == null) {
            log.error("❌ ConversionResultEvent с null fileId: {}", event);
            return;
        }

        try {
            FileRecord record = fileRecordRepository.findById(event.fileId())
                    .orElseThrow(() -> new FileNotFoundException("File not found: " + event.fileId()));

            if (event.status() == FileStatus.SUCCESS) {
                record.setStatus(FileStatus.SUCCESS);
                record.setConvertedPath(event.resultPath());
                log.info("✅ Файл {} сконвертирован успешно", event.fileId());
            } else {
                record.setStatus(FileStatus.ERROR);
                log.error("❌ Ошибка конвертации файла {}", event.fileId());
            }
            record.setUpdatedAt(LocalDateTime.now());
            fileRecordRepository.save(record);
            log.info("💾 Статус файла {} обновлён", event.fileId());

        } catch (Exception e) {
            log.error("❌ Ошибка обработки результата конвертации", e);
            throw e;
        }
    }
}