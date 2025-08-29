package com.teamproject.sellog.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.user.model.dto.UserContentCount;
import com.teamproject.sellog.domain.user.model.entity.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @EntityGraph(attributePaths = { "userProfile", "userPrivate" })
    Optional<User> findUserWithProfileAndPrivateByUserId(String userId);

    @Query("SELECT new com.teamproject.sellog.domain.user.model.dto.UserContentCount(" + // DTO Projection 사용
            "  (SELECT COUNT(p) FROM Post p WHERE p.author.id = u.id AND p.postType = 0), " + // 게시글 수
            "  (SELECT COUNT(prod) FROM Post prod WHERE prod.author.id = u.id  AND prod.postType = 1), " + // 상품 수 (예시)
            "  (SELECT COUNT(foll) FROM Follow foll WHERE foll.follower.id = u.id), " + // 팔로잉 수
            "  (SELECT COUNT(foll2) FROM Follow foll2 WHERE foll2.followed.id = u.id) " + // 팔로워 수
            ") " +
            "FROM User u " +
            "WHERE u.userId = :userId")
    Optional<UserContentCount> findContentCountByUserId(String userId);

    @EntityGraph(attributePaths = { "following", "blocking" })
    Optional<User> findByUserIdWithRelations(String userId);

    Optional<User> findByUserId(String userId);

    @EntityGraph(attributePaths = { "following", "following.followed", "following.followed.userProfile" })
    Optional<User> findUserWithFollowingByUserId(String userId);

    @EntityGraph(attributePaths = { "blocking", "blocking.blocked", "blocking.blocked.userProfile" })
    Optional<User> findUserWithBlockingByUserId(String userId);

}
