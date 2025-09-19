package com.teamproject.sellog.domain.user.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamproject.sellog.domain.user.model.entity.friend.FollowRequest;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, UUID> {
    Optional<FollowRequest> findByRequesterIdAndTargetId(UUID requesterId, UUID targetId);

    @EntityGraph(attributePaths = { "requester", "requester.userProfile" })
    @Query("SELECT fr FROM FollowRequest fr WHERE fr.target.id = :receiverId AND (fr.createAt < :lastCreateAt OR (fr.createAt = :lastCreateAt AND fr.id < :lastId)) ORDER BY fr.createAt DESC, fr.id DESC")
    List<FollowRequest> findByReceiverIdWithCursor(@Param("receiverId") UUID receiverId,
            @Param("lastCreateAt") Timestamp lastCreateAt, @Param("lastId") UUID lastId, Pageable pageable);
}