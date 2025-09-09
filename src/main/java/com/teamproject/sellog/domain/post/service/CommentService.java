package com.teamproject.sellog.domain.post.service;

import java.sql.Timestamp;
import java.util.UUID;

import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.domain.post.model.dto.response.CommentResponse;

import org.springframework.stereotype.Service;
import com.teamproject.sellog.domain.post.model.dto.request.CommentRequest;

@Service
public interface CommentService {
    void comment(CommentRequest dto, UUID postId, UUID parentId, String userId);

    void editComment(CommentRequest dto, UUID commentId, String userId);

    void deleteComment(UUID commentId, String userId);

    CursorPageResponse<CommentResponse> listComment(UUID postId, UUID lastGroupId, Timestamp lastCreateAt, UUID lastId,
            int limit);
}
