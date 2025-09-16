package com.teamproject.sellog.common.locationUtils.DataMapping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "spatial_ref_sys")
@Immutable
public class SpatialRefSys {

    @Id
    @Column(name = "SRID", nullable = false)
    private Integer srid;

    @Column(name = "AUTH_NAME", length = 256)
    private String authName;

    @Column(name = "AUTH_SRID")
    private Integer authSrid;

    @Column(name = "SRTEXT", length = 2048)
    private String srtext;

    // Getter methods
    public Integer getSrid() {
        return srid;
    }

    public String getAuthName() {
        return authName;
    }

    public Integer getAuthSrid() {
        return authSrid;
    }

    public String getSrtext() {
        return srtext;
    }

    // Hibernate 및 JPA를 위한 기본 생성자
    protected SpatialRefSys() {
    }
}
