package com.example.flowmanager.controller;

import com.example.flowmanager.dto.FileStatusResponse;
import com.example.flowmanager.dto.FileUploadResponse;
import com.example.flowmanager.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("\"Received file upload request: {}", file.getOriginalFilename());

        FileUploadResponse response = fileService.upload(file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/check/v1/{id}")
    public FileStatusResponse getStatus(@PathVariable UUID id) {
        return fileService.getStatus(id);
    }

    @GetMapping("/download/v1/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID id) {
        byte[] fileData = fileService.getFile(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"file\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileData);
    }
}
