package com.teamproject.sellog.domain.search.model.dto.request;

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

    private String keyword;

    private String targetType = "ALL";

    private Boolean searchOnlyFriends = false;

    // 페이징
    private int page = 0;

    private int size = 10;

    // 정렬
    private SortBy sortBy = SortBy.POPULARITY;

    // 위치 기반 검색
    private Double latitude;

    private Double longitude;

    private Double radius = 5.0;

    // 가격 범위 검색
    private BigInteger minPrice;

    private BigInteger maxPrice;

    public enum SortBy {
        LATEST, // 최신순 (게시물, 리뷰) / 최근 가입순 (사용자)
        POPULARITY // 인기순 (좋아요, 팔로워) - 기본값
    }
}