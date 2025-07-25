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

import com.teamproject.sellog.common.RestResponse;
import com.teamproject.sellog.domain.post.model.dto.request.PostRequestDto;
import com.teamproject.sellog.domain.post.model.dto.response.PostResponseDto;
import com.teamproject.sellog.domain.post.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "게시글", description = "게시글 관련 api")
public class PostController {

    private final PostService postService;

    @GetMapping("/post")
    @Operation(summary = "목록", description = "게시글 목록(-)")
    public ResponseEntity<?> postList(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") String limit) {

        return null;
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "읽기", description = "게시글 내용 보기(+)")
    public ResponseEntity<?> getPost(@PathVariable UUID postId) {
        try {
            PostResponseDto response = postService.getPost(postId);
            return ResponseEntity.ok(new RestResponse<PostResponseDto>(true, "200", "post", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", e.getMessage(), null));
        }
    }

    @PostMapping("/post")
    @Operation(summary = "업로드", description = "게시글 업로드시 사용(+)")
    public ResponseEntity<?> posting(@RequestBody PostRequestDto postRequestDto) {
        try {
            postService.posting(postRequestDto);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "posting success", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", e.getMessage(), null));
        }
    }

    @PatchMapping("/post")
    @Operation(summary = "수정", description = "게시글 수정시 사용(-)")
    public ResponseEntity<?> editPost() {
        return null;
    }

    @DeleteMapping("/post")
    @Operation(summary = "삭제", description = "게시글 삭제시 사용(-)")
    public ResponseEntity<?> deletePost() {
        return null;
    }

}
