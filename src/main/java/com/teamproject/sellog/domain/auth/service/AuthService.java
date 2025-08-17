package com.teamproject.sellog.domain.auth.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.domain.auth.model.dto.request.UserLoginDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserRegisterDto;
import com.teamproject.sellog.domain.auth.model.dto.response.UserLoginResponse;
import com.teamproject.sellog.domain.auth.model.jwt.JWT;
import com.teamproject.sellog.domain.auth.model.jwt.JwtProvider;
import com.teamproject.sellog.domain.auth.model.jwt.PasswordHasher;
import com.teamproject.sellog.domain.auth.repository.AuthRepository;
import com.teamproject.sellog.domain.auth.service.event.UserCreatedEvent;
import com.teamproject.sellog.domain.user.model.entity.user.AccountStatus;
import com.teamproject.sellog.domain.user.model.entity.user.AccountVisibility;
import com.teamproject.sellog.domain.user.model.entity.user.Role;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.model.entity.user.UserPrivate;
import com.teamproject.sellog.domain.user.model.entity.user.UserProfile;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Service
public class AuthService {
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    private final ApplicationEventPublisher eventPublisher;

    private final long refreshTokenValidityInMilliseconds; // 리프레시 토큰 유효 기간

    public AuthService(AuthRepository authRepository, JwtProvider jwtProvider, RedisService redisService,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenValidityInMilliseconds,
            ApplicationEventPublisher eventPublisher) {
        this.authRepository = authRepository;
        this.jwtProvider = jwtProvider;
        this.redisService = redisService;

        this.eventPublisher = eventPublisher;

        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
    }

    private static final String REFRESH_TOKEN_BLACKLIST_PREFIX = "refreshToken:blacklist:";

    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "accessToken:blacklist:";

    private static final String REFRESH_TOKEN_WHITELIST_PREFIX = "refreshToken:whitelist:";

    private static final String USERID_PREFIX = "userId:";

    @Transactional
    public User registerUser(UserRegisterDto userRegisterDto) {
        try {

            String salt = PasswordHasher.generateSalt();
            String hashedPassword = PasswordHasher.hashPassword(userRegisterDto.getPassword(), salt);

            User newUser = new User();
            newUser.setUserId(userRegisterDto.getUserId());
            newUser.setPasswordHash(hashedPassword);
            newUser.setPasswordSalt(salt);
            newUser.setEmail(userRegisterDto.getEmail());
            newUser.setRole(Role.USER); // 기본 역할 설정
            newUser.setAccountStatus(AccountStatus.STAY); // 기본 계정 상태 설정
            newUser.setLastLogin(Timestamp.valueOf(LocalDateTime.now()));
            newUser.setAccountVisibility(AccountVisibility.PUBLIC); // 기본 가시성 설정

            UserPrivate userInfoPrivate = new UserPrivate();
            userInfoPrivate.setUserName(userRegisterDto.getName());

            UserProfile userInfoProfile = new UserProfile();
            userInfoProfile.setNickname(userRegisterDto.getNickname());
            userInfoProfile.setProfileURL(
                    "https://sellogstorage.blob.core.windows.net/outcontents/Default/origin/307ce493-b254-4b2d-8ba4-d12c080d6651.jpg");
            userInfoProfile.setProfileThumbURL(
                    "https://sellogstorage.blob.core.windows.net/outcontents/Default/thumbnails/profile/307ce493-b254-4b2d-8ba4-d12c080d6651.webp");

            newUser.setUserPrivate(userInfoPrivate);
            newUser.setUserProfile(userInfoProfile);

            eventPublisher.publishEvent(new UserCreatedEvent(this, newUser));
            return authRepository.save(newUser);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UserLoginResponse loginUser(UserLoginDto userLoginDto) {
        // 사용자 ID로 사용자 정보 조회
        User user = authRepository.findByUserId(userLoginDto.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)); // 사용자가 없으면 예외 발생

        // 비밀번호 검증
        boolean passwordMatches = PasswordHasher.verifyPassword(
                userLoginDto.getPassword(),
                user.getPasswordHash(),
                user.getPasswordSalt());

        if (!passwordMatches) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD); // 비밀번호 불일치 시 예외 발생
        }
        // JWT 토큰 생성
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId()); // 사용자 ID 클레임 추가
        claims.put("role", user.getRole()); // 역할 클레임 추가

        user.setLastLogin(Timestamp.valueOf(LocalDateTime.now())); // 마지막 로그인 업데이트
        JWT jwt = jwtProvider.createJWT(claims);

        redisService.setValue(USERID_PREFIX + user.getUserId() + REFRESH_TOKEN_WHITELIST_PREFIX,
                jwt.getRefreshToken(),
                refreshTokenValidityInMilliseconds, TimeUnit.MILLISECONDS);

        String profileThumnail = user.getUserProfile().getProfileThumbURL();
        UserLoginResponse response = new UserLoginResponse(jwt.getAccessToken(), jwt.getRefreshToken(),
                profileThumnail);
        return response;
    }

    // -------------------수정중-------------
    @Transactional
    public void logoutUser(String accessToken, String refreshToken) {
        // 1. 액세스 토큰 유효성 검증 (만료 여부는 검증하지 않음)
        Claims accessClaims;
        try {
            accessClaims = jwtProvider.getClaims(accessToken);
        } catch (JwtException e) {
            // 이미 만료된 토큰이거나 유효하지 않은 토큰은 무시하거나 로깅
            System.err.println("Logout failed for invalid access token: " + e.getMessage());
            // return; // 이미 유효하지 않은 토큰은 블랙리스트에 추가할 필요 없음
            accessClaims = null; // 클레임 가져오기 실패 시 null 처리
        }

        // 2. 액세스 토큰을 블랙리스트에 추가 (남은 유효 시간 동안)
        if (accessClaims != null) {
            Date expiration = accessClaims.getExpiration();
            if (expiration != null) {
                long remainingTime = expiration.getTime() - System.currentTimeMillis();
                if (remainingTime > 0) {
                    redisService.setValue(ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken, "logout", remainingTime,
                            TimeUnit.MILLISECONDS);
                }
            }
        }

        // 3. 리프레시 토큰을 블랙리스트에 추가 (리프레시 토큰의 유효 시간 동안)
        Claims refreshClaims;
        try {
            refreshClaims = jwtProvider.getClaims(refreshToken);
        } catch (JwtException e) {
            // 이미 만료된 토큰이거나 유효하지 않은 토큰은 무시하거나 로깅
            System.err.println("Logout failed for invalid refresh token: " + e.getMessage());
            // return; // 이미 유효하지 않은 토큰은 블랙리스트에 추가할 필요 없음
            refreshClaims = null; // 클레임 가져오기 실패 시 null 처리
        }

        if (refreshClaims != null) {
            Date expiration = refreshClaims.getExpiration();
            if (expiration != null) {
                long remainingTime = expiration.getTime() - System.currentTimeMillis();
                if (remainingTime > 0) {
                    redisService.deleteValue(
                            USERID_PREFIX + refreshClaims.get("userId", String.class) + REFRESH_TOKEN_WHITELIST_PREFIX);
                    redisService.setValue(REFRESH_TOKEN_BLACKLIST_PREFIX + refreshToken, "logout", remainingTime,
                            TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    // 회원 탈퇴 처리
    @Transactional
    public void deleteUser(String userId, String password, String accessToken, String refreshToken) {
        // 1. 사용자 ID로 사용자 정보 조회
        User user = authRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 비밀번호 검증
        boolean passwordMatches = PasswordHasher.verifyPassword(
                password,
                user.getPasswordHash(),
                user.getPasswordSalt());

        if (!passwordMatches) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 3. 현재 사용 중인 액세스 토큰 및 리프레시 토큰 무효화 (블랙리스트 추가)
        logoutUser(accessToken, refreshToken); // 로그아웃 로직 재활용

        authRepository.delete(user); // User 테이블 데이터 삭제
    }

    // 사용자 ID로 사용자 정보 조회 (토큰 검증 후 사용될 수 있음)
    @Transactional(readOnly = true)
    public Optional<User> findByUserId(String userId) {
        return authRepository.findByUserId(userId);
    }

    // 액세스 토큰이 블랙리스트에 있는지 확인
    public boolean isAccessTokenBlacklisted(String token) {
        return redisService.hasKey(ACCESS_TOKEN_BLACKLIST_PREFIX + token);
    }

    // 리프레시 토큰이 블랙리스트에 있는지 확인
    public boolean isRefreshTokenBlacklisted(String token) {
        return redisService.hasKey(REFRESH_TOKEN_BLACKLIST_PREFIX + token);
    }

    @Transactional
    public JWT refreshToken(String refreshToken) {
        // 1. 리프레시 토큰 유효성 검증 (서명, 만료 시간 등)
        Claims claims;
        try {
            claims = jwtProvider.getClaims(refreshToken); // 유효성 검증 및 클레임 추출
        } catch (JwtException e) {
            // 토큰이 유효하지 않거나 만료된 경우
            throw new BusinessException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
        }

        // 2. 리프레시 토큰이 블랙리스트에 있는지 확인
        if (isRefreshTokenBlacklisted(refreshToken)) {
            // 이미 사용되었거나 무효화된 토큰인 경우
            throw new BusinessException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
        }
        // 3. 토큰에서 사용자 ID 추출
        String userId = claims.get("userId", String.class);

        if (!redisService.getValue(USERID_PREFIX + userId + REFRESH_TOKEN_WHITELIST_PREFIX).equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
        }
        // 4. 사용자 정보 조회
        User user = authRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 5. 사용자 계정 상태 확인
        if (AccountStatus.INACTIVE.equals(user.getAccountStatus())) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }

        // 6. 기존 리프레시 토큰을 블랙리스트에 추가 (재사용 방지)
        // 기존 토큰의 남은 유효 시간 동안 블랙리스트에 유지
        Date expiration = claims.getExpiration();
        if (expiration != null) {
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            if (remainingTime > 0) {
                redisService.setValue(REFRESH_TOKEN_BLACKLIST_PREFIX + refreshToken, "used", remainingTime,
                        TimeUnit.MILLISECONDS);
            }
        }

        // 7. 새로운 액세스 토큰 및 리프레시 토큰 생성
        Map<String, Object> Claims = new HashMap<>();
        Claims.put("userId", user.getUserId());
        Claims.put("role", user.getRole());
        user.setLastLogin(Timestamp.valueOf(LocalDateTime.now())); // 마지막 로그인 업데이트
        JWT jwt = jwtProvider.createJWT(Claims);
        redisService.setValue(
                USERID_PREFIX + userId + REFRESH_TOKEN_WHITELIST_PREFIX,
                jwt.getRefreshToken(), refreshTokenValidityInMilliseconds, TimeUnit.MILLISECONDS);
        return jwt;
    }

    @Transactional(readOnly = true)
    public String findUserId(String email) {
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getUserId();
    }

    @Transactional
    public void changePassword(String userId, String email, String password) {
        User user = authRepository.findByUserIdAndEmail(userId, email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user != null) {
            String salt = user.getPasswordSalt();
            String newPassword = PasswordHasher.hashPassword(password, salt);
            user.setPasswordHash(newPassword);
        }
    }

    @Transactional(readOnly = true)
    public boolean checkId(String userId) {
        return authRepository.existsByUserId(userId);
    }
}
