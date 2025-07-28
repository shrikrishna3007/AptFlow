package com.project.aptflow.unittest.config;

import com.project.aptflow.config.auth.JWTAuthenticationFilter;
import com.project.aptflow.service.auth.JWTService;
import com.project.aptflow.service.auth.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWTAuthenticationFilter Unit Tests")
class JWTAuthenticationFilterUnitTest {
    @Mock
    private JWTService jwtService;
    @Mock
    private UserService userService;
    @Mock
    private UserDetails userDetails;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should Not Authenticate When Authorization Header Is Missing")
    void doFilterInternal_When_NoAuthHeaderTest() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);
        // Act
        jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);
        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request,response);
    }

    @Test
    @DisplayName("Should Not Authenticate When Authorization Header Is Empty")
    void doInternalFilter_When_AuthHeaderIsEmptyTest() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("");
        // Act
        jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);
        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request,response);
        verify(jwtService,never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("Should Not Authenticate When Authorization Header Is Malformed")
    void doInternalFilter_When_AuthorizationIsMalformedTest() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");
        // Act
        jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);
        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request,response);
    }

    @Test
    @DisplayName("Should Not Authenticate When Using Basic Authentication")
    void doInternalFilter_When_UsingBasicAuthenticationTest() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic YWRtaW46YWRtaW4=");
        // Act
        jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);
        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request,response);
        verify(jwtService,never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("Should Not Authenticate When Extracting Username Returns Null")
    void doInternalFilter_When_ExtractUsernameReturnsNullTest() throws ServletException, IOException {
        String jwt = "valid.token.here";
        when(request.getHeader("Authorization")).thenReturn("Bearer "+jwt);
        when(jwtService.extractUsername(jwt)).thenReturn(null);
        // Act
        jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);
        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request,response);
    }

    @Test
    @DisplayName("Should Not Authenticate When Extracting Username Returns Empty String")
    void doInternalFilter_When_ExtractUsernameReturnsEmptyStringTest() throws ServletException, IOException {
        String jwt = "valid.token.here";
        when(request.getHeader("Authorization")).thenReturn("Bearer "+jwt);
        when(jwtService.extractUsername(jwt)).thenReturn("");
        // Act
        jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);
        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request,response);
    }

    @Test
    @DisplayName("Should Not Authenticate When Security Context Is Already Authenticated")
    void doInternalFilter_When_SecurityContextIsAlreadyAuthenticatedTest() throws ServletException, IOException {
        String jwt = "valid.token.here";
        Authentication existingAuthentication = mock(Authentication.class);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(existingAuthentication);
        SecurityContextHolder.setContext(context);

        when(request.getHeader("Authorization")).thenReturn("Bearer "+jwt);
        when(jwtService.extractUsername(jwt)).thenReturn("test@gmail");
        // Act
        jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);
        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuthentication);
        verify(filterChain).doFilter(request,response);
        verify(userService,never()).userDetailsService();
        verify(jwtService,never()).isTokenValid(anyString(),any());
    }

    @Test
    @DisplayName("Should Not Authentication If Token Is Invalid")
    void doInternalFilter_When_TokenIsInvalidTest() throws ServletException, IOException {
        // Arrange
        String jwt = "invalid.token.here";
        String email = "test@gmail.com";

        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        when(request.getHeader("Authorization")).thenReturn("Bearer "+jwt);
        when(jwtService.extractUsername(jwt)).thenReturn(email);

        when(userService.userDetailsService()).thenReturn(username -> userDetails);
        when(jwtService.isTokenValid(jwt,userDetails)).thenReturn(false);
        // Act
        jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);
        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request,response);
    }

    // Should authenticate and set SecurityContext when token is valid and user is not already authenticated - Main Path
    @Test
    @DisplayName("Should Authenticate And Set Security Context When Token Is Valid And User Not Authenticated")
    void doInternalFilter_When_TokenIsValidTest() throws ServletException, IOException {
        String jwt = "valid.token.here";
        String email = "test@gmail.com";

        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());

        when(request.getHeader("Authorization")).thenReturn("Bearer "+jwt);
        when(jwtService.extractUsername(jwt)).thenReturn(email);

        when(userService.userDetailsService()).thenReturn(username -> userDetails);
        when(jwtService.isTokenValid(jwt,userDetails)).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
        // Act
        jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Assert
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        assertThat(authentication.getCredentials()).isNull();
        assertThat(authentication.getAuthorities()).isEqualTo(Collections.emptyList());
        assertThat(authentication.getDetails()).isNotNull();
        // Verify
        verify(filterChain).doFilter(request,response);
        verify(jwtService).extractUsername(jwt);
        verify(jwtService).isTokenValid(jwt,userDetails);
    }

    @Test
    @DisplayName("Should Throw Exception When User Not Found While Loading UserDetails")
    void doInternalFilter_When_UserDetailsService_FailureTest() throws ServletException, IOException {
        String jwt = "valid.jwt.token";
        String email = "test@gmail.com";

        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        when(request.getHeader("Authorization")).thenReturn("Bearer "+jwt);
        when(jwtService.extractUsername(jwt)).thenReturn(email);

        when(userService.userDetailsService()).thenThrow(new UsernameNotFoundException("User not found"));
        // Act and Assert
        assertThatThrownBy(()->jwtAuthenticationFilter.doFilterInternal(request,response,filterChain))
                .isInstanceOf(UsernameNotFoundException.class);
        verify(filterChain,never()).doFilter(request,response);
        verify(jwtService,never()).isTokenValid(anyString(),any());
    }

    @Test
    @DisplayName("Should Extract Correct Token From Bearer Header")
    void doInternalFilter_ExtractCorrectToken_From_BearerHeaderTest() throws ServletException, IOException {
        // Arrange
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.example.signature";
        String authHeader = "Bearer "+expectedToken;
        String email = "test@gmail.com";

        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(expectedToken)).thenReturn(email);
        when(userService.userDetailsService()).thenReturn(username -> userDetails);
        when(jwtService.isTokenValid(expectedToken,userDetails)).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
        // Act
        jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);
        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(jwtService).extractUsername(expectedToken);
        verify(jwtService).isTokenValid(expectedToken,userDetails);
        verify(filterChain).doFilter(request,response);
    }

    @Test
    @DisplayName("Should Always Continue Filter Chain Regardless Of Authentication Result")
    void doInternalFilter_AlwaysContinue_FilterChainTest() throws ServletException, IOException {
        // Scenario 1: No Auth Header
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);
        verify(filterChain,times(1)).doFilter(request,response);

        reset(filterChain);

        // Scenario 2: Valid Authentication
        String jwt = "valid.jwt.token";
        String email = "test@gmail.com";

        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());

        when(request.getHeader("Authorization")).thenReturn("Bearer "+jwt);
        when(jwtService.extractUsername(jwt)).thenReturn(email);
        when(userService.userDetailsService()).thenReturn(username -> userDetails);
        when(jwtService.isTokenValid(jwt,userDetails)).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
        // Act
        jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);
        verify(filterChain,times(1)).doFilter(request,response);
    }
}
