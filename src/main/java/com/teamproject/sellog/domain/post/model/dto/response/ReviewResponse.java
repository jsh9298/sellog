package com.teamproject.sellog.domain.post.model.dto.response;

import com.teamproject.sellog.domain.post.model.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Builder
public class ReviewResponse {
    private final UUID reviewId;
    private final String content;
    private final BigInteger rating;
    private final String authorNickname;
    private final String authorProfileImage;
    private final UUID authorId;
    private final Timestamp createdAt;

    public static ReviewResponse fromEntity(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .content(review.getContent())
                .rating(review.getScore())
                .authorNickname(review.getAuthor().getUserProfile().getNickname())
                .authorProfileImage(review.getAuthor().getUserProfile().getProfileThumbURL())
                .authorId(review.getAuthor().getId())
                .createdAt(review.getCreateAt())
                .build();
    }
}