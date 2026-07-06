package com.example.flowmanager.client;

import com.example.flowmanager.dto.SubscriptionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "subscription-service", url = "http://localhost:8090")
public interface SubscriptionClient {

    @GetMapping("/api/v1/subscriptions/{login}")
    ResponseEntity<SubscriptionDto> getSubscription(@PathVariable("login") String login);

    @PostMapping("/api/v1/subscriptions/{login}")
    ResponseEntity<SubscriptionDto> createSubscription(
            @PathVariable("login") String login,
            @RequestParam("type") String type);
}
