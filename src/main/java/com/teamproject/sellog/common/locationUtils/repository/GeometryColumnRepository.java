package com.teamproject.sellog.common.locationUtils.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.common.locationUtils.DataMapping.GeometryColumns;
import com.teamproject.sellog.common.locationUtils.DataMapping.GeometryColumnsId;

@Repository
public interface GeometryColumnRepository extends JpaRepository<GeometryColumns, GeometryColumnsId> {

}
