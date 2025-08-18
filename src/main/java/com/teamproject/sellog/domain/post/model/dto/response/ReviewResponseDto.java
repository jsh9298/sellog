package com.teamproject.sellog.domain.post.model.dto.response;

import java.math.BigInteger;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ReviewResponseDto {
    private String nickname;
    private String contents;
    private BigInteger score;
    private Timestamp createAt;
    private Timestamp updateAt;
}
