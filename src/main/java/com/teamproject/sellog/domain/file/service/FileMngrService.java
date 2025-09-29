package com.teamproject.sellog.domain.file.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.teamproject.sellog.domain.file.model.FileResponse;
import com.teamproject.sellog.domain.file.model.FileTarget;

@Service
public interface FileMngrService {

    // 메타데이터와 같이 업로드
    FileResponse uploadWithMetadata(String userId, MultipartFile file, FileTarget fileTarget) throws Exception;

    // 여러 파일 업로드
    List<FileResponse> uploadMultiple(String userId, List<MultipartFile> files, FileTarget fileTarget) throws Exception;

    // 파일 삭제
    boolean deleteFile(String userId, String fileHash);
}
