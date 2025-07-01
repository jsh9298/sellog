package com.teamproject.sellog.domain.file.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.teamproject.sellog.domain.file.model.FileMetadata;
import com.teamproject.sellog.domain.file.model.FileResponse;
import com.teamproject.sellog.domain.file.model.FileType;
import com.teamproject.sellog.domain.file.service.AzureBlobService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final AzureBlobService azureBlobService;

    @PostMapping("/{userId}")
    public ResponseEntity<?> upload(@PathVariable String userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") FileType fileType) {
        try {
            FileMetadata saved = azureBlobService.uploadWithMetadata(userId, file, fileType);
            String filename = file.getOriginalFilename();
            FileResponse response = FileResponse
                    .builder()
                    .originalFilename(filename)
                    .fileUrl(azureBlobService.buildPublicUrl("outcontents", userId + "/" + filename))
                    .originUrl(azureBlobService.buildPublicUrl("outcontents", userId + "/origin/" + filename))
                    .thumbnailUrl(azureBlobService.buildPublicUrl("outcontents", userId + "/thumbnails/" + filename
                            + ".jpg"))
                    .build();
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}/multi")
    public ResponseEntity<?> uploadMultiple(@PathVariable String userId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("fileType") FileType fileType) {
        try {
            List<FileMetadata> result = azureBlobService.uploadMultiple(userId, files, fileType);
            List<FileResponse> list = new ArrayList<>();
            for (MultipartFile file : files) {
                String filename = file.getOriginalFilename();
                FileResponse temp = FileResponse
                        .builder()
                        .originalFilename(filename)
                        .fileUrl(azureBlobService.buildPublicUrl("outcontents", userId + "/" + filename))
                        .originUrl(azureBlobService.buildPublicUrl("outcontents", userId + "/origin/" + filename))
                        .thumbnailUrl(azureBlobService.buildPublicUrl("outcontents", userId + "/thumbnails/" + filename
                                + ".jpg"))
                        .build();
                list.add(temp);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(" Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String userId, @PathVariable UUID fileId) {
        boolean deleted = azureBlobService.deleteFile(userId, fileId);
        if (deleted)
            return ResponseEntity.ok("Deleted");
        return ResponseEntity.status(404).body(" File not found or unauthorized");
    }
}
