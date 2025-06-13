package com.teamproject.sellog.domain.user.model.follower;

import com.teamproject.sellog.domain.user.model.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Follow")
public class Follow {
    @OneToOne(cascade = CascadeType.REMOVE) // 관계맵핑 임시
    @JoinColumn(name = "user_id", referencedColumnName = "user_id") // 외래키 지정
    private User userId;

    private String other_id;

    private Integer type;
}
