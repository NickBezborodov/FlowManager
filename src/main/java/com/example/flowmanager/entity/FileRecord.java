package com.example.flowmanager.entity;

import com.example.flowmanager.enums.FileStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "file_record")
@Getter
@Setter
@NoArgsConstructor
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String originalPath;
    private String convertedPath;
    private String format;

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Long size;

}
