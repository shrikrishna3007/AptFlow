package com.project.aptflow.service.auth.impl;

import com.project.aptflow.dto.auth.LoginRequestDTO;
import com.project.aptflow.dto.auth.PasswordUpdateDTO;
import com.project.aptflow.dto.auth.SignUpRequestDTO;
import com.project.aptflow.dto.credential.ResetPasswordDTO;
import com.project.aptflow.entity.UserEntity;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.exceptions.UnAuthorizedException;
import com.project.aptflow.repository.UserRepository;
import com.project.aptflow.service.EmailService;
import com.project.aptflow.service.auth.AuthService;
import com.project.aptflow.service.auth.JWTService;
import com.project.aptflow.service.auth.RefreshTokenService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {
    // Forgot password related field
    @Value("${app.reset.url}")
    private String resetPasswordBaseUrl;
    private final EmailService emailService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthServiceImpl(EmailService emailService, UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JWTService jwtService, RefreshTokenService refreshTokenService, RedisTemplate<String, String> redisTemplate) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public UserEntity signUp(SignUpRequestDTO signUpRequestDTO) {
        UserEntity user = new UserEntity();
        user.setEmail(signUpRequestDTO.getEmail());
        user.setName(signUpRequestDTO.getName());
        user.setAddress(signUpRequestDTO.getAddress());
        user.setGender(signUpRequestDTO.getGender());
        user.setAdhaarNumber(signUpRequestDTO.getAdhaarNumber());
        user.setMobileNumber(signUpRequestDTO.getMobileNumber());
        user.setRole(signUpRequestDTO.getRole());
        user.setPassword(passwordEncoder.encode(signUpRequestDTO.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public String login(LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        // Step 1: Authenticate Credentials
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));
        }catch (AuthenticationException e){
            throw new UnAuthorizedException(" Invalid email or password...");
        }

        // Step 2: Find user in DB
        var user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(()-> new UnAuthorizedException(" Invalid email or password..."));
        // Step 3: Generate jti and extra claims
        String jti = UUID.randomUUID().toString();  // Unique ID For Each Refresh Token
        Map<String,Object> extraClaims = new HashMap<>();
        extraClaims.put("jti",jti);
        // Step 4: Generate access and refresh token
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(extraClaims, user);

        // Step 5: Generate Device ID
        String deviceId= UUID.randomUUID().toString();

        // Step 6: Store refresh token jti in redis
        refreshTokenService.storeRefreshToken(user.getAdhaarNumber(), deviceId, jti);

        // Step 7: Store refresh token in HttpOnly cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // only send over HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7*24*60*60); // 7 days expiration
        response.addCookie(refreshTokenCookie);

        // Step 8: Set Device ID Cookie(Non HttpOnly, readable by frontend)
        Cookie deviceIdCookie = new Cookie("deviceId", deviceId);
        deviceIdCookie.setHttpOnly(false);
        deviceIdCookie.setSecure(true);
        deviceIdCookie.setPath("/");
        deviceIdCookie.setMaxAge(365 * 24 * 60 * 60); // 1 year expiration
        response.addCookie(deviceIdCookie);

        // Step 9: Return Access Token
        return accessToken;
    }


    @Override
    public Map<String, String> getRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refreshToken");
        String deviceId = extractCookie(request, "deviceId");

        if (refreshToken == null || deviceId == null) {
            throw new UnAuthorizedException("Missing refresh token or deviceId");
        }

        try {
            // Step 2: Validate refresh token
            String username = jwtService.validateRefreshToken(refreshToken);
            String jti = jwtService.extractJti(refreshToken);
            // Step 3: Find user by email
            UserEntity user = userRepository.findByEmail(username)
                    .orElseThrow(()-> new ResourceNotFoundException("User not found"));
            String userId = user.getAdhaarNumber();
            // Step 4: Redis check for refresh token jti
            if (!refreshTokenService.isRefreshTokenValid(userId, deviceId, jti)) {
                throw new UnAuthorizedException("Invalid or reused refresh token");
            }
            // Step 5: Rotate refresh token
            String newJti = UUID.randomUUID().toString();
            Map<String,Object> extraClaims = new HashMap<>();
            extraClaims.put("jti",newJti);

            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(extraClaims, user);

            // Step 6: Store new refresh token jti in redis
            refreshTokenService.storeRefreshToken(userId, deviceId, newJti);
            // Step 7: Set refresh token cookie
            addCookie(response, "refreshToken", newRefreshToken, 7*24*60*60, true); // 7 days expiration
            // Step 8: Return new access token
            Map<String,String> result = new HashMap<>();
            result.put("accessToken",newAccessToken);
            return result;
        }catch (Exception e){
            throw new UnAuthorizedException("Invalid or expired refresh token");
        }
    }

    private String extractCookie(HttpServletRequest request, String refreshToken) {
        if (request.getCookies()== null) return null;
        for (Cookie cookie : request.getCookies()){
            if (cookie.getName().equals(refreshToken)){
                return cookie.getValue();
            }
        }
        return null;
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean httpOnly) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    @Override
    public void updatePassword(PasswordUpdateDTO passwordUpdateDTO) {
        // Find user by email
        UserEntity user =  userRepository.findByEmail(passwordUpdateDTO.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
        // Check if old password is correct
        if (!passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), user.getPassword())){
            throw new UnAuthorizedException("Old password is incorrect");
        }
        // Check if new password is same as old password
        if (passwordEncoder.matches(passwordUpdateDTO.getNewPassword(), user.getPassword())){
            throw new IllegalArgumentException("New password cannot be the same as old password");
        }
        // Encode and update password
        user.setPassword(passwordEncoder.encode(passwordUpdateDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void sendResetTokenAsJwt(String email) {
        try {
            var user = userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found"));

            String token = jwtService.generateResetToken(email);

            String resetLink = resetPasswordBaseUrl + "?token="+ token;
            // Send email to user
            String subject = "Reset Your Password";
            String body = "Hi "+user.getName()+",\n\n"
                    + "Please click the link below to reset your password:\n"
                    + resetLink + "\n\n"
                    + "If you did not request this, please ignore this email.\n\n"
                    + "Thank you,\n";
            emailService.sendEmail(user.getEmail(),subject,body);
        } catch (MessagingException e) {
            throw new com.project.aptflow.exceptions.MessagingException("Error sending email", e);
        }
    }

    @Override
    public void resetPasswordUsingJwt(ResetPasswordDTO resetPasswordDTO) {
        String email = jwtService.validateResetToken(resetPasswordDTO.getToken());
        if (email == null) {
            throw new UnAuthorizedException("Invalid or expired reset token");
        }
        var user = userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void logoutCurrentDevice(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refreshToken");
        String deviceId = extractCookie(request, "deviceId");

        if (refreshToken == null || deviceId == null) {
            throw new UnAuthorizedException("Missing refresh token or deviceId");
        }
        String email = jwtService.extractUsername(refreshToken);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        String redisKey = "refresh:"+user.getAdhaarNumber()+":"+deviceId;
        redisTemplate.delete(redisKey);

        clearCookie(response, "refreshToken");
        clearCookie(response, "deviceId");
    }

    @Override
    public void logoutAllDevices(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refreshToken");

        if (refreshToken == null) {
            throw new UnAuthorizedException("Missing refresh token");
        }
        String email = jwtService.extractUsername(refreshToken);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        String redisPattern = "refresh:"+user.getAdhaarNumber()+":*";
        Set<String> keys = redisTemplate.keys(redisPattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        clearCookie(response, "refreshToken");
        clearCookie(response, "deviceId");
    }

    private void clearCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(refreshToken, "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private Optional<String> extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
