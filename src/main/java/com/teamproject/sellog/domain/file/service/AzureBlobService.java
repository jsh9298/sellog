package com.teamproject.sellog.domain.file.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.*;
import com.teamproject.sellog.domain.file.model.*;
import com.teamproject.sellog.domain.file.repository.FileMetadataRepository;

@Service
public class AzureBlobService {
    private final FileMetadataRepository fileMetadataRepository;
    private final BlobServiceClient blobServiceClient;
    private final BlobContainerClient inputBlobContainerClient;
    private final BlobContainerClient outputBlobContainerClient;

    private static final List<String> THUMB_SUPPORTED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "mp4", "mov", "avi", "mkv", "wmv", "webm");

    public AzureBlobService(
            @Value("${azure.storage.connection-string}") String conn,
            @Value("${azure.storage.input-container-name}") String inContainer,
            @Value("${azure.storage.output-container-name}") String outContainer,
            FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.blobServiceClient = new BlobServiceClientBuilder().connectionString(conn).buildClient();
        this.inputBlobContainerClient = blobServiceClient.getBlobContainerClient(inContainer);
        this.outputBlobContainerClient = blobServiceClient.getBlobContainerClient(outContainer);
    }

    @Transactional
    public FileResponse uploadWithMetadata(String userId, MultipartFile file, FileTarget fileTarget) throws Exception {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new FileUploadException("File name empty."));

        byte[] fileBytes;
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
            fileBytes = baos.toByteArray();
            fileHash = HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new FileUploadException("Hashing error.", e);
        }

        String ext = StringUtils.getFilenameExtension(originalFilename);

        String blobName = "%s/%s.%s".formatted(userId, fileHash, ext);
        String thumbName = "%s/thumbnails/%s/%s.webp"
                .formatted(userId, fileTarget.name().toLowerCase(), fileHash);

        List<String> outPath = new ArrayList<String>();
        outPath.add(String.format("%s/origin/%s", userId, originalFilename));
        String outFilename = originalFilename.replaceAll("\\.[^.]+$", ".webp");
        if (checkSupportExtension(file)) {
            outPath.add(String.format("%s/thumbnails/profile/%s", userId, outFilename));
            outPath.add(String.format("%s/thumbnails/post/%s", userId, outFilename));
            outPath.add(String.format("%s/thumbnails/chat/%s", userId, outFilename));
        } else {
            outPath.add(String.format("%s/files/%s", userId, originalFilename));
        }

        Optional<FileMetadata> existing = fileMetadataRepository.findByFileHashAndUserId(fileHash, userId);

        FileMetadata meta = existing.orElseGet(() -> {

            BlobClient blobClient = inputBlobContainerClient.getBlobClient(blobName);

            try (InputStream uploadStream = new ByteArrayInputStream(fileBytes)) {
                blobClient.upload(uploadStream, fileBytes.length, true);

                Map<String, String> metaMap = new HashMap<>();
                metaMap.put("orig", originalFilename); // download.png
                metaMap.put("mime", file.getContentType());
                blobClient.setMetadata(metaMap);

                FileMetadata m = new FileMetadata();
                m.setUserId(userId);
                m.setOriginalFilename(originalFilename);
                m.setBlobPath(outPath); // List.of(blobName, thumbName)
                m.setContentType(file.getContentType());
                m.setFileSize(file.getSize());
                m.setFileHash(fileHash);
                m.setFileTarget(fileTarget);

                return fileMetadataRepository.save(m);
            } catch (Exception e) {
                blobClient.deleteIfExists();
                throw new RuntimeException("Blob upload failed", e);
            }
        });

        return createResponse(meta, file, fileTarget, fileHash);
    }

    private FileResponse createResponse(FileMetadata meta, MultipartFile file, FileTarget fileTarget, String fileHash) {
        boolean isThumbSupported = checkSupportExtension(file);
        String original = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new IllegalArgumentException("File name empty."));

        String outFilename = original.replaceAll("\\.[^.]+$", ".webp");

        String outFile = isThumbSupported
                ? String.format("%s/thumbnails/%s/%s", meta.getUserId(), fileTarget.name().toLowerCase(),
                        outFilename)
                : String.format("%s/file/%s", meta.getUserId(), original);
        String originFile = String.format("%s/origin/%s", meta.getUserId(), original);
        return FileResponse.builder()
                .originFileUrl(outputBlobContainerClient.getBlobClient(originFile).getBlobUrl())
                .outFileUrl(outputBlobContainerClient.getBlobClient(outFile).getBlobUrl())
                .fileHash(fileHash)
                .build();
    }

    private boolean checkSupportExtension(MultipartFile file) {
        String ext = Optional.ofNullable(StringUtils.getFilenameExtension(file.getOriginalFilename())).orElse("");
        boolean isThumbSupported = THUMB_SUPPORTED_EXTENSIONS.stream()
                .anyMatch(s -> s.equalsIgnoreCase(ext));
        return isThumbSupported;
    }

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

    @Transactional
    public boolean deleteFile(String userId, String fileHash) {
        return fileMetadataRepository.findByFileHashAndUserId(fileHash, userId)
                .map(meta -> {

                    meta.getBlobPath().parallelStream().forEach(path -> {
                        // 1) 같은 컨테이너라면 필드로 둔 outputBlobContainerClient 재사용
                        BlobClient blob = outputBlobContainerClient.getBlobClient(path);

                        // 2) deleteIfExists() → 404일 때 false 반환하고 예외 안 던짐
                        blob.deleteIfExists();
                    });

                    fileMetadataRepository.delete(meta);
                    return true;
                })
                .orElse(false);
    }
}