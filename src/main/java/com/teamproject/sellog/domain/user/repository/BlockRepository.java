package com.teamproject.sellog.domain.user.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamproject.sellog.domain.user.model.entity.friend.Block;

public interface BlockRepository extends JpaRepository<Block, UUID> {
    @EntityGraph(attributePaths = { "blocked", "blocked.userProfile" })
    List<Block> findByBlockingIdOrderByCreateAtDescIdDesc(UUID blockedId, Pageable Pageable);

    @EntityGraph(attributePaths = { "blocked", "blocked.userProfile" })
    @Query("SELECT b FROM Block b WHERE b.blocking.id = :blockingUserId AND (b.createAt < :lastCreateAt OR (b.createAt = :lastCreateAt AND b.id < :lastId)) ORDER BY b.createAt DESC, b.blocking.userId DESC")
    List<Block> findByBlockingIdAndCursor(@Param("blockingUserId") UUID blockingUserId,
            @Param("lastCreateAt") Timestamp lastCreateAt,
            @Param("lastId") UUID lastId,
            Pageable pageable);
}
