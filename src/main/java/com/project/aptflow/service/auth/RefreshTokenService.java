package com.project.aptflow.service.auth;

public interface RefreshTokenService {
    void storeRefreshToken(String adhaarNumber, String deviceId, String jti);

    boolean isRefreshTokenValid(String userId, String deviceId, String jti);
}
