package com.teamproject.sellog.domain.user.model.entity.user;

import java.io.Serializable;
import java.util.Objects;
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
public class UserProfile implements Serializable {

    private static final long serialVersionUID = 1L;

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

    @Column(name = "score", nullable = false)
    private Integer score = 0;

    @Column(name = "profile_img_small", nullable = true)
    private String profileThumbURL;

    @Column(name = "profile_img_origin", nullable = true)
    private String profileURL;

    @Column(name = "post_count", nullable = false)
    private long postCount = 0L;

    @Column(name = "product_count", nullable = false)
    private long productCount = 0L;

    @Column(name = "follower_count", nullable = false)
    private long followerCount = 0L;

    @Column(name = "following_count", nullable = false)
    private long followingCount = 0L;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof UserProfile)) {
            return false;
        }
        UserProfile userProfile = (UserProfile) o;
        return this.id != null && Objects.equals(this.id, userProfile.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : Objects.hash(id);
    }
}
