package com.project.aptflow.service.auth.impl;

import com.project.aptflow.service.auth.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RedisTemplate<String,String> redisTemplate;
    private final long refreshTokenTTL;
    public RefreshTokenServiceImpl(RedisTemplate<String, String> redisTemplate,
                                   @Value("${jwt.refresh-token.expiration}") long refreshTokenTTL) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenTTL = refreshTokenTTL;
    }


    @Override
    public void storeRefreshToken(String adhaarNumber, String deviceId, String jti) {
        String key = getRedisKey(adhaarNumber, deviceId);
        // Convert TTL to seconds if required
        Duration ttl = Duration.ofMillis(refreshTokenTTL);
        redisTemplate.opsForValue().set(key, jti, ttl);
    }

    public boolean isRefreshTokenValid(String adhaarNumber, String deviceId, String jti) {
        String key = getRedisKey(adhaarNumber, deviceId);
        String storedJti = redisTemplate.opsForValue().get(key);
        return jti != null && jti.equals(storedJti);
    }

    private String getRedisKey(String adhaarNumber, String deviceId) {
        return "refresh:"+adhaarNumber+":"+deviceId;
    }
}
