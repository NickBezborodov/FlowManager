package com.example.flowmanager.dto;

import java.util.UUID;

public record ConversionRequestEvent(UUID fileId, String filePath, String format) {
}