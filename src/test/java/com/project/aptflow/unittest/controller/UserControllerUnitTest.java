package com.project.aptflow.unittest.controller;

import com.project.aptflow.controller.UserController;
import com.project.aptflow.dto.UserDTO;
import com.project.aptflow.dto.apiresponse.ResponseDTO;
import com.project.aptflow.enums.Role;
import com.project.aptflow.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Controller Unit Tests")
public class UserControllerUnitTest {

    @Mock
    private UserService userService;

    private UserDTO userDTO;
    private List<UserDTO> userDTOList;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp(){
        userDTO = new UserDTO("Test-Number","Name-Test","Test-address","Gender-Test","Mobile-Test","Email-Test", Role.ROLE_USER);
        userDTOList = List.of(userDTO);
    }

    @Test
    public void getAllUsersTest(){
        // Call mocked service method
        when(userService.getAllUsers()).thenReturn(userDTOList);
        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();

        // Assert statements
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Name-Test");
        // Verify that service method was called
        verify(userService).getAllUsers();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getUserDetailsTest() {
        String adhaarNumber = "Test-Number";
        when(userService.getUserDetails(adhaarNumber)).thenReturn(userDTO);
        ResponseEntity<UserDTO> response = userController.getUserDetails(adhaarNumber);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userDTO);
        verify(userService).getUserDetails(adhaarNumber);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void updateUserTest() {
        String adhaarNumber = "Test-Number";
        // Arrange
        when(userService.updateUser(adhaarNumber,userDTO)).thenReturn(userDTO);
        ResponseEntity<ResponseDTO<UserDTO>> response = userController.updateUser(adhaarNumber,userDTO);
        // Assert statements
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseDTO<UserDTO> responseDTO = response.getBody();
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getMessage()).isEqualTo("User updated successfully");
        assertThat(responseDTO.getStatus()).isEqualTo(HttpStatus.OK.name());
        assertThat(responseDTO.getData()).isEqualTo(userDTO);

        // Verify that service method was called
        verify(userService,times(1)).updateUser(adhaarNumber,userDTO);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void deleteUserTest(){
        String adhaarNumber = "Test-Number";
        // Call mocked service method
        doNothing().when(userService).deleteUser(adhaarNumber);

        // Call controller method
        ResponseEntity<Void> response = userController.deleteUser(adhaarNumber);
        // Assert statements
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        // Verify that service method was called
        verify(userService,times(1)).deleteUser(adhaarNumber);
        verifyNoMoreInteractions(userService);
    }
}
