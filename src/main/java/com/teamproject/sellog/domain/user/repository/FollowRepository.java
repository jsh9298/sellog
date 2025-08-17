package com.teamproject.sellog.domain.user.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamproject.sellog.domain.user.model.entity.friend.Follow;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    @EntityGraph(attributePaths = { "followed", "followed.userProfile" })
    List<Follow> findByFollowerIdOrderByCreateAtDescIdDesc(UUID followerId, Pageable Pageable);

    @EntityGraph(attributePaths = { "followed", "followed.userProfile" })
    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId AND (f.createAt < :lastCreateAt OR (f.createAt = :lastCreateAt AND f.id < :lastId)) ORDER BY f.createAt DESC, f.follower.userId DESC")
    List<Follow> findByFollowerIdAndCursor(@Param("followerId") UUID userId,
            @Param("lastCreateAt") Timestamp lastCreateAt,
            @Param("lastId") UUID lastId, Pageable pageable);

    // 특정 사용자가 팔로우하는 모든 사용자의 ID를 조회
    @Query("SELECT f.followed.id FROM Follow f WHERE f.follower.id = :followerId")
    List<UUID> findFollowingIdsByFollowerId(@Param("followerId") UUID followerId);
}
