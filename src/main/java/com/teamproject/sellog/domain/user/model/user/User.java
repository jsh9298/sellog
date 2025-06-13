package com.teamproject.sellog.domain.user.model.user;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
// 사용자 기본정보(변동이 없을것 같은거만 모아둠)
@Table(name = "user")
public class User {
    @Id
    @Column(name = "user_id", nullable = false, columnDefinition = "varchar")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String userId; // 사용자 id
    @Column(name = "salt", nullable = false, columnDefinition = "varchar")
    private String randomSalt; // 사용자 별 랜덤 솔트값
    @Column(name = "create_at", nullable = false, columnDefinition = "varchar")
    private Timestamp createAt; // 가입일
}
