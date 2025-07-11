package com.teamproject.sellog.domain.file.controller;

import java.util.List;

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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final AzureBlobService azureBlobService;

    @PostMapping("/file/{userId}")
    public ResponseEntity<?> upload(@PathVariable String userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") FileTarget fileType) {
        try {
            FileResponse saved = azureBlobService.uploadWithMetadata(userId, file, fileType);
            return ResponseEntity.ok(new RestResponse<>(true, "200", fileType + " thumbnail urls", saved));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(true, "500", "Error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/file/{userId}/multi")
    public ResponseEntity<?> uploadMultiple(@PathVariable String userId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("fileType") FileTarget fileType) {
        try {
            List<FileResponse> result = azureBlobService.uploadMultiple(userId, files,
                    fileType);
            return ResponseEntity.ok(new RestResponse<>(true, "200", fileType + " thumbnail urls", result));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(true, "500", "Error: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/file/{userId}/{fileHash}")
    public ResponseEntity<?> deleteFile(@PathVariable String userId, @PathVariable String fileHash) {
        boolean deleted = azureBlobService.deleteFile(userId, fileHash);
        if (deleted)
            return ResponseEntity.ok("Deleted");
        return ResponseEntity.ok(new RestResponse<>(true, "404", "File not found or unauthorized", null));
    }
}
