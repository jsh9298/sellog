package com.teamproject.sellog.domain.recommend.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

//추천 대상의 메타데이터 테이블
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    private String name;
    private String category;
    private boolean isActive; // 추천대상의 삭제 여부 체크. false일시 대상이 삭제된 상태
    private int popularityScore; // 레거시 추천(인기 기반)을 위한 임시 스코어

    public Item(String name, String category, int popularityScore) {
        this.name = name;
        this.category = category;
        this.popularityScore = popularityScore;
    }
}