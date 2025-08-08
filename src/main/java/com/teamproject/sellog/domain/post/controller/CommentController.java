package com.teamproject.sellog.domain.post.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

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

    @GetMapping("/comment/{postId}")
    @Operation(summary = "목록(-)", description = "댓글 목록 무조건 최신순(생성일 기준)으로 정렬할 생각. 어차피 댓글에 대한 좋아요 기능 같은거는 존재하지 않기 때문")
    public ResponseEntity<?> commentList(
            @PathVariable UUID postId,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") String limit) {

        return null;
    }

    @PostMapping("/comment/{postId}")
    @Operation(summary = "댓글 입력(-)", description = "url쿼리에 댓글id 값이 존재할시, 해당하는 댓글에 대한 답글")
    public ResponseEntity<?> comment(@PathVariable UUID postId, @RequestBody String entity,
            @RequestParam(required = false) UUID commentId) {

        return null;
    }

    @PatchMapping("/comment/{postId}")
    @Operation(summary = "댓글 수정(-)", description = "url쿼리의 댓글id에 해당하는 댓글 수정")
    public ResponseEntity<?> editComment(@PathVariable UUID postId, @RequestBody String entity,
            @RequestParam(required = false) UUID commentId) {
        return null;
    }

    @DeleteMapping("/comment/{postId}")
    @Operation(summary = "댓글 삭제(-)", description = "url쿼리의 댓글id에 해당하는 댓글 삭제")
    public ResponseEntity<?> deleteComment(@PathVariable UUID postId, @RequestBody String entity,
            @RequestParam(required = false) UUID commentId) {
        return null;
    }
}
