package com.teamproject.sellog.domain.user.model.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profile")
public class UserProfile {
    @OneToOne(cascade = CascadeType.REMOVE) // 관계맵핑 임시
    @JoinColumn(name = "user_id", referencedColumnName = "user_id") // 외래키 지정
    private User userId;

    private String nickname;

    private String discription;

    private Integer score;
}
