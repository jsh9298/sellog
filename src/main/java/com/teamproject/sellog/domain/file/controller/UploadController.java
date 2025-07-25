package com.teamproject.sellog.domain.file.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.teamproject.sellog.common.RestResponse;
import com.teamproject.sellog.domain.file.model.FileResponse;
import com.teamproject.sellog.domain.file.model.FileTarget;
import com.teamproject.sellog.domain.file.service.AzureBlobService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "파일", description = "사이트 공용 파일 업로드 api")
public class UploadController {

    private final AzureBlobService azureBlobService;

    @PostMapping(value = "/file/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "업로드", description = "단일 파일 업로드시 사용(*)")
    public ResponseEntity<?> upload(@PathVariable String userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") FileTarget fileType) {
        try {
            FileResponse saved = azureBlobService.uploadWithMetadata(userId, file, fileType);
            return ResponseEntity.ok(new RestResponse<>(true, "200", fileType + " thumbnail urls", saved));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", "Error: " + e.getMessage(), null));
        }
    }

    @PostMapping(value = "/file/{userId}/multi", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "업로드", description = "복수 파일 업로드시 사용(*)")
    public ResponseEntity<?> uploadMultiple(@PathVariable String userId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("fileType") FileTarget fileType) {
        try {
            List<FileResponse> result = azureBlobService.uploadMultiple(userId, files,
                    fileType);
            return ResponseEntity.ok(new RestResponse<>(true, "200", fileType + " thumbnail urls", result));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", "Error: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/file/{userId}/{fileHash}")
    @Operation(summary = "삭제", description = "단일 파일 삭제시 사용(*)")
    public ResponseEntity<?> deleteFile(@PathVariable String userId, @PathVariable String fileHash) {
        boolean deleted = azureBlobService.deleteFile(userId, fileHash);
        if (deleted)
            return ResponseEntity.ok(new RestResponse<>(false, "200", "File deleted successful", null));
        return ResponseEntity.ok(new RestResponse<>(false, "404", "File not found or unauthorized", null));
    }
}
