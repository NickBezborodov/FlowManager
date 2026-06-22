package com.example.flowmanager.dto;

import com.example.flowmanager.enums.FileStatus;

import java.util.UUID;

public record ConversionResultEvent(UUID id, String resultPath, FileStatus status) {
}
