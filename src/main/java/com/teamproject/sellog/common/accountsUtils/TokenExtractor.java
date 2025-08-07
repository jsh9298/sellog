package com.teamproject.sellog.common.accountsUtils;

import jakarta.servlet.http.HttpServletRequest;

public final class TokenExtractor {

    public static String extractTokenFromHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        // 헤더가 존재하고 "Bearer "로 시작하는지 확인
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // "Bearer " 접두사(7자)를 제거하고 토큰 문자열 반환
            // 토큰 문자열이 비어있는 경우 null 또는 예외 처리 고려 가능
            if (authorizationHeader.length() > 7) {
                return authorizationHeader.substring(7);
            } else {
                // "Bearer "만 있고 토큰이 없는 경우
                System.err.println("Authorization header is 'Bearer ' but token is missing."); // 로깅
                return null;
            }
        }

        return null;
    }
}
