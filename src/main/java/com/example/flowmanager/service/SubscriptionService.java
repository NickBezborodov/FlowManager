package com.example.flowmanager.service;

import com.example.flowmanager.dto.SubscriptionDto;

public interface SubscriptionService {
    SubscriptionDto getSubscription(String login);
}
