package com.teamproject.sellog.domain.user.model.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "user_profile")
public class UserProfile {
    @OneToOne(cascade = CascadeType.REMOVE) // 관계맵핑 임시
    @JoinColumn(name = "id", referencedColumnName = "id") // 외래키 지정
    private User id;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "profile_message", nullable = false)
    private String profileMessage;

    @Column(name = "score", nullable = false)
    private Integer score;
}
