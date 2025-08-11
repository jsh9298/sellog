package com.teamproject.sellog.domain.post.model;

public enum PostSort {
    AREA, CREATE, LIKECNT,
}

// CREATE -- 기본값
// AREA -- 위치기반으로 가까운순 (행정구역 단위로 새분화)
// LIKECNT -- 좋아요 많은 순

// 임시로 정해두기
// 조회수 많은순, 리뷰/댓글 많은 순(하나의 중고 거래 글에 리뷰가.,. 많을수가 있으려나?..),
// 팔로우 많은 사람이 쓴 순서

// 특정 사용자가 작성한 게시글 목록
