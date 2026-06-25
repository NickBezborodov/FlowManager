package com.example.flowmanager.service.impl;

import com.example.flowmanager.dto.ConversionRequestEvent;
import com.example.flowmanager.entity.outbox.OutboxEvent;
import com.example.flowmanager.enums.OutboxStatus;
import com.example.flowmanager.kafka.producer.ConversionRequestProducer;
import com.example.flowmanager.dao.OutboxEventRepository;
import com.example.flowmanager.service.OutboxProcessorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxProcessorServiceImpl implements OutboxProcessorService {

    private final OutboxEventRepository outboxRepository;
    private final ConversionRequestProducer producer;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRY_COUNT = 5;

    @Override
    public int getMaxRetryCount() {
        return MAX_RETRY_COUNT;
    }

    @Override
    @Transactional
    public void processEvent(OutboxEvent event) {
        try {
            ConversionRequestEvent requestEvent = objectMapper.readValue(
                    event.getPayload(),
                    ConversionRequestEvent.class
            );

            producer.sendMessage(requestEvent);

            // 4.3. Успех → статус SENT
            event.setStatus(OutboxStatus.SENT);
            event.setUpdatedAt(LocalDateTime.now());
            outboxRepository.save(event);

            log.info("✅ Outbox event sent: {}", event.getId());

        } catch (Exception e) {
            log.error("❌ Failed to send outbox event: {}", event.getId(), e);

            event.setRetryCount(event.getRetryCount() + 1);
            event.setUpdatedAt(LocalDateTime.now());

            if (event.getRetryCount() >= MAX_RETRY_COUNT) {
                event.setStatus(OutboxStatus.FAILED);
                log.error("💀 Outbox event FAILED after {} retries: {}", MAX_RETRY_COUNT, event.getId());
            }

            outboxRepository.save(event);
        }
    }
}