package com.teamproject.sellog.domain.user.model.entity.user;

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
@Table(name = "user_private")
public class UserPrivate {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY) // 관계맵핑 임시
    @JoinColumn(name = "id") // 외래키 지정
    @MapsId
    private User user;

    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    @Column(name = "user_address", nullable = true)
    private String userAddress;

    @Column(name = "user_name", nullable = true)
    private String userName;

    @Column(name = "gender", nullable = true)
    private Gender gender;

    @Column(name = "birth_day", nullable = true)
    private String birthDay;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof UserPrivate)) {
            return false;
        }
        UserPrivate userPrivate = (UserPrivate) o;
        return this.id != null && Objects.equals(this.id, userPrivate.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : Objects.hash(id);
    }
}
