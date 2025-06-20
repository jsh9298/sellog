package com.teamproject.sellog.domain.user.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.user.model.user.UserPrivate;

@Repository
public interface UserPrivateRepository extends JpaRepository<UserPrivate, UUID> {

}