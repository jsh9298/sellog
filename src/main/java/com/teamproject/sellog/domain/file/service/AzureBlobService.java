package com.teamproject.sellog.domain.file.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.HexFormat;

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
                .orElseThrow(() -> new FileUploadException("파일 이름이 비어 있습니다."));

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
            throw new FileUploadException("파일 데이터를 읽거나 해시를 계산하는 중 오류 발생.", e);
        }

        Optional<FileMetadata> existing = fileMetadataRepository.findByFileHash(fileHash);
        FileMetadata meta = existing.orElseGet(() -> {
            String blobPath = String.format("%s/%s", userId, originalFilename);
            BlobClient blobClient = inputBlobContainerClient.getBlobClient(blobPath);

            try (InputStream uploadStream = new ByteArrayInputStream(fileBytes)) {
                blobClient.upload(uploadStream, fileBytes.length, true);

                FileMetadata m = new FileMetadata();
                m.setUserId(userId);
                m.setOriginalFilename(originalFilename);
                m.setBlobPath(blobPath);
                m.setContentType(file.getContentType());
                m.setFileSize(file.getSize());
                m.setFileHash(fileHash);
                m.setFileTarget(fileTarget);

                return fileMetadataRepository.save(m);
            } catch (Exception e) {
                if (blobClient.exists())
                    blobClient.delete();
                throw new RuntimeException("Blob 업로드 실패", e);
            }
        });

        return createResponse(meta, file, fileTarget);
    }

    private FileResponse createResponse(FileMetadata meta, MultipartFile file, FileTarget fileTarget) {
        String ext = Optional.ofNullable(StringUtils.getFilenameExtension(file.getOriginalFilename())).orElse("");
        boolean isThumbSupported = THUMB_SUPPORTED_EXTENSIONS.stream()
                .anyMatch(s -> s.equalsIgnoreCase(ext));

        String baseDir = String.format("%s/%s/", meta.getUserId(), fileTarget.name());
        String outFile = isThumbSupported
                ? baseDir + file.getName() + ".webp"
                : baseDir + "file/" + file.getOriginalFilename();
        String originFile = baseDir + "origin" + file.getOriginalFilename();

        return FileResponse.builder()
                .originFileUrl(outputBlobContainerClient.getBlobClient(originFile).getBlobUrl())
                .outFileUrl(outputBlobContainerClient.getBlobClient(outFile).getBlobUrl())
                .build();
    }

    @Transactional
    public List<FileResponse> uploadMultiple(String userId, List<MultipartFile> files, FileTarget fileTarget)
            throws Exception {
        List<FileResponse> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(uploadWithMetadata(userId, file, fileTarget));
        }
        return results;
    }

    @Transactional
    public boolean deleteFile(String userId, UUID fileId) {
        Optional<FileMetadata> metaOpt = fileMetadataRepository.findByIdAndUserId(fileId, userId);
        if (metaOpt.isEmpty())
            return false;

        FileMetadata meta = metaOpt.get();
        BlobClient blobClient = inputBlobContainerClient.getBlobClient(meta.getBlobPath());
        if (blobClient.exists()) {
            blobClient.delete();
        }

        fileMetadataRepository.delete(meta);
        return true;
    }
}
