package com.teamproject.sellog.domain.user.model.entity.friend;

import com.teamproject.sellog.domain.user.model.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "follow_request")
public class FollowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // 팔로우를 요청한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // 팔로우 요청을 받은 사용자 (비공개 계정)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    @Column(name = "create_at", nullable = false, updatable = false)
    private Timestamp createAt;

    @PrePersist
    public void onCreate() {
        if (this.createAt == null) {
            this.createAt = Timestamp.valueOf(LocalDateTime.now());
        }
    }
}