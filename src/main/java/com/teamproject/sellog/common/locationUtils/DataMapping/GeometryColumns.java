package com.teamproject.sellog.common.locationUtils.DataMapping;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "geometry_columns")
@Immutable
public class GeometryColumns {

    @EmbeddedId
    private GeometryColumnsId id;

    @Column(name = "F_TABLE_CATALOG", length = 256)
    private String fTableCatalog;

    @Column(name = "F_TABLE_SCHEMA", length = 256)
    private String fTableSchema;

    @Column(name = "COORD_DIMENSION")
    private Integer coordDimension;

    @Column(name = "SRID")
    private Integer srid;

    @Column(name = "TYPE", length = 256, nullable = false)
    private String type;

    // Hibernate 및 JPA를 위한 기본 생성자
    protected GeometryColumns() {
    }

    // Getter methods
    public GeometryColumnsId getId() {
        return id;
    }

    // (선택 사항) 필드 값을 받는 생성자 추가
    public GeometryColumns(GeometryColumnsId id, String fTableCatalog, String fTableSchema, Integer coordDimension,
            Integer srid, String type) {
        this.id = id;
        this.fTableCatalog = fTableCatalog;
        this.fTableSchema = fTableSchema;
        this.coordDimension = coordDimension;
        this.srid = srid;
        this.type = type;
    }

    public String getFTableCatalog() {
        return fTableCatalog;
    }

    public String getFTableSchema() {
        return fTableSchema;
    }

    // 복합 키 내부 필드에 대한 편의성 getter (선택 사항)
    public String getFTableName() {
        return id != null ? id.getFTableName() : null;
    }

    public String getFGeometryColumn() {
        return id != null ? id.getFGeometryColumn() : null;
    }

    public Integer getCoordDimension() {
        return coordDimension;
    }

    public Integer getSrid() {
        return srid;
    }

    public String getType() {
        return type;
    }
}