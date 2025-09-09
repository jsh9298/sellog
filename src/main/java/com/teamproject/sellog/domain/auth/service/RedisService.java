package com.teamproject.sellog.domain.auth.service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public interface RedisService {

    // 데이터 저장 (키, 값, 만료 시간, 시간 단위)
    void setValue(String key, Object value, long timeout, TimeUnit unit);

    // 데이터 조회
    Object getValue(String key);

    // 데이터 삭제
    void deleteValue(String key);

    // 키 존재 여부 확인
    boolean hasKey(String key);

    // 키의 만료 시간 설정 (기존 키에 대해)
    void setExpireTime(String key, long timeout, TimeUnit unit);

    // 키의 남은 만료 시간 조회
    Long getExpireTime(String key, TimeUnit unit);
}
