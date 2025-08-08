package com.teamproject.sellog.domain.post.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teamproject.sellog.common.dtoUtils.RestResponse;
import com.teamproject.sellog.domain.post.model.SortKey;
import com.teamproject.sellog.domain.post.model.dto.request.PostRequestDto;
import com.teamproject.sellog.domain.post.model.dto.response.PostResponseDto;
import com.teamproject.sellog.domain.post.model.entity.PostType;
import com.teamproject.sellog.domain.post.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "게시글", description = "게시글 관련 api")
public class PostController {

    private final PostService postService;

    @GetMapping("/post")
    @Operation(summary = "목록(-)", description = "게시글 목록 - 현재 정렬 기준 종류 정하는 중. 추천 시스템의 경우 python으로 외부 서버로 구현할까 고민중..")
    public ResponseEntity<?> postList(
            @RequestParam(required = false) List<SortKey> sort,
            @RequestParam(required = false) PostType type,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") int limit) {

        return null;
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "읽기(+)", description = "게시글 내용 보기 조회수 정책은 cookie로 판별 예정")
    public ResponseEntity<?> getPost(@PathVariable UUID postId,
            @CookieValue(value = "postView", required = false) Cookie postViewCookie, HttpServletResponse response) {
        try {
            PostResponseDto responseData = postService.getPost(postId, postViewCookie, response);
            return ResponseEntity.ok(new RestResponse<PostResponseDto>(true, "200", "post", responseData));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", e.getMessage(), null));
        }
    }

    @PostMapping("/post")
    @Operation(summary = "업로드(+)", description = "게시글 업로드시 사용")
    public ResponseEntity<?> posting(@RequestBody PostRequestDto postRequestDto) {
        try {
            postService.posting(postRequestDto);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "posting success", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", e.getMessage(), null));
        }
    }

    @PatchMapping("/post/{postId}")
    @Operation(summary = "수정(+)", description = "게시글 수정시 사용")
    public ResponseEntity<?> editPost(@PathVariable UUID postId, HttpServletRequest request,
            @RequestBody PostRequestDto dto) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        try {
            PostResponseDto response = postService.editPost(postId, dto, userId);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "edit post success", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", e.getMessage(), null));
        }

    }

    @DeleteMapping("/post/{postId}")
    @Operation(summary = "삭제(+)", description = "게시글 삭제시 사용")
    public ResponseEntity<?> deletePost(@PathVariable UUID postId, HttpServletRequest request) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        try {
            postService.deletePost(postId, userId);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "delete post success", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", e.getMessage(), null));
        }

    }

    @PatchMapping("/post/{postId}/like")
    @Operation(summary = "좋아요 토글(-)", description = "좋아요 토글")
    public ResponseEntity<?> toggleLike(@PathVariable UUID postId, HttpServletRequest request) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        try {
            postService.toggleLike(postId, userId);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "toggle like success", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", "toggle like failed", null));
        }

    }

    @PatchMapping("/post/{postId}/dislike")
    @Operation(summary = "싫어요 토글(-)", description = "싫어요 토글 스팩에는 없지만 혹시 몰라서 만들어둠")
    public ResponseEntity<?> toggleDislike(@PathVariable UUID postId, HttpServletRequest request) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        try {
            postService.toggleDisLike(postId, userId);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "toggle dislike success", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", "toggle dislike failed", null));
        }
    }
}
