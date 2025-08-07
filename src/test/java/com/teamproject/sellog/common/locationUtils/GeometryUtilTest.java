package com.teamproject.sellog.common.locationUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GeometryUtil 단위 테스트")
class GeometryUtilTest {

    // 오차 허용 범위: 부동 소수점 계산의 정확성을 위해 사용 (1cm 이내)
    private static final double EPSILON = 0.00001; // 0.01미터 = 1cm 정도의 오차 허용

    // 테스트 기준점: 서울 시청 (대략적인 위치)
    private final double SEOUL_LATITUDE = 37.5665;
    private final double SEOUL_LONGITUDE = 126.9780;

    @Test
    @DisplayName("1. toRadian: 도(Degree)를 라디안(Radian)으로 변환")
    void testToRadian() {
        assertEquals(0.0, GeometryUtil.toRadian(0.0), EPSILON);
        assertEquals(Math.PI / 2, GeometryUtil.toRadian(90.0), EPSILON); // 90도는 PI/2 라디안
        assertEquals(Math.PI, GeometryUtil.toRadian(180.0), EPSILON); // 180도는 PI 라디안
        assertEquals(2 * Math.PI, GeometryUtil.toRadian(360.0), EPSILON); // 360도는 2PI 라디안
    }

    @Test
    @DisplayName("2. toDegree: 라디안(Radian)을 도(Degree)로 변환")
    void testToDegree() {
        assertEquals(0.0, GeometryUtil.toDegree(0.0), EPSILON);
        assertEquals(90.0, GeometryUtil.toDegree(Math.PI / 2), EPSILON);
        assertEquals(180.0, GeometryUtil.toDegree(Math.PI), EPSILON);
        assertEquals(360.0, GeometryUtil.toDegree(2 * Math.PI), EPSILON);
    }

    @Test
    @DisplayName("3. normalizeLongitude: 경도 값 정규화 (-180 ~ 180)")
    void testNormalizeLongitude() {
        assertEquals(0.0, GeometryUtil.normalizeLongitude(0.0), EPSILON);
        assertEquals(-180.0, GeometryUtil.normalizeLongitude(180.0), EPSILON);
        assertEquals(-180.0, GeometryUtil.normalizeLongitude(-180.0), EPSILON);
        assertEquals(-90.0, GeometryUtil.normalizeLongitude(270.0), EPSILON); // 270도는 -90도와 동일
        assertEquals(90.0, GeometryUtil.normalizeLongitude(-270.0), EPSILON); // -270도는 90도와 동일
        assertEquals(-179.9, GeometryUtil.normalizeLongitude(180.1), EPSILON);
        assertEquals(179.9, GeometryUtil.normalizeLongitude(-180.1), EPSILON);
    }

    @Test
    @DisplayName("4. calculateDistance: 동일 지점 간 거리 계산")
    void testCalculateDistance_SameLocation() {
        double distance = GeometryUtil.calculateDistance(SEOUL_LATITUDE, SEOUL_LONGITUDE,
                SEOUL_LATITUDE, SEOUL_LONGITUDE);
        assertEquals(0.0, distance, EPSILON); // 동일 지점 간 거리는 0m
    }

    @Test
    @DisplayName("5. calculateDistance: 약 1km 거리 계산 (북쪽)")
    void testCalculateDistance_Around1KmNorth() {
        // 서울 시청에서 약 1km 북쪽 지점 (위도 1도 = 약 111.32km이므로 1km = 1/111.32도)
        double northLat = SEOUL_LATITUDE + (1.0 / 111.32);
        double northLon = SEOUL_LONGITUDE;
        double distance = GeometryUtil.calculateDistance(SEOUL_LATITUDE, SEOUL_LONGITUDE,
                northLat, northLon);
        assertEquals(1000.0, distance, 10.0); // 1000m 예상, 약간의 오차 허용 (10m)
    }

    @Test
    @DisplayName("6. calculateDistance: 다른 두 도시 간 거리 계산 (서울-부산 대략)")
    void testCalculateDistance_SeoulToBusan() {
        double busanLat = 35.1796;
        double busanLon = 129.0756;
        double expectedDistanceKm = 325.0; // km 단위, 실제 거리는 약 325km 정도

        // GeometryUtil은 미터 단위로 반환하므로 km를 m로 변환하여 예상값 지정
        double distance = GeometryUtil.calculateDistance(SEOUL_LATITUDE, SEOUL_LONGITUDE, busanLat, busanLon);
        assertEquals(expectedDistanceKm * 1000, distance, 2000.0); // 2km 오차 허용 (대략적인 값)
    }

    @Test
    @DisplayName("7. calculateByDirection: 기준점으로부터 북쪽으로 1km 이동")
    void testCalculateByDirection_1KmNorth() {
        Location newLoc = GeometryUtil.calculateByDirection(SEOUL_LATITUDE, SEOUL_LONGITUDE, 1.0,
                Direction.NORTH.getBearing());

        assertNotNull(newLoc);
        // 북쪽으로 이동했으니 위도 증가, 경도 동일
        assertTrue(newLoc.getLatitude() > SEOUL_LATITUDE);
        assertEquals(SEOUL_LONGITUDE, newLoc.getLongitude(), EPSILON);

        // 새로운 지점과 원래 지점 간의 거리가 1km(1000m)인지 확인
        double distance = GeometryUtil.calculateDistance(SEOUL_LATITUDE, SEOUL_LONGITUDE, newLoc.getLatitude(),
                newLoc.getLongitude());
        assertEquals(1000.0, distance, 10.0); // 10m 오차 허용

        System.out.println("\n--- [GeometryUtilTest] 1km 북쪽 지점 ---");
        System.out.printf("계산된 좌표: 위도 %.6f, 경도 %.6f%n", newLoc.getLatitude(), newLoc.getLongitude());
    }

    @Test
    @DisplayName("8. calculateByDirection: 기준점으로부터 동쪽으로 1km 이동")
    void testCalculateByDirection_1KmEast() {
        Location newLoc = GeometryUtil.calculateByDirection(SEOUL_LATITUDE, SEOUL_LONGITUDE, 1.0,
                Direction.EAST.getBearing());

        assertNotNull(newLoc);
        // 동쪽으로 이동했으니 위도 동일, 경도 증가
        assertEquals(SEOUL_LATITUDE, newLoc.getLatitude(), EPSILON);
        assertTrue(newLoc.getLongitude() > SEOUL_LONGITUDE);

        double distance = GeometryUtil.calculateDistance(SEOUL_LATITUDE, SEOUL_LONGITUDE, newLoc.getLatitude(),
                newLoc.getLongitude());
        assertEquals(1000.0, distance, 10.0);

        System.out.println("\n--- [GeometryUtilTest] 1km 동쪽 지점 ---");
        System.out.printf("계산된 좌표: 위도 %.6f, 경도 %.6f%n", newLoc.getLatitude(), newLoc.getLongitude());
    }

    @Test
    @DisplayName("9. calculateByDirection: 기준점으로부터 남서쪽으로 0.5km 이동")
    void testCalculateByDirection_0_5KmSouthwest() {
        Location newLoc = GeometryUtil.calculateByDirection(SEOUL_LATITUDE, SEOUL_LONGITUDE, 0.5,
                Direction.SOUTHWEST.getBearing());

        assertNotNull(newLoc);
        // 남서쪽으로 이동했으니 위도 감소, 경도 감소
        assertTrue(newLoc.getLatitude() < SEOUL_LATITUDE);
        assertTrue(newLoc.getLongitude() < SEOUL_LONGITUDE);

        double distance = GeometryUtil.calculateDistance(SEOUL_LATITUDE, SEOUL_LONGITUDE, newLoc.getLatitude(),
                newLoc.getLongitude());
        assertEquals(500.0, distance, 10.0); // 10m 오차 허용

        System.out.println("\n--- [GeometryUtilTest] 0.5km 남서쪽 지점 ---");
        System.out.printf("계산된 좌표: 위도 %.6f, 경도 %.6f%n", newLoc.getLatitude(), newLoc.getLongitude());
    }
}