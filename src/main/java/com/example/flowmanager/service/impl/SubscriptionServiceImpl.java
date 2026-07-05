package com.example.flowmanager.service.impl;

import com.example.flowmanager.dto.SubscriptionDto;
import com.example.flowmanager.client.SubscriptionFeignClient;
import com.example.flowmanager.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionFeignClient subscriptionFeignClient;

    @Override
    @Cacheable(value = "subscriptions", key = "#login", unless = "#result == null")
    public SubscriptionDto getSubscription(String login) {
        log.info("Запрос подписки через REST для логина: {}", login);
        return subscriptionFeignClient.getSubscription(login);
    }
}