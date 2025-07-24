package com.teamproject.sellog.domain.post.model.dto.response;

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
public class PostResponseDto {
    private PostType type;
    private String title;

    private String userId;

    private String contents;
    private String thumbnail;
    private String[] tagNames;

    private String place;
    private BigInteger price;
}
