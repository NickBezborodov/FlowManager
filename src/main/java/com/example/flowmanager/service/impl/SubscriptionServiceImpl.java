package com.example.flowmanager.service.impl;

import com.example.flowmanager.dto.SubscriptionDto;
import com.example.flowmanager.client.SubscriptionFeignClient;
import com.example.flowmanager.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionFeignClient feignClient;

    public SubscriptionDto getSubscription(String login) {
        ResponseEntity<SubscriptionDto> response = feignClient.getSubscription(login);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }

        if (response.getStatusCode().is4xxClientError()) {
            return createDefaultSubscription(login);
        }

        throw new RuntimeException("Не удалось проверить подписку: " + response.getStatusCode());
    }

    private SubscriptionDto createDefaultSubscription(String login) {
        ResponseEntity<SubscriptionDto> created = feignClient.createSubscription(login, "FREE");
        return created.getBody();
    }
}
