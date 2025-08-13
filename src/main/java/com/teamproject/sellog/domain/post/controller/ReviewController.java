package com.teamproject.sellog.domain.post.controller;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teamproject.sellog.common.responseUtils.RestResponse;
import com.teamproject.sellog.domain.post.model.dto.request.ReviewRequest;
import com.teamproject.sellog.domain.post.model.dto.response.ReviewResponseDto;
import com.teamproject.sellog.domain.post.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "리뷰", description = "리뷰 관련 api")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/review/{postId}")
    @Operation(summary = "목록(-)", description = "거래 게시글에 대한 리뷰 목록. 보통은 1개의 거래당 1개의 리뷰라고 생각되지만, 하나의 판매글에서 다수의 물품을 다수의 사용자와 거래할것을 염두해두고 작성")
    public ResponseEntity<?> reviewList(
            @PathVariable UUID postId,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") int limit) {

        return null;
    }

    @PostMapping("/review/{postId}")
    @Operation(summary = "리뷰 등록(+)", description = "등록")
    public ResponseEntity<?> review(@PathVariable UUID postId, @RequestBody ReviewRequest dto,
            HttpServletRequest request) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        reviewService.review(dto, postId, userId);
        return ResponseEntity.ok(RestResponse.success("Add Review success", null));

    }

    @PatchMapping("/review/{postId}")
    @Operation(summary = "리뷰 수정(-)", description = "수정")
    public ResponseEntity<?> editReview(@PathVariable UUID postId, @RequestBody ReviewRequest dto,
            HttpServletRequest request) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        ReviewResponseDto response = reviewService.edit(dto, postId, userId);
        return ResponseEntity.ok(RestResponse.success("Edit Review success", response));
    }

    @DeleteMapping("/review/{postId}")
    @Operation(summary = "리뷰 삭제(-)", description = "삭제")
    public ResponseEntity<?> deleteReview(@PathVariable UUID postId,
            HttpServletRequest request) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        reviewService.deleteReview(postId, userId);
        return ResponseEntity.ok(RestResponse.success("Remove Review success", null));
    }
}
