package com.teamproject.sellog.domain.recommend.model;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

//추천 대상의 메타데이터 테이블
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Item {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    private String title;
    private String category;

    public Item(UUID id) {
        this.id = id;
    }
}