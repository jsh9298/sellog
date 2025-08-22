package com.teamproject.sellog.domain.post.model.dto.request;

import java.math.BigInteger;
import java.util.List;

import com.teamproject.sellog.domain.post.model.entity.PostType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class PostRequestDto {
    private PostType type;
    private String title; // 제목

    private String userId; // 작성자

    private String contents; // md?html?
    private String thumbnail;
    private List<String> tagNames;
    private Double latitude; // 경위도 좌표
    private Double longitude;
    private String place; // 장소
    private BigInteger price;

    private Boolean isPublic; // true - 전체 공개 false - 팔로워 공개
}