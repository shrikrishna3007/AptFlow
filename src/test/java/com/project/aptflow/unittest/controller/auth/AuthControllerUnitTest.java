package com.project.aptflow.unittest.controller.auth;

import com.project.aptflow.controller.auth.AuthController;
import com.project.aptflow.dto.apiresponse.MessageResponseDTO;
import com.project.aptflow.dto.auth.LoginRequestDTO;
import com.project.aptflow.dto.auth.PasswordUpdateDTO;
import com.project.aptflow.dto.auth.SignUpRequestDTO;
import com.project.aptflow.dto.credential.ForgotPasswordRequestDTO;
import com.project.aptflow.dto.credential.ResetPasswordDTO;
import com.project.aptflow.entity.UserEntity;
import com.project.aptflow.enums.Role;
import com.project.aptflow.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerUnitTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("User Sign Up Test")
    void signUp_SuccessTest() {
        SignUpRequestDTO signUpRequestDTO = new SignUpRequestDTO("adhaarNumber", "name", "address", "gender", "mobileNumber", "email", "password", Role.ROLE_USER);
        UserEntity userEntity = new UserEntity();
        // Arrange
        when(authService.signUp(signUpRequestDTO)).thenReturn(userEntity);
        // Act
        ResponseEntity<UserEntity> response = authController.signUp(signUpRequestDTO);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(userEntity);
        // Verify
        verify(authService,times(1)).signUp(signUpRequestDTO);
    }

    @Test
    @DisplayName("User Sign In Test")
    void signIn_SuccessTest() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        // Arrange
        when(authService.login(loginRequestDTO,httpServletResponse)).thenReturn("mock-access-token");
        // Act
        ResponseEntity<Map<String,String>> response = authController.signIn(loginRequestDTO,httpServletResponse);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("accessToken","mock-access-token");
        // Verify
        verify(authService).login(loginRequestDTO,httpServletResponse);
    }

    @Test
    @DisplayName("Get New Access Token Test")
    void getRefreshToken_SuccessTest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        // Arrange
        when(authService.getRefreshToken(request,response)).thenReturn(Map.of("accessToken","new-access-token"));
        // Act
        ResponseEntity<Map<String,String>> result = authController.getRefreshToken(request,response);
        // Assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsEntry("accessToken","new-access-token");
        // Verify
        verify(authService).getRefreshToken(request,response);
    }

    @Test
    @DisplayName("Update Password Test")
    void updatePassword_SuccessTest() {
        PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
        // Arrange
        doNothing().when(authService).updatePassword(passwordUpdateDTO);
        // Act
        ResponseEntity<MessageResponseDTO> response = authController.updatePassword(passwordUpdateDTO);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Password updated successfully");
        // Verify
        verify(authService).updatePassword(passwordUpdateDTO);
    }

    @Test
    @DisplayName("Forgot Password Test")
    void forgotPassword_SuccessTest() {
        ForgotPasswordRequestDTO forgotPasswordRequestDTO = new ForgotPasswordRequestDTO();
        // Arrange
        doNothing().when(authService).sendResetTokenAsJwt(forgotPasswordRequestDTO.getEmail());
        // Act
        ResponseEntity<MessageResponseDTO> response = authController.forgotPassword(forgotPasswordRequestDTO);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Email sent successfully");
        // Verify
        verify(authService).sendResetTokenAsJwt(forgotPasswordRequestDTO.getEmail());
    }

    @Test
    @DisplayName("Reset Password Test")
    void resetPassword_SuccessTest() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        doNothing().when(authService).resetPasswordUsingJwt(resetPasswordDTO);
        // Act
        ResponseEntity<MessageResponseDTO> response = authController.resetPassword(resetPasswordDTO);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Password updated successfully");
        // Verify
        verify(authService).resetPasswordUsingJwt(resetPasswordDTO);
    }
}
