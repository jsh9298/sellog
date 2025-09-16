package com.teamproject.sellog.common.locationUtils.DataMapping;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GeometryColumnsId implements Serializable {

    private static final long serialVersionUID = 1L; // Serializable을 위한 serialVersionUID 추가

    @Column(name = "F_TABLE_NAME", length = 256, nullable = false)
    private String fTableName;

    @Column(name = "F_GEOMETRY_COLUMN", length = 256, nullable = false)
    private String fGeometryColumn;

    // 복합 키를 위한 기본 생성자
    public GeometryColumnsId() {
    }

    // 필드 값을 받는 생성자 추가 (선택 사항이지만 유용합니다)
    public GeometryColumnsId(String fTableName, String fGeometryColumn) {
        this.fTableName = fTableName;
        this.fGeometryColumn = fGeometryColumn;
    }

    // Getter methods
    public String getFTableName() {
        return fTableName;
    }

    public String getFGeometryColumn() {
        return fGeometryColumn;
    }

    // Setter methods (JPA 사양에서는 필요 없지만, 유연성을 위해 추가할 수 있습니다.)
    public void setFTableName(String fTableName) {
        this.fTableName = fTableName;
    }

    public void setFGeometryColumn(String fGeometryColumn) {
        this.fGeometryColumn = fGeometryColumn;
    }

    // equals 및 hashCode 구현 (필수)
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GeometryColumnsId that = (GeometryColumnsId) o;
        return Objects.equals(fTableName, that.fTableName) &&
                Objects.equals(fGeometryColumn, that.fGeometryColumn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fTableName, fGeometryColumn);
    }
}
