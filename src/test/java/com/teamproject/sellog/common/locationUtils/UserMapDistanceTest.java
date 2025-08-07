package com.teamproject.sellog.common.locationUtils;

import com.teamproject.sellog.domain.post.model.entity.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows; // 이전에 추가한 assertThrows import
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("UserMapDistance 단위 테스트")
class UserMapDistanceTest {

    private static final double EPSILON = 0.00001; // 오차 허용 범위
    private static final double DISTANCE_TOLERANCE_METERS = 10.0; // 거리 오차 허용 (10m)

    // 테스트 기준점: 서울 시청
    private final Location SEOUL_CITY_HALL = new Location(37.5665, 126.9780);

    @Test
    @DisplayName("1. aroundCustomerNortheastDot: 북동쪽 경계 좌표 계산 (1km)")
    void testAroundCustomerNortheastDot_1km() {
        double searchDistance = 1.0; // 1km
        Location northeastDot = UserMapDistance.aroundCustomerNortheastDot(SEOUL_CITY_HALL, searchDistance);

        assertNotNull(northeastDot);
        // 계산된 점이 기준점보다 위도와 경도가 모두 크거나 같은지 확인 (북동쪽이므로)
        assertTrue(northeastDot.getLatitude() > SEOUL_CITY_HALL.getLatitude());
        assertTrue(northeastDot.getLongitude() > SEOUL_CITY_HALL.getLongitude());

        // *************** 수정된 부분 ***************
        // northeastDot은 기준점에서 북동쪽 45도 방향으로 정확히 searchDistance(1km)만큼 떨어진 지점이어야 합니다.
        // 따라서 계산된 거리도 searchDistance와 동일해야 합니다 (미터 단위이므로 * 1000).
        double distanceToOrigin = GeometryUtil.calculateDistance(northeastDot.getLatitude(),
                northeastDot.getLongitude(),
                SEOUL_CITY_HALL.getLatitude(), SEOUL_CITY_HALL.getLongitude());
        assertEquals(searchDistance * 1000, distanceToOrigin, DISTANCE_TOLERANCE_METERS); // 1km = 1000m
        // ********************************************
        System.out.println("\n--- [UserMapDistanceTest] 북동쪽 경계 좌표 (1km) ---");
        System.out.printf("계산된 좌표: 위도 %.6f, 경도 %.6f%n", northeastDot.getLatitude(), northeastDot.getLongitude());
    }

    @Test
    @DisplayName("2. aroundCustomerSouthwestDot: 남서쪽 경계 좌표 계산 (0.5km)")
    void testAroundCustomerSouthwestDot_0_5km() {
        double searchDistance = 0.5; // 0.5km
        Location southwestDot = UserMapDistance.aroundCustomerSouthwestDot(SEOUL_CITY_HALL, searchDistance);

        assertNotNull(southwestDot);
        // 계산된 점이 기준점보다 위도와 경도가 모두 작거나 같은지 확인 (남서쪽이므로)
        assertTrue(southwestDot.getLatitude() < SEOUL_CITY_HALL.getLatitude());
        assertTrue(southwestDot.getLongitude() < SEOUL_CITY_HALL.getLongitude());

        // *************** 수정된 부분 ***************
        // southwestDot은 기준점에서 남서쪽 225도 방향으로 정확히 searchDistance(0.5km)만큼 떨어진 지점이어야 합니다.
        // 따라서 계산된 거리도 searchDistance와 동일해야 합니다 (미터 단위이므로 * 1000).
        double distanceToOrigin = GeometryUtil.calculateDistance(southwestDot.getLatitude(),
                southwestDot.getLongitude(),
                SEOUL_CITY_HALL.getLatitude(), SEOUL_CITY_HALL.getLongitude());
        assertEquals(searchDistance * 1000, distanceToOrigin, DISTANCE_TOLERANCE_METERS); // 0.5km = 500m
        // ********************************************
        System.out.println("\n--- [UserMapDistanceTest] 남서쪽 경계 좌표 (0.5km) ---");
        System.out.printf("계산된 좌표: 위도 %.6f, 경도 %.6f%n", southwestDot.getLatitude(), southwestDot.getLongitude());
    }

    @Test
    @DisplayName("3. calculateDistance: 사용자 위치와 게시글 위치 간 거리 계산 - 1km 떨어진 게시글")
    void testCalculateDistance_Post1KmAway() {
        // 사용자: 서울 시청
        // 게시글: 서울 시청에서 약 1km 북쪽 (대략적인 위도 증분 사용)
        Location postLocation = new Location(SEOUL_CITY_HALL.getLatitude() + (1.0 / 111.32),
                SEOUL_CITY_HALL.getLongitude());

        Post mockPost = mock(Post.class); // Post 엔티티 Mock
        when(mockPost.getLocation()).thenReturn(postLocation);

        double distance = UserMapDistance.calculateDistance(SEOUL_CITY_HALL, mockPost);
        assertEquals(1000.0, distance, 10.0); // 약 1km (10m 오차 허용)
    }

    @Test
    @DisplayName("4. calculateDistance: 사용자 위치와 게시글 위치 간 거리 계산 - 동일 지점")
    void testCalculateDistance_SamePostLocation() {
        Post mockPost = mock(Post.class);
        when(mockPost.getLocation()).thenReturn(SEOUL_CITY_HALL);

        double distance = UserMapDistance.calculateDistance(SEOUL_CITY_HALL, mockPost);
        assertEquals(0.0, distance, EPSILON); // 동일 지점 간 거리는 0m
    }

    @Test
    @DisplayName("5. calculateDistance: 게시글 위치 정보가 null일 경우 - 예외 처리 또는 0 반환 예상")
    void testCalculateDistance_NullPostLocation() {
        Post mockPost = mock(Post.class);
        when(mockPost.getLocation()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> {
            UserMapDistance.calculateDistance(SEOUL_CITY_HALL, mockPost);
        }, "post.getLocation()이 null이면 NullPointerException이 발생해야 합니다.");
    }
}