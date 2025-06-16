package com.teamproject.sellog.auth.repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.teamproject.sellog.domain.user.model.user.User;

@Repository
public interface AuthRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUserId(String userId);

    Optional<User> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.userId = :userId")
    void updateLastLogin(String userId, Timestamp lastLogin);
}
