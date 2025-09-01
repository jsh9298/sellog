package com.teamproject.sellog.common.locationUtils.DataMapping;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "geometry_columns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(GeometryColumnsId.class)
@Immutable
public class GeometryColumns {

    @Column(name = "F_TABLE_CATALOG", length = 256)
    private String fTableCatalog;

    @Column(name = "F_TABLE_SCHEMA", length = 256)
    private String fTableSchema;

    @Id
    @Column(name = "F_TABLE_NAME", nullable = false, length = 256)
    private String fTableName;

    @Id
    @Column(name = "F_GEOMETRY_COLUMN", nullable = false, length = 256)
    private String fGeometryColumn;

    @Column(name = "COORD_DIMENSION")
    private Integer coordDimension;

    @Column(name = "SRID")
    private Integer srid;

    @Column(name = "TYPE", nullable = false, length = 256)
    private String type;
}