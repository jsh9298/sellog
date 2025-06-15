package com.teamproject.sellog.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.user.model.user.User;

@Repository
public interface AuthRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUserId(String userId);
}
