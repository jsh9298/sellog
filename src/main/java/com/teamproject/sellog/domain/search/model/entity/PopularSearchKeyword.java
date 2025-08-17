// src/main/java/com/teamproject/sellog.domain.search.model.entity/PopularSearchKeyword.java
package com.teamproject.sellog.domain.search.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "popular_search_keyword")
public class PopularSearchKeyword {
    @Id
    @Column(name = "keyword", unique = true, nullable = false)
    private String keyword;

    @Column(name = "search_count", nullable = false)
    private Long searchCount;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    public PopularSearchKeyword(String keyword, Long searchCount, LocalDateTime lastUpdatedAt) {
        this.keyword = keyword;
        this.searchCount = searchCount;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    @PrePersist
    @PreUpdate // 검색 횟수 업데이트 시 시간 갱신
    public void prePersistOrUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
}