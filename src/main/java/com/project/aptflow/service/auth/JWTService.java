package com.project.aptflow.service.auth;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JWTService {
    String extractUsername(String token);

    String generateToken(UserDetails userDetails);

    boolean isTokenValid(String token, UserDetails userDetails);

    String generateRefreshToken(Map<String,Object> extraClaims, UserDetails userDetails);

    String validateRefreshToken(String refreshToken);

    String generateResetToken(String email);

    String validateResetToken(String token);

    String extractJti(String refreshToken);
}
