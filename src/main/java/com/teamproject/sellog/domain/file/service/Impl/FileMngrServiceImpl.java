package com.teamproject.sellog.domain.file.service.Impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import com.teamproject.sellog.domain.file.model.FileMetadata;
import com.teamproject.sellog.domain.file.model.FileResponse;
import com.teamproject.sellog.domain.file.model.FileTarget;
import com.teamproject.sellog.domain.file.repository.FileMetadataRepository;
import com.teamproject.sellog.domain.file.service.FileMngrService;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileMngrServiceImpl implements FileMngrService {
    @Value("${file.upload-dir}")
    private String uploadDir;
    // 이미지 가공 시 사이즈
    private static final Map<String, Map<String, Integer>> SIZES = Map.of(
            "profile", Map.of("width", 640, "height", 640),
            "post", Map.of("width", 1080, "height", 1080),
            "chat", Map.of("width", 200, "height", 200));

    private static final List<String> IMAGE_EXT = List.of("jpg", "jpeg", "png",
            "gif", "bmp", "webp");
    private static final List<String> VIDEO_EXT = List.of("mp4", "mov", "avi",
            "mkv", "wmv", "webm");

    private final FileMetadataRepository fileMetadataRepository;

    @Override
    public FileResponse uploadWithMetadata(String userId, MultipartFile file, FileTarget fileTarget) throws Exception {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new FileUploadException("File name empty."));

        // 1. 파일을 한번만 읽어 해시 계산 및 byte 배열 생성
        byte[] fileData;
        String fileHash;
        try (InputStream inputStream = file.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                md.update(buffer, 0, bytesRead);
            }
            fileData = baos.toByteArray();
            fileHash = HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new FileUploadException("Hashing error.", e);
        }

        // 2. DB에서 기존 파일 메타데이터 조회
        Optional<FileMetadata> existing = fileMetadataRepository.findByFileHashAndUserId(fileHash, userId);

        FileMetadata metadata = existing.orElseGet(() -> {
            // 3. 신규 파일인 경우: 파일 저장 및 메타데이터 생성
            try {
                String ext = getExtension(originalFilename);
                List<String> savedFilePaths = new ArrayList<>();

                // 원본 파일 경로 (해시 기반)
                String originHashedPath = String.format("%s/origin/%s.%s", userId, fileHash, ext);
                saveFileLocally(originHashedPath, fileData);
                savedFilePaths.add(originHashedPath);

                // 썸네일 생성 가능 파일 처리
                if (IMAGE_EXT.contains(ext) || VIDEO_EXT.contains(ext)) {
                    byte[] bufferForThumb = VIDEO_EXT.contains(ext) ? extractFrameFromVideo(file) : fileData;

                    for (Map.Entry<String, Map<String, Integer>> sizeEntry : SIZES.entrySet()) {
                        String folder = sizeEntry.getKey();
                        int width = sizeEntry.getValue().get("width");
                        int height = sizeEntry.getValue().get("height");

                        byte[] thumbnailBytes = createThumbnail(bufferForThumb, width, height);
                        String thumbnailHashedPath = String.format("%s/thumbnails/%s/%s.webp", userId, folder,
                                fileHash);
                        saveFileLocally(thumbnailHashedPath, thumbnailBytes);
                        savedFilePaths.add(thumbnailHashedPath);
                    }
                } else {
                    // 기타 파일 처리 (필요 시)
                    String miscHashedPath = String.format("%s/files/%s.%s", userId, fileHash, ext);
                    saveFileLocally(miscHashedPath, fileData);
                    savedFilePaths.add(miscHashedPath);
                }

                // DB에 메타데이터 저장
                FileMetadata newMeta = new FileMetadata();
                newMeta.setUserId(userId);
                newMeta.setOriginalFilename(originalFilename);
                newMeta.setBlobPath(savedFilePaths);
                newMeta.setContentType(file.getContentType());
                newMeta.setFileSize(file.getSize());
                newMeta.setFileHash(fileHash);
                newMeta.setFileTarget(fileTarget);

                return fileMetadataRepository.save(newMeta);

            } catch (Exception e) {
                // TODO: 오류 발생 시 저장된 파일들 롤백(삭제) 로직 추가
                throw new RuntimeException("File processing failed", e);
            }
        });

        // 4. 응답 생성
        String originUrl = metadata.getBlobPath().stream()
                .filter(p -> p.contains("/origin/"))
                .findFirst()
                .map(p -> "/media/" + p)
                .orElse("");

        // fileTarget에 맞는 썸네일 URL 찾기
        String targetThumbUrl = metadata.getBlobPath().stream()
                .filter(p -> p.contains("/thumbnails/" + fileTarget.name().toLowerCase() + "/"))
                .findFirst()
                .map(p -> "/media/" + p)
                .orElse(originUrl); // 썸네일 없으면 원본 URL 반환

        return FileResponse.builder()
                .originFileUrl(originUrl)
                .outFileUrl(targetThumbUrl)
                .build();
    }

    @Override
    public List<FileResponse> uploadMultiple(String userId, List<MultipartFile> files, FileTarget fileTarget)
            throws Exception {
        return files.parallelStream().map(file -> {
            try {
                // uploadWithMetadata 내부에만 @Transactional
                return uploadWithMetadata(userId, file, fileTarget);
            } catch (Exception e) {
                throw new RuntimeException("Upload failed: " + file.getOriginalFilename(), e);
            }
        }).toList();
    }

    @Override
    public boolean deleteFile(String userId, String fileHash) {
        return fileMetadataRepository.findByFileHashAndUserId(fileHash, userId)
                .map(meta -> {
                    meta.getBlobPath().parallelStream().forEach(path -> {
                        try {
                            Path filePath = Paths.get(uploadDir, path);
                            Files.deleteIfExists(filePath);
                        } catch (IOException e) {
                            // TODO: 삭제 실패시 로직
                        }
                    });

                    fileMetadataRepository.delete(meta);
                    return true;
                })
                .orElse(false);
    }

    private void saveFileLocally(String relativePath, byte[] data) throws IOException {
        Path destinationFile = Paths.get(uploadDir, relativePath).normalize();
        Path parentDir = destinationFile.getParent();

        // 부모 디렉토리가 없으면 생성
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }

        Files.write(destinationFile, data);
    }

    private byte[] createThumbnail(byte[] imageBytes, int width, int height) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(imageBytes))
                .size(width, height)
                .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
                .outputFormat("webp")
                .outputQuality(0.8)
                .toOutputStream(baos);
        return baos.toByteArray();
    }

    private byte[] extractFrameFromVideo(MultipartFile videoFile) throws Exception {
        File tempVideoFile = Files.createTempFile("input_", videoFile.getOriginalFilename()).toFile();
        File tempFrameFile = Files.createTempFile("frame_", ".jpg").toFile();

        try {
            videoFile.transferTo(tempVideoFile);

            VideoAttributes videoAttrs = new VideoAttributes();
            videoAttrs.setSize(null);
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setVideoAttributes(videoAttrs);
            attrs.setOffset(0.5f);
            attrs.setDuration(0.1f);

            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(tempVideoFile), tempFrameFile, attrs);

            return Files.readAllBytes(tempFrameFile.toPath());
        } finally {
            Files.deleteIfExists(tempVideoFile.toPath());
            Files.deleteIfExists(tempFrameFile.toPath());
        }
    }

    private String getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".") + 1).toLowerCase())
                .orElse("");
    }
}