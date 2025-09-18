package com.teamproject.sellog.common.locationUtils.repository;

import com.teamproject.sellog.common.locationUtils.DataMapping.KoreaEmdBoundary;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KoreaEmdBoundaryRepository extends JpaRepository<KoreaEmdBoundary, Integer> {

    // ST_Contains 함수를 사용하여 특정 Point를 포함하는 행정구역 경계를 찾습니다.
    @Query(value = "SELECT * FROM korea_emd_boundary WHERE ST_Contains(shape, :point)", nativeQuery = true)
    Optional<KoreaEmdBoundary> findBoundaryContainingPoint(@Param("point") Point point);

}