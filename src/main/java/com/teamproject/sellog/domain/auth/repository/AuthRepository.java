package com.teamproject.sellog.domain.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.user.model.entity.user.User;

@Repository
public interface AuthRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUserId(String userId);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserIdAndEmail(String userId, String email);

    boolean existsByUserId(String userId);

    @Query("SELECT u.id FROM User u WHERE u.userId = :userId")
    Optional<UUID> findIdByUserId(String userId);

    @Query("SELECT u.email FROM User u WHERE u.userId = :userId")
    Optional<String> findEmailByUserId(String userId);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile JOIN FETCH u.userPrivate WHERE u.userId = :userId")
    Optional<User> findUserWithDetailsByUserId(@Param("userId") String userId);
}
