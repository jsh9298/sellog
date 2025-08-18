package com.teamproject.sellog.domain.post.model.dto.response;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ReviewListResponse {
    private UUID reviewId;
    private Timestamp updateAt;
    private Timestamp createAt;
    private BigInteger score;
    private String preview;
    private String nickname;
}
