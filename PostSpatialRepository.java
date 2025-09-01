package com.teamproject.sellog.domain.post.repository;

import com.teamproject.sellog.domain.post.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostSpatialRepository extends JpaRepository<Post, UUID> {

    @Query(value = "SELECT * FROM feed p WHERE ST_Distance_Sphere(p.location_point, POINT(:longitude, :latitude)) <= :distance", countQuery = "SELECT count(*) FROM feed p WHERE ST_Distance_Sphere(p.location_point, POINT(:longitude, :latitude)) <= :distance", nativeQuery = true)
    Page<Post> findPostsWithinDistance(@Param("longitude") double longitude, @Param("latitude") double latitude,
            @Param("distance") double distance, Pageable pageable);
}