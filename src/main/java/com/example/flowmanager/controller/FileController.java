package com.example.flowmanager.controller;

import com.example.flowmanager.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private static final String UPLOAD_DIRECTORY = ;
    private final FileService FileService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Пожалуйста, выберите файл для загрузки.");
        }

        try {
            // Создаем директорию, если она не существует
            File uploadDir = new File(UPLOAD_DIRECTORY);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Полный путь к сохраняемому файлу
            Path filePath = Paths.get(UPLOAD_DIRECTORY + File.separator + Objects.requireNonNull(file.getOriginalFilename()));

            // Копируем содержимое файла
            Files.copy(file.getInputStream(), filePath);
            return ResponseEntity.ok("Файл успешно загружен: " + file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Не удалось загрузить файл: " + file.getOriginalFilename());
        }
    }
}
