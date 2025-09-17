package com.teamproject.sellog.domain.recommend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.teamproject.sellog.domain.recommend.model.InteractionType;

import com.teamproject.sellog.domain.recommend.model.UserInteraction;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, UUID> {
    // 특정 유저의 상호작용 기록 조회
    List<UserInteraction> findByUserId(UUID userId);

    // userId, itemId, interactionType을 기준으로 상호작용 기록을 삭제합니다.
    void deleteByUserIdAndItemIdAndInteractionType(String userId, UUID itemId, InteractionType interactionType);
}