package com.teamproject.sellog.domain.auth.model.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

//JWT의 생성 담당
@Component
public class JwtProvider {

    private final SecretKey key; // JWT 서명에 사용할 SecretKey
    private final long accessTokenValidityInMilliseconds; // 액세스 토큰 유효 기간
    private final long refreshTokenValidityInMilliseconds; // 리프레시 토큰 유효 기간

    // 생성자를 통해 @Value로 설정 값 주입
    public JwtProvider(
            @Value("${jwt.secret}") String secret, // application.properties의 jwt.secret 값 주입
            @Value("${jwt.access-token-expiration-ms}") long accessTokenValidityInMilliseconds, // 액세스 토큰 유효 기간 주입
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenValidityInMilliseconds) { // 리프레시 토큰 유효 기간 주입
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // 주입받은 secret으로 key 생성
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
    }

    public String createToken(Map<String, Object> claims, Date expireDate) {
        JwtBuilder builder = Jwts.builder();

        // claims 맵의 각 엔트리를 페이로드에 직접 추가
        if (claims != null) {
            claims.forEach(builder::claim);
        }

        return builder
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key)
                .compact();
    }

    public Claims getClaims(String token) {

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰 파싱 또는 검증 실패 시 예외 처리
            // 예: 만료된 토큰, 잘못된 서명 등
            System.err.println("Invalid JWT token: " + e.getMessage());
            throw new JwtException("Invalid or expired JWT token");
        }

    }

    public JWT createJWT(Map<String, Object> claims) {
        String accessToken = createToken(claims, getExpireDateAccessToken());
        String refreshToken = createToken(claims, getExpireDateRefreshToken());

        return JWT.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    public JWT createAccessToken(String userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        String accessToken = createToken(claims, getExpireDateAccessToken());
        // AccessToken만 생성하므로 RefreshToken은 null
        return JWT.builder().accessToken(accessToken).refreshToken(null).build();
    }

    public Date getExpireDateAccessToken() {
        long expireTimeMils = accessTokenValidityInMilliseconds; // 별도의 환경변수로 대체 필요
        return new Date(System.currentTimeMillis() + expireTimeMils);
    }

    public Date getExpireDateRefreshToken() {
        long expireTimeMils = refreshTokenValidityInMilliseconds; // 별도의 환경변수로 대체 필요
        return new Date(System.currentTimeMillis() + expireTimeMils);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT validation failed: " + e.getMessage());
            return false; // 유효성 검증 실패
        }
    }
}
