package com.example.flowmanager.dao;

import com.example.flowmanager.entity.outbox.OutboxEvent;
import com.example.flowmanager.enums.OutboxStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    Page<OutboxEvent> findByStatusAndCreatedAtBefore(
            OutboxStatus status,
            LocalDateTime dateTime,
            Pageable pageable
    );

    Page<OutboxEvent> findByStatusAndCreatedAtBeforeAndRetryCountLessThan(
            OutboxStatus status,
            LocalDateTime dateTime,
            int retryCount,
            Pageable pageable
    );
}