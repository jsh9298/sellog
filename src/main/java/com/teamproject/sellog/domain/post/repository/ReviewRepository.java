package com.teamproject.sellog.domain.post.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.model.entity.Review;
import com.teamproject.sellog.domain.user.model.entity.user.User;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByPostAndAuthor(Post post, User Author);

    boolean existsByAuthor(User Author);

    @Query("SELECT p FROM Review p WHERE p.post.id = :postId")
    List<Review> findAllByOrderByReviewIdDesc(@Param("postId") UUID postId, Pageable Pageable);

    @Query("SELECT p FROM Review p WHERE p.post.id = :postId AND p.createAt < :lastCreateAt OR (p.createAt = : lastCreateAt AND p.id < :lastId) ORDER BY p.createAt DESC, p.id DESC")
    List<Review> findAllByReviewIdAndCursor(
            @Param("postId") UUID postId,
            @Param("lastCreateAt") Timestamp lastCreateAt,
            @Param("lastId") UUID lastId,
            Pageable pageable);
}