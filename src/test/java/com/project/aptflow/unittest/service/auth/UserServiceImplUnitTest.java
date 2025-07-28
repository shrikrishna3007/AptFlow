package com.project.aptflow.unittest.service.auth;

import com.project.aptflow.entity.UserEntity;
import com.project.aptflow.repository.UserRepository;
import com.project.aptflow.service.auth.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplUnitTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userServiceImpl;
    String email;

    @BeforeEach
    void setUp() {
        userServiceImpl = new UserServiceImpl(userRepository);
        email = "test@gmail.com";
    }

    @Test
    @DisplayName("Load User By Username Success Test: User Found")
    void loadUserByUsername_SuccessTest() {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        UserDetailsService userDetailsService = userServiceImpl.userDetailsService();
        UserDetails actualUserDetails = userDetailsService.loadUserByUsername(email);
        // Assert statements
        assertThat(actualUserDetails.getUsername()).isEqualTo(email);
        assertThat(actualUserDetails).isNotNull();
        assertThat(userEntity).isEqualTo(actualUserDetails);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Load User By Username Failure Test: User Not Found")
    void loadUserByUsername_FailureTest() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        UserDetailsService userDetailsService = userServiceImpl.userDetailsService();
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User Not Found!!!");
        verify(userRepository, times(1)).findByEmail(email);
    }
}
