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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Immutable
public class SpatialRefSys {

    @Id
    @Column(name = "SRID")
    private Integer srid;

    @Column(name = "AUTH_NAME", length = 256)
    private String authName;

    @Column(name = "AUTH_SRID")
    private Integer authSrid;

    @Column(name = "SRTEXT", length = 2048)
    private String srtext;
}