package com.example.flowmanager.kafka.consumer;

import com.example.flowmanager.dto.ConversionResultEvent;
import com.example.flowmanager.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversionResultConsumer {

    private final FileService fileService;

    @KafkaListener(topics = "${app.kafka.topics.result}",
            groupId = "${app.kafka.consumer.group-id}")
    public void listen(ConversionResultEvent event) {
        log.info("Received result event: {}", event);
        fileService.handleConversionResult(event);
    }
}