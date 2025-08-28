package com.teamproject.sellog.domain.post.controller;

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

import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.common.responseUtils.RestResponse;
import com.teamproject.sellog.domain.post.model.dto.request.PostRequestDto;
import com.teamproject.sellog.domain.post.model.dto.response.PostListResponseDto;
import com.teamproject.sellog.domain.post.model.dto.response.PostListResponseDto;
import com.teamproject.sellog.domain.post.model.dto.response.PostResponseDto;
import com.teamproject.sellog.domain.post.model.entity.PostType;
import com.teamproject.sellog.domain.post.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import java.sql.Timestamp;
import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "게시글", description = "게시글 관련 api")
public class PostController {

    private final PostService postService;

    @GetMapping("/post")
    @Operation(summary = "목록(+)", description = "게시글 목록. 게시글 타입(전체,일반,판매)에 따라 필터링. 최신순으로 정렬")
    public ResponseEntity<?> postList( // 반환 타입 수정
            @RequestParam(required = false) PostType type,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") int limit) {
        CursorPageResponse<PostListResponseDto> responseData = postService.listPost(type, lastCreateAt, lastId, limit);
        return ResponseEntity.ok(RestResponse.success("게시글 목록을 성공적으로 조회했습니다.", responseData));
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "읽기(+)", description = "게시글 내용 보기 조회수 정책은 cookie로 판별 예정")
    public ResponseEntity<?> getPost(@PathVariable UUID postId,
            @CookieValue(value = "postView", required = false) Cookie postViewCookie, HttpServletResponse response) {

        PostResponseDto responseData = postService.getPost(postId, postViewCookie, response);
        return ResponseEntity.ok(RestResponse.success("post", responseData));

    }

    @PostMapping("/post")
    @Operation(summary = "업로드(+)", description = "게시글 업로드시 사용")
    public ResponseEntity<?> posting(@Valid @RequestBody PostRequestDto postRequestDto) {

        postService.posting(postRequestDto);
        return ResponseEntity.ok(RestResponse.success("posting success", null));
    }

    @PatchMapping("/post/{postId}")
    @Operation(summary = "수정(+)", description = "게시글 수정시 사용")
    public ResponseEntity<?> editPost(@PathVariable UUID postId, HttpServletRequest request,
            @Valid @RequestBody PostRequestDto dto) {
        String userId = request.getAttribute("authenticatedUserId").toString();

        PostResponseDto response = postService.editPost(postId, dto, userId);
        return ResponseEntity.ok(RestResponse.success("edit post success", response));

    }

    @DeleteMapping("/post/{postId}")
    @Operation(summary = "삭제(+)", description = "게시글 삭제시 사용")
    public ResponseEntity<?> deletePost(@PathVariable UUID postId, HttpServletRequest request) {
        String userId = request.getAttribute("authenticatedUserId").toString();

        postService.deletePost(postId, userId);
        return ResponseEntity.ok(RestResponse.success("delete post success", null));

    }

    @PatchMapping("/post/{postId}/like")
    @Operation(summary = "좋아요 토글(+)", description = "좋아요 토글")
    public ResponseEntity<?> toggleLike(@PathVariable UUID postId, HttpServletRequest request) {
        String userId = request.getAttribute("authenticatedUserId").toString();

        postService.toggleLike(postId, userId);
        return ResponseEntity.ok(RestResponse.success("toggle like success", null));

    }

    @PatchMapping("/post/{postId}/dislike")
    @Operation(summary = "싫어요 토글(+)", description = "싫어요 토글 스팩에는 없지만 혹시 몰라서 만들어둠")
    public ResponseEntity<?> toggleDislike(@PathVariable UUID postId, HttpServletRequest request) {
        String userId = request.getAttribute("authenticatedUserId").toString();

        postService.toggleDislike(postId, userId);
        return ResponseEntity.ok(RestResponse.success("toggle dislike success", null));

    }
}
