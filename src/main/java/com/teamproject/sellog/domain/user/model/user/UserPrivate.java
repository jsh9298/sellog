package com.teamproject.sellog.domain.user.model.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
// 사용자 개인정보
@Table(name = "user_private")
public class UserPrivate {
    @OneToOne(cascade = CascadeType.REMOVE) // 관계맵핑 임시
    @JoinColumn(name = "user_id", referencedColumnName = "user_id") // 외래키 지정
    private User userId;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "user_address", nullable = false)
    private String userAddress;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "password", nullable = false)
    private String password;
}
