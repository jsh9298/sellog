package com.teamproject.sellog.domain.post.model.dto.request;

import java.math.BigInteger;

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
    private String[] tagNames;

    private String place;
    private BigInteger price;
}