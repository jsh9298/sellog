package com.teamproject.sellog.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.user.model.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @EntityGraph(attributePaths = { "userProfile", "userPrivate" })
    Optional<User> findByUserId(String userId);
}
