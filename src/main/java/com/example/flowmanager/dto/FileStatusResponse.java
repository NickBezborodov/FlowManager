package com.example.flowmanager.dto;

import com.example.flowmanager.enums.FileStatus;

import java.util.UUID;

public record FileStatusResponse(UUID id, FileStatus status, String convertedPath, java.time.LocalDateTime createdAt) {
}
