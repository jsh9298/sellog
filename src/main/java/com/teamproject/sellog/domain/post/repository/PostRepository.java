package com.teamproject.sellog.domain.post.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.post.model.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    @Modifying
    @Query("UPDATE Post p SET p.likeCnt = p.likeCnt + 1 WHERE p.id = :id")
    void updateViewCount(UUID id);

    List<Post> findAllByOrderByIdDesc(Pageable Pageable);

    @Query("SELECT p FROM Post p WHERE p.createAt < :lastCreateAt OR (p.createAt = : lastCreateAt AND p.id < :lastId) ORDER BY p.createAt DESC, p.id DESC")
    List<Post> findAllByIdAndCursor(@Param("lastCreateAt") Timestamp lastCreateAt, @Param("lastId") UUID lastId,
            Pageable pageable);
}