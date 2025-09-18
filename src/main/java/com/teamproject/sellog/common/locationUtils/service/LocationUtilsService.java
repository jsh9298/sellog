package com.teamproject.sellog.common.locationUtils.service;

import com.teamproject.sellog.common.locationUtils.DataMapping.KoreaEmdBoundary;
import com.teamproject.sellog.common.locationUtils.repository.CoordinateTransformRepository;
import com.teamproject.sellog.common.locationUtils.repository.KoreaEmdBoundaryRepository;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationUtilsService {

    private final CoordinateTransformRepository coordinateTransformRepository;
    private final KoreaEmdBoundaryRepository koreaEmdBoundaryRepository;

    // SRID 4326 GeometryFactory
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    // SRID 5186 GeometryFactory
    private final GeometryFactory geometryFactory5186 = new GeometryFactory(new PrecisionModel(), 5186);

    // 위도, 경도를 사용하여 Point 객체를 생성 5186
    public Point createPoint(double longitude, double latitude) {
        Point point = geometryFactory5186.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(5186);
        return point;
    }

    // 좌표계변경
    public Point transform(Point point, int targetSrid) throws ParseException {
        if (point.getSRID() == targetSrid) {
            return point;
        }
        String wkt = coordinateTransformRepository.transform(point, targetSrid);
        WKTReader reader = (targetSrid == 5186) ? new WKTReader(geometryFactory5186) : new WKTReader(geometryFactory);
        Point transformedPoint = (Point) reader.read(wkt);
        transformedPoint.setSRID(targetSrid);
        return transformedPoint;
    }

    public Point transformTo4326(Point point) throws ParseException {
        return transform(point, 4326);
    }

    public Point transformTo5186(Point point) throws ParseException {
        return transform(point, 5186);
    }

    // 좌표가 속한 행정구역(읍면동)의 이름을 반환
    @Transactional(readOnly = true)
    public String getEmdNameFromCoordinates(double longitude, double latitude) {
        Point point = createPoint(longitude, latitude);
        // 5186 좌표 반환
        Optional<KoreaEmdBoundary> boundaryOptional = koreaEmdBoundaryRepository.findBoundaryContainingPoint(point);

        return boundaryOptional.map(KoreaEmdBoundary::getEmdNm).orElse(null);
    }

    // WKT(Well-Known Text) 형식의 문자열을 Geometry 객체로 변환
    public Point createPointFromWKT(String wktPoint) throws ParseException {
        WKTReader reader = new WKTReader(geometryFactory5186);
        Point point = (Point) reader.read(wktPoint);
        point.setSRID(5186);
        return point;
    }
}