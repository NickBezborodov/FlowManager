package com.example.flowmanager.job;

import com.example.flowmanager.entity.outbox.OutboxEvent;
import com.example.flowmanager.enums.OutboxStatus;
import com.example.flowmanager.dao.OutboxEventRepository;
import com.example.flowmanager.service.OutboxProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxSchedulerJob {

    private final OutboxEventRepository outboxRepository;
    private final OutboxProcessorService processor;

    private static final int BATCH_SIZE = 100;
    private static final int NEW_EVENTS_DELAY_SECONDS = 1;
    private static final int RETRY_DELAY_MINUTES = 3;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processNewEvents() {
        log.debug("🔄 Running processNewEvents scheduler...");

        Pageable pageable = PageRequest.of(0, BATCH_SIZE);

        Page<OutboxEvent> page = outboxRepository
                .findByStatusAndCreatedAtBefore(
                        OutboxStatus.PENDING,
                        LocalDateTime.now().minusSeconds(NEW_EVENTS_DELAY_SECONDS),
                        pageable
                );

        if (!page.hasContent()) {
            log.debug("No new outbox events found");
            return;
        }

        log.info("📤 Found {} new outbox events to process", page.getTotalElements());
        for (OutboxEvent event : page.getContent()) {
            processor.processEvent(event);
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryFailedEvents() {
        log.debug("🔄 Running retryFailedEvents scheduler...");

        Pageable pageable = PageRequest.of(0, BATCH_SIZE);

        Page<OutboxEvent> page = outboxRepository
                .findByStatusAndCreatedAtBeforeAndRetryCountLessThan(
                        OutboxStatus.PENDING,
                        LocalDateTime.now().minusMinutes(RETRY_DELAY_MINUTES),
                        processor.getMaxRetryCount(),
                        pageable
                );

        if (!page.hasContent()) {
            log.debug("No events to retry");
            return;
        }

        log.info("🔄 Found {} events to retry", page.getTotalElements());
        for (OutboxEvent event : page.getContent()) {
            processor.processEvent(event);
        }
    }
}
