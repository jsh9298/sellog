package com.teamproject.sellog.domain.user.model.user;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
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

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY) // 관계맵핑 임시
    @MapsId
    @JoinColumn(name = "id") // 외래키 지정
    private User user;

    @Column(name = "nickname", nullable = true)
    private String nickname;

    @Column(name = "profile_message", nullable = true)
    private String profileMessage;

    @Column(name = "score", nullable = true)
    private Integer score;

    @Column(name = "profile_img_small", nullable = true)
    private String profileThumbURL;

    @Column(name = "profile_img_origin", nullable = true)
    private String profileURL;
}
