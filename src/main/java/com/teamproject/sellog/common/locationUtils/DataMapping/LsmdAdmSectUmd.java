package com.teamproject.sellog.common.locationUtils.DataMapping;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "lsmd_adm_sect_umd")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Immutable
public class LsmdAdmSectUmd {

    @Id
    @Column(name = "OGR_FID")
    private Integer ogrFid;

    @Column(name = "SHAPE", columnDefinition = "geometry")
    private Geometry shape;

    @Column(name = "emd_cd", length = 10)
    private String emdCd;

    @Column(name = "col_adm_se", length = 5)
    private String colAdmSe;

    @Column(name = "emd_nm", length = 20)
    private String emdNm;

    @Column(name = "sgg_oid")
    private Integer sggOid;
}