package com.teamproject.sellog.domain.post.model.dto.request;

import java.math.BigInteger;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequest {
    @NotBlank(message = "리뷰 내용은 비워둘 수 없습니다.")
    @Schema(description = "리뷰 내용", example = "상품 상태가 아주 좋네요!")
    private String content;

    @NotNull(message = "평점을 입력해주세요.")
    @Min(value = 1, message = "평점은 1 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5 이하이어야 합니다.")
    @Schema(description = "평점 (1~5)", example = "5")
    private BigInteger rating;
}