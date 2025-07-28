package com.project.aptflow.service;

import com.project.aptflow.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    UserDTO getUserDetails(String adhaarNumber);

    UserDTO updateUser(String adhaarNumber, UserDTO updateUser);

    void deleteUser(String adhaarNumber);
}
