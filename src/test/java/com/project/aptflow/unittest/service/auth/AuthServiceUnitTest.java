package com.project.aptflow.unittest.service.auth;

import com.project.aptflow.dto.auth.LoginRequestDTO;
import com.project.aptflow.dto.auth.PasswordUpdateDTO;
import com.project.aptflow.dto.auth.SignUpRequestDTO;
import com.project.aptflow.dto.credential.ResetPasswordDTO;
import com.project.aptflow.entity.UserEntity;
import com.project.aptflow.enums.Role;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.exceptions.UnAuthorizedException;
import com.project.aptflow.repository.UserRepository;
import com.project.aptflow.service.EmailService;
import com.project.aptflow.service.auth.JWTService;
import com.project.aptflow.service.auth.impl.AuthServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {

    @Mock private EmailService emailService;
    @Mock private JWTService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    private UserEntity userEntity;
    private SignUpRequestDTO signUpRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private PasswordUpdateDTO passwordUpdateDTO;
    private ResetPasswordDTO resetPasswordDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        userEntity = new UserEntity();
        userEntity.setEmail("test@example.com");
        userEntity.setName("Test User");
        userEntity.setAddress("Test Address");
        userEntity.setGender("Male");
        userEntity.setAdhaarNumber("123456789012");
        userEntity.setMobileNumber("9876543210");
        userEntity.setRole(Role.ROLE_USER);
        userEntity.setPassword("encodedPassword");

        signUpRequestDTO = new SignUpRequestDTO("123456789012", "Test User", "Test Address", "Male", "9876543210", "test@example.com", "plainPassword", Role.ROLE_USER);

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("test@example.com");
        loginRequestDTO.setPassword("plainPassword");

        passwordUpdateDTO = new PasswordUpdateDTO();
        passwordUpdateDTO.setEmail("test@example.com");
        passwordUpdateDTO.setOldPassword("plainPassword");
        passwordUpdateDTO.setNewPassword("newPlainPassword");

        resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setToken("resetToken");
        resetPasswordDTO.setNewPassword("newPlainPassword");
    }

    @Nested
    @DisplayName("User Sign Up Tests")
    class UserSignUpTest{
        @Test
        @DisplayName("Sign Up Test")
        void signUp_SuccessTest(){
            // Arrange
            when(passwordEncoder.encode(signUpRequestDTO.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

            // Act
            UserEntity result = authServiceImpl.signUp(signUpRequestDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(signUpRequestDTO.getEmail());
            assertThat(result.getName()).isEqualTo(signUpRequestDTO.getName());
            // Verify
            verify(passwordEncoder).encode(signUpRequestDTO.getPassword());
            verify(userRepository,times(1)).save(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("User Login Tests")
    class UserLoginTest{
        @Test
        @DisplayName("Login Success Test")
        void login_SuccessTest(){
            // Arrange
            String expectedAccessToken = "accessTokenTest";
            String expectedRefreshToken = "refreshTokenTest";

            when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(userEntity));
            when(jwtService.generateToken(userEntity)).thenReturn(expectedAccessToken);
            when(jwtService.generateRefreshToken(anyMap(),eq(userEntity))).thenReturn(expectedRefreshToken);

            // Act
            String result = authServiceImpl.login(loginRequestDTO, response);

            // Assert
            assertThat(result).isEqualTo(expectedAccessToken);

            // Verify
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByEmail(loginRequestDTO.getEmail());
            verify(jwtService).generateToken(userEntity);
            verify(jwtService).generateRefreshToken(anyMap(),eq(userEntity));
            verify(response).addCookie(any(Cookie.class));
        }

        @Test
        @DisplayName("Login Failure Test- Authentication Failed")
        void login_FailureTest_AuthenticationFailed(){
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new UnAuthorizedException(" Invalid email or password..."));

            // Act and Assert
            assertThatThrownBy(() -> authServiceImpl.login(loginRequestDTO, response))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessage(" Invalid email or password...");

            // Verify
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verifyNoInteractions(userRepository,jwtService,response);

        }

        @Test
        @DisplayName("Login Failure Test- User Not Found")
        void login_FailureTest_UserNotFound(){
            // Arrange
            when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.empty());

            // Act and Assert
            assertThatThrownBy(() -> authServiceImpl.login(loginRequestDTO, response))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessage(" Invalid email or password...");

            // Verify
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByEmail(loginRequestDTO.getEmail());
            verifyNoInteractions(jwtService,response);
        }
    }

    @Nested
    @DisplayName("Get New Access Token Tests")
    class GetNewAccessTokenTest{
        @Test
        @DisplayName("Get New Access Token Using Valid Refresh Token Test")
        void getRefreshToken_SuccessTest() {
            String refreshToken = "validRefreshToken";
            String newAccessToken = "newAccessToken";
            Cookie[] cookies = {new Cookie("refreshToken",refreshToken)};

            when(request.getCookies()).thenReturn(cookies);
            when(jwtService.validateRefreshToken(refreshToken)).thenReturn("test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
            when(jwtService.generateToken(userEntity)).thenReturn(newAccessToken);

            // Act
            Map<String,String> result = authServiceImpl.getRefreshToken(request, response);
            // Assert
            assertThat(result.get("accessToken")).isEqualTo(newAccessToken);
            // Verify
            verify(jwtService).validateRefreshToken(refreshToken);
            verify(userRepository).findByEmail("test@example.com");
            verify(jwtService).generateToken(userEntity);
        }

        @Test
        @DisplayName("Refresh Token Not Found Test")
        void getRefreshToken_Failure_RefreshTokenNotFound(){
            // Arrange
            when(request.getCookies()).thenReturn(null);
            // Act and Assert
            assertThatThrownBy(()-> authServiceImpl.getRefreshToken(request, response))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessage("Refresh token not found in request");
            // Verify
            verifyNoInteractions(jwtService,userRepository);
        }

        @Test
        @DisplayName("Get New Access Token Using Invalid Refresh Token Test")
        void getRefreshToken_Failure_InvalidRefreshToken(){
            // Arrange
            String refreshToken = "invalidRefreshToken";
            Cookie[] cookies = {new Cookie("refreshToken",refreshToken)};
            when(request.getCookies()).thenReturn(cookies);
            when(jwtService.validateRefreshToken(refreshToken)).thenReturn(null);
            // Act and Assert
            assertThatThrownBy(()->authServiceImpl.getRefreshToken(request, response))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessage("Invalid or expired refresh token");
            // Verify
            verify(jwtService).validateRefreshToken(refreshToken);
            verifyNoInteractions(userRepository);
        }
    }

    @Nested
    @DisplayName("Update Password Tests")
    class UpdatePasswordTest{
        @Test
        @DisplayName("Update Password Success Test")
        void updatePassword_SuccessTest(){
            // Arrange
            when(userRepository.findByEmail(passwordUpdateDTO.getEmail())).thenReturn(Optional.of(userEntity));
            when(passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), userEntity.getPassword())).thenReturn(true);
            when(passwordEncoder.matches(passwordUpdateDTO.getNewPassword(), userEntity.getPassword())).thenReturn(false);
            when(passwordEncoder.encode(passwordUpdateDTO.getNewPassword())).thenReturn("encodedPassword");
            when(userRepository.save(userEntity)).thenReturn(userEntity);
            // Act
            authServiceImpl.updatePassword(passwordUpdateDTO);
            // Verify
            verify(userRepository).findByEmail(passwordUpdateDTO.getEmail());
            verify(passwordEncoder).matches(passwordUpdateDTO.getOldPassword(), userEntity.getPassword());
            verify(passwordEncoder).matches(passwordUpdateDTO.getNewPassword(), userEntity.getPassword());
            verify(passwordEncoder).encode(passwordUpdateDTO.getNewPassword());
            verify(userRepository).save(userEntity);
        }

        @Test
        @DisplayName("Update Password Failure Test- User Not Found")
        void updatePassword_FailureTest_UserNotFound(){
            // Arrange
            when(userRepository.findByEmail(passwordUpdateDTO.getEmail())).thenReturn(Optional.empty());
            // Act and Assert
            assertThatThrownBy(() -> authServiceImpl.updatePassword(passwordUpdateDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
            // Verify
            verify(userRepository).findByEmail(passwordUpdateDTO.getEmail());
            verifyNoInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("Update Password Failure Test- Old Password Incorrect")
        void updatePassword_FailureTest_OldPasswordIncorrect(){
            // Arrange
            when(userRepository.findByEmail(passwordUpdateDTO.getEmail())).thenReturn(Optional.of(userEntity));
            when(passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), userEntity.getPassword())).thenReturn(false);
            // Act and Assert
            assertThatThrownBy(() -> authServiceImpl.updatePassword(passwordUpdateDTO))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessage("Old password is incorrect");
            // Verify
            verify(userRepository).findByEmail(passwordUpdateDTO.getEmail());
            verify(passwordEncoder).matches(passwordUpdateDTO.getOldPassword(), userEntity.getPassword());
        }

        @Test
        @DisplayName("Update Password Failure Test- New Password Same as Old Password")
        void updatePassword_FailureTest_NewPasswordSameAsOldPassword(){
            // Arrange
            when(userRepository.findByEmail(passwordUpdateDTO.getEmail())).thenReturn(Optional.of(userEntity));
            when(passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), userEntity.getPassword())).thenReturn(true);
            when(passwordEncoder.matches(passwordUpdateDTO.getNewPassword(), userEntity.getPassword())).thenReturn(true);
            // Act and Assert
            assertThatThrownBy(() -> authServiceImpl.updatePassword(passwordUpdateDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("New password cannot be the same as old password");
            // Verify
            verify(userRepository).findByEmail(passwordUpdateDTO.getEmail());
            verify(passwordEncoder).matches(passwordUpdateDTO.getOldPassword(), userEntity.getPassword());
            verify(passwordEncoder).matches(passwordUpdateDTO.getNewPassword(), userEntity.getPassword());
        }
    }

    @Nested
    @DisplayName("Forgot Password Sending Mail Tests")
    class ForgotPasswordTest{
        @Test
        @DisplayName("Send Password Reset Mail as JWT: Success")
        void sendResetTokenAsJwt_SuccessTest() throws MessagingException {
            // Arrange
            String resetToken = "resetTokenTest";
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
            when(jwtService.generateResetToken("test@example.com")).thenReturn(resetToken);
            // Act
            authServiceImpl.sendResetTokenAsJwt("test@example.com");
            // Verify
            verify(userRepository).findByEmail("test@example.com");
            verify(jwtService).generateResetToken("test@example.com");
            verify(emailService).sendEmail(eq("test@example.com"),eq("Reset Your Password"),anyString());
        }

        @Test
        @DisplayName("Send Password Reset Mail as JWT: Failure- User Not Found")
        void sendResetTokenAsJwt_FailureTest_UserNotFound(){
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
            // Act and Assert
            assertThatThrownBy(() -> authServiceImpl.sendResetTokenAsJwt("test@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
            // Verify
            verify(userRepository).findByEmail("test@example.com");
            verifyNoInteractions(jwtService,emailService);
        }

        @Test
        @DisplayName("Send Password Reset Mail as JWT: Failure- Email Sending Failed")
        void sendResetTokenAsJwt_FailureTest_EmailSendingFailed() throws MessagingException {
            // Arrange
            String resetToken = "resetTokenTest";
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
            when(jwtService.generateResetToken("test@example.com")).thenReturn(resetToken);
            doThrow(new MessagingException("Error sending email")).when(emailService).sendEmail(eq("test@example.com"),eq("Reset Your Password"),anyString());
            // Act and Assert
            assertThatThrownBy(() -> authServiceImpl.sendResetTokenAsJwt("test@example.com"))
                    .isInstanceOf(com.project.aptflow.exceptions.MessagingException.class)
                    .hasMessage("Error sending email");
            // Verify
            verify(userRepository).findByEmail("test@example.com");
            verify(jwtService).generateResetToken("test@example.com");
            verify(emailService).sendEmail(eq("test@example.com"),eq("Reset Your Password"),anyString());
        }
    }

    @Nested
    @DisplayName("Reset Password Tests")
    class ResetPasswordTest{
        @Test
        @DisplayName("Reset Password Using JWT: Success")
        void resetPasswordUsingJwt_SuccessTest(){
            // Arrange
            when(jwtService.validateResetToken("resetToken")).thenReturn("test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
            when(passwordEncoder.encode("newPlainPassword")).thenReturn("encodedPassword");
            when(userRepository.save(userEntity)).thenReturn(userEntity);
            // Act
            authServiceImpl.resetPasswordUsingJwt(resetPasswordDTO);
            // Verify
            verify(jwtService).validateResetToken("resetToken");
            verify(userRepository).findByEmail("test@example.com");
            verify(passwordEncoder).encode("newPlainPassword");
            verify(userRepository).save(userEntity);
        }

        @Test
        @DisplayName("Reset Password Using JWT: Failure- Token Invalid")
        void resetPasswordUsingJwt_FailureTest_TokenInvalid(){
            // Arrange
            when(jwtService.validateResetToken("resetToken")).thenReturn(null);
            // Act and Assert
            assertThatThrownBy(() -> authServiceImpl.resetPasswordUsingJwt(resetPasswordDTO))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessage("Invalid or expired reset token");
            // Verify
            verify(jwtService).validateResetToken("resetToken");
            verifyNoInteractions(userRepository,passwordEncoder);
        }

        @Test
        @DisplayName("Reset Password Using JWT: Failure- User Not Found")
        void resetPasswordUsingJwt_FailureTest_UserNotFound(){
            // Arrange
            when(jwtService.validateResetToken("resetToken")).thenReturn("test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
            // Act and Assert
            assertThatThrownBy(() -> authServiceImpl.resetPasswordUsingJwt(resetPasswordDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found");
            // Verify
            verify(jwtService).validateResetToken("resetToken");
            verify(userRepository).findByEmail("test@example.com");
            verifyNoInteractions(passwordEncoder);
        }
    }
}
