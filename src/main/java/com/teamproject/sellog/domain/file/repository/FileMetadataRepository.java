package com.teamproject.sellog.domain.file.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.file.model.FileMetadata;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
    Optional<FileMetadata> findByFileHash(String fileHash);

    Optional<FileMetadata> findByFileHashAndUserId(String id, String userId);
}
