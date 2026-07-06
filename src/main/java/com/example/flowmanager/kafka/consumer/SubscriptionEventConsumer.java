package com.example.flowmanager.kafka.consumer;

import com.example.flowmanager.dto.SubscriptionChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionEventConsumer {

    @KafkaListener(
            topics = "${app.kafka.topics.subscription-changed}",
            groupId = "${app.kafka.consumer.group-id}"
    )
    public void listen(SubscriptionChangeEvent event) {
        log.info("📥 Получено событие об изменении подписки: {}", event);
    }
}