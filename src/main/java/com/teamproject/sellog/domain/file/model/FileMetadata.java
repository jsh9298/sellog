package com.teamproject.sellog.domain.file.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.GenerationType;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "file_meta")
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(name = "original_file", nullable = false)
    private String originalFilename;
    @Column(name = "store_path", nullable = false)
    private String blobPath;
    @Column(name = "type", nullable = false)
    private String contentType;
    @Column(name = "size", nullable = false)
    private long fileSize;
    @Column(name = "hash", nullable = false)
    private String fileHash;
    @Column(name = "create_at", nullable = false)
    private Timestamp createAt;

    private FileTarget fileTarget;

    @PrePersist
    public void onCreate() {
        if (this.createAt == null) {
            this.createAt = Timestamp.valueOf(LocalDateTime.now());
        }
    }
}
