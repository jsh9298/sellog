package com.teamproject.sellog.domain.user.model.entity.friend;

import java.sql.Timestamp;
import java.time.LocalDateTime;

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
@Table(name = "block_list")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false) // 외래키 지정
    private User blocking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "other_id", referencedColumnName = "id", nullable = false)
    private User blocked;

    @Column(name = "create_at", nullable = true)
    private Timestamp createAt;

    @PrePersist
    public void onCreate() {
        if (this.createAt == null) {
            this.createAt = Timestamp.valueOf(LocalDateTime.now());
        }
    }
}
