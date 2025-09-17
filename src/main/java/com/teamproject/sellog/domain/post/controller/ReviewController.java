package com.teamproject.sellog.domain.post.controller;

import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.common.responseUtils.RestResponse;
import com.teamproject.sellog.domain.post.model.dto.request.ReviewRequest;
import com.teamproject.sellog.domain.post.model.dto.response.ReviewResponse;
import com.teamproject.sellog.domain.post.service.ReviewService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "리뷰", description = "리뷰 관련 api")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/review/{postId}")
    @Operation(summary = "목록(+)", description = "리뷰 목록. 최신순으로 정렬")
    public ResponseEntity<RestResponse<CursorPageResponse<ReviewResponse>>> reviewList(@PathVariable UUID postId,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") int limit) {
        CursorPageResponse<ReviewResponse> responseData = reviewService.listReview(postId, lastCreateAt, lastId, limit);
        return ResponseEntity.ok(RestResponse.success("리뷰 목록을 성공적으로 조회했습니다.", responseData));
    }

    @PostMapping("/review/{postId}")
    @Operation(summary = "리뷰 입력(+)", description = "리뷰 작성")
    public ResponseEntity<RestResponse<Void>> createReview(@PathVariable UUID postId, @RequestBody ReviewRequest dto,
            HttpServletRequest request) {
        String userId = getAuthenticatedUserId(request);
        reviewService.createReview(postId, userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponse.success("리뷰가 성공적으로 작성되었습니다.", null));
    }

    @PatchMapping("/review/{reviewId}")
    @Operation(summary = "리뷰 수정(+)", description = "리뷰 수정")
    public ResponseEntity<RestResponse<Void>> editReview(@PathVariable UUID reviewId,
            @RequestBody ReviewRequest dto,
            HttpServletRequest request) {
        String userId = getAuthenticatedUserId(request);
        reviewService.editReview(reviewId, userId, dto);
        return ResponseEntity.ok(RestResponse.success("리뷰가 성공적으로 수정되었습니다.", null));
    }

    @DeleteMapping("/review/{reviewId}")
    @Operation(summary = "리뷰 삭제(+)", description = "리뷰 삭제")
    public ResponseEntity<RestResponse<Void>> deleteReview(@PathVariable UUID reviewId,
            HttpServletRequest request) {
        String userId = getAuthenticatedUserId(request);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok(RestResponse.success("리뷰가 성공적으로 삭제되었습니다.", null));
    }

    private String getAuthenticatedUserId(HttpServletRequest request) {
        Object userIdAttribute = request.getAttribute("authenticatedUserId");
        if (userIdAttribute == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userIdAttribute.toString();
    }
}
