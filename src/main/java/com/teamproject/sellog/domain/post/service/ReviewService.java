package com.teamproject.sellog.domain.post.service;

import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.domain.post.model.dto.request.ReviewRequest;
import com.teamproject.sellog.domain.post.model.dto.response.ReviewResponse;

import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import java.util.UUID;

@Service
public interface ReviewService {
    CursorPageResponse<ReviewResponse> listReview(UUID postId, Timestamp lastCreateAt, UUID lastId, int limit);

    void createReview(UUID postId, String userId, ReviewRequest dto);

    void editReview(UUID reviewId, String userId, ReviewRequest dto);

    void deleteReview(UUID reviewId, String userId);
}