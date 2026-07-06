package com.example.flowmanager.dto;

import java.time.LocalDateTime;

public record SubscriptionChangeEvent(String login, String oldType, String newType, LocalDateTime changedAt) {
}
