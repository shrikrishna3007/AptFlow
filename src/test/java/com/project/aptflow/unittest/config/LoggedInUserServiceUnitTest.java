package com.project.aptflow.unittest.config;

import com.project.aptflow.config.auth.LoggedInUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for LoggedInUserService")
class LoggedInUserServiceUnitTest {
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private LoggedInUserService loggedInUserService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should Return Current User Email When User Is Authenticated")
    void getCurrentUserEmail_When_UserIsAuthenticated() {
        // Arrange
        String email = "test@gmailcom";

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        // Act
        String result = loggedInUserService.getCurrentUserEmail();
        // Assert
        assertThat(result).isEqualTo(email);
    }

    @Test
    @DisplayName("Should Throw Exception When User Is Not Authenticated")
    void getCurrentUserEmail_When_UserIsNotAuthenticated() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        // Act & Assert
        assertThatThrownBy(() -> loggedInUserService.getCurrentUserEmail())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Current user is not authenticated.");
    }

    @Test
    @DisplayName("Should Throw Exception When Authentication Is Null")
    void getCurrentUserEmail_When_AuthenticationIsNull() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
        // Act & Assert
        assertThatThrownBy(() -> loggedInUserService.getCurrentUserEmail())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Current user is not authenticated.");
    }
}
