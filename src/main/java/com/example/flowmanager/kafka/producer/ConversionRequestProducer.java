package com.example.flowmanager.kafka.producer;

import com.example.flowmanager.dto.ConversionRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversionRequestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${app.kafka.topics.request}")
    private String outputTopic;

    public void sendMessage(ConversionRequestEvent event) throws JsonProcessingException {
        kafkaTemplate.send(outputTopic, objectMapper.writeValueAsString(event));
    }
}
