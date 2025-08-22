package com.teamproject.sellog.domain.recommend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

//사용자 로그 테이블
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    private UUID userId;
    private UUID itemId;
    private String interactionType; // 예: "VIEW", "PURCHASE", "LIKE"
    private LocalDateTime timestamp; // 소요시간? 체류시간?

    public UserInteraction(UUID userId, UUID itemId, String interactionType) {
        this.userId = userId;
        this.itemId = itemId;
        this.interactionType = interactionType;
        this.timestamp = LocalDateTime.now();
    }
}