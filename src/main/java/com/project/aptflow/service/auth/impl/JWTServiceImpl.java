package com.project.aptflow.service.auth.impl;

import com.project.aptflow.config.auth.JWTTokenConfig;
import com.project.aptflow.entity.UserEntity;
import com.project.aptflow.exceptions.BadRequestException;
import com.project.aptflow.exceptions.UnAuthorizedException;
import com.project.aptflow.service.auth.JWTService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTServiceImpl implements JWTService {

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private final JWTTokenConfig jwtTokenConfig;

    public JWTServiceImpl(JWTTokenConfig jwtTokenConfig) {
        this.jwtTokenConfig = jwtTokenConfig;
    }

    @Override
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            throw new UnAuthorizedException("Token has expired",e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnAuthorizedException("Invalid token",e);
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSignKey() {
        byte[] key = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(key);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        String role = getRoleFromUserDetails(userDetails);
        long ttl = "ROLE_ADMIN".equals(role)
                ? jwtTokenConfig.getAdmin()
                : jwtTokenConfig.getUser();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ttl);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String getRoleFromUserDetails(UserDetails userDetails) {
        if (userDetails instanceof UserEntity userEntity) {
            return userEntity.getRole().name();
        }
        return "ROLE_USER";
    }

    @Override
    public String generateRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String validateRefreshToken(String refreshToken) {
        try {
            return extractUsername(refreshToken);
        } catch (UnAuthorizedException e) {
            throw new UnAuthorizedException("Invalid or expired refresh token",e);
        }
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (UnAuthorizedException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    @Override
    public String generateResetToken(String email) {
        Map<String,Object> claims = Map.of("type","reset");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String validateResetToken(String token) {
        try {
            Claims claims =  Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (!"reset".equals(claims.get("type"))){
                throw new BadRequestException("Invalid reset token type");
            }
            return claims.getSubject();    // Returns the email
        }catch (ExpiredJwtException e){
            throw new UnAuthorizedException("Reset Token Has Expired.",e);
        }catch (JwtException | IllegalArgumentException e){
            throw new UnAuthorizedException("Invalid reset token.",e);
        }
    }

    @Override
    public String extractJti(String refreshToken) {
        return extractClaim(refreshToken, claims -> claims.get("jti",String.class));
    }
}
