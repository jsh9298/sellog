package com.teamproject.sellog.domain.user.model.follower;

import java.util.Date;
import java.util.UUID;

import com.teamproject.sellog.domain.user.model.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
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
@Table(name = "Follower")
public class Follow {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToMany(cascade = CascadeType.REMOVE) // 관계맵핑 임시
    @JoinColumn(name = "user_id", referencedColumnName = "id") // 외래키 지정
    private User userId;

    @ManyToMany(cascade = CascadeType.REMOVE) // 관계맵핑 임시
    @JoinColumn(name = "other_id", referencedColumnName = "id") // 외래키 지정
    private User other_id;

    @Column(name = "status", nullable = true)
    private String status;

    @Column(name = "create_at", nullable = true)
    private Date createAt;
}
