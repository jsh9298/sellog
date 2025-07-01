package com.teamproject.sellog.domain.file.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.teamproject.sellog.domain.file.model.FileMetadata;
import com.teamproject.sellog.domain.file.model.FileType;
import com.teamproject.sellog.domain.file.repository.FileMetadataRepository;

@Service
public class AzureBlobService {
    private final BlobContainerClient blobContainerClient;
    private final FileMetadataRepository fileMetadataRepository;

    private String storageAccountName;
    private String publicUrlBase;

    public AzureBlobService(
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.container-name}") String containerName,
            @Value("${azure.storage.account-name}") String storageAccountName,
            @Value("${azure.storage.public-url-base}") String publicUrlBase,
            FileMetadataRepository fileMetadataRepository) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString)
                .buildClient();
        this.blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        this.fileMetadataRepository = fileMetadataRepository;
        this.storageAccountName = storageAccountName;

        this.publicUrlBase = publicUrlBase;
    }

    @Transactional
    public FileMetadata uploadWithMetadata(String userId, MultipartFile file, FileType fileType) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String fileHash;

        try (InputStream is = file.getInputStream()) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192]; // 8KB 버퍼
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                md.update(buffer, 0, bytesRead);
            }
            fileHash = HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            // 파일 읽기 또는 해시 계산 중 오류 발생 시
            throw new FileUploadException("파일 데이터를 읽거나 해시를 계산하는 중 오류가 발생했습니다.", e);
        }

        byte[] fileBytes = baos.toByteArray(); // 메모리 버퍼에서 byte[] 얻기
        final long fileSize = file.getSize();
        return fileMetadataRepository.findByFileHash(fileHash).orElseGet(
                () -> {
                    String blobPath = userId + "/" + file.getOriginalFilename();
                    BlobClient blobClient = null;

                    try {
                        blobClient = blobContainerClient.getBlobClient(blobPath);
                        // 2. 메모리 버퍼에서 새로운 InputStream을 얻어 Blob 업로드
                        try (InputStream uploadIs = new ByteArrayInputStream(fileBytes)) {
                            blobClient.upload(uploadIs, fileSize, true); // overwrite=true
                        }

                        FileMetadata meta = new FileMetadata();
                        meta.setUserId(userId);
                        meta.setOriginalFilename(file.getOriginalFilename());
                        meta.setBlobPath(blobPath);
                        meta.setContentType(file.getContentType());
                        meta.setFileSize(fileSize);
                        meta.setFileHash(fileHash);
                        meta.setFileType(fileType);
                        return fileMetadataRepository.save(meta);
                    } catch (Exception e) {
                        if (blobClient != null && blobClient.exists()) { // 파일이 업로드되었는지 확인
                            blobContainerClient.delete(); // 업로드된 Blob 삭제
                        }
                        throw new RuntimeException("Upload failed", e); // 더 구체적인 예외로 변경 권장
                    }
                });
    }

    @Transactional
    public List<FileMetadata> uploadMultiple(String userId, List<MultipartFile> files, FileType fileType)
            throws Exception {
        List<FileMetadata> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(uploadWithMetadata(userId, file, fileType));
        }
        return results;
    }

    @Transactional
    public boolean deleteFile(String userId, UUID fileId) {
        Optional<FileMetadata> metaOpt = fileMetadataRepository.findByIdAndUserId(fileId, userId);
        if (metaOpt.isEmpty())
            return false;

        FileMetadata meta = metaOpt.get();
        BlobClient blobClient = blobContainerClient.getBlobClient(meta.getBlobPath());
        blobClient.deleteIfExists();
        fileMetadataRepository.delete(meta);
        return true;
    }

    public String buildPublicUrl(String containerName, String blobPath) {
        return String.format(publicUrlBase + "/%s/%s",
                storageAccountName, containerName, blobPath);
    }

}
