package com.teamproject.sellog.domain.file.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class FileResponse {
    private String originalFilename;
    private String fileUrl;
    private String thumbnailUrl;
    private String originUrl;
}
