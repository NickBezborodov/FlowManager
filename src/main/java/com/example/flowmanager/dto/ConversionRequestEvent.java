package com.example.flowmanager.dto;

import java.util.UUID;

public record ConversionRequestEvent(UUID id, String filePath, String format) {
}
