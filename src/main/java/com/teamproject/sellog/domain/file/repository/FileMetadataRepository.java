package com.teamproject.sellog.domain.file.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.file.model.FileMetadata;
import com.teamproject.sellog.domain.file.model.FileMetadataId;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, FileMetadataId> {
    Optional<FileMetadata> findByFileHash(String fileHash);

    Optional<FileMetadata> findByFileHashAndUserId(String id, String userId);
}
