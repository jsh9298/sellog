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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import com.teamproject.sellog.common.locationUtils.Location;

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

    @Column(name = "location_latitude", nullable = true)
    private Double latitude;
    @Column(name = "location_longitude", nullable = true)
    private Double longitude;

    @Column(name = "location_point", columnDefinition = "POINT SRID 4326", nullable = true)
    private Point locationPoint;

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

    @PreUpdate
    public void onUpdate() {
        if (this.latitude != null && this.longitude != null) {
            // GeometryFactory는 Point 객체를 생성하는 데 사용됩니다.
            // PrecisionModel: 공간 데이터의 정밀도를 정의합니다.
            // SRID (Spatial Reference ID): 좌표계 정보를 나타냅니다.
            // 4326은 WGS84 (World Geodetic System 1984) 좌표계를 의미하며,
            // GPS에서 사용하는 표준 위경도 좌표계입니다.
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

            // JTS Coordinate 객체는 (경도 X, 위도 Y) 순서로 인자를 받습니다.
            Coordinate coordinate = new Coordinate(this.longitude, this.latitude);

            // Point 객체를 생성하고 `locationPoint` 필드에 설정합니다.
            this.locationPoint = geometryFactory.createPoint(coordinate);
        } else {
            // location이 null이라면 locationPoint도 null로 설정하여 데이터 일관성을 유지할 수 있습니다.
            // 또는 이전 값을 유지하도록 아무것도 하지 않을 수도 있습니다.
            // 여기서는 null로 설정하여 명확하게 합니다.
            this.locationPoint = null;
        }
    }
}
