package com.example.flowmanager.service;

import com.example.flowmanager.entity.outbox.OutboxEvent;

public interface OutboxProcessorService {
    void processEvent(OutboxEvent event);
    int getMaxRetryCount();
}