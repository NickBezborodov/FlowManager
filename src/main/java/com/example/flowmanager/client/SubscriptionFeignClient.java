package com.example.flowmanager.client;

import com.example.flowmanager.dto.SubscriptionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "subscription-service", url = "http://localhost:8090")
public interface SubscriptionFeignClient {
    @GetMapping("/api/v1/subscriptions/{login}")
    SubscriptionDto getSubscription(@PathVariable("login") String login);
}
