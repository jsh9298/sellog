package com.teamproject.sellog.common.locationUtils.DataMapping;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GeometryColumnsId implements Serializable {
    private String fTableName;
    private String fGeometryColumn;
}