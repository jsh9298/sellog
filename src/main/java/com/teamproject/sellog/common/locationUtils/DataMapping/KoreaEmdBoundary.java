package com.teamproject.sellog.common.locationUtils.DataMapping;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.locationtech.jts.geom.Geometry;

//SRID 5186로 저장되어있어  SRID 4326로 변환 해서 사용해야 할 수도있음
@Entity
@Table(name = "korea_emd_boundary")
@Immutable
public class KoreaEmdBoundary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OGR_FID")
    private Integer ogrFid;

    // geometry 타입은 org.locationtech.jts.geom.Geometry 로 매핑
    @Column(name = "SHAPE", nullable = false, columnDefinition = "geometry")
    private Geometry shape;

    @Column(name = "emd_cd", length = 8)
    private String emdCd;

    @Column(name = "col_adm_se", length = 5)
    private String colAdmSe;

    @Column(name = "emd_nm", length = 100)
    private String emdNm;

    @Column(name = "sgg_oid", precision = 9, scale = 0)
    private Long sggOid;

    // Getter methods
    public Integer getOgrFid() {
        return ogrFid;
    }

    public Geometry getShape() {
        return shape;
    }

    public String getEmdCd() {
        return emdCd;
    }

    public String getColAdmSe() {
        return colAdmSe;
    }

    public String getEmdNm() {
        return emdNm;
    }

    public Long getSggOid() {
        return sggOid;
    }

    // Hibernate 및 JPA를 위한 기본 생성자
    protected KoreaEmdBoundary() {
    }
}
