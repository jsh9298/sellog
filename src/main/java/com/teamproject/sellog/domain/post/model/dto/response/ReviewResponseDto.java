package com.teamproject.sellog.domain.post.model.dto.response;

import java.math.BigInteger;
import java.sql.Timestamp;

public class ReviewResponseDto {
    private String userId;
    private String contents;
    private BigInteger score;
    private Timestamp createAt;
    private Timestamp updateAt;
}
