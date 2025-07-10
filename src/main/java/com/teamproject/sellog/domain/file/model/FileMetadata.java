package com.teamproject.sellog.domain.file.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "file_meta")
@IdClass(FileMetadataId.class)
public class FileMetadata {
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Id
    @Column(name = "hash", nullable = false)
    private String fileHash;

    @Column(name = "original_file", nullable = false)
    private String originalFilename;
    @Column(name = "store_path", nullable = false)
    private List<String> blobPath;
    @Column(name = "type", nullable = false)
    private String contentType;
    @Column(name = "size", nullable = false)
    private long fileSize;

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
