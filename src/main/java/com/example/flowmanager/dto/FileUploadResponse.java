package com.example.flowmanager.dto;

import com.example.flowmanager.enums.FileStatus;

import java.util.UUID;

public record FileUploadResponse(UUID id, FileStatus status) {
}
