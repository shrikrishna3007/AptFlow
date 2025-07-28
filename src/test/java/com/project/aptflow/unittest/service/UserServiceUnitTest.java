package com.project.aptflow.unittest.service;

import com.project.aptflow.dto.UserDTO;
import com.project.aptflow.entity.UserEntity;
import com.project.aptflow.enums.Role;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.mapper.UserMapper;
import com.project.aptflow.repository.UserRepository;
import com.project.aptflow.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("User Service Unit Tests")
public class UserServiceUnitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private UserEntity userEntity;
    private UserDTO userDTO;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        userEntity = new UserEntity();
        userEntity.setAdhaarNumber("Test-Id");
        userEntity.setName("Test-Name");
        userEntity.setAddress("Test-Address");
        userEntity.setGender("Test-Gender");
        userEntity.setMobileNumber("Test-Mobile");
        userEntity.setEmail("Test-Email");
        userEntity.setRole(Role.ROLE_USER);

        userDTO = new UserDTO("Test-Id","Test-Name","Test-Address","Test-Gender","Test-Mobile","Test-Email", Role.ROLE_USER);
    }

    @Test
    void getAllUsersTest(){
        List<UserEntity> userEntities = List.of(userEntity);
        List<UserDTO> userDTOList = List.of(userDTO);

        when(userRepository.findAll()).thenReturn(userEntities);
        when(userMapper.entityToDTO(userEntity)).thenReturn(userDTO);

        List<UserDTO> result = userServiceImpl.getAllUsers();
        // Assert statements
        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(userDTOList);
        assertThat(result.get(0).getName()).isEqualTo("Test-Name");

        // Verify
        verify(userRepository,times(1)).findAll();
        verify(userMapper,times(1)).entityToDTO(userEntity);
        verifyNoMoreInteractions(userRepository,userMapper);
    }

    @Test
    void getAllUsers_EmptyListTest(){
        List<UserEntity> userEntities = List.of();
        List<UserDTO> userDTOList = List.of();

        when(userRepository.findAll()).thenReturn(userEntities);

        List<UserDTO> result = userServiceImpl.getAllUsers();
        // Assert statements
        assertThat(result).hasSize(0);
        assertThat(result).isEqualTo(userDTOList);

        // Verify
        verify(userRepository,times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserDetails_SuccessTest(){
        String adhaarNumber = "Test-Id";

        when(userRepository.findById(adhaarNumber)).thenReturn(Optional.of(userEntity));
        when(userMapper.entityToDTO(userEntity)).thenReturn(userDTO);

        UserDTO result = userServiceImpl.getUserDetails(adhaarNumber);

        // Assert statements
        assertThat(result).isEqualTo(userDTO);

        verify(userRepository,times(1)).findById(adhaarNumber);
        verify(userMapper,times(1)).entityToDTO(userEntity);
        verifyNoMoreInteractions(userRepository,userMapper);
    }

    @Test
    void getUserDetails_NotFoundTest() {
        String adhaarNumber = "Test-Id";
        when(userRepository.findById(adhaarNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(()->userServiceImpl.getUserDetails(adhaarNumber))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found: ");

        verify(userRepository,times(1)).findById(adhaarNumber);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_SuccessTest() {
        String adhaarNumber = "Test-Id";
        when(userRepository.findById(adhaarNumber)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.entityToDTO(userEntity)).thenReturn(userDTO);

        UserDTO result = userServiceImpl.updateUser(adhaarNumber, userDTO);

        // Assert statements
        assertThat(result).isEqualTo(userDTO);
        // Verify
        verify(userRepository, times(1)).findById(adhaarNumber);
        verify(userMapper).updateUserEntity(userEntity, userDTO);
        verify(userRepository, times(1)).save(userEntity);
        verify(userMapper, times(1)).entityToDTO(userEntity);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    void updateUser_NotFoundTest() {
        String adhaarNumber = "Test-Id";
        when(userRepository.findById(adhaarNumber)).thenReturn(Optional.empty());
        // Act and Assert
        assertThatThrownBy(()->userServiceImpl.updateUser(adhaarNumber, userDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        // Verify
        verify(userRepository,times(1)).findById(adhaarNumber);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    void userDelete_SuccessTest() {
        String adhaarNumber = "Test-Id";
        when(userRepository.existsById(adhaarNumber)).thenReturn(true);
        userServiceImpl.deleteUser(adhaarNumber);
        // Verify
        verify(userRepository,times(1)).existsById(adhaarNumber);
        verify(userRepository,times(1)).deleteById(adhaarNumber);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void userDelete_NotFoundTest() {
        String adhaarNumber = "Test-Id";
        when(userRepository.existsById(adhaarNumber)).thenReturn(false);
        // Act and Assert
        assertThatThrownBy(()->userServiceImpl.deleteUser(adhaarNumber))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
        // Verify
        verify(userRepository,times(1)).existsById(adhaarNumber);
        verify(userRepository,never()).deleteById(adhaarNumber);
        verifyNoMoreInteractions(userRepository);
    }
}
