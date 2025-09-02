package com.teamproject.sellog.domain.post.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.model.entity.PostType;
import com.teamproject.sellog.domain.user.model.dto.response.SimplePostList;
import com.teamproject.sellog.domain.user.model.entity.user.User;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID>, JpaSpecificationExecutor<Post> {
        @Modifying
        @Query("UPDATE Post p SET p.likeCnt = p.likeCnt + 1 WHERE p.id = :id")
        void updateViewCount(UUID id);

        @Query("SELECT p.id as postId, p.title as title, p.thumbnail as thumbnail, p.createAt as createAt FROM Post p WHERE p.author = :user AND p.postType = :type")
        List<SimplePostList> findAllByAuthorAndPostType(@Param("user") User user, @Param("type") PostType type,
                        Pageable pageable);

        @Query("SELECT p.id as postId, p.title as title, p.thumbnail as thumbnail, p.createAt as createAt FROM Post p WHERE p.author = :user AND p.postType = :type AND (p.createAt < :lastCreateAt OR (p.createAt = :lastCreateAt AND p.id < :lastId)) ORDER BY p.createAt DESC, p.id DESC")
        List<SimplePostList> findAllByAuthorAndPostTypeAndCursor(@Param("user") User user, @Param("type") PostType type,
                        @Param("lastCreateAt") Timestamp lastCreateAt,
                        @Param("lastId") UUID lastId,
                        Pageable pageable);

        @EntityGraph(attributePaths = { "author", "author.userProfile", "hashBoard", "hashBoard.tag" })
        Optional<Post> findWithDetailsById(UUID postId);

        @Query(value = "SELECT * FROM feed p WHERE ST_Distance_Sphere(p.location_point, POINT(:longitude, :latitude)) <= :distance", countQuery = "SELECT count(*) FROM feed p WHERE ST_Distance_Sphere(p.location_point, POINT(:longitude, :latitude)) <= :distance", nativeQuery = true)
        Page<Post> findPostsWithinDistance(@Param("longitude") double longitude, @Param("latitude") double latitude,
                        @Param("distance") double distance, Pageable pageable);
}