package com.project.aptflow.controller.auth;

import com.project.aptflow.dto.apiresponse.MessageResponseDTO;
import com.project.aptflow.dto.auth.LoginRequestDTO;
import com.project.aptflow.dto.auth.PasswordUpdateDTO;
import com.project.aptflow.dto.auth.SignUpRequestDTO;
import com.project.aptflow.dto.credential.ForgotPasswordRequestDTO;
import com.project.aptflow.dto.credential.ResetPasswordDTO;
import com.project.aptflow.entity.UserEntity;
import com.project.aptflow.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signUp")
    public ResponseEntity<UserEntity> signUp(@RequestBody SignUpRequestDTO signUpRequestDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(signUpRequestDTO));
    }

    @PostMapping("/signIn")
    public ResponseEntity<Map<String,String>> signIn(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response){
        String accessToken = authService.login(loginRequestDTO, response);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("accessToken",accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDTO> logoutCurrentDevice(HttpServletRequest request, HttpServletResponse response){
        authService.logoutCurrentDevice(request, response);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponseDTO("Logout successful",HttpStatus.OK));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<MessageResponseDTO> logoutAllDevices(HttpServletRequest request, HttpServletResponse response){
        authService.logoutAllDevices(request, response);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponseDTO("Logout successful",HttpStatus.OK));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String,String>> getRefreshToken(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> token = authService.getRefreshToken(request, response);
        return ResponseEntity.ok(token);
    }

    @PutMapping("/password")
    public ResponseEntity<MessageResponseDTO> updatePassword(@RequestBody PasswordUpdateDTO passwordUpdateDTO){
        authService.updatePassword(passwordUpdateDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponseDTO("Password updated successfully",HttpStatus.OK));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDTO> forgotPassword(@RequestBody ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
        authService.sendResetTokenAsJwt(forgotPasswordRequestDTO.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponseDTO("Email sent successfully",HttpStatus.OK));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO){
        authService.resetPasswordUsingJwt(resetPasswordDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponseDTO("Password updated successfully",HttpStatus.OK));
    }
}
