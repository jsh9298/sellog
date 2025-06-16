package com.teamproject.sellog.domain.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamproject.sellog.domain.user.model.user.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
}
