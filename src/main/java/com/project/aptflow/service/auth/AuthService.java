package com.project.aptflow.service.auth;

import com.project.aptflow.dto.auth.LoginRequestDTO;
import com.project.aptflow.dto.auth.PasswordUpdateDTO;
import com.project.aptflow.dto.auth.SignUpRequestDTO;
import com.project.aptflow.dto.credential.ResetPasswordDTO;
import com.project.aptflow.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface AuthService {

    UserEntity signUp(SignUpRequestDTO signUpRequestDTO);

    String login(LoginRequestDTO loginRequestDTO, HttpServletResponse response);

    Map<String, String> getRefreshToken(HttpServletRequest request, HttpServletResponse response);

    void updatePassword(PasswordUpdateDTO passwordUpdateDTO);

    void sendResetTokenAsJwt(String email);

    void resetPasswordUsingJwt(ResetPasswordDTO resetPasswordDTO);

    void logoutCurrentDevice(HttpServletRequest request, HttpServletResponse response);

    void logoutAllDevices(HttpServletRequest request, HttpServletResponse response);
}
