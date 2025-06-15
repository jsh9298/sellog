package com.teamproject.sellog.domain.user.model.user;

import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId; // 사용자 id

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "password_salt", nullable = false)
    private String passwordSalt; // 사용자 별 랜덤 솔트값

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "account_status", nullable = false)
    private String accountStatus;

    @Column(name = "create_at", nullable = false)
    private Timestamp createAt;

    @Column(name = "last_login", nullable = false)
    private Timestamp lastLogin;

    @Column(name = "account_visibility", nullable = false)
    private String accountVisibility;
}
