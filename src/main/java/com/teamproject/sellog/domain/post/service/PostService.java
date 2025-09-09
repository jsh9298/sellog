package com.teamproject.sellog.domain.post.service;

import java.sql.Timestamp;

import java.util.UUID;

import org.springframework.data.domain.Page;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.common.responseUtils.CursorPageResponse;

import com.teamproject.sellog.domain.post.model.dto.request.PostRequestDto;
import com.teamproject.sellog.domain.post.model.dto.response.PostListResponseDto;
import com.teamproject.sellog.domain.post.model.dto.response.PostResponseDto;

import com.teamproject.sellog.domain.post.model.entity.Post;

import com.teamproject.sellog.domain.post.model.entity.PostType;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public interface PostService {
    void posting(PostRequestDto dto);

    PostResponseDto getPost(UUID postId, Cookie postViewCookie, HttpServletResponse response);

    PostResponseDto editPost(UUID postId, PostRequestDto dto, String userId);

    void deletePost(UUID postId, String userId);

    void toggleLike(UUID postId, String userId);

    void toggleDislike(UUID postId, String userId);

    CursorPageResponse<PostListResponseDto> listPost(PostType type, Timestamp lastCreateAt,
            UUID lastId, int limit);

    Page<Post> findPostsNearby(Double latitude,
            Double longitude, double distanceInMeters);
}