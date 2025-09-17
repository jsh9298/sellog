package com.teamproject.sellog.domain.post.controller;

import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.common.responseUtils.RestResponse;
import com.teamproject.sellog.domain.post.model.dto.request.CommentRequest;
import com.teamproject.sellog.domain.post.model.dto.response.CommentResponse;
import com.teamproject.sellog.domain.post.service.CommentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "댓글", description = "댓글 관련 api")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comment/{postId}")
    @Operation(summary = "목록(+)", description = "댓글 목록을 계층형 구조에 맞게 정렬하여 조회합니다. (그룹ID, 생성시간 오름차순)")
    public ResponseEntity<?> commentList(
            @PathVariable UUID postId,
            @RequestParam(required = false) UUID lastGroupId,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") int limit) {

        CursorPageResponse<CommentResponse> responseData = commentService.listComment(postId, lastGroupId, lastCreateAt,
                lastId, limit);
        return ResponseEntity.ok(RestResponse.success("댓글 목록을 성공적으로 조회했습니다.", responseData));
    }

    @PostMapping("/comment/{postId}")
    @Operation(summary = "댓글 입력(+)", description = "게시글에 댓글 또는 대댓글을 작성합니다. 대댓글을 작성하려면 쿼리 파라미터로 'parentId'를 포함해야 합니다.")
    public ResponseEntity<?> comment(@PathVariable UUID postId,
            @RequestParam(required = false) UUID parentId,
            @RequestBody CommentRequest dto,
            HttpServletRequest request) {
        String userId = getAuthenticatedUserId(request);
        commentService.comment(dto, postId, parentId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.success("댓글이 성공적으로 작성되었습니다.", null));
    }

    @PatchMapping("/comment/{commentId}")
    @Operation(summary = "댓글 수정(+)", description = "자신이 작성한 댓글을 수정합니다.")
    public ResponseEntity<?> editComment(@PathVariable UUID commentId,
            @RequestBody CommentRequest dto,
            HttpServletRequest request) {
        String userId = getAuthenticatedUserId(request);
        commentService.editComment(dto, commentId, userId);
        return ResponseEntity.ok(RestResponse.success("댓글이 성공적으로 수정되었습니다.", null));
    }

    @DeleteMapping("/comment/{commentId}")
    @Operation(summary = "댓글 삭제(+)", description = "자신이 작성한 댓글을 삭제합니다.")
    public ResponseEntity<?> deleteComment(@PathVariable UUID commentId,
            HttpServletRequest request) {
        String userId = getAuthenticatedUserId(request);
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(RestResponse.success("댓글이 성공적으로 삭제되었습니다.", null));
    }

    private String getAuthenticatedUserId(HttpServletRequest request) {
        Object userIdAttribute = request.getAttribute("authenticatedUserId");
        if (userIdAttribute == null) {
            // JWT 필터에서 처리되지만, 안전장치로 추가
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userIdAttribute.toString();
    }
}
