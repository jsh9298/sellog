// src/main/java/com/teamproject/sellog.domain.search.model.entity/SearchIndex.java
package com.teamproject.sellog.domain.search.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point; // JTS Point import

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "search_index") // 검색 전용 테이블
public class SearchIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // 또는 UUID
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId; // 원본 엔티티의 ID (Post, User 등)
    @Column(name = "source_type", nullable = false) // 원본 엔티티의 타입 (예: "POST", "USER", "COMMENT")
    private String sourceType;

    // 검색 대상 텍스트 필드들을 모두 통합 (Full-Text Index 적용 대상)
    @Column(name = "full_text_content", columnDefinition = "TEXT")
    private String fullTextContent;

    // 검색 결과로 보여줄 주요 정보들
    @Column(name = "main_title") // 예: Post의 제목, User의 이름, Comment 내용 요약 등
    private String mainTitle;
    @Column(name = "sub_content", columnDefinition = "TEXT") // 예: Post 내용 일부, Comment 내용 전부 등
    private String subContent;
    @Column(name = "thumbnail_url") // 검색 결과에 썸네일 필요 시
    private String thumbnailUrl;

    // 필터링/정렬에 사용될 필드들 (다양한 원본 엔티티에 맞춰 적절히 추가)
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Column(name = "like_count")
    private BigInteger likeCount;
    @Column(name = "author_id") // Post의 author, Comment의 commenter 등
    private String authorId; // 이 필드가 특정 상황에서는 user_id 역할을 함.
    @Column(name = "author_nickname")
    private String authorNickname; // usernickname
    @Column(name = "location_point", nullable = true) // JTS Point (위치 기반 검색용)
    private Point locationPoint;
    @Column(name = "price") // 상품 게시글의 가격
    private BigInteger price;

    // 생성자나 빌더를 통해 값 설정
    public SearchIndex(UUID sourceId, String sourceType, String fullTextContent, String mainTitle, String subContent,
            String thumbnailUrl, Timestamp createdAt, BigInteger likeCount, String authorId, Point locationPoint,
            BigInteger price) {
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.fullTextContent = fullTextContent;
        this.mainTitle = mainTitle;
        this.subContent = subContent;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.authorId = authorId;
        this.locationPoint = locationPoint;
        this.price = price;
    }
}