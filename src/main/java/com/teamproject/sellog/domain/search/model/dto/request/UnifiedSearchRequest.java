package com.teamproject.sellog.domain.search.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedSearchRequest {

    @Schema(description = "검색 키워드")
    private String keyword;

    @Schema(description = "검색 대상 타입", example = "ALL, USER, POST, REVIEW")
    private String targetType = "ALL";

    @Schema(description = "친구만 검색할지 여부 (targetType이 USER일 때 유효)")
    private Boolean searchOnlyFriends = false;

    // 페이징
    @Schema(description = "페이지 번호 (0부터 시작)", defaultValue = "0")
    private int page = 0;
    @Schema(description = "페이지 크기", defaultValue = "10")
    private int size = 10;

    // 정렬
    @Schema(description = "정렬 기준", defaultValue = "POPULARITY")
    private SortBy sortBy = SortBy.POPULARITY;

    // 위치 기반 검색
    @Schema(description = "위도 (위치 기반 검색 시)")
    private Double latitude;
    @Schema(description = "경도 (위치 기반 검색 시)")
    private Double longitude;
    @Schema(description = "검색 반경 (km, 위치 기반 검색 시)", defaultValue = "5.0")
    private Double radius = 5.0;

    // 가격 범위 검색
    @Schema(description = "최소 가격")
    private BigInteger minPrice;
    @Schema(description = "최대 가격")
    private BigInteger maxPrice;

    public enum SortBy {
        LATEST, // 최신순 (게시물, 리뷰) / 최근 가입순 (사용자)
        POPULARITY // 인기순 (좋아요, 팔로워) - 기본값
    }
}