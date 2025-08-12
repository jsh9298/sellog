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
public class ReviewRequest {
    private PostType type;
    private String contents; // md?html?
    private BigInteger score;
}
