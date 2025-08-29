package com.teamproject.sellog.domain.user.repository;

import com.teamproject.sellog.domain.user.model.entity.friend.FollowRequest;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, UUID> {
    boolean existsByRequesterAndTarget(User requester, User target);
    List<FollowRequest> findByTarget(User target);
}