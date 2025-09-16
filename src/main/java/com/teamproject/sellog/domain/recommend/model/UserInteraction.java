package com.teamproject.sellog.domain.recommend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

//사용자 로그 테이블
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String userId; // userId를 String 타입으로 변경
    private UUID itemId;
    private InteractionType interactionType; // 행위타입 "VIEW", "PURCHASE", "LIKE"
    private LocalDateTime timestamp; // 소요시간? 체류시간?

    public UserInteraction(String userId, UUID itemId, InteractionType interactionType) {
        this.userId = userId;
        this.itemId = itemId;
        this.interactionType = interactionType;
        this.timestamp = LocalDateTime.now();
    }
}