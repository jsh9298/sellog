package com.teamproject.sellog.domain.user.model.entity.friend;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.teamproject.sellog.domain.user.model.entity.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "follower_list")
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false) // 외래키 지정
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "other_id", referencedColumnName = "id", nullable = false)
    private User followed;

    @Column(name = "create_at", nullable = true)
    private Timestamp createAt;

    @PrePersist
    public void onCreate() {
        if (this.createAt == null) {
            this.createAt = Timestamp.valueOf(LocalDateTime.now());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Follow)) {
            return false;
        }
        Follow follow = (Follow) o;
        if (id != null && follow.id != null) {
            return Objects.equals(id, follow.id);
        }

        return Objects.equals(this.follower.getId(), follow.follower.getId())
                && Objects.equals(this.followed.getId(), follow.followed.getId());

    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(this.follower.getId(), this.followed.getId());
    }
}
