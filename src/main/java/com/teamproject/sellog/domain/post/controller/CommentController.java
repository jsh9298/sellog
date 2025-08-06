package com.teamproject.sellog.domain.post.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teamproject.sellog.domain.post.model.SortKey;
import com.teamproject.sellog.domain.post.model.entity.PostType;
import com.teamproject.sellog.domain.post.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "댓글", description = "댓글 관련 api")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/comment/{postId}")
    @Operation(summary = "목록", description = "댓글 목록(-)")
    public ResponseEntity<?> commentList(
            @RequestParam(required = false) List<SortKey> sort,
            @RequestParam(required = false) PostType type,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") String limit) {

        return null;
    }

    @PostMapping("/comment/{postId}")
    public ResponseEntity<?> comment(@RequestBody String entity, @RequestParam(required = false) UUID commentId) {
        // 쿼리스트링 값이 존재하면 해당하는 댓글에 대한 답글
        return null;
    }

    @PatchMapping("/comment/{postId}")
    public ResponseEntity<?> editComment(@RequestBody String entity, @RequestParam(required = false) UUID commentId) {
        return null;
    }

    @DeleteMapping("/comment/{postId}")
    public ResponseEntity<?> deleteComment(@RequestBody String entity, @RequestParam(required = false) UUID commentId) {
        return null;
    }
}
