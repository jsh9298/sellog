package com.teamproject.sellog.domain.auth.service.Impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.teamproject.sellog.domain.auth.service.RedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    // 데이터 저장 (키, 값, 만료 시간, 시간 단위)
    public void setValue(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // 데이터 조회
    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 데이터 삭제
    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    // 키 존재 여부 확인
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    // 키의 만료 시간 설정 (기존 키에 대해)
    public void setExpireTime(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    // 키의 남은 만료 시간 조회
    public Long getExpireTime(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }
}
