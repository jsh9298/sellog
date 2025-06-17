package com.teamproject.sellog.auth.model.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

//JWT의 생성 담당
@Component
public class JwtProvider {

    // private final SecretKey key;
    // private final long validityInMilliseconds;

    // public JwtProvider(@Value("${jwt.secret}") String secret,
    // @Value("${jwt.expiration}") long validityInMilliseconds) {
    // this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    // this.validityInMilliseconds = validityInMilliseconds;
    // }

    public static final byte[] secret = "selllogJWTSecretselllogJWTSecretselllogJWTSecret".getBytes(); // 별도의 환경변수로 대체필요
    private final SecretKey key = Keys.hmacShaKeyFor(secret);

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
        String refreshToken = createToken(new HashMap<>(), getExpireDateRefreshToken());

        return JWT.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    public Date getExpireDateAccessToken() {
        long expireTimeMils = 1000 * 60 * 60; // 별도의 환경변수로 대체 필요
        return new Date(System.currentTimeMillis() + expireTimeMils);
    }

    public Date getExpireDateRefreshToken() {
        long expireTimeMils = 1000L * 60 * 60 * 24 * 60; // 별도의 환경변수로 대체 필요
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
