package com.teamproject.sellog.domain.file.model;

import java.io.Serializable;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileMetadataId implements Serializable {
    private String userId;
    private String fileHash;

    public FileMetadataId(String userId, String fileHash) {
        this.userId = userId;
        this.fileHash = fileHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FileMetadataId))
            return false;
        FileMetadataId that = (FileMetadataId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(fileHash, that.fileHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, fileHash);
    }

}
