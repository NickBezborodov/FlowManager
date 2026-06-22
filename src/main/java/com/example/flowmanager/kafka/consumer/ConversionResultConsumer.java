package com.example.flowmanager.kafka.consumer;

import com.example.flowmanager.dto.ConversionResultEvent;
import com.example.flowmanager.service.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class ConversionResultConsumer {
    private final FileService fileService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.uploaded}", groupId = "flowmanager-group")
    public void listen(String message) {
        log.info("Received result message: {}", message);
        try {
            ConversionResultEvent event = objectMapper.readValue(message, ConversionResultEvent.class);
            log.info("Deserialized result event: {}", event);
            fileService.handleConversionResult(event);
        } catch (Exception e) {
            log.error("Failed to process result event", e);
        }
    }
}