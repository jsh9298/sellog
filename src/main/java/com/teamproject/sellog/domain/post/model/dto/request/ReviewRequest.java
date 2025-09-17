package com.teamproject.sellog.domain.post.model.dto.request;

import java.math.BigInteger;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequest {

    private String content;

    private BigInteger rating;
}