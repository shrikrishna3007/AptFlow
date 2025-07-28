package com.project.aptflow.controller;

import com.project.aptflow.dto.apiresponse.ResponseDTO;
import com.project.aptflow.dto.UserDTO;
import com.project.aptflow.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/details")
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        List<UserDTO> dtoList = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(dtoList);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{adhaarNumber}")
    public ResponseEntity<UserDTO> getUserDetails(@PathVariable String adhaarNumber){
        UserDTO userDTO = userService.getUserDetails(adhaarNumber);
        return ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PutMapping("/{adhaarNumber}")
    public ResponseEntity<ResponseDTO<UserDTO>> updateUser(@PathVariable String adhaarNumber,@RequestBody UserDTO updateUser){
        UserDTO userDTO = userService.updateUser(adhaarNumber,updateUser);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("User updated successfully", HttpStatus.OK, userDTO));
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @DeleteMapping("/{adhaarNumber}")
    public ResponseEntity<Void> deleteUser(@PathVariable String adhaarNumber){
        userService.deleteUser(adhaarNumber);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
