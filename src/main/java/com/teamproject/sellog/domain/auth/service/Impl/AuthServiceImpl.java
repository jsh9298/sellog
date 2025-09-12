package com.teamproject.sellog.domain.auth.service.Impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.teamproject.sellog.common.emailUtils.EmailSendDto;
import com.teamproject.sellog.common.emailUtils.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.domain.auth.model.dto.request.UserOtpRequestDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserOtpVerifyDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserLoginDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserRegisterDto;
import com.teamproject.sellog.domain.auth.model.dto.response.UserLoginResponse;
import com.teamproject.sellog.domain.auth.model.jwt.JWT;
import com.teamproject.sellog.domain.auth.model.jwt.JwtProvider;
import com.teamproject.sellog.domain.auth.model.jwt.PasswordHasher;
import com.teamproject.sellog.domain.auth.repository.AuthRepository;
import com.teamproject.sellog.domain.auth.service.AuthService;
import com.teamproject.sellog.domain.auth.service.RedisService;
import com.teamproject.sellog.domain.auth.service.event.UserCreatedEvent;
import com.teamproject.sellog.domain.auth.service.event.UserDeletedEvent;
import com.teamproject.sellog.domain.user.model.entity.user.AccountStatus;
import com.teamproject.sellog.domain.user.model.entity.user.AccountVisibility;
import com.teamproject.sellog.domain.user.model.entity.user.Role;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.model.entity.user.UserPrivate;
import com.teamproject.sellog.domain.user.model.entity.user.UserProfile;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final EmailService emailService;

    private final ApplicationEventPublisher eventPublisher;

    private final long refreshTokenValidityInMilliseconds; // 리프레시 토큰 유효 기간

    @Value("${user.default-profile-url}")
    private String defaultProfileUrl;

    @Value("${user.default-profile-thumb-url}")
    private String defaultProfileThumbUrl;

    public AuthServiceImpl(AuthRepository authRepository, JwtProvider jwtProvider, RedisService redisService,
            EmailService emailService,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenValidityInMilliseconds,
            ApplicationEventPublisher eventPublisher) {
        this.authRepository = authRepository;
        this.jwtProvider = jwtProvider;
        this.redisService = redisService;
        this.emailService = emailService;

        this.eventPublisher = eventPublisher;

        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
    }

    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "accessToken:blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";
    private static final String OTP_PREFIX = "otp:";
    private static final long OTP_EXPIRATION_MINUTES = 5;
    private static final String OTP_VERIFIED_PREFIX = "otp:verified:";
    private static final long OTP_VERIFIED_EXPIRATION_MINUTES = 10;

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
            userInfoProfile.setProfileURL(defaultProfileUrl);
            userInfoProfile.setProfileThumbURL(defaultProfileThumbUrl);

            newUser.setUserPrivate(userInfoPrivate);
            newUser.setUserProfile(userInfoProfile);

            User savedUser = authRepository.save(newUser);
            eventPublisher.publishEvent(new UserCreatedEvent(this, savedUser.getId()));
            return savedUser;
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

        redisService.setValue(REFRESH_TOKEN_PREFIX + user.getUserId(),
                jwt.getRefreshToken(),
                refreshTokenValidityInMilliseconds, TimeUnit.MILLISECONDS);

        String profileThumnail = user.getUserProfile().getProfileThumbURL();
        UserLoginResponse response = new UserLoginResponse(jwt.getAccessToken(), jwt.getRefreshToken(),
                profileThumnail);
        return response;
    }

    @Transactional
    public void logoutUser(String accessToken, String refreshToken) {
        // 1. Access Token 블랙리스트에 추가
        try {
            Claims accessClaims = jwtProvider.getClaims(accessToken);
            long remainingTime = accessClaims.getExpiration().getTime() - System.currentTimeMillis();
            if (remainingTime > 0) {
                redisService.setValue(ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken, "logout", remainingTime,
                        TimeUnit.MILLISECONDS);
            }
        } catch (JwtException e) {
            // 만료되었거나 유효하지 않은 Access Token은 블랙리스트에 추가할 필요 없음
            log.warn("Logout with invalid access token: {}", e.getMessage());
        }

        // 2. Refresh Token 화이트리스트에서 삭제
        try {
            Claims refreshClaims = jwtProvider.getClaims(refreshToken);
            String userId = refreshClaims.get("userId", String.class);
            redisService.deleteValue(REFRESH_TOKEN_PREFIX + userId);
        } catch (JwtException e) {
            // 만료되었거나 유효하지 않은 Refresh Token은 이미 화이트리스트에 없거나 의미 없음
            log.warn("Logout with invalid refresh token: {}", e.getMessage());
        }
    }

    // 회원 탈퇴 처리
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
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
        eventPublisher.publishEvent(new UserDeletedEvent(this, user.getId()));

        // [개선] 물리적 삭제(Hard Delete) 대신 논리적 삭제(Soft Delete) 수행
        user.setAccountStatus(AccountStatus.DELETED); // 계정 상태를 '삭제됨'으로 변경
        // authRepository.delete(user); // User 테이블 데이터 삭제 로직 제거
    }

    // 사용자 ID로 사용자 정보 조회 (토큰 검증 후 사용될 수 있음)
    @Transactional(readOnly = true)
    public Optional<User> findByUserId(String userId) {
        return authRepository.findByUserId(userId);
    }

    // UserProfile과 UserPrivate을 함께 조회하는 메서드 추가
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public Optional<User> findByUserIdWithDetails(String userId) {
        return authRepository.findUserWithDetailsByUserId(userId);
    }

    // 액세스 토큰이 블랙리스트에 있는지 확인
    public boolean isAccessTokenBlacklisted(String token) {
        return redisService.hasKey(ACCESS_TOKEN_BLACKLIST_PREFIX + token);
    }

    @Override
    @Transactional
    public JWT refreshToken(String accessToken, String refreshToken) {
        try {
            // 1. Refresh Token이 유효한지 먼저 확인
            if (refreshToken != null && !refreshToken.isEmpty()) {
                Claims claims = jwtProvider.getClaims(refreshToken);
                String userId = claims.get("userId", String.class);
                String storedToken = (String) redisService.getValue(REFRESH_TOKEN_PREFIX + userId);

                // 1-1. Refresh Token이 화이트리스트에 존재하면 Access Token만 갱신
                if (refreshToken.equals(storedToken)) {
                    return refreshAccessTokenOnly(userId, accessToken);
                }
            }

            // 2. Refresh Token이 유효하지 않거나 없는 경우, Access Token으로 시도
            if (accessToken != null && !accessToken.isEmpty()) {
                // Access Token의 유효성(만료, 서명)은 getClaims에서 확인됨
                if (!isAccessTokenBlacklisted(accessToken)) {
                    // 2-1. Access Token이 유효하면 두 토큰 모두 재발급
                    return refreshBothTokens(accessToken);
                }
            }
        } catch (JwtException e) {
            // 모든 토큰 유효성 검증(getClaims) 과정에서 예외 발생 시, 최종 실패 처리로 이어짐
            log.warn("Token refresh failed due to invalid token: {}", e.getMessage());
        }

        // 3. 두 토큰 모두 유효하지 않은 경우
        throw new BusinessException(ErrorCode.INVALID_OR_EXPIRED_TOKEN, "모든 토큰이 유효하지 않습니다. 다시 로그인해주세요.");
    }

    private JWT refreshBothTokens(String accessToken) {
        Claims claims;
        try {
            claims = jwtProvider.getClaims(accessToken);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
        }
        String userId = claims.get("userId", String.class);
        User user = authRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (AccountStatus.INACTIVE.equals(user.getAccountStatus())) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }

        // 기존 Access Token 블랙리스트 처리
        long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (remainingTime > 0) {
            redisService.setValue(ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken, "refreshed", remainingTime,
                    TimeUnit.MILLISECONDS);
        }

        // 5. 새로운 토큰 생성 및 화이트리스트 갱신
        Map<String, Object> Claims = new HashMap<>();
        Claims.put("userId", user.getUserId());
        Claims.put("role", user.getRole());
        user.setLastLogin(Timestamp.valueOf(LocalDateTime.now())); // 마지막 로그인 업데이트
        JWT jwt = jwtProvider.createJWT(Claims);
        redisService.setValue(REFRESH_TOKEN_PREFIX + userId,
                jwt.getRefreshToken(), refreshTokenValidityInMilliseconds, TimeUnit.MILLISECONDS);
        return jwt;
    }

    private JWT refreshAccessTokenOnly(String userId, String oldAccessToken) {
        User user = authRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (AccountStatus.INACTIVE.equals(user.getAccountStatus())) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }

        // 기존 Access Token 블랙리스트 처리
        if (oldAccessToken != null && !oldAccessToken.isEmpty()) {
            try {
                Claims oldClaims = jwtProvider.getClaims(oldAccessToken);
                long remainingTime = oldClaims.getExpiration().getTime() - System.currentTimeMillis();
                if (remainingTime > 0) {
                    redisService.setValue(ACCESS_TOKEN_BLACKLIST_PREFIX + oldAccessToken, "refreshed", remainingTime,
                            TimeUnit.MILLISECONDS);
                }
            } catch (JwtException e) {
                log.warn("Could not blacklist old access token: {}", e.getMessage());
            }
        }
        return jwtProvider.createAccessToken(userId, user.getRole().toString());
    }

    @Transactional(readOnly = true)
    public String findUserId(String email) {
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getUserId();
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void changePassword(String userId, String password) {
        // 1. OTP 인증이 완료되었는지 확인
        String verifiedKey = OTP_VERIFIED_PREFIX + userId;
        if (!redisService.hasKey(verifiedKey)) {
            throw new BusinessException(ErrorCode.OTP_NOT_VERIFIED);
        }

        // 2. 사용자 조회 및 비밀번호 변경
        User user = authRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String salt = user.getPasswordSalt();
        String newPasswordHash = PasswordHasher.hashPassword(password, salt);
        user.setPasswordHash(newPasswordHash);

        // 3. 인증 완료 플래그 삭제 (재사용 방지)
        redisService.deleteValue(verifiedKey);
    }

    @Transactional(readOnly = true)
    public boolean checkId(String userId) {
        return authRepository.existsByUserId(userId);
    }

    @Override
    @Transactional
    public void sendOtpForPasswordReset(UserOtpRequestDto dto) {
        User user = authRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.getEmail().equals(dto.getEmail())) {
            throw new BusinessException(ErrorCode.USER_EMAIL_MISMATCH);
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        redisService.setValue(OTP_PREFIX + dto.getUserId(), otp, OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        EmailSendDto emailDto = new EmailSendDto(dto.getEmail(), "[Sellog] 비밀번호 재설정 인증번호", "인증번호: " + otp);
        emailService.sendEmail(emailDto);
    }

    @Override
    public boolean verifyOtp(UserOtpVerifyDto dto) {
        String storedOtp = (String) redisService.getValue(OTP_PREFIX + dto.getUserId());
        if (storedOtp == null || !storedOtp.equals(dto.getOtp())) {
            throw new BusinessException(ErrorCode.OTP_VERIFICATION_FAILED);
        }
        redisService.setValue(OTP_VERIFIED_PREFIX + dto.getUserId(), "true", OTP_VERIFIED_EXPIRATION_MINUTES,
                TimeUnit.MINUTES);
        redisService.deleteValue(OTP_PREFIX + dto.getUserId()); // 사용된 OTP 삭제
        return true;
    }
}
